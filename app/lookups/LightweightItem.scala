package lookups

import anorm._
import play.api.db.DB

import play.api.Play.current

import play.api.libs.json._

import com.eveonline.sde.InventoryType

case class LightweightItem(name: String, id: Long)

object LightweightItem {
  def listInventoryTypes = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""SELECT id, name 
        FROM ${InventoryType.dataSetName}""")

      sql().map { row => LightweightItem(row[String]("name"), row[Long]("id")) }.toList
    }
  }
  
  def getByID(itemID: Long) = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""SELECT id, name 
        FROM ${InventoryType.dataSetName}
      WHERE id={id}""").on('id -> itemID)

      sql().map { row => LightweightItem(row[String]("name"), row[Long]("id")) }.toList
    }.headOption
  }
  
  implicit val lightweightItemFormat = Json.format[LightweightItem]
}