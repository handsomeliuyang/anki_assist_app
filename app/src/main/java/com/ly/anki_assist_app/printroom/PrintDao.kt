package com.ly.anki_assist_app.printroom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PrintDao {

    @Query("SELECT * FROM print_table")
    fun getAll(): List<PrintEntity>

    @Query("SELECT * FROM print_table WHERE id=:printId")
    fun getPrintById(printId: Int): PrintEntity

    @Insert
    fun insertAll(vararg printEntitys: PrintEntity)

//
//    @Delete
//    fun delete(printEntity: PrintEntity)

}