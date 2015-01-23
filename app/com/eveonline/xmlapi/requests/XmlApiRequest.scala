package com.eveonline.xmlapi.requests

import play.api.Play.current
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future }

import com.eveonline.crest.FakeResponse

trait XmlApiRequest {
  def get(url: String, apiKeyID: Option[Long] = None, vCode: Option[String] = None) = {

    val config = current.configuration
    val offlineMode = config.getBoolean("development.offline").getOrElse(false)

    val fullUrl = if (apiKeyID.isDefined && vCode.isDefined) {
      s"${url}?keyID=${apiKeyID.get}&vCode=${vCode.get}"
    } else {
      url
    }
    
    if (!offlineMode) {
      val request = WS.url(fullUrl)

      // TODO: add user agent

      // TODO: support non-get requests
      request.get()
    } else {
      Future(FakeResponse("""<notsupported />"""))
    }
  }
}