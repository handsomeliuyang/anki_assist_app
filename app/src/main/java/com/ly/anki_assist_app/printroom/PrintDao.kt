package com.ly.anki_assist_app.printroom

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface PrintDao {

//    @Query("SELECT * FROM print_table")
//    fun getPrintByDate(): List<PrintEntity>

    @Insert
    fun insertAll(vararg printEntitys: PrintEntity)
//
//    @Delete
//    fun delete(printEntity: PrintEntity)

}