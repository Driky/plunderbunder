import play.api.GlobalSettings
import play.api.Application
import play.api.Logger

import com.eveonline.crest.requests.CrestRequest

object PlunderbunderGlobal extends GlobalSettings {

  override def onStart(app: Application) {
    // There is probably a cleaner way to initialize this
    CrestRequest.initialize
  }
}