package com.eveonline.crest

import play.api.libs.json.Json

case class MarketOrder(
  href: String,
  location: NamedReference,
  volume: Int,
  duration: Int,
  //issued: DateTime,
  price: Long)
object MarketOrder {
  implicit val marketSellOrderFormat = Json.format[MarketOrder]
}