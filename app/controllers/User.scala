package controllers

import auth.AuthenticatedAction
import play.api._
import play.api.mvc._

import play.api.libs.json._

import scala.concurrent.Future

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

import auth.UserProfile

object User extends Controller {

  def user = AuthenticatedAction { authedRequest =>
    Ok(Json.toJson(authedRequest.authenticationProfile))
  }

  def userProfile = AuthenticatedAction { authedRequest =>
    val userID = authedRequest.authenticationProfile.userID
    val userProfileO = UserProfile.getWithID(userID)

    userProfileO.fold({
      val errResult = "result" -> JsString("ko")
      val errMsg = "message" -> JsString("Invalid User")
      BadRequest(JsObject(Seq(errResult, errMsg)))
    })({ profile =>
      Ok(Json.toJson(profile))
    })
  }

  case class UpdateUserRequest(apiKey: Option[Long], apiVCode: Option[String], emailAddress: Option[String])

  object UpdateUserRequest {
    implicit val format = Json.format[UpdateUserRequest]
  }

  def updateUserProfile = AuthenticatedAction(parse.json) { authedRequest =>
    val body = authedRequest.body
    
    Logger.info("RBody: " + body.toString())

    body.validate[UpdateUserRequest] match {
      case JsSuccess(uur, _) => {
        val userID = authedRequest.authenticationProfile.userID
        val userProfileO = UserProfile.getWithID(userID)

        userProfileO.fold(
          {
            val errResult = "result" -> JsString("ko")
            val errMsg = "message" -> JsString("Invalid User")
            BadRequest(JsObject(Seq(errResult, errMsg)))
          })(profile => {
            if (uur.apiKey.isDefined) {
              profile.updateApiKey(uur.apiKey.get)
            }

            if (uur.apiVCode.isDefined) {
              profile.updateApiVCode(uur.apiVCode.get)
            }

            if (uur.emailAddress.isDefined) {
              Logger.info("Updating email address")
              profile.updateEmailAddress(uur.emailAddress.get)
            }

            Ok(JsObject(Seq("result" -> JsString("ok"))))
          })
      }
      case JsError(e) => BadRequest(JsObject(Seq("result" -> JsString("ko"), "message" -> JsString("Invalid Json"))))
    }

  }
}