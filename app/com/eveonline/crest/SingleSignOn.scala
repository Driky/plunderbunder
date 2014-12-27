package com.eveonline.crest

import play.api.Play.current

import play.api.libs.ws._
import play.api.libs.json._
import play.api.Logger

import TokenResponseSerializer._
import VerifyResponseSerializer._

class InvalidTokenException extends Exception {
  
}

object SingleSignOn {

  val generateTokenURI = "https://login.eveonline.com/oauth/token"
  val verifyTokenURI = "https://login.eveonline.com/oauth/verify"

  def generateAuthToken(authorizationCode: String) = {

    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    val config = current.configuration

    val clientID = config.getString("crest.client_id", None).getOrElse("missing_client_id")
    val appSecret = config.getString("crest.app_secret", None).getOrElse("missing_app_secret")

    val tokenEndpoint = WS.url(generateTokenURI)
      .withAuth(clientID, appSecret, WSAuthScheme.BASIC)
      .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")

    val postBody = Map(
      "grant_type" -> Seq("authorization_code"),
      "code" -> Seq(authorizationCode))

    val result = tokenEndpoint.post(postBody)

    result.map { response =>

      if (response.status == 200) {

        response.json.validate[TokenResponse] match {
          case JsError(e) => {
            Logger.info(s"Response Status: ${response.status}")
            Logger.info(s"Response Text: ${response.body}")
            Logger.info(e.toString)
            throw new Exception(e.toString())
          }
          case JsSuccess(tr, _) => tr.accessToken
        }
      } else {
        throw new Exception(s"Error during request: ${response.status}")
      }
    }
  }

  def verifyAuthToken(authToken: String) = {
    implicit val context = scala.concurrent.ExecutionContext.Implicits.global

    val tokenEndpoint = WS.url(verifyTokenURI)
      .withHeaders("Authorization" -> s"Bearer ${authToken}")

    val result = tokenEndpoint.get()

    result.map { response =>

      if (response.status == 200) {

        val js = response.json

        val errorReason = (js \ "error")
        if (errorReason.asOpt[String].isEmpty) {

          js.validate[VerifyResponse] match {
            case JsError(e) => {
              Logger.info(s"Response Status: ${response.status}")
              Logger.info(s"Response Text: ${response.body}")
              Logger.info(e.toString)
              throw new Exception(e.toString())
            }
            case JsSuccess(vr, _) => vr
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
        throw new Exception(s"Error during request ${response.status}")
      }
    }
  }
}