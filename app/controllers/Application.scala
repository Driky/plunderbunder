package controllers

import lookups._
import play.api._
import play.api.mvc._

import play.api.Play.current

import play.api.libs.ws._
import scala.concurrent.Future

import com.eveonline.crest.TokenResponse
import com.eveonline.crest.TokenResponseSerializer._
import com.eveonline.crest.SingleSignOn._
import com.eveonline.crest.InvalidTokenException

import play.api.libs.json._

import auth.AuthenticatedAction

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = Action.async { request =>
    Future(Ok(views.html.index()))
  }

  def assetRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("ar")(controllers.routes.javascript.Assets.at)).as("text/javascript")
  }

  def jsRoutes = Action { implicit request =>
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
        routes.javascript.MarketController.jitaPriceForItem)).as("text/javascript")
  }

  def configuration = Action { implicit request =>
    // This is just common things probably needed by coffeescript frontends

    val config = current.configuration
    val clientID = config.getString("crest.client_id", None).getOrElse("missing_client_id")
    val callbackURI = config.getString("crest.callback", None).getOrElse("missing_callback_uri")

    val eveLoginUrl = s"https://login.eveonline.com/oauth/authorize/?response_type=code&redirect_uri=${callbackURI}&client_id=${clientID}&scope=publicData&state=uniquestate123"

    val jsResult = JsObject(Seq("eve_login" -> JsString(eveLoginUrl)))

    val preloaded = s"var plunderbunderConfig = ${jsResult.toString()}"

    Ok(preloaded).as("text/javascript")

  }

  def buildableInventoryItems = Action {
    val manufacturables = LightweightItem.listInventoryTypes(onlyManufacturable=true)

    val js = Json.toJson(manufacturables)

    Ok(js)
  }
  
  def inventoryItems = Action { implicit request =>
    val inventoryNames = LightweightItem.listInventoryTypes()

    val js = Json.toJson(inventoryNames)

    Ok(js)
  }

}