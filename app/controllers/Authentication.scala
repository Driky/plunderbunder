package controllers

import play.api.Logger
import play.api.mvc.{ Controller, Session, Action, AnyContent, Result }
import play.api.Play.current
import scala.concurrent.Future

import com.eveonline.crest.SingleSignOn
import auth.{ AuthenticatedAction, AuthenticationProfile }
import com.eveonline.crest.{ InvalidTokenException, SignOnTokens, TokenResponse, VerifyResponse }

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import scala.util.Try

object Authentication extends Controller {

  def establishSession(
    existingSession: Session,
    tokens: SignOnTokens,
    verifyResult: VerifyResponse,
    userID: Long): Future[Result] = {

    val profile = AuthenticationProfile(
      tokens.accessToken,
      verifyResult.expiresOn,
      tokens.refreshToken,
      verifyResult.characterName,
      userID)

    val serializedProfile = Json.toJson(profile).toString
    val newSession = existingSession + (AuthenticationProfile.sessionKey, serializedProfile)
    Logger.info(s"New Session authenticated: ${tokens}")
    Future(Redirect(routes.Application.index()).withSession(newSession))
  }

  def authCallback: Action[AnyContent] = Action.async { request =>

    val codeO = request.getQueryString("code")
    val stateO = request.getQueryString("state")

    stateO.fold {
      Future(BadRequest("State is missing, not ok"))
    } { state =>
      codeO.fold {
        Future(BadRequest("Code is missing, not ok"))
      } { authorizationCode =>
        val authToken = SingleSignOn.generateAuthTokenFromAuthCode(authorizationCode)

        authToken.flatMap { tokens =>
          {
            val verifyFuture = SingleSignOn.verifyAuthToken(tokens.accessToken)
            verifyFuture.flatMap { vr =>
              {
                val userID = AuthenticatedAction.createUserIfApplicable(vr).getOrElse(0L)
                establishSession(request.session, tokens, vr, userID)
              }
            }
          }
        }
      }
    }
  }

  def logout: Action[AnyContent] = Action { request =>
    Try(Redirect(routes.Application.index()).withNewSession).recover { case e: Throwable => Ok("fail") }.get
  }
}
