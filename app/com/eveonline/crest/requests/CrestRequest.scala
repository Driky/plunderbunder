package com.eveonline.crest.requests

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.util.Timeout
import play.api.libs.concurrent.Akka.system
import akka.contrib.throttle._
import akka.contrib.throttle.Throttler._
import akka.pattern.ask
import scala.concurrent.duration._

import play.api.Play.current
import play.api.Logger
import play.api.libs.ws._
import play.api.cache.Cache

import scala.concurrent.Future
import com.eveonline.crest.FakeResponse

import scala.concurrent.ExecutionContext.Implicits.global

object CrestRequest {
  def initialize = {
    val throttledRequestActor = system.actorOf(Props[ThrottledRequest], "throttled")
    val throttler = system.actorOf(Props(classOf[TimerBasedThrottler], 30 msgsPer 1.second), "throttler")
    throttler ! SetTarget(Some(throttledRequestActor))
  }
}

trait CrestRequest {
  def get(url: String, accessToken: String, salt: Option[String] = None) = {

    val config = current.configuration
    val offlineMode = config.getBoolean("development.offline").getOrElse(false)
    val timeoutDuration = config.getInt("crest.timeout").getOrElse(20)

    val cacheKey = salt.fold(url)(sel => s"${sel}-${url}")

    if (!offlineMode) {

      val cachedValue = Cache.getAs[WSResponse](cacheKey)

      cachedValue.fold({

        val request = WS.url(url)
          .withHeaders("Authorization" -> s"Bearer ${accessToken}")

        // TODO: add version
        // TODO: add content-type

        // TODO: support non-get requests

        implicit val timeout = Timeout(timeoutDuration.seconds)
        val ttl = system.actorSelection("/user/throttler")
        val result = (ttl ? Get(request)).mapTo[Future[WSResponse]].flatMap(f => f)

        result foreach { res =>
          if (res.status == 200) {
            // TODO: Extract cached-until
            val cacheControl = res.header("Cache-Control")

            cacheControl.foreach { ctl =>
              {

                val maxAgeSetting = ctl.split(",\\s*").filter { _.startsWith("max-age") }.headOption

                maxAgeSetting.foreach { maxAge =>
                  {

                    // save to cache

                    val expiration = maxAge.split("=")(1).toInt
                    Cache.set(cacheKey, res, expiration)
                  }
                }
              }
            }

          }
        }

        result
      })({ cached =>
        Future { cached }
      })
    } else {
      Future(FakeResponse("""{
        "totalCount": 1,
        "items": [
            {
              "href": "http://offline.com/not/there",
              "location": {
                "href": "http://offline.com/not/there/60003760/",
                "name": "FakeStation IV - Caldari Bootcamp"
              },
              "volume": 1000,
              "duration": 300,
              "price": 1234
            }
        ]
        }"""))
    }
  }

  val crestEndpoint = "https://crest-tq.eveonline.com"
}