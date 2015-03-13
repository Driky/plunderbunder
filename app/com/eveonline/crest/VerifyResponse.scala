package com.eveonline.crest

import play.api.libs.json.{ Reads, __ }
import play.api.libs.functional.syntax._ // scalastyle:ignore
import org.joda.time.{ DateTime, DateTimeZone }

case class VerifyResponse(
  expiresOn: DateTime,
  characterName: String,
  scopes: String,
  characterOwnerHash: String,
  characterID: Long,
  tokenType: String)

object VerifyResponseSerializer {

  implicit val jdr = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'Z", s => s"${s}+0000")

  implicit val reads: Reads[VerifyResponse] = (
    (__ \ "ExpiresOn").read[DateTime].map { _.toDateTime(DateTimeZone.UTC) }
    and (__ \ "CharacterName").read[String]
    and (__ \ "Scopes").read[String]
    and (__ \ "CharacterOwnerHash").read[String]
    and (__ \ "CharacterID").read[Long]
    and (__ \ "TokenType").read[String])(VerifyResponse)
}
