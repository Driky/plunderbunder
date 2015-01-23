package com.eveonline.xmlapi.requests

import auth.UserProfile

import com.eveonline.xmlapi.ApiKeyInfoResponse
import com.eveonline.xmlapi.Character

import play.api.Logger

import scala.xml.XML
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.joda.time.DateTime

object ApiKeyInfo extends XmlApiRequest {
  def verifyKey(profile: UserProfile) = {
    val keyID = profile.apiKey
    val vCode = profile.apiVCode

    if (keyID.isDefined && vCode.isDefined) {
      val verifyUrl = "https://api.eveonline.com/account/APIKeyInfo.xml.aspx"

      val response = get(verifyUrl, profile.apiKey, profile.apiVCode)

      val result = response.map { r =>
        {
          if (r.status == 200) {
            val xml = XML.loadString(r.body)
            val response = ApiKeyInfoResponse.fromXml(xml)
            response
          } else {
            Logger.error("Error during api key info:" + r.body)
            throw new Exception("Failure during api key info call")
          }
        }
      }
      result
    } else {
      Future { throw new Exception("Missing key or vCode") }
    }
  }
}