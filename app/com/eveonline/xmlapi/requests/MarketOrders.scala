package com.eveonline.xmlapi.requests

import auth.UserProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.eveonline.xmlapi._

import scala.xml.XML

object MarketOrders extends XmlApiRequest {
  def listForCharacter(profile: UserProfile) = {
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