package com.ly.anki_assist_app.printroom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class PrintUtils {

    companion object {
        suspend fun asynSavePrint(vararg printEntitys: PrintEntity) {
            withContext(Dispatchers.IO) {
                savePrint(*printEntitys)
            }
        }

        private fun savePrint(vararg printEntitys: PrintEntity){
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            printDao.insertAll(*printEntitys)
        }

        suspend fun asynPrintsByDate(date: Date): List<PrintEntity> {
            return withContext(Dispatchers.IO) {
                return@withContext getPrintsByDate(date)
            }
        }

        private fun getPrintsByDate(date: Date): List<PrintEntity>{
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar[Calendar.MILLISECOND] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.HOUR_OF_DAY] = 0
            val start = calendar.time.time.toLong()

            calendar[Calendar.MILLISECOND] = 0
            calendar[Calendar.SECOND] = 59
            calendar[Calendar.MINUTE] = 59
            calendar[Calendar.HOUR_OF_DAY] = 23
            val end = calendar.time.time.toLong()

            Timber.d("end %s", Date(end))

            return printDao.getPrintsByDate(start, end)
        }

        suspend fun asynGetPrintById(printId: Int): PrintEntity {
            return withContext(Dispatchers.IO) {
                return@withContext getPrintById(printId)
            }
        }

        private fun getPrintById(printId: Int): PrintEntity {
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            return printDao.getPrintById(printId)
        }
    }

}