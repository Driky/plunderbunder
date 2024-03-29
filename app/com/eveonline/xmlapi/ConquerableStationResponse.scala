package com.eveonline.xmlapi

import com.eveonline.sde.{ Station, SolarSystem }

import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.format.DateTimeFormat

case class ConquerableStationResponse(
  apiVersion: Int,
  responseTime: DateTime,
  cachedUntil: DateTime,
  stations: List[Station])

object ConquerableStationResponse {

  // The optional parameter is required for unit testing & CI to work correctly
  // Without requiring a loaded database
  def stationFromXml(node: scala.xml.Node, solarSystem: Option[SolarSystem] = None): Station = {
    val stationID = (node \@ "stationID").toInt
    val stationName = (node \@ "stationName")
    val stationTypeID = (node \@ "stationTypeID").toInt
    val solarSystemID = (node \@ "solarSystemID").toInt
    val corporationID = (node \@ "corporationID").toLong

    val resolvedSystem = solarSystem.fold(SolarSystem.getByID(solarSystemID))(s => Option(s))

    resolvedSystem.fold(
      throw new Exception("Solar system not found!"))(solarSystem => {
        val constellationID = solarSystem.constellationID
        val regionID = solarSystem.regionID

        Station(
          stationID, stationName, constellationID, corporationID,
          None, None, None, None,
          regionID,
          None, None, None, None,
          solarSystemID, stationTypeID,
          None, None, None)
      })

  }

  def fromXml(node: scala.xml.Node, solarSystem: Option[SolarSystem] = None): ConquerableStationResponse = {
    val apiVersion = (node \@ "version").toInt

    val timeNodes = (node \ "currentTime")
    val cacheTimeNodes = (node \ "cachedUntil")

    val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val currentTime = DateTime.parse(timeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)
    val cacheTime = DateTime.parse(cacheTimeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)

    val stationNodes = (node \ "result") \ "rowset" \ "row"
    val stations = stationNodes.map { stationFromXml(_, solarSystem) }.toList

    ConquerableStationResponse(apiVersion, currentTime, cacheTime, stations)
  }
}
