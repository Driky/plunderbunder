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
import com.eveonline.crest.VerifyResponse
import com.eveonline.crest.SignOnTokens
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import auth._

import anorm._
import play.api.db.DB

object Authentication extends Controller {

  def establishSession(existingSession: Session, tokens: SignOnTokens, verifyResult: VerifyResponse, userID: Long) = {
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

  def authCallback = Action.async { request =>

    val codeO = request.getQueryString("code")
    val stateO = request.getQueryString("state")

    stateO.fold {
      Future(BadRequest("State is missing, not ok"))
    } { state =>
      codeO.fold {
        Future(BadRequest("Code is missing, not ok"))
      } { authorizationCode =>
        val authToken = generateAuthTokenFromAuthCode(authorizationCode)

        authToken.flatMap { tokens =>
          {
            val verifyFuture = verifyAuthToken(tokens.accessToken)
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

  def logout = Action { request =>
    Redirect(routes.Application.index()).withNewSession
  }
}