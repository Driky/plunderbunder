import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.Logger

import scala.xml._
import com.eveonline.xmlapi.{ AssetResponse, ConquerableStationResponse }
import com.eveonline.sde.Station
import org.joda.time.DateTimeZone
import lookups.LightweightItem

@RunWith(classOf[JUnitRunner])
class XmlApiSpec extends Specification {

  "EVE Xml Api" should {
    "parse a valid asset xml result" in running(
      FakeApplication( /*additionalConfiguration = inMemoryDatabase()*/ )) {
        testAssetXmlParsing
      }

    "parse a valid conquerable station result" in running(
      FakeApplication()) {
        testConquerableStationXmlParsing
      }
  }

  def testConquerableStationXmlParsing = {
    // This requires the database for lookups
    val rawXml = """<?xml version='1.0' encoding='UTF-8'?>
<eveapi version="2">
  <currentTime>2015-01-22 22:20:55</currentTime>
  <result>
    <rowset name="outposts" key="stationID" columns="stationID,stationName,stationTypeID,solarSystemID,corporationID,corporationName">
      <row stationID="60014917" stationName="VFK-IV VI - Moon 1 - Mittanigrad" stationTypeID="12295" solarSystemID="30002904" corporationID="667531913" corporationName="GoonWaffe"/>
    </rowset>
  </result>
  <cachedUntil>2015-01-22 23:04:09</cachedUntil>
</eveapi>
      
      """
    val xml = XML.loadString(rawXml)
    val response = ConquerableStationResponse.fromXml(xml)

    response.apiVersion mustEqual 2

    response.responseTime.getYear mustEqual 2015
    response.responseTime.getMonthOfYear mustEqual 1
    response.responseTime.getDayOfMonth mustEqual 22
    response.responseTime.getHourOfDay() mustEqual 22
    response.responseTime.getMinuteOfHour() mustEqual 20
    response.responseTime.getSecondOfMinute() mustEqual 55
    response.responseTime.getZone mustEqual DateTimeZone.UTC

    response.cachedUntil.getYear mustEqual 2015
    response.cachedUntil.getMonthOfYear mustEqual 1
    response.cachedUntil.getDayOfMonth mustEqual 22
    response.cachedUntil.getHourOfDay() mustEqual 23
    response.cachedUntil.getMinuteOfHour() mustEqual 4
    response.cachedUntil.getSecondOfMinute() mustEqual 9
    response.cachedUntil.getZone mustEqual DateTimeZone.UTC

    response.stations.length mustEqual 1
    val station = response.stations.head
    
    station.stationID mustEqual 60014917
    station.stationName mustEqual "VFK-IV VI - Moon 1 - Mittanigrad"
    station.stationTypeID mustEqual 12295
    station.solarSystemID mustEqual 30002904
    station.corporationID mustEqual 667531913L
  }

  def testAssetXmlParsing = {
    val rawXml = """<?xml version='1.0' encoding='UTF-8'?>
<eveapi version="2">
  <currentTime>2015-01-21 20:56:15</currentTime>
  <result>
    <rowset name="assets" key="itemID" columns="itemID,locationID,typeID,quantity,flag,singleton">
      <row itemID="100504209" locationID="60001846" typeID="2078" quantity="1" flag="4" singleton="0" />
      <row itemID="593918585" locationID="60001846" typeID="586" quantity="1" flag="4" singleton="1" rawQuantity="-1">
        <rowset name="contents" key="itemID" columns="itemID,typeID,quantity,flag,singleton">
          <row itemID="598265660" typeID="18639" quantity="1" flag="27" singleton="1" rawQuantity="-1" />
          <row itemID="1811332588" typeID="30028" quantity="7" flag="5" singleton="0" />
        </rowset>
      </row>
    </rowset>
  </result>
  <cachedUntil>2015-01-22 02:55:13</cachedUntil>
</eveapi>
      """

    val xml = XML.loadString(rawXml)
    val response = AssetResponse.fromXml(xml)

    response.apiVersion mustEqual 2

    response.responseTime.getYear mustEqual 2015
    response.responseTime.getMonthOfYear mustEqual 1
    response.responseTime.getDayOfMonth mustEqual 21
    response.responseTime.getHourOfDay() mustEqual 20
    response.responseTime.getMinuteOfHour() mustEqual 56
    response.responseTime.getSecondOfMinute() mustEqual 15
    response.responseTime.getZone mustEqual DateTimeZone.UTC

    response.cachedUntil.getYear mustEqual 2015
    response.cachedUntil.getMonthOfYear mustEqual 1
    response.cachedUntil.getDayOfMonth mustEqual 22
    response.cachedUntil.getHourOfDay() mustEqual 2
    response.cachedUntil.getMinuteOfHour() mustEqual 55
    response.cachedUntil.getSecondOfMinute() mustEqual 13
    response.cachedUntil.getZone mustEqual DateTimeZone.UTC

    response.assets.length mustEqual 2

    val sortedAssets = response.assets.sortBy { _.eveItemID }

    val asset1 :: asset2 :: tail = sortedAssets

    asset1.eveItemID mustEqual 100504209L
    asset1.locationID mustEqual 60001846L
    asset1.typeID mustEqual 2078L
    asset1.quantity mustEqual 1

    asset2.eveItemID mustEqual 593918585L
    asset2.locationID mustEqual 60001846L
    asset2.typeID mustEqual 586L
    asset2.quantity mustEqual 1

    val asset1Name = LightweightItem.getByID(asset1.typeID).map { _.name }
    asset1Name.isDefined must beTrue
    asset1Name.getOrElse("fail") mustEqual "Zephyr"

    val asset2Name = LightweightItem.getByID(asset2.typeID).map { _.name }
    asset2Name.isDefined must beTrue
    asset2Name.getOrElse("fail") mustEqual "Probe"

    val stationO = Station.getByID(asset1.locationID)

    stationO.isDefined must beTrue
    val station = stationO.get
    station.stationName mustEqual "Trer VIII - Moon 7 - Nugoeihuvi Corporation Development Studio"

    asset1.contents.length mustEqual 0
    val a2contents = asset2.contents
    a2contents.length mustEqual 2

    val sortedContents = a2contents.sortBy { _.eveItemID }
    val content1 :: content2 :: contentTail = sortedContents

    content1.quantity mustEqual 1
    content2.quantity mustEqual 7
 
    val c1Name = LightweightItem.getByID( content1.typeID ).map { _.name }

    val c2Name = LightweightItem.getByID(content2.typeID).map { _.name }

    c1Name mustEqual Some("Expanded Probe Launcher I")
    c2Name mustEqual Some("Combat Scanner Probe I")
  }
}