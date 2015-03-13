package controllers

import lookups.LightweightItem
import play.api.Routes
import play.api.mvc.{ Action, AnyContent, Controller }

import play.api.Play.current

import scala.concurrent.Future

import com.eveonline.crest.{ InvalidTokenException, TokenResponse }
import play.api.libs.json.{ Json, JsObject, JsString }

import auth.AuthenticatedAction

object Application extends Controller {
  val jsContentType = "text/javascript"

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index: Action[AnyContent] = Action.async { request =>
    Future(Ok(views.html.index()))
  }

  def assetRoutes: Action[AnyContent] = Action { implicit request =>
    Ok(Routes.javascriptRouter("ar")(controllers.routes.javascript.Assets.at)).as(jsContentType)
  }

  def jsRoutes: Action[AnyContent] = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.User.user,
        routes.javascript.User.userProfile,
        routes.javascript.User.updateUserProfile,
        routes.javascript.User.getUserAssets,
        routes.javascript.User.getMarketOrders,
        routes.javascript.Configure.maintenanceStatus,
        routes.javascript.Configure.reloadSde,
        routes.javascript.Configure.reloadNullsecStations,
        routes.javascript.Application.inventoryItems,
        routes.javascript.Application.buildableInventoryItems,
        routes.javascript.BlueprintController.materialsForProduct,
        routes.javascript.BlueprintController.productsForMaterial,
        routes.javascript.Authentication.logout,
        routes.javascript.MarketController.jitaPriceForItem)).as(jsContentType)
  }

  def configuration: Action[AnyContent] = Action { implicit request =>
    // This is just common things probably needed by coffeescript frontends

    val config = current.configuration
    val clientID = config.getString("crest.client_id", None).getOrElse("missing_client_id")
    val callbackURI = config.getString("crest.callback", None).getOrElse("missing_callback_uri")

    val eveLoginUrl = s"https://login.eveonline.com/oauth/authorize/?" +
      s"response_type=code&redirect_uri=${callbackURI}&client_id=${clientID}" +
      s"&scope=publicData&state=uniquestate123"

    val jsResult = JsObject(Seq("eve_login" -> JsString(eveLoginUrl)))

    val preloaded = s"var plunderbunderConfig = ${jsResult.toString()}"

    Ok(preloaded).as(jsContentType)

  }

  def buildableInventoryItems: Action[AnyContent] = Action {
    val manufacturables = LightweightItem.listInventoryTypes(onlyManufacturable = true)

    val js = Json.toJson(manufacturables)

    Ok(js)
  }

  def inventoryItems: Action[AnyContent] = Action { implicit request =>
    val inventoryNames = LightweightItem.listInventoryTypes()

    val js = Json.toJson(inventoryNames)

    Ok(js)
  }
}
