package controllers

import play.api.Logger
import play.api.mvc.{ Controller, Action, AnyContent, Result }

import play.api.libs.json.{ Json, JsBoolean, JsError, JsNumber, JsObject, JsValue, JsSuccess }

import scala.concurrent.Future

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

import auth.{ UserProfile, AuthenticatedAction, AuthenticatedRequest }

import com.eveonline.xmlapi.Asset
import com.eveonline.xmlapi.requests.{ ApiKeyInfo, CharacterAssets, MarketOrders }
import com.eveonline.sde.{ Station, InventoryType }

import utility.ExceptionLogger

object User extends Controller with JsonController {

  def user: Action[AnyContent] = AuthenticatedAction { authedRequest =>
    Ok(Json.toJson(authedRequest.authenticationProfile))
  }

  def userProfile: Action[AnyContent] = AuthenticatedAction { authedRequest =>
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

  def updateUserProfile: Action[JsValue] = AuthenticatedAction.async(parse.json) { authedRequest =>
    val body = authedRequest.body

    Logger.info("RBody: " + body.toString())

    body.validate[UpdateUserRequest] match {
      case JsSuccess(uur, _) => {
        val userID = authedRequest.authenticationProfile.userID
        val userProfileO = UserProfile.getWithID(userID)

        userProfileO.fold(BadJsonRequestFuture("Invalid User"))(profile => {

          uur.apiKey.foreach { profile.updateApiKey(_) }
          uur.apiVCode.foreach { profile.updateApiVCode(_) }
          uur.emailAddress.foreach { profile.updateEmailAddress(_) }

          if (uur.apiKey.isDefined || uur.apiVCode.isDefined) {
            val updatedProfile = UserProfile.getWithID(userID).get
            val apiKeyResponse = ApiKeyInfo.verifyKey(updatedProfile)

            apiKeyResponse.map { resp =>
              {
                val matchingCharacter = resp.characters.find { _.characterName.equals(updatedProfile.characterName) }
                updateAPIKeyAttributes(updatedProfile, resp.accessMask, matchingCharacter)

                val resultFields = JsObject(
                  "keyIsValid" -> JsBoolean(true) :: "accessMask" -> JsNumber(resp.accessMask) :: Nil)

                OkJson(resultFields)
              }
            }.recoverWith({
              case e: Exception => {
                Logger.debug(ExceptionLogger(e).printableStackTrace)
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

  private def updateAPIKeyAttributes(
    profile: UserProfile,
    accessMask: Long,
    character: Option[com.eveonline.xmlapi.Character]) {

    profile.updateAccessMask(Option(accessMask))

    val updatedCharacterID = character.map { _.characterID }
    profile.updateCharacterID(updatedCharacterID)
  }

  case class VerboseAsset(
    locationID: Int,
    eveItemID: Long,
    typeID: Long,
    quantity: Int,
    contents: List[VerboseAsset],
    locationName: Option[String],
    assetName: Option[String],
    usedInManufacturing: Boolean)

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
        Option(itemTypes.get(asset.typeID).fold(s"Unknown Item #${asset.typeID}")(_.name)),
        asset.manufacturingComponent)
    }
  }

  def getUserAssets: Action[AnyContent] = AuthenticatedAction.async { authedRequest =>
    operateOnProfile(authedRequest) { profile =>
      val assets = CharacterAssets.listForCharacter(profile)

      assets.map { a =>
        {
          val locationIDs = a.map(_.locationID).distinct
          val typeIDs = a.map(asset => asset.typeID :: asset.contents.map { _.typeID }).flatten.distinct

          val locationMap = Station.mapForIDs(locationIDs)
          val typeMap = InventoryType.mapForIDs(typeIDs)

          val vAssets = a.map { VerboseAsset.fromAsset(_, typeMap, locationMap) }

          val results = JsObject("assets" -> Json.toJson(vAssets) :: Nil)
          OkJson(results)
        }
      }
    }
  }

  def operateOnProfile(request: AuthenticatedRequest[AnyContent])(block: UserProfile => Future[Result]): Future[Result] = {
    val userID = request.authenticationProfile.userID
    val profileO = UserProfile.getWithID(userID)
    profileO.fold({
      BadJsonRequestFuture("Profile Missing")
    })(p => block(p))
  }

  def getMarketOrders: Action[AnyContent] = AuthenticatedAction.async { implicit authedRequest =>
    operateOnProfile(authedRequest) { profile =>
      val orders = MarketOrders.listForCharacter(profile).map { _.filter { _.orderState == 0 } }

      orders.map { o =>

        val stationIDs = o.map { _.stationID }.distinct
        val stationNames = Station.mapForIDs(stationIDs)

        val itemIDs = o.map { _.typeID }.distinct
        val itemNames = InventoryType.mapForIDs(itemIDs)

        val namedOrders = o.map { order =>
          {
            order.copy(
              stationName = stationNames.get(order.stationID).map(_.stationName),
              typeName = itemNames.get(order.typeID).map { _.name })
          }
        }

        val (buyOrders, sellOrders) = namedOrders.partition(_.isBuyOrder)

        val results = JsObject(Seq("buyOrders" -> Json.toJson(buyOrders),
          "sellOrders" -> Json.toJson(sellOrders)))
        OkJson(results)
      }

    }
  }
}
