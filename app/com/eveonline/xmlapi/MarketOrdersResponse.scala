package com.eveonline.xmlapi

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat

case class MarketOrdersResponse(apiVersion: Int,
  responseTime: DateTime,
  cachedUntil: DateTime,
  marketOrders: List[MarketOrder])

object MarketOrdersResponse {

  def fromXml(node: scala.xml.Node): MarketOrdersResponse = {
    val apiVersion = (node \@ "version").toInt

    val timeNodes = (node \ "currentTime")
    val cacheTimeNodes = (node \ "cachedUntil")

    val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val currentTime = DateTime.parse(timeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)
    val cacheTime = DateTime.parse(cacheTimeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)

    val orderNodes = (node \ "result") \ "rowset" \ "row"

    val orders = orderNodes.map { MarketOrder.fromXml(_) }.toList

    MarketOrdersResponse(apiVersion, currentTime, cacheTime, orders)
  }
}
