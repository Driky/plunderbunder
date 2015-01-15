package controllers

import play.api._
import play.api.mvc._

import play.api.Play.current

import play.api.libs.ws._
import scala.concurrent.Future

import com.eveonline.crest.SingleSignOn._

import com.eveonline.crest.TokenResponse
import com.eveonline.crest.TokenResponseSerializer._
import com.eveonline.crest.SingleSignOn._
import com.eveonline.crest.InvalidTokenException

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.Json

object Authentication extends Controller {
  def authCallback = Action.async { request =>

    Logger.info("This needs to be moved!!")
    val codeO = request.getQueryString("code")
    val stateO = request.getQueryString("state")

    stateO.fold {
      Future(BadRequest("State is missing, not ok"))
    } { state =>
      codeO.fold {
        Future(BadRequest("Code is missing, not ok"))
      } { authorizationCode =>
        val authToken = generateAuthTokenFromAuthCode(authorizationCode)

        authToken.flatMap { token =>
          {
            val verifyFuture = verifyAuthToken(token.accessToken)

            verifyFuture.flatMap { vrfy =>
              {
                val profile = AuthenticationProfile(token.accessToken, vrfy.expiresOn, token.refreshToken, vrfy.characterName)
                val serializedProfile = Json.toJson(profile).toString
                val newSession = request.session + (AuthenticationProfile.sessionKey, serializedProfile)
                Logger.info(s"New Session authenticated: ${token}")
                Future(Redirect(routes.Application.index()).withSession(newSession))
              }
            }
          }
        }

      }
    }
  }

  def logout = Action { request =>
    Redirect(routes.Application.index()).withNewSession
  }
}