package com.eveonline.sde

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import anorm._
import play.api.db.DB

import play.api.Play.current

case class SolarSystem(
  solarSystemID: Long,
  solarSystemName: String,
  regionID: Long,
  factionID: Option[Long],
  radius: BigDecimal,
  luminosity: BigDecimal,
  sunTypeID: Int,
  constellationID: Long,
  x: BigDecimal,
  y: BigDecimal,
  z: BigDecimal,
  security: BigDecimal,
  securityClass: Option[String],
  border: Boolean,
  constellation: Boolean,
  corridor: Boolean,
  fringe: Boolean,
  hub: Boolean,
  international: Boolean,
  regional: Boolean /*  // drop this due to unapply limitations
  ,xMax: BigDecimal,
  xMin: BigDecimal,
  yMax: BigDecimal,
  yMin: BigDecimal,
  zMax: BigDecimal,
  zMin: BigDecimal*/ ) {
}

object SolarSystem extends BaseDataset {
  implicit val solarSystemReads =
    (
      (__ \ "solarSystemID").read[Long] and
      (__ \ "solarSystemName").read[String] and
      (__ \ "regionID").read[Long] and
      (__ \ "factionID").read[Option[Long]] and
      (__ \ "radius").read[BigDecimal] and
      (__ \ "luminosity").read[BigDecimal] and
      (__ \ "sunTypeID").read[Int] and
      (__ \ "constellationID").read[Long] and
      (__ \ "x").read[BigDecimal] and
      (__ \ "y").read[BigDecimal] and
      (__ \ "z").read[BigDecimal] and
      (__ \ "security").read[BigDecimal] and
      (__ \ "securityClass").read[Option[String]] and
      (__ \ "border").read[Int].map { _ != 0 } and
      (__ \ "constellation").read[Int].map { _ != 0 } and
      (__ \ "corridor").read[Int].map { _ != 0 } and
      (__ \ "fringe").read[Int].map { _ != 0 } and
      (__ \ "hub").read[Int].map { _ != 0 } and
      (__ \ "international").read[Int].map { _ != 0 } and
      (__ \ "regional").read[Int].map { _ != 0 })(SolarSystem.apply _)

  // Not terribly confident this would write out correctly
  //implicit val solarSystemWrites = Json.writes[SolarSystem]

  def dataSetName = "sde_solarsystems"

  def create(value: SolarSystem) = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO ${dataSetName} 
        (solar_system_id, solar_system_name,
        region_id, faction_id,
        radius, luminosity,
        sun_type_id, constellation_id,
        x, y, z,
        security, security_class,
        border, constellation,
        corridor, fringe,
        hub, international,
        regional) VALUES (
         {solarSystemID}, {solarSystemName},
         {regionID}, {factionID},
         {radius}, {luminosity},
         {sunTypeID}, {constellationID},  
         {x}, {y}, {z}, 
         {security}, {securityClass},
         {border}, {constellation},
         {corridor}, {fringe},
         {hub}, {international},
         {regional}
        );""").on(
        'solarSystemID -> value.solarSystemID,
        'solarSystemName -> value.solarSystemName,
        'regionID -> value.regionID,
        'factionID -> value.factionID,
        'radius -> value.radius,
        'luminosity -> value.luminosity,
        'sunTypeID -> value.sunTypeID,
        'constellationID -> value.constellationID,
        'x -> value.x,
        'y -> value.y,
        'z -> value.z,
        'security -> value.security,
        'securityClass -> value.securityClass,
        'border -> value.border,
        'constellation -> value.constellation,
        'corridor -> value.corridor,
        'fringe -> value.fringe,
        'hub -> value.hub,
        'international -> value.international,
        'regional -> value.regional)
      sql.executeInsert()
    }
  }
}