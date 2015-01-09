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
  
  implicit val lightweightItemFormat = Json.format[LightweightItem]
}