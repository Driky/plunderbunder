package controllers

import auth.AuthenticatedAction
import play.api._
import play.api.mvc._

import play.api.libs.json._

import scala.concurrent.Future

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

import auth.UserProfile

import com.eveonline.xmlapi.requests.CharacterAssets
import com.eveonline.sde.Station
import com.eveonline.sde.InventoryType

object User extends Controller with JsonController {

  def user = AuthenticatedAction { authedRequest =>
    Ok(Json.toJson(authedRequest.authenticationProfile))
  }

  def userProfile = AuthenticatedAction { authedRequest =>
    val userID = authedRequest.authenticationProfile.userID
    val userProfileO = UserProfile.getWithID(userID)

    userProfileO.fold({
      BadJsonRequest("Invalid User")
    })({ profile =>
      Ok(Json.toJson(profile))
    })
  }

  case class UpdateUserRequest(apiKey: Option[Long], apiVCode: Option[String], emailAddress: Option[String])
  object UpdateUserRequest { implicit val format = Json.format[UpdateUserRequest] }

  def updateUserProfile = AuthenticatedAction.async(parse.json) { authedRequest =>
    val body = authedRequest.body

    Logger.info("RBody: " + body.toString())

    body.validate[UpdateUserRequest] match {
      case JsSuccess(uur, _) => {
        val userID = authedRequest.authenticationProfile.userID
        val userProfileO = UserProfile.getWithID(userID)

        userProfileO.fold(
          {
            BadJsonRequestFuture("Invalid User")
          })(profile => {
            if (uur.apiKey.isDefined) {
              profile.updateApiKey(uur.apiKey.get)
            }

            if (uur.apiVCode.isDefined) {
              profile.updateApiVCode(uur.apiVCode.get)
            }

            if (uur.emailAddress.isDefined) {
              profile.updateEmailAddress(uur.emailAddress.get)
            }

            if (uur.apiKey.isDefined || uur.apiVCode.isDefined) {
              import com.eveonline.xmlapi.requests._
              val updatedProfile = UserProfile.getWithID(userID).get
              val apiKeyResponse = ApiKeyInfo.verifyKey(updatedProfile)

              apiKeyResponse.map { resp =>
                {
                  val accessMask = resp.accessMask
                  updatedProfile.updateAccessMask(Option(accessMask))

                  val resultFields = JsObject(
                    "keyIsValid" -> JsBoolean(true)
                      :: "accessMask" -> JsNumber(accessMask)
                      :: Nil)

                  OkJson(resultFields)
                }
              }.recoverWith({
                case e: Exception => {
                  val resultFields = JsObject(Seq("keyIsValid" -> JsBoolean(false)))
                  OkJsonFuture(resultFields)
                }
              })

            } else {
              OkJsonFuture()
            }

          })
      }
      case JsError(e) => BadJsonRequestFuture("Invalid JSON")
    }
  }

  import com.eveonline.xmlapi.Asset

  case class VerboseAsset(
    locationID: Int,
    eveItemID: Long,
    typeID: Long,
    quantity: Int,
    contents: List[VerboseAsset],
    locationName: Option[String],
    assetName: Option[String]) {}
  object VerboseAsset {
    implicit val format = Json.format[VerboseAsset]

    def fromAsset(asset: Asset, itemTypes: Map[Long, InventoryType], locations: Map[Int, Station]): VerboseAsset = {
      val contents = asset.contents.map { VerboseAsset.fromAsset(_, itemTypes, locations) }
      VerboseAsset(
        asset.locationID,
        asset.eveItemID,
        asset.typeID,
        asset.quantity,
        contents,
        locations.get(asset.locationID).map { _.stationName },
        itemTypes.get(asset.typeID).map { _.name })
    }
  }

  def getUserAssets = AuthenticatedAction.async { authedRequest =>

    val userID = authedRequest.authenticationProfile.userID

    val profileO = UserProfile.getWithID(userID)

    profileO.fold({
      BadJsonRequestFuture("Profile missing")
    })({ profile =>
      val assets = CharacterAssets.listForCharacter(profile)

      assets.map { a =>
        {
          val locationIDs = a.map(_.locationID).distinct
          val typeIDs = a.map(_.typeID).distinct

          val locationMap = Station.mapForIDs(locationIDs)
          val typeMap = InventoryType.mapForIDs(typeIDs)

          val vAssets = a.map { VerboseAsset.fromAsset(_, typeMap, locationMap) }

          val results = JsObject("assets" -> Json.toJson(vAssets) :: Nil)
          OkJson(results)
        }
      }

    })

  }
}
