package controllers

import anorm.SQL

import play.api.Play.current
import play.api.db.DB
import play.api.Play.current
import play.api.mvc.{ Action, Controller, AnyContent }
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.eveonline.crest.{ MarketOrder, NamedReference }
import com.eveonline.crest.requests.RegionalMarketOrders

import auth.AuthenticatedAction

object MarketController extends Controller with JsonController {

  def reducedCnapOrders(
    orders: Future[List[MarketOrder]],
    reduction: (MarketOrder, MarketOrder) => MarketOrder): Future[MarketOrder] = {

    orders.flatMap { os =>
      {
        val cnapOrders = os.filter { o => o.location.href.endsWith("/60003760/") }
        val result = cnapOrders.reduce(reduction)
        Future(result)
      }
    }
  }

  case class MarketRates(
    itemID: Long,
    buyLocation: NamedReference,
    buyPrice: Long,
    sellLocation: NamedReference,
    sellPrice: Long)

  object MarketRates {
    implicit val marketRatesFormat = Json.format[MarketRates]
  }

  def jitaPriceForItem(itemID: Long): Action[AnyContent] = AuthenticatedAction.async { implicit request =>

    val theForgeID = 10000002

    val orderSells = RegionalMarketOrders.regionalSellOrders(theForgeID, itemID, request.authenticationProfile.accessToken)
    val orderBuys = RegionalMarketOrders.regionalBuyOrders(theForgeID, itemID, request.authenticationProfile.accessToken)

    val cnapMinSell = reducedCnapOrders(orderSells, (a, b) => if (a.price < b.price) a else b)
    val cnapMaxBuy = reducedCnapOrders(orderBuys, (a, b) => if (a.price > b.price) a else b)

    for {
      maxResult <- cnapMinSell
      minResult <- cnapMaxBuy

      result = MarketRates(itemID, minResult.location, minResult.price, maxResult.location, maxResult.price)
      response = Ok(Json.toJson(result))
    } yield response
  }
}
