package com.eveonline.crest

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class VerifyResponse(expiresOn: String, characterName: String, scopes: String, characterOwnerHash: String, characterID: Long, tokenType: String) {

}

object VerifyResponseSerializer {
  implicit val reads: Reads[VerifyResponse] = (
    (__ \ "ExpiresOn").read[String]
    and (__ \ "CharacterName").read[String]
    and (__ \ "Scopes").read[String]
    and (__ \ "CharacterOwnerHash").read[String]
    and (__ \ "CharacterID").read[Long]
    and (__ \ "TokenType").read[String])(VerifyResponse)
}