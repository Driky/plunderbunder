import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import play.api.Logger

import play.api.libs.json._
import com.eveonline.crest.VerifyResponse
import com.eveonline.crest.VerifyResponseSerializer._
import org.joda.time.DateTimeZone

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class CrestSsoSpec extends Specification {

  "CREST SSO" should {

    "validate a correct verify response" in running(
      FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        testCorrectVerifyResponse
      }

    def testCorrectVerifyResponse = {
      val rawJson = """
        {
          "CharacterID":42,
          "CharacterName":"Ford Prefect",
          "ExpiresOn":"2015-01-15T17:59:23.0086947Z",
          "Scopes":"publicData",
          "TokenType":"Character",
          "CharacterOwnerHash":"0000AAAA1111BBBB2222CCCC333="
        }
        """

      val vrfyJson = Json.parse(rawJson)

      vrfyJson.validate[VerifyResponse] match {
        case JsSuccess(vr, _) => {
          vr.characterID mustEqual 42
          vr.characterName mustEqual "Ford Prefect"

          val expiration = vr.expiresOn

          Logger.info(expiration.toString())

          expiration.getZone mustEqual DateTimeZone.UTC
          expiration.getYear mustEqual 2015
          expiration.getMonthOfYear mustEqual 1
          expiration.getDayOfMonth mustEqual 15
          expiration.getHourOfDay mustEqual 17
          expiration.getMinuteOfHour mustEqual 59
          expiration.getSecondOfMinute mustEqual 23

          vr.scopes mustEqual "publicData"
          vr.tokenType mustEqual "Character"
          vr.characterOwnerHash mustEqual "0000AAAA1111BBBB2222CCCC333="
        }
        case JsError(e) => ko(e.toString())
      }
    }
  }
}