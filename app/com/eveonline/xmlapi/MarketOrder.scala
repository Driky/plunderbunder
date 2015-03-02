package com.eveonline.xmlapi

import play.api.libs.json._

import anorm._
import play.api.db.DB
import anorm.SqlParser.{ scalar }

import play.api.Play.current
import play.api.Logger

import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.format.DateTimeFormat

// range,accountKey,duration,escrow,price,bid,issued
case class MarketOrder(
  stationID: Int,
  orderID: Long,
  isBuyOrder: Boolean,
  characterID: Long,
  typeID: Long,
  volEntered: Int,
  volRemaining: Int,
  minVolume: Int,
  orderState: Int,
  price: BigDecimal,
  escrow: BigDecimal,
  issued: DateTime,
  duration: Int,
  stationName: Option[String] = None,
  typeName: Option[String] = None) {

}

object MarketOrder {
  implicit val format = Json.format[MarketOrder]

  def fromXml(node: scala.xml.Node, parentLocationID: Option[Int] = None): MarketOrder = {
    val stationID = parentLocationID.fold((node \@ "stationID").toInt)(l => l)
    val orderID = (node \@ "orderID").toLong
    val typeID = (node \@ "typeID").toLong
    val volRemaining = (node \@ "volRemaining").toInt
    val volEntered = (node \@ "volEntered").toInt
    val minVolume = (node \@ "minVolume").toInt
    val orderState = (node \@ "orderState").toInt
    val price = BigDecimal(node \@ "price")
    val escrow = BigDecimal(node \@ "escrow")
    val duration = (node \@ "duration").toInt
    val isBuyOrder = (node \@ "bid").toInt != 0

    val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
    val issued = DateTime.parse(node \@ "issued", format).withZoneRetainFields(DateTimeZone.UTC)

    val characterID = (node \@ "charID").toLong

    MarketOrder(stationID, orderID, isBuyOrder, characterID, typeID, volEntered, volRemaining, minVolume, orderState, price, escrow, issued, duration)
  }
}