package com.eveonline.crest

import play.api.libs.ws._

case class FakeResponse(fakeBody: String) extends WSResponse {
  def body = fakeBody
  def allHeaders = ???
  def cookie(name: String) = ???
  def statusText = ???
  def status = 200
  def underlying[T] = ???
  def xml = ???
  def header(key: String) = ???
  def json = ???
  def cookies = ???
}