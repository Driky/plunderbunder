
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import com.eveonline.sde._
import play.api.libs.json._
import play.api.libs.json.JsSuccess

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class SdeFormatTests extends Specification {

  "SDE" should {

    "parse a region" in {
      testRegion
    }

    "parse a region with a null faction ID" in {
      testRegionWithMissingFaction
    }

    "parse a solar system" in {
      testSolarSystem
    }

    "parse a solar system with a null faction ID" in {
      testSolarSystemWithMissingFaction
    }

    "parse an inventory type" in {
      testInventoryType
    }

    "parse an inventory type with no racial id" in {
      testInventoryTypeWithMissingFields
    }

    "parse a blueprint" in {
      testBasicBlueprint
    }

    "parse a manufacturing activity" in {
      testManufacturingActivity
    }

    "parse a blueprint with manufacturing omitted" in {
      testBlueprintWithoutManufacturing
    }

    "parse a blueprint with time and material research and copying omitted" in {
      testBlueprintWithoutResearch
    }

    "parse a station" in {
      testBasicStation
    }
  }

  def testBasicStation = {
    val rawJson = """
      {
        "constellationID": "20000407",
        "corporationID": "1000002",
        "dockingCostPerVolume": "0.0",
        "maxShipVolumeDockable": "50000000.0",
        "officeRentalCost": "10000",
        "operationID": "26",
        "regionID": "10000033",
        "reprocessingEfficiency": "0.5",
        "reprocessingHangarFlag": "4",
        "reprocessingStationsTake": "5.0000000000000003E-2",
        "security": "0",
        "solarSystemID": "30002780",
        "stationID": "60000004",
        "stationName": "Muvolailen X - Moon 3 - CBD Corporation Storage",
        "stationTypeID": "1531",
        "x": "1723680890880.0",
        "y": "256414064640.0",
        "z": "-60755435520.0"
    }
      """
    val js = Json.parse(rawJson)
    val result = js.validate[Station]

    result match {
      case JsSuccess(staResult, _) => {
        // Not crashing is all that we're testing
        staResult.constellationID mustEqual 20000407
        staResult.stationName mustEqual "Muvolailen X - Moon 3 - CBD Corporation Storage"
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testBlueprintWithoutResearch = {
    val rawJson = """{"activities":{
      "manufacturing":{
      "materials":[{"quantity":1,"typeID":28661}],
      "products":[{"quantity":1,"typeID":34241}],
      "skills":[{"level":1,"typeID":3380}],
      "time":10}},"blueprintTypeID":34242,
      "id":34242,"maxProductionLimit":1}"""

    val js = Json.parse(rawJson)

    val result = js.validate[Blueprint]

    result match {
      case JsSuccess(bpoResult, _) => {
        // Not crashing is all that we're testing
        bpoResult.activities.researchMaterial.isEmpty must beTrue
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testBlueprintWithoutManufacturing = {
    val rawJson = """{"activities":{
        "copying":{"time":4800},
        "research_material":{"time":2100},
        "research_time":{"time":2100}},
        "blueprintTypeID":33080,
        "id":33080,
        "maxProductionLimit":30}"""

    val js = Json.parse(rawJson)

    val result = js.validate[Blueprint]

    result match {
      case JsSuccess(bpoResult, _) => {
        // Not crashing is all that we're testing
        ok
        bpoResult.activities.manufacturing.isEmpty must beTrue
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testManufacturingActivity = {
    val rawJson = """{
                "materials": [
                    {
                        "quantity": 1000,
                        "typeID": 17425
                    }
                ],
                "products": [
                    {
                        "quantity": 1,
                        "typeID": 28385
                    }
                ],
                "skills": [
                    {
                        "level": 4,
                        "typeID": 12180
                    }
                ],
                "time": 600
            }"""

    val js = Json.parse(rawJson)

    val result = js.validate[ManufacturingActivity]

    result match {
      case JsSuccess(mfgResult, _) => {
        mfgResult.activityTime mustEqual 600
        mfgResult.materials.length mustEqual 1
        mfgResult.materials.head.quantity mustEqual 1000
        mfgResult.materials.head.typeID mustEqual 17425
        mfgResult.products.length mustEqual 1
        mfgResult.products.head.quantity mustEqual 1
        mfgResult.products.head.typeID mustEqual 28385

        mfgResult.skills.length mustEqual 1
        mfgResult.skills.head.level mustEqual 4
        mfgResult.skills.head.typeID mustEqual 12180

      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testBasicBlueprint = {
    val basicBlueprint = """{
        "activities": {
            "copying": {
                "time": 480
            },
            "manufacturing": {
                "materials": [
                    {
                        "quantity": 1000,
                        "typeID": 17425
                    }
                ],
                "products": [
                    {
                        "quantity": 1,
                        "typeID": 28385
                    }
                ],
                "skills": [
                    {
                        "level": 4,
                        "typeID": 12180
                    }
                ],
                "time": 600
            },
            "research_material": {
                "time": 210
            },
            "research_time": {
                "time": 210
            }
        },
        "blueprintTypeID": 28386,
        "id": 28386,
        "maxProductionLimit": 300
    }"""

    val js = Json.parse(basicBlueprint)

    val result = js.validate[Blueprint]

    result match {
      case JsSuccess(bpResult, _) => {
        bpResult.typeID mustEqual 28386
        bpResult.id mustEqual 28386
        bpResult.maxProductionLimit mustEqual 300

        val activities = bpResult.activities
        activities.copying.isDefined must beTrue
        activities.copying.get.activityTime mustEqual 480

        activities.manufacturing.isDefined must beTrue
        val manufacturing = activities.manufacturing.get

        manufacturing.activityTime mustEqual 600
        manufacturing.materials.length mustEqual 1
        manufacturing.materials.head.quantity mustEqual 1000
        manufacturing.materials.head.typeID mustEqual 17425

        manufacturing.products.length mustEqual 1
        manufacturing.products.head.quantity mustEqual 1
        manufacturing.products.head.typeID mustEqual 28385

        manufacturing.skills.length mustEqual 1
        manufacturing.skills.head.level mustEqual 4
        manufacturing.skills.head.typeID mustEqual 12180

        activities.researchMaterial.isDefined must beTrue
        activities.researchTime.isDefined must beTrue
        activities.invention.isDefined must beFalse
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testInventoryTypeWithMissingFields = {
    val broadswordBPO = """
              {
        "basePrice": ".0000",
        "capacity": "0.0",
        "chanceOfDuplicating": "0.0",
        "description": null,
        "groupID": "106",
        "marketGroupID": null,
        "mass": "0.0",
        "portionSize": "1",
        "published": "1",
        "raceID": null,
        "typeID": "12014",
        "typeName": "Broadsword Blueprint",
        "volume": "0.01"
    }
    """
    val js = Json.parse(broadswordBPO)

    val result = js.validate[InventoryType]

    result match {
      case JsSuccess(broadsword, _) => {
        broadsword.name mustEqual "Broadsword Blueprint"
        broadsword.id mustEqual 12014
        broadsword.raceID.isEmpty must beTrue
        broadsword.description.isDefined must beFalse
        broadsword.marketGroupID.isDefined must beFalse
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testInventoryType = {
    val ishtarType = """
      {
        "basePrice": "45590490.0000",
        "capacity": "560.0",
        "chanceOfDuplicating": "7.0000000000000007E-2",
        "description": "While not endowed with as much pure firepower as other ships of its category, the Ishtar is more than able to hold its own by virtue of its tremendous capacity for drones and its unique hard-coded drone-control subroutines. \n<br>Developer: CreoDron \nTouted as \"the Ishkur's big brother,\" the Ishtar design is the furthest CreoDron have ever gone towards creating a completely dedicated drone carrier. At various stages in its development process plans were made to strengthen the vessel in other areas, but ultimately the CreoDron engineers' fascination with pushing the drone carrier envelope overrode all other concerns.",
        "groupID": "358",
        "marketGroupID": "451",
        "mass": "11100000.0",
        "portionSize": "1",
        "published": "1",
        "raceID": "8",
        "typeID": "12005",
        "typeName": "Ishtar",
        "volume": "115000.0"
    }
      """

    val js = Json.parse(ishtarType)

    val result = js.validate[InventoryType]

    result match {
      case JsSuccess(ishtar, _) => {
        ishtar.name mustEqual "Ishtar"
        ishtar.id mustEqual 12005
        ishtar.raceID.getOrElse(-1) mustEqual 8
        ishtar.description.isDefined must beTrue
        ishtar.marketGroupID.getOrElse(-1) mustEqual 451
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testSolarSystemWithMissingFaction = {
    val j000102System = """{"factionID": null, "securityClass": null, "radius": 14959787070000.0, 
      "xMax": 7.632731613699681e+18, "constellation": 0, "zMax": -9.909122018184212e+18,
       "border": 0, "zMin": -9.909151937758351e+18, "regionID": 11000032, "corridor": 0, 
       "xMin": 7.63270169412554e+18, "international": 0, "yMin": 1.6298295108296737e+18, 
       "yMax": 1.6298594304038136e+18, "hub": 0, "sunTypeID": 34331, "regional": 0, 
       "security": -0.99, "constellationID": 21000333, "solarSystemName": "J000102", 
       "luminosity": 0.0, "fringe": 0, "y": 1.6298444706167437e+18, "x": 7.632716653912611e+18,
        "solarSystemID": 31002604, "z": -9.909136977971282e+18}
"""

    val js = Json.parse(j000102System)
    val result = js.validate[SolarSystem]

    result match {
      case JsSuccess(system, _) => {
        system.factionID.isEmpty must beTrue
        system.solarSystemName mustEqual "J000102"
        system.securityClass.isEmpty must beTrue
        system.solarSystemID mustEqual 31002604
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testSolarSystem = {
    val tanooSystem = """{"factionID": 500007, "securityClass": "B", "radius": 1323338364984.0, "xMax": -8.850925647176062e+16, 
    "constellation": 0, "zMax": 4.451411168978288e+16, 
    "border": 1, "zMin": 4.451303635589282e+16, "regionID": 10000001, 
    "corridor": 0, "xMin": -8.85119031484906e+16, "international": 1, "yMin": 4.236929792748907e+16, "yMax": 4.236964574926392e+16, 
    "hub": 1, "sunTypeID": 3802, 
    "regional": 1, "security": 0.8583240688484681, "constellationID": 20000001, 
    "solarSystemName": "Tanoo", "luminosity": 0.01575, "fringe": 0, "y": 4.236944396687888e+16, "x": -8.851079259998058e+16, 
    "solarSystemID": 30000001, "z": -4.451352534647966e+16}"""

    val js = Json.parse(tanooSystem)
    val result = js.validate[SolarSystem]

    result match {
      case JsSuccess(tanooSystem, _) => {
        tanooSystem.factionID.getOrElse(-1) mustEqual 500007
        tanooSystem.solarSystemName mustEqual "Tanoo"
      }
      case JsError(error) => {
        ko(s"Json Parsing Error: ${error}")
      }
    }
  }

  def testRegionWithMissingFaction = {
    val valeRegion = """{"factionID": null, 
      "yMin": 5.820417040751427e+16, "zMax": -1.438897984136382e+17, 
      "yMax": 1.3125471789740226e+17, "zMin": -2.1887959335751645e+17, 
      "regionID": 10000003, "xMin": -9.923376026076944e+16, 
      "radius": 7.604655192445226e+16, "xMax": 1.109511358509361e+16, 
      "y": 9.472944415245827e+16, 
      "x": -4.406932333783791e+16, "z": 1.8138469588557734e+17, 
      "regionName": "Vale of the Silent"}"""

    val js = Json.parse(valeRegion)
    val result = js.validate[Region]

    val itsOk = result match {
      case JsSuccess(theVale, _) => {
        theVale.regionName mustEqual ("Vale of the Silent")

        theVale.regionID mustEqual 10000003

        theVale.factionID.isEmpty must beTrue
      }
      case JsError(error) => ko(s"Json Parsing Error: ${error}")
    }

    itsOk
  }

  def testRegion = {
    val forgeRegion = """{
        "factionID": 500001,
        "radius": 6.3892719581567224e+16,
        "regionID": 10000002,
        "regionName": "The Forge",
        "x": -9.642032966461757e+16,
        "xMax": -4.919500463095301e+16,
        "xMin": -1.4364565469828213e+17,
        "y": 6.40270758377404e+16,
        "yMax": 9.289959527872566e+16,
        "yMin": 3.5154556396755124e+16,
        "z": 1.125398171329042e+17,
        "zMax": -8.062703110404978e+16,
        "zMin": -1.4445260316175862e+17
    }"""

    val js = Json.parse(forgeRegion)
    val result = js.validate[Region]

    val itsOk = result match {
      case JsSuccess(theForge, _) => {
        theForge.regionName mustEqual ("The Forge")

        val expected = BigDecimal("-96420329664617570")
        theForge.x.compare(expected) mustEqual 0

        theForge.regionID mustEqual 10000002

        theForge.factionID.getOrElse(-1) mustEqual 500001
      }
      case JsError(error) => ko(s"Json Parsing Error: ${error}")
    }

    itsOk

  }

}