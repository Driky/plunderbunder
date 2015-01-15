package controllers

import play.api._

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.eveonline.crest._
import com.eveonline.crest.SingleSignOn._
import com.eveonline.crest.VerifyResponse
import org.joda.time.DateTime

case class AuthenticatedRequest[A](
  request: Request[A],
  authenticationProfile: AuthenticationProfile) extends WrappedRequest(request) {}

case class AuthenticationProfile(
  accessToken: String,
  accessExpiration: DateTime,
  refreshToken: Option[String],
  characterName: String) {

  def copyWithAccess(newAccessToken: String, newAccessExpiration: DateTime) = {
    AuthenticationProfile(newAccessToken, newAccessExpiration, refreshToken, characterName)
  }
}
object AuthenticationProfile {
  val sessionKey = "authProfile"
  implicit val format = Json.format[AuthenticationProfile]
}

object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
    request.session.get(AuthenticationProfile.sessionKey).fold({
      // no auth, redirect
      unauthorizedFuture
    })({
      authProfileJson =>

        val authProfile = Json.parse(authProfileJson).validate[AuthenticationProfile]

        authProfile match {
          case JsSuccess(profile, _) => {
 
            val isAccessValid = DateTime.now().isBefore(profile.accessExpiration)

            if (isAccessValid) {
              block(AuthenticatedRequest(request, profile))
            } else {

              // Offline mode is mainly for development purposes
              val offline = Play.current.configuration.getBoolean("development.offline").getOrElse(false)

              // Verify it's invalid, just to be sure
              val verifiedResult = if (!offline) {
                verifyAuthToken(profile.accessToken)
              } else {
                Logger.info("Faking the verification")
                val expires = (new DateTime()).plusYears(10)
                Future(VerifyResponse(expires, "Offline User", "all", "hash", 0, "offline"))
              }

              verifiedResult.map(res => {
                val ar = AuthenticatedRequest(request, profile)
                block(ar)
              }).recover({
                case _: InvalidTokenException => {
                  // Generate refresh token
                  generateAuthTokenFromRefresh[A](request, block)
                }
                case e => throw e
              }).flatMap(f => f)
            }
          }
          case JsError(e) => unauthorizedFuture
        }

    })
  }

  def unauthorizedFuture = Future(Unauthorized(JsObject(Seq("error" -> JsString("Token Expired")))))

  def generateAuthTokenFromRefresh[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    val session = request.session
    val ret: Future[Result] = session.get(AuthenticationProfile.sessionKey) match {
      case None => unauthorizedFuture
      case Some(rawAuthProfile) => {

        val existingProfileJs = Json.parse(rawAuthProfile).validate[AuthenticationProfile]

        existingProfileJs match {
          case JsSuccess(existingProfile, _) => {
            existingProfile.refreshToken.fold(unauthorizedFuture)(rt => {
              val authToken = generateAuthTokenFromRefreshToken(rt)
              val r = authToken.map { token =>
                {
                  Logger.info(s"Refreshed Session authenticated: ${token}")

                  // Verify to be safe
                  val verifiedResult = verifyAuthToken(token.accessToken)

                  verifiedResult.map(res => {
                    val newProfile = AuthenticationProfile(token.accessToken, res.expiresOn, token.refreshToken, res.characterName)
                    val accessSession = session + (AuthenticationProfile.sessionKey -> Json.toJson(newProfile).toString())
                    val ar = AuthenticatedRequest(request, newProfile)
                    block(ar).map(_.withSession(accessSession))
                  }).recover({
                    case _ => unauthorizedFuture
                  })

                }
              }

              r.flatMap { f => f.flatMap { fm => fm } }

            })
          }
          case JsError(e) => unauthorizedFuture
        }
      }
    }
    ret
  }
}