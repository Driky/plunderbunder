package controllers

import play.api._
import play.api.mvc._

import play.api.Play.current

import anorm._
import play.api.db.DB

import play.api.Play.current

import play.api.libs.json._

import scala.language.postfixOps

object BlueprintController extends Controller {

  case class BillOfMaterials(blueprintID: Long, materialID: Long, quantity: Int, name: String, volume: BigDecimal)
  
  def materialsForProduct(productID: Long) = AuthenticatedAction { 
    implicit val bomFormat = Json.format[BillOfMaterials]

    val result = DB.withConnection { implicit c =>
      val sql = SQL("""SELECT blueprint.id as blueprint_id, material.type_id as material_id, material.quantity, item.name, item.volume
      FROM sde_blueprint blueprint
       LEFT JOIN sde_blueprint_activity activity 
         on blueprint.id=activity.blueprint_id 
         AND activity.activity_type=1
       LEFT JOIN sde_blueprint_activity_materials material 
         on material.blueprint_activity_id=activity.id
       LEFT JOIN sde_inventorytypes item 
         on item.id = material.type_id
      WHERE blueprint.id = 
        (SELECT activity.blueprint_id FROM SDE_BLUEPRINT_ACTIVITY_PRODUCTS product
          left join sde_blueprint_activity activity on activity.id=product.blueprint_activity_id
          left join sde_blueprint blueprint on blueprint.id=activity.blueprint_id
         WHERE product.TYPE_ID = {productID});""").on('productID -> productID)

      sql().map { row =>
        {
          BillOfMaterials(
            row[Long]("SDE_BLUEPRINT.ID"),
            row[Long]("SDE_BLUEPRINT_ACTIVITY_MATERIALS.TYPE_ID"),
            row[Int]("quantity"),
            row[String]("name"),
            row[BigDecimal]("volume"))
        }
      } toList
    }
    
    val response = Json.toJson(result)
    
    Ok(response)
  }
}