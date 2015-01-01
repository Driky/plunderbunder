package com.eveonline.sde

import play.api.libs.json._
import play.api.libs.json.Reads._

import anorm._
import play.api.db.DB

import play.api.Play.current

/**
 * Corresponds to the data in mapRegions.json
 */
case class Region(
  factionID: Option[Long],
  radius: BigDecimal,
  regionID: Long,
  regionName: String,
  x: BigDecimal,
  xMax: BigDecimal,
  xMin: BigDecimal,
  y: BigDecimal,
  yMax: BigDecimal,
  yMin: BigDecimal,
  z: BigDecimal,
  zMax: BigDecimal,
  zMin: BigDecimal) {
}

object Region extends BaseDataset {
  implicit val regionReads = Json.reads[Region]
  implicit val regionWrites = Json.writes[Region]

  def dataSetName = "sde_regions"

  def create(value: Region) = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO ${dataSetName} 
        (region_id, region_name,
        x, y, z, x_min, x_max, y_min, y_max, z_min, z_max, 
        faction_id, radius) VALUES (
         ${value.regionID}, '${value.regionName}',
         ${value.x}, ${value.y}, ${value.z}, 
         ${value.xMin}, ${value.xMax}, 
         ${value.yMin}, ${value.yMax}, 
         ${value.zMin}, ${value.zMax},
         {factionID}, ${value.radius} 
        );""").on('factionID -> value.factionID)
      sql.executeInsert()
    }
  }
}