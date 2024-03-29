package controllers

import play.api.mvc.{ Action, AnyContent, Controller }

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

import anorm.SQL
import play.api.db.DB
import org.joda.time.DateTime

import play.api.libs.json.{ Json, JsError, JsObject, JsString, JsSuccess, JsValue }

import auth.AuthenticatedAction

import com.eveonline.xmlapi.requests.ConquerableStations
import com.eveonline.sde.{ Blueprint, InventoryType, MarketGroup, Region, SolarSystem, Station }

object Configure extends Controller {
  def index: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.configure())
  }

  case class SdeMaintenanceLog(dataSet: String, lastImport: DateTime)

  implicit val sdeMaintenanceLogFormat = Json.format[SdeMaintenanceLog]

  def getLastMaintenanceStatus: List[SdeMaintenanceLog] = {
    DB.withConnection { implicit c =>
      val sql = SQL("SELECT data_set, last_import FROM sde_maintenance;")

      val result = sql().map { row =>
        SdeMaintenanceLog(
          row[String]("data_set"),
          row[DateTime]("last_import"))
      }.toList

      result
    }
  }

  def maintenanceStatus: Action[AnyContent] = AuthenticatedAction { implicit request =>
    val result = Json.toJson(getLastMaintenanceStatus)
    Ok(result)
  }

  def reloadRegions: Boolean = {
    val jsResult = loadSdeJson("sde_scripts/json/mapRegions.json")

    jsResult.validate[List[Region]] match {
      case JsSuccess(regions, _) => {
        Region.deleteAll
        regions.foreach { r => Region.create(r) }
        Region.updateModificationTime
        true
      }
      case JsError(reason) => {
        throw new Exception(reason.seq.toString)
      }
    }
  }

  def reloadSolarSystems: Boolean = {
    val jsResult = loadSdeJson("sde_scripts/json/mapSolarSystems.json")
    jsResult.validate[List[SolarSystem]] match {
      case JsSuccess(solarSystems, _) => {
        SolarSystem.deleteAll
        solarSystems.foreach { s => SolarSystem.create(s) }
        SolarSystem.updateModificationTime
        true
      }
      case JsError(reason) => {
        throw new Exception(reason.seq.toString)
      }
    }
  }

  def reloadStations: Boolean = {
    val jsResult = loadSdeJson("sde_scripts/json/staStations.json")
    jsResult.validate[List[Station]] match {
      case JsSuccess(stations, _) => {
        Station.deleteAll
        stations.foreach { s => Station.create(s) }
        Station.updateModificationTime
        true
      }
      case JsError(reason) => {
        throw new Exception(reason.seq.toString)
      }
    }
  }

  def reloadInventoryTypes: Boolean = {
    val jsResult = loadSdeJson("sde_scripts/json/invTypes.json")
    jsResult.validate[List[InventoryType]] match {
      case JsSuccess(inventoryTypes, _) => {
        InventoryType.deleteAll
        inventoryTypes.foreach { t => InventoryType.create(t) }
        InventoryType.updateModificationTime
        true
      }
      case JsError(reason) => {
        throw new Exception(reason.seq.toString)
      }
    }
  }

  def reloadBlueprints: Boolean = {
    val jsResult = loadSdeJson("sde_scripts/json/blueprints.json")
    jsResult.validate[List[Blueprint]] match {
      case JsSuccess(blueprints, _) => {
        Blueprint.deleteAll
        blueprints.foreach {
          bp => Blueprint.create(bp)
        }
        Blueprint.updateModificationTime
        true
      }
      case JsError(reason) => {
        throw new Exception(reason.seq.toString)
      }
    }
  }

  def reloadMarketGroups: Boolean = {
    val jsResult = loadSdeJson("sde_scripts/json/invMarketGroups.json")
    jsResult.validate[List[MarketGroup]] match {
      case JsSuccess(marketGroups, _) => {
        MarketGroup.deleteAll
        marketGroups.foreach {
          mg => MarketGroup.create(mg)
        }
        MarketGroup.updateModificationTime
        true
      }
      case JsError(reason) => {
        throw new Exception(reason.seq.toString)
      }
    }
  }

  def loadSdeJson(filename: String): JsValue = {
    val dataFile = io.Source.fromFile(filename)
    val dataContents = dataFile.getLines().mkString("\n")
    Json.parse(dataContents)
  }

  def reloadSde: Action[AnyContent] = AuthenticatedAction { implicit request =>
    reloadRegions
    reloadSolarSystems
    reloadStations
    reloadInventoryTypes
    reloadBlueprints
    reloadMarketGroups

    Ok(JsObject(Seq("result" -> JsString("ok"))))
  }

  def updateNullsecStationsFromApi() {
    val stationList = ConquerableStations.list
    stationList.map {
      _.foreach { _.upsert }
    }
  }

  def reloadNullsecStations: Action[AnyContent] = AuthenticatedAction { implicit request =>
    updateNullsecStationsFromApi

    Ok(JsObject(Seq("result" -> JsString("ok"))))
  }
}
