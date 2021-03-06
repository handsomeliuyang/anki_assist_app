package com.ly.anki_assist_app.printroom

import androidx.room.*
import java.util.*

@Dao
interface PrintDao {
    @Query("SELECT * FROM print_table ORDER BY time DESC")
    fun getPrints(): List<PrintEntity>

    @Query("SELECT * FROM print_table WHERE time BETWEEN :start AND :end ORDER BY time DESC")
    fun getPrintsByDate(start: Long, end: Long): List<PrintEntity>

    @Query("SELECT * FROM print_table WHERE id=:printId")
    fun getPrintById(printId: Int): PrintEntity

    @Insert
    fun insertAll(vararg printEntitys: PrintEntity)

    @Update
    fun update(printEntity: PrintEntity)

    @Delete
    fun delete(printEntity: PrintEntity)

    @Query("DELETE FROM print_table WHERE time < :date")
    fun deleteBeforeDate(date: Long)

}