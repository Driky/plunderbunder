package com.eveonline.xmlapi

import play.api.libs.json._

case class Asset(locationID: Int, eveItemID: Long, typeID: Long, quantity: Int, contents: List[Asset]) {
  
}

object Asset {
  implicit val format = Json.format[Asset]
  
  def fromXml(node: scala.xml.Node, parentLocationID: Option[Int] = None): Asset = {
    val locationID = parentLocationID.fold((node \@ "locationID").toInt)(l => l) 
    val eveItemID = (node \@ "itemID").toLong
    val typeID = (node \@ "typeID").toLong
    val quantity = (node \@ "quantity").toInt
    
    val contentNodes = (node \ "rowset") \ "row"
    
    val contents = contentNodes.map { Asset.fromXml(_, Option(locationID)) }.toList
    
    Asset(locationID, eveItemID, typeID, quantity, contents)
  }
}