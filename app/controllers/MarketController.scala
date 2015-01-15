package controllers

import play.api._
import play.api.mvc._

import play.api.Play.current

import anorm._
import play.api.db.DB

import play.api.Play.current

import play.api.libs.json._

import com.eveonline.crest.RegionalMarketOrders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.eveonline.crest.MarketOrder
import com.eveonline.crest.MarketOrderLocation

object MarketController extends Controller {

  def reducedCnapOrders(orders: Future[List[MarketOrder]], reduction: (MarketOrder, MarketOrder) => MarketOrder) = {
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
    buyLocation: MarketOrderLocation,
    buyPrice: Long,
    sellLocation: MarketOrderLocation,
    sellPrice: Long)
  object MarketRates {
    implicit val marketRatesFormat = Json.format[MarketRates]
  }

  def jitaPriceForItem(itemID: Long) = AuthenticatedAction.async { implicit request =>

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