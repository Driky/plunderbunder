package com.eveonline.xmlapi.requests

import play.api.Play.current
import play.api.Logger
import play.api.libs.ws.{ WS, WSResponse }
import play.api.cache.Cache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future }

import org.joda.time.{ DateTime, DateTimeZone, Seconds }
import org.joda.time.format.DateTimeFormat

import scala.xml.{ XML, Node }

import com.eveonline.crest.FakeResponse

trait XmlApiRequest {

  def parseEveTime(nodes: Seq[Node]): DateTime = {
    if (nodes.length > 0) {
      val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
      val currentTime = DateTime.parse(nodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)
      currentTime
    } else {
      DateTime.now().withZone(DateTimeZone.UTC)
    }
  }

  def get(url: String, apiKeyID: Option[Long] = None, vCode: Option[String] = None): Future[WSResponse] = {

    val config = current.configuration
    val offlineMode = config.getBoolean("development.offline").getOrElse(false)

    val fullUrl = if (apiKeyID.isDefined && vCode.isDefined) {
      s"${url}?keyID=${apiKeyID.get}&vCode=${vCode.get}"
    } else {
      url
    }

    if (!offlineMode) {

      val cachedValue = Cache.getAs[WSResponse](fullUrl)

      cachedValue.fold({
        val request = WS.url(fullUrl)

        // TODO: add user agent

        // TODO: support non-get requests
        val result = request.get()

        result.foreach { res =>
          {
            val node = XML.loadString(res.body)
            val cacheTimeoutNodes = (node \ "cachedUntil")
            val cacheTimeout = parseEveTime(cacheTimeoutNodes)
            val duration = Seconds.secondsBetween(DateTime.now(), cacheTimeout).getSeconds()
            Cache.set(fullUrl, res, duration)
          }
        }

        result
      })(ws => Future { ws })

    } else {
      Future(FakeResponse("""<notsupported />"""))
    }
  }
}
