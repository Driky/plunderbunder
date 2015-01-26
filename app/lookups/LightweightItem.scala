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

  def productsForMaterial(materialID: Long) = {
    import play.api.Logger
    Logger.info("Checking material: " + materialID)
    
    DB.withConnection { implicit c =>
      val sql = SQL("""SELECT t.id, t.name
        FROM sde_blueprint_activity_materials m
          LEFT JOIN sde_blueprint_activity a 
            ON a.id = m.blueprint_activity_id 
              
          LEFT JOIN sde_blueprint_activity_products p ON
            m.blueprint_activity_id = p.blueprint_activity_id
          LEFT JOIN sde_inventorytypes t ON p.type_id=t.id
        WHERE m.type_id={materialID}
          AND a.activity_type=1;""").on('materialID -> materialID)

      sql().map { row =>
        {
          LightweightItem(
            row[String]("name"),
            row[Long]("id"))
        }
      }.toList
    }

  }

  implicit val lightweightItemFormat = Json.format[LightweightItem]
}