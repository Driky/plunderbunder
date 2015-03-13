package lookups

import anorm.SQL
import play.api.db.DB
import play.api.Logger
import play.api.Play.current
import play.api.libs.json.Json

import com.eveonline.sde.InventoryType

case class LightweightItem(name: String, id: Long)

object LightweightItem {

  val nameField = "name"
  val idField = "id"

  def listInventoryTypes(onlyManufacturable: Boolean = false): List[LightweightItem] = {
    DB.withConnection { implicit c =>

      val sqlPartial = if (onlyManufacturable) {
        """INNER JOIN sde_blueprint_activity_products p on p.type_id=t.id
           INNER JOIN sde_blueprint_activity a on a.id = p.blueprint_activity_id AND a.activity_type = 1"""
      } else {
        " "
      }

      val sql = SQL(s"""SELECT t.id, t.name
        FROM sde_inventorytypes t
          ${sqlPartial}
        GROUP BY t.id
        ORDER BY t.id;""")

      sql().map { row => LightweightItem(row[String](nameField), row[Long](idField)) }.toList
    }
  }

  def getByID(itemID: Long): Option[LightweightItem] = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"""SELECT id, name
        FROM ${InventoryType.dataSetName}
      WHERE id={id}""").on('id -> itemID)

      sql().map { row => LightweightItem(row[String](nameField), row[Long](idField)) }.toList
    }.headOption
  }

  def productsForMaterial(materialID: Long): List[LightweightItem] = {
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
