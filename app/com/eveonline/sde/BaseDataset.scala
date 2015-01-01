package com.eveonline.sde

import anorm._
import play.api.db.DB
import play.api.Logger

import play.api.Play.current

trait BaseDataset {
  // Abstract defined by actual dataset
  def dataSetName: String

  // There's a way to do this, I'm just not sure how
//  def create[T <: BaseDataset](value: T)

  def deleteAll = {
    this.deleteFromMaintenance()
    this.deleteDataset()
  }

  def deleteFromMaintenance() = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"DELETE FROM sde_maintenance WHERE data_set='${dataSetName}';")
      sql.execute()
    }
  }

  def deleteDataset() = {
    DB.withConnection { implicit c =>
      val sql = SQL(s"DELETE FROM ${dataSetName};")
      sql.execute()
    }
  }

  def updateModificationTime = {
    // Probably a duplicate call, but safe than 
    deleteFromMaintenance
    DB.withConnection { implicit c =>
      val sql = SQL(s"""INSERT INTO sde_maintenance 
        (data_set, last_import) VALUES (
         '${dataSetName}', NOW() 
        );""")
      sql.executeInsert()
    }
  }
}