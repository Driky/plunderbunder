package com.eveonline.sde

import play.api.libs.json._
import play.api.libs.json.Reads._

import play.api.libs.functional.syntax._

import anorm._
import play.api.db.DB

import play.api.Play.current

case class MarketGroup(
  description: Option[String],
  hasTypes: Boolean,
  iconID: Option[Long],
  marketGroupID: Int,
  marketGroupName: String,
  parentGroupID: Option[Int]) {

}

object MarketGroup extends BaseDataset {

  implicit val marketGroupReads = (
    (__ \ "description").readNullable[String] and
    (__ \ "hasTypes").read[String].map { _ == "1" } and
    (__ \ "iconID").read[Option[String]].map { _.flatMap(v => Option(v.toLong)) } and
    (__ \ "marketGroupID").read[String].map { _.toInt } and
    (__ \ "marketGroupName").read[String] and
    (__ \ "parentGroupID").read[Option[String]].map { _.flatMap(v => Option(v.toInt)) })(MarketGroup.apply _)

  def dataSetName = "sde_marketgroups"

  def create(value: MarketGroup) = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO ${dataSetName} 
        (description, has_types, icon_id,
        market_group_id, market_group_name, parent_group_id
        ) VALUES (
         {description}, {hasTypes},
         {iconID}, {marketGroupID}, {marketGroupName}, 
         {parentGroupID}
         );""").on(
        'description -> value.description,
        'hasTypes -> value.hasTypes,
        'iconID -> value.iconID,
        'marketGroupID -> value.marketGroupID,
        'marketGroupName -> value.marketGroupName,
        'parentGroupID -> value.parentGroupID)
      sql.executeInsert()
    }
  }
}