package com.eveonline.crest

import play.api.Play.current

import play.api.libs.ws._
import play.api.libs.json._
import play.api.Logger

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

case class MarketOrderLocation(
  href: String,
  name: String)
object MarketOrderLocation {
  implicit val marketOrderLocationFormat = Json.format[MarketOrderLocation]
}

case class MarketOrder(
  href: String,
  location: MarketOrderLocation,
  volume: Int,
  duration: Int,
  //issued: DateTime,
  price: Long)
object MarketOrder {
  implicit val marketSellOrderFormat = Json.format[MarketOrder]
}

case class MarketOrdersResponse(
  totalCount: Int,
  items: List[MarketOrder])
object MarketOrdersResponse {
  implicit val marketSellOrdersResponseFormat = Json.format[MarketOrdersResponse]
}

case class FakeResponse(fakeBody: String) extends WSResponse {
  def body = fakeBody
  def allHeaders = ???
  def cookie(name: String) = ???
  def statusText = ???
  def status = 200
  def underlying[T] = ???
  def xml = ???
  def header(key: String) = ???
  def json = ???
  def cookies = ???
}

object RegionalMarketOrders {

  def crestRequest(url: String, accessToken: String) = {
    val request = WS.url(url)
      .withHeaders("Authorization" -> s"Bearer ${accessToken}")

    // TODO: add version
    // TODO: add content-type

    // TODO: support non-get requests
    request.get()
  }

  val crestEndpoint = "https://crest-tq.eveonline.com"

  def regionalBuyOrders(regionID: Long, itemID: Long, token: String) = {
    val buyOrderUrl = s"${crestEndpoint}/market/${regionID}/orders/buy/?type=${crestEndpoint}/types/${itemID}/"

    val response = crestRequest(buyOrderUrl, token)

    response.flatMap { r =>
      {
        if (r.status == 200) {
          parseMarketOrdersResponse(r.body)
        } else {
          throw new Exception(s"Error ${r.status} while getting market buy data")
        }
      }
    }
  }

  def parseMarketOrdersResponse(rawResponse: String) = {
    val jsonBody = Json.parse(rawResponse)

    val marketOrdersJs = jsonBody.validate[MarketOrdersResponse]

    marketOrdersJs match {
      case JsSuccess(marketOrdersResp, _) => {
        Future(marketOrdersResp.items)
      }
      case JsError(error) => throw new Exception(error.toString)
    }
  }

  def regionalSellOrders(regionID: Long, itemID: Long, token: String) = {
    // Crawling this URL doesn't really make much sense because the argument isn't obvious 
    val sellOrderUrl = s"${crestEndpoint}/market/${regionID}/orders/sell/?type=${crestEndpoint}/types/${itemID}/"

    val response = crestRequest(sellOrderUrl, token)

    response.flatMap { r =>
      {
        if (r.status == 200) {
          parseMarketOrdersResponse(r.body)
        } else {
          throw new Exception(s"Error ${r.status} while getting market sell data")
        }
      }
    }
  }
}