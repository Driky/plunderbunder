package com.eveonline.crest

import play.api.libs.json.Json

case class NamedReference(href: String, name: String)

object NamedReference {
  implicit val marketOrderLocationFormat = Json.format[NamedReference]
}
