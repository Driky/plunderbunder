package com.eveonline.xmlapi

case class Character(
  characterID: Long,
  characterName: String,
  corporationID: Long,
  corporationName: String,
  allianceID: Long,
  allianceName: String,
  factionID: Int,
  factionName: String) {}

object Character {
  def fromXml(node: scala.xml.Node): Character = {

    val characterID = (node \@ "characterID").toLong
    val characterName = (node \@ "characterName")
    val corpID = (node \@ "corporationID").toLong
    val corpName = (node \@ "corporationName")
    val allianceID = (node \@ "allianceID").toLong
    val allianceName = (node \@ "allianceName")
    val factionID = (node \@ "factionID").toInt
    val factionName = (node \@ "factionName")

    Character(characterID, characterName, corpID, corpName, allianceID, allianceName, factionID, factionName)
  }
}