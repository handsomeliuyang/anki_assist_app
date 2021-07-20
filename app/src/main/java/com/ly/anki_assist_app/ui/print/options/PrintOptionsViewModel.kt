package com.ly.anki_assist_app.ui.print.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.ui.home.Overview
import com.ly.anki_assist_app.utils.Resource
import timber.log.Timber
import java.util.*

class PrintOptionsViewModel : ViewModel() {

    val items: LiveData<Resource<List<DeckParent>>> = liveData {
        emit(Resource.loading("加载中...", null))
        try {
            // Anki里所有类别
            val dueDecks = DeckApi.asynGetDueDeckList()

            // 本地已打印的类别
            val prints = PrintUtils.asynPrintsByDate(Date())
            val printDeckList = prints.flatMap {
                it.deckEntitys
            }
            val notPrintDueDecks = dueDecks.filter {
                for (printDeck in printDeckList) {
                    if(it.name == printDeck.name) {
                        return@filter false
                    }
                }
                return@filter true
            }

            val deckParentList = mutableListOf<DeckParent>()
            val parentMap = mutableMapOf<String, DeckParent>()

            // 先创建一级目录
            notPrintDueDecks
                .filter { !it.isSubDeck }
                .map {
                    val deckParent = DeckParent(it, true, mutableListOf<DeckChild>())
                    deckParentList.add(deckParent)
                    parentMap.put(it.name, deckParent)
                }

            // 归类二级目录
            notPrintDueDecks
                .filter { it.isSubDeck }
                .map {
                    val deckParent = parentMap.get(it.rootDir)
                    deckParent?.children?.add(DeckChild(it, true))
                }

            emit(Resource.success(deckParentList))
        } catch (e: Exception) {
            emit(Resource.error("加载出错", null))
        }
    }

}