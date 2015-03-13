package com.eveonline.xmlapi.requests

import scala.xml.XML
import scala.concurrent.{ Future }
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Logger

import com.eveonline.sde.Station
import com.eveonline.xmlapi.ConquerableStationResponse

object ConquerableStations extends XmlApiRequest {
  def list: Future[List[Station]] = {
    val apiUrl = "https://api.eveonline.com/eve/ConquerableStationList.xml.aspx"

    val response = get(apiUrl)

    val result = response.map { r =>
      {
        if (r.status == 200) {
          val xml = XML.loadString(r.body)
          val csr = ConquerableStationResponse.fromXml(xml)
          csr.stations
        } else {
          Logger.error("Error during station list:" + r.body)
          throw new Exception("Failure during conquerable station call")
        }
      }
    }
    result
  }
}
