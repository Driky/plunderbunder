package com.eveonline.sde

import anorm.SQL
import play.api.db.DB
import play.api.Logger

import play.api.Play.current

trait BaseDataset {
  // Abstract defined by actual dataset
  def dataSetName: String

  // There's a way to do this, I'm just not sure how
  //  def create[T <: BaseDataset](value: T)

  def deleteAll: Boolean = {
    this.deleteFromMaintenance()
    this.deleteDataset()
  }

  def deleteFromMaintenance(): Boolean = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"DELETE FROM sde_maintenance WHERE data_set='${dataSetName}';")
      sql.execute()
    }
  }

  def deleteDataset(): Boolean = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"DELETE FROM ${dataSetName};")
      sql.execute()
    }
  }

  def updateModificationTime: Option[Long] = {
    // Probably a duplicate call, but safe than
    deleteFromMaintenance
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO sde_maintenance
        (data_set, last_import) VALUES (
         {dataSetName}, NOW()
        );""").on('dataSetName -> dataSetName)
      sql.executeInsert()
    }
  }
}
