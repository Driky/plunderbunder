package controllers

import play.api._
import play.api.mvc._

import play.api.Play.current

import play.api.libs.ws._
import scala.concurrent.Future

import com.eveonline.crest.TokenResponse
import com.eveonline.crest.TokenResponseSerializer._
import com.eveonline.crest.SingleSignOn._
import com.eveonline.crest.InvalidTokenException

import play.api.libs.json._

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = Action.async { request =>
    Future(Ok(views.html.index()))
  }

  def user = Action.async { request =>
    request.session.get("authenticated").map { token =>
      val verifiedResult = verifyAuthToken(token)

      verifiedResult.map(res => {
        Ok(s"""{ "character": "${res.characterName}" } """)
      }).recover({
        case _: InvalidTokenException => Unauthorized(JsObject(Seq("error" -> JsString("Token Expired"))))
        case e                        => throw e
      })

    }.getOrElse {
      Future(Unauthorized(JsObject(Seq("error" -> JsString("Missing Auth Token")))))
    }
  }

  def authCallback = Action.async { request =>

    val codeO = request.getQueryString("code")
    val stateO = request.getQueryString("state")

    stateO.fold {
      Future(BadRequest("State is missing, not ok"))
    } { state =>
      codeO.fold {
        Future(BadRequest("Code is missing, not ok"))
      } { authorizationCode =>
        val authToken = generateAuthToken(authorizationCode)

        authToken.map { token =>
          {
            val newSession = request.session + ("authenticated", token)
            Logger.info(s"New Session authenticated: ${token}")
            Redirect(routes.Application.index()).withSession(newSession)
          }
        }

      }
    }
  }

  def logout = Action { request =>
    Redirect(routes.Application.index()).withNewSession
  }

  def assetRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("ar")(controllers.routes.javascript.Assets.at)).as("text/javascript")
  }

  def jsRoutes = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Application.user)).as("text/javascript")
  }

  def configuration = Action { implicit request =>
    // This is just common things probably needed by coffeescript frontends

    val config = current.configuration
    val clientID = config.getString("crest.client_id", None).getOrElse("missing_client_id")
    val callbackURI = config.getString("crest.callback", None).getOrElse("missing_callback_uri")

    val eveLoginUrl = s"https://login.eveonline.com/oauth/authorize/?response_type=code&redirect_uri=${callbackURI}&client_id=${clientID}&scope=&state=uniquestate123"

    val jsResult = JsObject(Seq("eve_login" -> JsString(eveLoginUrl)))

    val preloaded = s"var kartelConfig = ${jsResult.toString()}"

    Ok(preloaded).as("text/javascript")

  }

}