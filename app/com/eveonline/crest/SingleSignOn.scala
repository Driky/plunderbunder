package com.eveonline.crest

import play.api.Play.current

import play.api.libs.ws.{ WS, WSAuthScheme }
import play.api.libs.json.{ JsError, JsSuccess }
import play.api.Logger
import scala.concurrent.Future

import TokenResponseSerializer.tokenResponseReads
import VerifyResponseSerializer.reads

class InvalidTokenException extends Exception

case class SignOnTokens(accessToken: String, refreshToken: Option[String])

object SingleSignOn {

  val generateTokenURI = "https://login.eveonline.com/oauth/token"
  val verifyTokenURI = "https://login.eveonline.com/oauth/verify"

  def generateAuthTokenFromRefreshToken(token: String): Future[SignOnTokens] = {
    generateAuthToken("refresh_token", "refresh_token", token)
  }

  def generateAuthTokenFromAuthCode(code: String): Future[SignOnTokens] = {
    generateAuthToken("authorization_code", "code", code)
  }

  def generateAuthToken(authMethod: String, authName: String, code: String): Future[SignOnTokens] = {

    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    val config = current.configuration

    val clientID = config.getString("crest.client_id", None).getOrElse("missing_client_id")
    val appSecret = config.getString("crest.app_secret", None).getOrElse("missing_app_secret")

    val tokenEndpoint = WS.url(generateTokenURI)
      .withAuth(clientID, appSecret, WSAuthScheme.BASIC)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")

    val postBody = Map(
      "grant_type" -> Seq(authMethod),
      authName -> Seq(code))

    val result = tokenEndpoint.post(postBody)

    result.map { response =>

      if (response.status == 200) {

        Logger.info("Token Response: " + response.json.toString())

        response.json.validate[TokenResponse] match {
          case JsError(e) => {
            Logger.info(s"Response Status: ${response.status}")
            Logger.info(s"Response Text: ${response.body}")
            Logger.info(e.toString)
            throw new Exception(e.toString())
          }
          case JsSuccess(tr, _) => SignOnTokens(tr.accessToken, tr.refreshToken)
        }
      } else {
        throw new Exception(s"Error during request: ${response.status}\n${response.body}\n")
      }
    }
  }

  def verifyAuthToken(authToken: String): Future[VerifyResponse] = {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    val tokenEndpoint = WS.url(verifyTokenURI)
      .withHeaders("Authorization" -> s"Bearer ${authToken}")

    val result = tokenEndpoint.get()

    result.map { response =>

      if (response.status == 200) {

        val js = response.json

        Logger.info("Verify Request: " + js.toString())

        val errorReason = (js \ "error")
        if (errorReason.asOpt[String].isEmpty) {

          js.validate[VerifyResponse] match {
            case JsError(e) => {
              Logger.info(s"Response Status: ${response.status}")
              Logger.info(s"Response Text: ${response.body}")
              Logger.info(e.toString)
              throw new Exception(e.toString())
            }
            case JsSuccess(vr, _) => {
              Logger.debug(s"Verified good token: ${authToken}!")
              vr
            }
          }
        } else {
          if (errorReason.as[String].equals("invalid_token")) {
            throw new InvalidTokenException
          } else {
            val reason = (js \ "error_description")
            throw new Exception(s"error json: ${reason} reason: [${errorReason}]")
          }
        }
      } else {
        throw new Exception(s"Error during request ${response.status}\n${response.body}\n")
      }
    }
  }
}
