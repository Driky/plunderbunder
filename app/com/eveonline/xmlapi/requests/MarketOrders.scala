package com.eveonline.xmlapi.requests

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import auth.UserProfile
import com.eveonline.xmlapi.{ MarketOrder, MarketOrdersResponse }

import scala.xml.XML

object MarketOrders extends XmlApiRequest {
  def listForCharacter(profile: UserProfile): Future[List[MarketOrder]] = {
    val keyID = profile.apiKey
    val vCode = profile.apiVCode
    val assetUrl = "https://api.eveonline.com/char/MarketOrders.xml.aspx"

    val response = get(assetUrl, keyID, vCode)

    response.map { r =>
      {
        val marketOrdersResponse = MarketOrdersResponse.fromXml(XML.loadString(r.body))

        marketOrdersResponse.marketOrders
      }
    }
  }
}
