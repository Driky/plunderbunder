package auth

import anorm._
import play.api.db.DB

import play.api.Play.current

import play.api.libs.json._

case class UserProfile(
  id: Long,
  eveID: Long,
  characterName: String,
  apiKey: Option[Long],
  apiVCode: Option[String],
  accessMask: Option[Long],
  email: Option[String]) {

  def updateApiKey(apiKey: Long) {
    DB.withConnection { implicit c =>
      val sql = SQL("""UPDATE kartel_users SET api_key_id={api_key} where id={user_id}""")
      sql.on('api_key -> apiKey, 'user_id -> id).executeUpdate()
    }
  }

  def updateApiVCode(apiVCode: String) {
    DB.withConnection { implicit c =>
      val sql = SQL("""UPDATE kartel_users SET api_key_vcode={vcode} where id={user_id}""")
      sql.on('vcode -> apiVCode, 'user_id -> id).executeUpdate()
    }
  }

  def updateEmailAddress(emailAddress: String) {
    DB.withConnection { implicit c =>
      val sql = SQL("""UPDATE kartel_users SET email_address={email_address} where id={user_id}""")
      sql.on('email_address -> emailAddress, 'user_id -> id).executeUpdate()
    }
  }
  
  def updateAccessMask(accessMask: Option[Long]) {
    DB.withConnection { implicit c =>
      val sql = SQL("""UPDATE kartel_users SET access_mask={accessMask} where id={user_id}""")
      sql.on('accessMask -> accessMask, 'user_id -> id).executeUpdate()
    }
  }
}

object UserProfile {
  implicit val format = Json.format[UserProfile]
  
  def getWithID(userID: Long) = {
    DB.withConnection { implicit c =>
      val sql = SQL("""SELECT id, eve_id, character_name, api_key_id, api_key_vcode, access_mask, email_address  
        FROM kartel_users
        WHERE id={user_id}""").on('user_id -> userID)

      sql().map { row =>
        {
          UserProfile(
            row[Long]("id"),
            row[Long]("eve_id"),
            row[String]("character_name"),
            row[Option[Long]]("api_key_id"),
            row[Option[String]]("api_key_vcode"),
            row[Option[Long]]("access_mask"),
            row[Option[String]]("email_address"))
        }
      }.headOption
    }
  }
}