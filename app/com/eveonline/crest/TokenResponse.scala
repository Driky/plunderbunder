package com.eveonline.crest

import play.api.libs.json.{ Reads, __ }
import play.api.libs.functional.syntax._ // scalastyle:ignore

case class TokenResponse(refreshToken: Option[String], tokenType: String, accessToken: String, expiresIn: Long)

object TokenResponseSerializer {
  implicit val tokenResponseReads: Reads[TokenResponse] = (
    (__ \ "refresh_token").read[Option[String]]
    and (__ \ "token_type").read[String]
    and (__ \ "access_token").read[String]
    and (__ \ "expires_in").read[Long])(TokenResponse)
}