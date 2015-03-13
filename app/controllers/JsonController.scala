package controllers

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.Results.{ BadRequest, Ok }
import play.api.libs.json.{ Json, JsObject, JsString }
import play.api.mvc.Result

trait JsonController {

  def BadJsonRequest(message: String, additional: JsObject = JsObject(Seq.empty)): Result = {
    val baseResult = JsObject(Seq("result" -> JsString("ko"), "message" -> JsString("Invalid Json")))

    val combinedResult = baseResult ++ additional

    BadRequest(combinedResult)
  }

  def BadJsonRequestFuture(message: String, additional: JsObject = JsObject(Seq.empty)): Future[Result] = {
    Future { BadJsonRequest(message, additional) }
  }

  def OkJson(additional: JsObject = JsObject(Seq.empty)): Result = {
    val baseResult = JsObject(Seq("result" -> JsString("ok")))
    val combinedResult = baseResult ++ additional
    Ok(combinedResult)
  }

  def OkJsonFuture(additional: JsObject = JsObject(Seq.empty)): Future[Result] = {
    Future { OkJson(additional) }
  }
}
