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

case class AuthenticatedRequest[A](
  request: Request[A],
  verifyResponse: VerifyResponse,
  accessToken: String) extends WrappedRequest(request) {}

object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {
    request.session.get("authenticated").fold({
      // no auth, redirect
      unauthorizedFuture
    })({
      token =>
        // verify then continue

        // Offline mode is mainly for development purposes
        val offline = Play.current.configuration.getBoolean("development.offline").getOrElse(false)
        
        val verifiedResult = if (!offline) {
          verifyAuthToken(token)
        } else {
          Logger.info("Faking the verification")
          Future(VerifyResponse("n/a", "Offline User", "all", "hash", 0, "offline"))
        }

        verifiedResult.map(res => {
          val ar = AuthenticatedRequest(request, res, token)
          block(ar)
        }).recover({
          case _: InvalidTokenException => {
            // Generate refresh token
            generateAuthTokenFromRefresh[A](request, block)
          }
          case e => throw e
        }).flatMap(f => f)

    })
  }

  def unauthorizedFuture = Future(Unauthorized(JsObject(Seq("error" -> JsString("Token Expired")))))

  def generateAuthTokenFromRefresh[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    val session = request.session
    val ret: Future[Result] = session.get("refresh") match {
      case None => unauthorizedFuture
      case Some(refreshToken) => {
        val authToken = generateAuthTokenFromRefreshToken(refreshToken)
        val r = authToken.map { token =>
          {
            val accessSession = session + ("authenticated" -> token.accessToken)
            val newSession = token.refreshToken.fold(accessSession)(rt => {
              accessSession + ("refresh" -> rt)
            })
            Logger.info(s"Refreshed Session authenticated: ${token}")

            // Verify to be safe
            val verifiedResult = verifyAuthToken(token.accessToken)

            verifiedResult.map(res => {
              val ar = AuthenticatedRequest(request, res, token.accessToken)
              block(ar)
            }).recover({
              case _ => unauthorizedFuture
            })

          }
        }

        r.flatMap { f => f.flatMap { fm => fm } }
      }
    }
    ret
  }
}