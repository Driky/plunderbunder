package com.eveonline.sde

import play.api.libs.json.{ Reads, __ }

import play.api.libs.functional.syntax._ // scalastyle:ignore

import anorm.SQL
import play.api.db.DB

import play.api.Play.current

case class Blueprint(
  id: Long,
  typeID: Long,
  maxProductionLimit: Int,
  activities: BlueprintActivities)

object Blueprint extends BaseDataset {
  def dataSetName: String = "sde_blueprint"

  implicit val blueprintReads = (
    (__ \ "id").read[Long] and
    (__ \ "blueprintTypeID").read[Long] and
    (__ \ "maxProductionLimit").read[Int] and
    (__ \ "activities").read[BlueprintActivities])(Blueprint.apply _)

  override def deleteDataset: Boolean = {
    super.deleteDataset
    def deleter(tableName: String): Boolean = {
      DB.withConnection { implicit c =>
        val sql = SQL(s"DELETE FROM ${tableName};")
        sql.execute()
      }
    }

    deleter("sde_blueprint_activity")
    deleter("sde_blueprint_activity_materials")
    deleter("sde_blueprint_activity_skills")
    deleter("sde_blueprint_activity_products")

    true
  }

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
