package com.eveonline.sde

import play.api.libs.json._
import play.api.libs.json.Reads._

import play.api.libs.functional.syntax._

case class BlueprintActivities(
  copying: Option[CopyingActivity],
  invention: Option[InventionActivity],
  manufacturing: Option[ManufacturingActivity],
  researchMaterial: Option[ResearchMaterialActivity],
  researchTime: Option[ResearchTimeActivity])

object BlueprintActivities {
  implicit val blueprintActivitiesReads = (
    (__ \ "copying").readNullable[CopyingActivity] and
    (__ \ "invention").readNullable[InventionActivity] and
    (__ \ "manufacturing").readNullable[ManufacturingActivity] and
    (__ \ "research_material").readNullable[ResearchMaterialActivity] and
    (__ \ "research_time").readNullable[ResearchTimeActivity])(BlueprintActivities.apply _)

  def createForBlueprint(value: Blueprint) {
    val activities = value.activities

    activities.copying.foreach { c => BlueprintActivity.createForBlueprint(value, c) }
    activities.invention.foreach { i => BlueprintActivity.createForBlueprint(value, i) }
    activities.manufacturing.foreach { m => BlueprintActivity.createForBlueprint(value, m) }
    activities.researchMaterial.foreach { rm => BlueprintActivity.createForBlueprint(value, rm) }
    activities.researchTime.foreach { rt => BlueprintActivity.createForBlueprint(value, rt) }
  }
}