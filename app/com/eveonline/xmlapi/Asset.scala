package com.eveonline.xmlapi

import play.api.libs.json.Json

import anorm.SQL
import play.api.db.DB
import anorm.SqlParser.{ scalar }

import play.api.Play.current
import play.api.Logger

case class Asset(locationID: Int, eveItemID: Long, typeID: Long, quantity: Int, contents: List[Asset]) {
  lazy val manufacturingComponent = {
     DB.withConnection { implicit c =>
      val sql = SQL(s"""SELECT count(*)
        FROM sde_blueprint_activity_materials
        where type_id={typeID};""").on('typeID -> typeID)

        sql.as(scalar[Long].single) > 0
     }
  }
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
