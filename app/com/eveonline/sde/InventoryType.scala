package com.eveonline.sde

import play.api.libs.json.{ Reads, __ }

import play.api.libs.functional.syntax._ // scalastyle:ignore

import anorm.SQL
import play.api.db.DB

import play.api.Play.current

case class InventoryType(
  id: Long,
  name: String,
  description: Option[String],
  raceID: Option[Int],
  basePrice: BigDecimal,
  capacity: BigDecimal,
  chanceOfDuplicating: BigDecimal,
  groupID: Long,
  marketGroupID: Option[Long],
  mass: BigDecimal,
  portionSize: Int,
  published: Int,
  volume: BigDecimal)

object InventoryType extends BaseDataset {

  def dataSetName: String = "sde_inventorytypes"

  implicit val inventoryTypeReads = (
    (__ \ "typeID").read[String].map { _.toLong } and
    (__ \ "typeName").read[String] and
    (__ \ "description").read[Option[String]] and
    (__ \ "raceID").read[Option[String]].map { _.flatMap(v => Option(v.toInt)) } and
    (__ \ "basePrice").read[BigDecimal] and
    (__ \ "capacity").read[BigDecimal] and
    (__ \ "chanceOfDuplicating").read[BigDecimal] and
    (__ \ "groupID").read[String].map { _.toLong } and
    (__ \ "marketGroupID").read[Option[String]].map { _.flatMap(v => Option(v.toLong)) } and
    (__ \ "mass").read[BigDecimal] and
    (__ \ "portionSize").read[String].map { _.toInt } and
    (__ \ "published").read[String].map { _.toInt } and
    (__ \ "volume").read[BigDecimal])(InventoryType.apply _)

  def create(value: InventoryType): Option[Long] = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO ${dataSetName}
        (id, name,
        base_price, capacity,
        chance_of_duplicating, description,
        group_id, market_group_id,
        mass, portion_size,
        published, race_id,
        volume) VALUES (
         {id}, {name},
         {basePrice}, {capacity},
         {chanceOfDuplicating}, {description},
         {groupID}, {marketGroupID},
         {mass}, {portionSize},
         {published}, {raceID},
         {volume}
        );""").on(
        'id -> value.id,
        'name -> value.name,
        'capacity -> value.capacity,
        'basePrice -> value.basePrice,
        'chanceOfDuplicating -> value.chanceOfDuplicating,
        'description -> value.description,
        'groupID -> value.groupID,
        'marketGroupID -> value.marketGroupID,
        'mass -> value.mass,
        'portionSize -> value.portionSize,
        'published -> value.published,
        'raceID -> value.raceID,
        'volume -> value.volume)
      sql.executeInsert()
    }
  }

  def getByID(typeID: Long): Option[InventoryType] = {
    DB.withConnection { implicit c =>
      val sql = SQL("""SELECT id, name,
          base_price, capacity,
          chance_of_duplicating, description,
          group_id, market_group_id,
          mass, portion_size,
          published, race_id,
          volume
        FROM sde_inventorytypes
        WHERE id={typeID};
        """).on('typeID -> typeID)

      sql().map { row =>
        {
          InventoryType(
            row[Long]("id"),
            row[String]("name"),
            row[Option[String]]("description"),
            row[Option[Int]]("race_id"),
            row[BigDecimal]("base_price"),
            row[BigDecimal]("capacity"),
            row[BigDecimal]("chance_of_duplicating"),
            row[Long]("group_id"),
            row[Option[Long]]("market_group_id"),
            row[BigDecimal]("mass"),
            row[Int]("portion_size"),
            row[Int]("published"),
            row[BigDecimal]("volume"))
        }
      }.headOption
    }
  }

  def mapForIDs(typeIDs: List[Long]): Map[Long, InventoryType] = {
    val result = for {
      tid <- typeIDs
      item <- getByID(tid)
    } yield (tid -> item)

    result.toMap
  }
}
