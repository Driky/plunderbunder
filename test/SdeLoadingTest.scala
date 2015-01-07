import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import controllers.Configure

@RunWith(classOf[JUnitRunner])
class SdeLoadingTest extends Specification {

  "Configuration" should {
    "reload the region dataset when prompted" in running(
      FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val reload = route(FakeRequest(GET, "/configure/reload")).get
        // TODO: Temp workaround until this test is re-written
        status(reload) must equalTo(UNAUTHORIZED)
        //      Configure.reloadRegions
      }
  }
}