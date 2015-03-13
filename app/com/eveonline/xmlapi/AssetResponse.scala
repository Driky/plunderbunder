package com.eveonline.xmlapi

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat

case class AssetResponse(
  apiVersion: Int,
  responseTime: DateTime,
  cachedUntil: DateTime,
  assets: List[Asset])

object AssetResponse {

  def fromXml(node: scala.xml.Node): AssetResponse = {
    val apiVersion = (node \@ "version").toInt

    val timeNodes = (node \ "currentTime")
    val cacheTimeNodes = (node \ "cachedUntil")

    val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val currentTime = DateTime.parse(timeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)
    val cacheTime = DateTime.parse(cacheTimeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)

    val assetNodes = (node \ "result") \ "rowset" \ "row"

    val assets = assetNodes.map { Asset.fromXml(_) }.toList

    AssetResponse(apiVersion, currentTime, cacheTime, assets)
  }

}
