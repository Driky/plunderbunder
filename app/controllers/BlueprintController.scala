package controllers

import play.api.Logger
import play.api.mvc.{ Action, AnyContent, Controller }

import play.api.Play.current

import anorm.SQL
import play.api.db.DB

import play.api.Play.current

import play.api.libs.json.{ Json, JsNumber, JsObject }

import scala.language.postfixOps
import auth.AuthenticatedAction

object BlueprintController extends Controller with JsonController {

  case class BillOfMaterials(blueprintID: Long, materialID: Long, quantity: Int, name: String, volume: BigDecimal, materialBlueprint: Option[Long])

  def blueprintIDForItem(itemID: Long): Option[Long] = {
    DB.withConnection { implicit c =>
      val sql = SQL("""
        SELECT blueprint_id FROM SDE_BLUEPRINT_ACTIVITY_PRODUCTS product
          left join sde_blueprint_activity activity on activity.id=product.blueprint_activity_id
          left join sde_blueprint blueprint on blueprint.id=activity.blueprint_id
         WHERE product.TYPE_ID = {productID};""").on('productID -> itemID)

      sql().map { row =>
        {
          val bpid = row[Long]("blueprint_id")
          Logger.info("Found + " + bpid.toString)
          bpid
        }
      }.headOption
    }
  }

  def baseMaterialsForProduct(productID: Long): List[BillOfMaterials] = {
    DB.withConnection { implicit c =>
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
          val materialID = row[Long]("SDE_BLUEPRINT_ACTIVITY_MATERIALS.TYPE_ID")
          val materialBlueprint = blueprintIDForItem(materialID)
          BillOfMaterials(
            row[Long]("SDE_BLUEPRINT.ID"),
            materialID,
            row[Int]("quantity"),
            row[String]("name"),
            row[BigDecimal]("volume"),
            materialBlueprint)
        }
      } toList
    }
  }

  def quantityProducedPerRun(productID: Long): Option[Long] = {
    DB.withConnection { implicit c =>
      val sql = SQL("""SELECT quantity  from sde_blueprint_activity_products p
                        WHERE type_id = {productID};""").on('productID -> productID)

      sql().map { row => row[Long]("quantity") }.toList.headOption
    }

  }

  def materialsForProduct(productID: Long): Action[AnyContent] = AuthenticatedAction {
    implicit val bomFormat = Json.format[BillOfMaterials]

    val baseMaterials = baseMaterialsForProduct(productID)
    val quantityProduced = quantityProducedPerRun(productID).getOrElse(0L)

    val response = JsObject("materials" -> Json.toJson(baseMaterials) ::
      "quantityProduced" -> JsNumber(quantityProduced) ::
      Nil)

    OkJson(response)
  }

  def productsForMaterial(materialID: Long): Action[AnyContent] = AuthenticatedAction {
    val products = lookups.LightweightItem.productsForMaterial(materialID)

    Logger.info("Products: " + products)
    val response = JsObject("items" -> Json.toJson(products) :: Nil)
    OkJson(response)
  }
}
