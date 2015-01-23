package com.eveonline.xmlapi.requests

import auth.UserProfile

import com.eveonline.xmlapi.AssetResponse

import scala.xml.XML

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CharacterAssets extends XmlApiRequest {

  def listForCharacter(profile: UserProfile) = {
    val keyID = profile.apiKey
    val vCode = profile.apiVCode
    val assetUrl = "https://api.eveonline.com/char/AssetList.xml.aspx"

    val response = get(assetUrl, keyID, vCode)

    response.map { r =>
      {
        val assetResponse = AssetResponse.fromXml(XML.loadString(r.body))
        
        assetResponse.assets
      }
    }

  }
}