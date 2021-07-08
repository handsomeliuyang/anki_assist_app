package com.ly.anki_assist_app.printroom

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ly.anki_assist_app.App

@Database(entities = arrayOf(PrintEntity::class), version = 1)
@TypeConverters(Converters::class)
abstract class PrintRoomDatabase : RoomDatabase() {

    abstract fun printDao(): PrintDao

//    abstract fun printCardDao(): PrintCardDao

    companion object {

        @Volatile
        private var INSTANCE: PrintRoomDatabase? = null

        fun getDatabase(): PrintRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    App.context,
                    PrintRoomDatabase::class.java,
                    "print_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }

}