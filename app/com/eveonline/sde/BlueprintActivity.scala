package com.eveonline.sde

import play.api.libs.json._
import play.api.libs.json.Reads._

import play.api.libs.functional.syntax._

import anorm._
import play.api.db.DB

import play.api.Play.current

abstract class BlueprintActivity(
  id: Long,
  val activityTime: Int,
  val materials: List[BlueprintActivity.Material],
  val skills: List[BlueprintActivity.Skill],
  val products: List[BlueprintActivity.Product],
  val activityType: Int) {

}

object BlueprintActivity {
  val Manufacturing = 1
  val Copying = 2
  val ResearchMaterial = 3
  val ResearchTime = 4
  val Invention = 5

  case class Material(
    blueprintActivityID: Long,
    typeID: Long,
    quantity: Int) {

  }

  object Material {
    def createForActivity(activityID: Long, material: Material) {

      DB.withConnection { implicit c =>
        val sql = SQL(s"""INSERT INTO sde_blueprint_activity_materials 
        (blueprint_activity_id, type_id, quantity) VALUES (
         {blueprintActivityID}, {typeID}, {quantity}
        );""").on(
          'blueprintActivityID -> activityID,
          'typeID -> material.typeID,
          'quantity -> material.quantity)

        val inserted = sql.executeInsert()
      }
    }
  }

  case class Skill(
    blueprintActivityID: Long,
    typeID: Long,
    level: Int) {

  }

  object Skill {
    def createForActivity(activityID: Long, skill: Skill) {

      DB.withConnection { implicit c =>
        val sql = SQL(s"""INSERT INTO sde_blueprint_activity_skills 
        (blueprint_activity_id, type_id, level) VALUES (
         {blueprintActivityID}, {typeID}, {level}
        );""").on(
          'blueprintActivityID -> activityID,
          'typeID -> skill.typeID,
          'level -> skill.level)

        val inserted = sql.executeInsert()
      }
    }
  }

  case class Product(
    blueprintActivityID: Long,
    typeID: Long,
    quantity: Int,
    probability: Option[BigDecimal]) {
    
  }
  
  object Product {
    def createForActivity(activityID: Long, product: Product) {

      DB.withConnection { implicit c =>
        val sql = SQL(s"""INSERT INTO sde_blueprint_activity_products 
        (blueprint_activity_id, type_id, quantity, probability) VALUES (
         {blueprintActivityID}, {typeID}, {quantity}, {probability}
        );""").on(
          'blueprintActivityID -> activityID,
          'typeID -> product.typeID,
          'quantity -> product.quantity,
          'probability -> product.probability)

        val inserted = sql.executeInsert()
      }
    }
  }

  implicit val materialReads = (
    Reads.pure(-1L) and
    (__ \ "typeID").read[Long] and
    (__ \ "quantity").read[Int])(Material.apply _)
  implicit val skillReads = (
    Reads.pure(-1L) and
    (__ \ "typeID").read[Long] and
    (__ \ "level").read[Int])(Skill.apply _)
  implicit val productReads = (
    Reads.pure(-1L) and
    (__ \ "typeID").read[Long] and
    (__ \ "quantity").read[Int] and
    (__ \ "probability").readNullable[BigDecimal])(Product.apply _)

  def createForBlueprint(blueprint: Blueprint, activity: BlueprintActivity) {

    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO sde_blueprint_activity 
        (blueprint_id, activity_type, activity_time) VALUES (
         {blueprintID}, {activityType}, {activityTime}
        );""").on(
        'blueprintID -> blueprint.id,
        'activityType -> activity.activityType,
        'activityTime -> activity.activityTime)

      val inserted = sql.executeInsert()

      inserted.foreach { activityID =>
        // insert the activity components
        activity.materials.foreach { m => Material.createForActivity(activityID, m) }
        activity.products.foreach { p => Product.createForActivity(activityID, p) }
        activity.skills.foreach { s => Skill.createForActivity(activityID, s) }
      }
    }
  }
}



