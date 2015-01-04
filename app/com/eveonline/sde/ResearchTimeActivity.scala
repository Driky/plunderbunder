package com.eveonline.sde

import play.api.libs.json._
import play.api.libs.json.Reads._

import play.api.libs.functional.syntax._

case class ResearchTimeActivity(
  id: Long,
  override val activityTime: Int,
  override val materials: List[BlueprintActivity.Material],
  override val skills: List[BlueprintActivity.Skill],
  override val products: List[BlueprintActivity.Product]) extends BlueprintActivity(id, activityTime,
  materials, skills, products, BlueprintActivity.ResearchTime) {
  
}

object ResearchTimeActivity {
  implicit val researchTimeActivityReads = (
    Reads.pure(-1L) and
    (__ \ "time").read[Int] and
    (__ \ "materials").readNullable[List[BlueprintActivity.Material]].map {
      _.fold(List[BlueprintActivity.Material]())(l => l)
    } and
    (__ \ "skills").readNullable[List[BlueprintActivity.Skill]].map {
      _.fold(List[BlueprintActivity.Skill]())(l => l)
    } and
    (__ \ "products").readNullable[List[BlueprintActivity.Product]].map {
      _.fold(List[BlueprintActivity.Product]())(l => l)
    })(ResearchTimeActivity.apply _)
}