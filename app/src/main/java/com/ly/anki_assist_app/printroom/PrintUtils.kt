package com.ly.anki_assist_app.printroom

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

        suspend fun asynGetAllPrint(): List<PrintEntity> {
            return withContext(Dispatchers.IO) {
                return@withContext getAllPrint()
            }
        }

        private fun getAllPrint(): List<PrintEntity>{
            val database = PrintRoomDatabase.getDatabase()
            val printDao = database.printDao()

            return printDao.getAll()
        }
    }

}