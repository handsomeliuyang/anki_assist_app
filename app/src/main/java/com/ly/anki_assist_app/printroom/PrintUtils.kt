package com.ly.anki_assist_app.printroom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class PrintUtils {

    companion object {

        suspend fun asynUpdate(printEntity: PrintEntity){
            withContext(Dispatchers.IO) {
                update(printEntity)
            }
        }

        private fun update(printEntity: PrintEntity){
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            printDao.update(printEntity)
        }

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

        suspend fun asynGetPrints(): List<PrintEntity> {
            return withContext(Dispatchers.IO) {
                return@withContext getPrints()
            }
        }

        private fun getPrints(): List<PrintEntity> {
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            return printDao.getPrints()
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

        suspend fun asynDeletePrint(printEntity: PrintEntity){
            withContext(Dispatchers.IO) {
                deletePrint(printEntity)
            }
        }

        private fun deletePrint(printEntity: PrintEntity) {
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            return printDao.delete(printEntity)
        }

        suspend fun asynClearHistoryBeforeDate(date: Date){
            withContext(Dispatchers.IO) {
                clearHistoryBeforeDate(date)
            }
        }

        private fun clearHistoryBeforeDate(date: Date) {
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar[Calendar.MILLISECOND] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.HOUR_OF_DAY] = 0
            val start = calendar.time.time.toLong()

            return printDao.deleteBeforeDate(start)
        }
    }

}