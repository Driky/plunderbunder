package com.eveonline.sde

import play.api.libs.json._
import play.api.libs.json.Reads._

import play.api.libs.functional.syntax._

import anorm._
import play.api.db.DB

import play.api.Play.current

case class Blueprint(
  id: Long,
  typeID: Long,
  maxProductionLimit: Int,
  activities: BlueprintActivities) {

}

object Blueprint extends BaseDataset {
  def dataSetName = "sde_blueprint"

  implicit val blueprintReads = (
    (__ \ "id").read[Long] and
    (__ \ "blueprintTypeID").read[Long] and
    (__ \ "maxProductionLimit").read[Int] and
    (__ \ "activities").read[BlueprintActivities])(Blueprint.apply _)

  def create(value: Blueprint) {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO ${dataSetName} 
        (id, type_id, max_production_limit) VALUES (
         {id}, {typeID}, {maxProductionLimit}
        );""").on(
        'id -> value.id,
        'typeID -> value.typeID,
        'maxProductionLimit -> value.maxProductionLimit)
      sql.executeInsert()

      // insert the activities as well
      BlueprintActivities.createForBlueprint(value)
    }
  }
}

