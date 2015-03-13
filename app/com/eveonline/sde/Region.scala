package com.eveonline.sde

import play.api.libs.json.Json

import anorm.SQL
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
  zMin: BigDecimal)

object Region extends BaseDataset {
  implicit val regionReads = Json.reads[Region]
  implicit val regionWrites = Json.writes[Region]

  def dataSetName: String = "sde_regions"

  def create(value: Region): Option[Long] = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO ${dataSetName}
        (region_id, region_name,
        x, y, z, x_min, x_max, y_min, y_max, z_min, z_max,
        faction_id, radius) VALUES (
         {regionID}, {regionName},
         {x}, {y}, {z},
         {xMin},{xMax},
         {yMin}, {yMax},
         {zMin}, {zMax},
         {factionID}, {radius}
        );""").on(
        'regionID -> value.regionID,
        'regionName -> value.regionName,
        'x -> value.x,
        'y -> value.y,
        'z -> value.z,
        'xMin -> value.xMin,
        'xMax -> value.xMax,
        'yMin -> value.yMin,
        'yMax -> value.yMax,
        'zMin -> value.zMin,
        'zMax -> value.zMax,
        'factionID -> value.factionID,
        'radius -> value.radius)
      sql.executeInsert()
    }
  }
}
