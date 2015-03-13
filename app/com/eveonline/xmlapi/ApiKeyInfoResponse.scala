package com.eveonline.xmlapi

import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.format.DateTimeFormat

import play.api.Logger

case class ApiKeyInfoResponse(
  apiVersion: Int,
  responseTime: DateTime,
  cachedUntil: DateTime,
  accessMask: Long,
  expires: Option[DateTime],
  characters: List[Character])

object ApiKeyInfoResponse {
  def fromXml(node: scala.xml.Node): ApiKeyInfoResponse = {
    val apiVersion = (node \@ "version").toInt

    val timeNodes = (node \ "currentTime")
    val cacheTimeNodes = (node \ "cachedUntil")

    val keyNode = ((node \ "result").head \ "key").head

    val expiresString = (keyNode \@ "expires")
    val accessMask = (keyNode \@ "accessMask").toLong

    val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val currentTime = DateTime.parse(timeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)
    val cacheTime = DateTime.parse(cacheTimeNodes.head.text, format).withZoneRetainFields(DateTimeZone.UTC)
    val expires = if (expiresString.length() == 0) {
      None
    } else {
      Option(DateTime.parse(expiresString, format).withZoneRetainFields(DateTimeZone.UTC))
    }

    val characterNodes = keyNode \ "rowset" \ "row"
    val characters = characterNodes.map { Character.fromXml(_) }.toList

    ApiKeyInfoResponse(apiVersion, currentTime, cacheTime, accessMask, expires, characters)
  }
}
