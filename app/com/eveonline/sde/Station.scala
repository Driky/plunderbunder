package com.eveonline.sde

import play.api.libs.json.{ Json, Reads, __ }
import play.api.libs.functional.syntax._ // scalastyle:ignore

import anorm.SQL
import play.api.db.DB
import anorm.SqlParser.{ scalar }

import play.api.Play.current
import play.api.Logger

case class Station(
  stationID: Int,
  stationName: String,
  constellationID: Int,
  corporationID: Long,
  dockingCostPerVolume: Option[BigDecimal],
  maxShipVolumeDockable: Option[BigDecimal],
  officeRentalCost: Option[Int],
  operationID: Option[Int],
  regionID: Int,
  reprocessingEfficiency: Option[BigDecimal],
  reprocessingHangarFlag: Option[Int],
  reprocessingStationsTake: Option[BigDecimal],
  security: Option[BigDecimal],
  solarSystemID: Int,
  stationTypeID: Int,
  x: Option[BigDecimal],
  y: Option[BigDecimal],
  z: Option[BigDecimal]) {

  // Used to update the portions of a station
  // likely to change (eg. name, corp)
  def updateVolatileFields: Int = {
    DB.withConnection { implicit c =>
      val sql = SQL("""UPDATE sde_stations
        SET
          station_name={stationName},
          corporation_id={corporationID}
        WHERE station_id={stationID};""").on(
        'stationID -> stationID,
        'stationName -> stationName,
        'corporationID -> corporationID)

      sql.executeUpdate()
    }
  }

  def upsert: Option[Long] = {
    DB.withConnection { implicit c =>
      val existingCount = SQL("""SELECT COUNT(station_id) FROM sde_stations
        WHERE station_id={stationID}""").on('stationID -> stationID).as(scalar[Long].single)

      if (existingCount == 1) {
        updateVolatileFields
      } else {
        Station.create(this)
      }
      Station.updateModificationTime
    }
  }
}

object Station extends BaseDataset {
  implicit val reads = (
    (__ \ "stationID").read[String].map { _.toInt } and
    (__ \ "stationName").read[String] and
    (__ \ "constellationID").read[String].map { _.toInt } and
    (__ \ "corporationID").read[String].map { _.toLong } and
    (__ \ "dockingCostPerVolume").read[Option[BigDecimal]] and
    (__ \ "maxShipVolumeDockable").read[Option[BigDecimal]] and
    (__ \ "officeRentalCost").read[String].map { v => Option(v.toInt) } and
    (__ \ "operationID").read[String].map { v => Option(v.toInt) } and
    (__ \ "regionID").read[String].map { _.toInt } and
    (__ \ "reprocessingEfficiency").read[Option[BigDecimal]] and
    (__ \ "reprocessingHangarFlag").read[String].map { v => Option(v.toInt) } and
    (__ \ "reprocessingStationsTake").read[Option[BigDecimal]] and
    (__ \ "security").read[Option[BigDecimal]] and
    (__ \ "solarSystemID").read[String].map { _.toInt } and
    (__ \ "stationTypeID").read[String].map { _.toInt } and
    (__ \ "x").read[Option[BigDecimal]] and
    (__ \ "y").read[Option[BigDecimal]] and
    (__ \ "z").read[Option[BigDecimal]])(Station.apply _)

  def dataSetName: String = "sde_stations"

  def create(value: Station): Option[Long] = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO ${dataSetName}
        (station_id, constellation_id, corporation_id,
    docking_cost_per_volume, max_ship_volume_dockable,
    office_rental_cost, operation_id, region_id,
    reprocessing_efficiency, reprocessing_hangar_flag, reprocessing_stations_take,
    security, solar_system_id,
    station_name, station_type_id,
    x, y, z)
    VALUES (
         {stationID}, {constellationID},
         {corporationID}, {dockingCostPerVolume},
         {maxShipVolumeDockable}, {officeRentalCost},
         {operationID}, {regionID},
         {reprocessingEfficiency}, {reprocessingHangarFlag}, {reprocessingStationsTake},
         {security}, {solarSystemID},
         {stationName}, {stationTypeID},
         {x}, {y}, {z}
        );""").on(
        'stationID -> value.stationID,
        'constellationID -> value.constellationID,
        'corporationID -> value.corporationID,
        'dockingCostPerVolume -> value.dockingCostPerVolume,
        'maxShipVolumeDockable -> value.maxShipVolumeDockable,
        'officeRentalCost -> value.officeRentalCost,
        'operationID -> value.operationID,
        'regionID -> value.regionID,
        'reprocessingEfficiency -> value.reprocessingEfficiency,
        'reprocessingHangarFlag -> value.reprocessingHangarFlag,
        'reprocessingStationsTake -> value.reprocessingStationsTake,
        'security -> value.security,
        'solarSystemID -> value.solarSystemID,
        'stationName -> value.stationName,
        'stationTypeID -> value.stationTypeID,
        'x -> value.x,
        'y -> value.y,
        'z -> value.z)
      sql.executeInsert()
    }
  }

  def getByID(stationID: Int): Option[Station] = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""SELECT station_id, constellation_id, corporation_id,
    docking_cost_per_volume, max_ship_volume_dockable,
    office_rental_cost, operation_id, region_id,
    reprocessing_efficiency, reprocessing_hangar_flag, reprocessing_stations_take,
    security, solar_system_id,
    station_name, station_type_id,
    x, y, z
        FROM ${dataSetName}
      WHERE station_id={id}""").on('id -> stationID)

      sql().map { row =>
        {
          Station(
            row[Int]("station_id"),
            row[String]("station_name"),
            row[Int]("constellation_id"),
            row[Int]("corporation_id"),
            row[Option[BigDecimal]]("docking_cost_per_volume"),
            row[Option[BigDecimal]]("max_ship_volume_dockable"),
            row[Option[Int]]("office_rental_cost"),
            row[Option[Int]]("operation_id"),
            row[Int]("region_id"),
            row[Option[BigDecimal]]("reprocessing_efficiency"),
            row[Option[Int]]("reprocessing_hangar_flag"),
            row[Option[BigDecimal]]("reprocessing_stations_take"),
            row[Option[BigDecimal]]("security"),
            row[Int]("solar_system_id"),
            row[Int]("station_type_id"),
            row[Option[BigDecimal]]("x"),
            row[Option[BigDecimal]]("y"),
            row[Option[BigDecimal]]("z"))
        }
      }
    }.headOption
  }

  def mapForIDs(stationIDs: List[Int]): Map[Int, Station] = {
    val result = for {
      sid <- stationIDs
      station <- getByID(sid)
    } yield (sid -> station)

    result.toMap
  }
}
