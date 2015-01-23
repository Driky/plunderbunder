package com.eveonline.crest.requests

import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.eveonline.crest.MarketOrder



case class MarketOrdersResponse(
  totalCount: Int,
  items: List[MarketOrder])
object MarketOrdersResponse {
  implicit val marketSellOrdersResponseFormat = Json.format[MarketOrdersResponse]
}

object RegionalMarketOrders extends CrestRequest {

  def regionalBuyOrders(regionID: Long, itemID: Long, token: String) = {
    val buyOrderUrl = s"${crestEndpoint}/market/${regionID}/orders/buy/?type=${crestEndpoint}/types/${itemID}/"

    val response = get(buyOrderUrl, token)

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

    val response = get(sellOrderUrl, token)

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