package controllers

import play.api._
import play.api.mvc._

import play.api.Play.current

import anorm._
import play.api.db.DB
import org.joda.time.DateTime

import play.api.libs.json._

object Configure extends Controller {
  def index = Action { implicit request =>
    Ok(views.html.configure())
  }

  case class SdeMaintenaceLog(dataSet: String, lastImport: DateTime)

  implicit val sdeMaintenaceLogFormat = Json.format[SdeMaintenaceLog]

  def getLastMaintenanceStatus = {
    DB.withConnection { implicit c =>
      val sql = SQL("SELECT data_set, last_import FROM sde_maintenance;")

      val result = sql().map { row =>
        SdeMaintenaceLog(
          row[String]("data_set"),
          row[DateTime]("last_import"))
      }.toList

      result
    }
  }

  def maintenanceStatus = Action { implicit request =>
    val result = Json.toJson(getLastMaintenanceStatus)
    Ok(result)
  }

  import com.eveonline.sde._

  def reloadRegions = {
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
  
  def reloadSolarSystems = {
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

  def loadSdeJson(filename: String) = {
    val dataFile = io.Source.fromFile(filename)
    val dataContents = dataFile.getLines().mkString("\n")
    Json.parse(dataContents)
  }

  def reloadSde = Action { implicit request =>
    reloadRegions
    reloadSolarSystems

    Ok("ok" mkString "\n")
  }
}
