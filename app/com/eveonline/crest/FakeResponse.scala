package com.eveonline.crest

import play.api.libs.ws.{ WSResponse, WSCookie }
import scala.xml.Elem
import play.api.libs.json.JsValue

case class FakeResponse(fakeBody: String) extends WSResponse {
  def body: String = fakeBody
  def allHeaders: Map[String, Seq[String]] = ???
  def cookie(name: String): Option[WSCookie] = ???
  def statusText: String = ???
  def status: Int = 200 // scalastyle:ignore
  def underlying[T]: T = ???
  def xml: Elem = ???
  def header(key: String): Option[String] = ???
  def json: JsValue = ???
  def cookies: Seq[WSCookie] = ???
}
