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

            val parentMap = mutableMapOf<String, DeckParent>()

            // 筛选出一级类别
            notPrintDueDecks
                .filter { it.category.isEmpty() }
                .map {
                    val deckParent = DeckParent(it, false, mutableListOf<DeckChild>())
                    parentMap.put(it.name, deckParent)
                }

            // 没有一级的创建一级类别
            notPrintDueDecks
                .filter { it.category.isNotEmpty() }
                .filter { !parentMap.containsKey(it.category) }
                .map {
                    val deckParent = DeckParent(AnkiDeck.fromName(it.name), false, mutableListOf<DeckChild>())
                    parentMap.put(it.category, deckParent)
                }

            // 分配子View
            notPrintDueDecks
                .filter { it.category.isNotEmpty() }
                .map {
                    val deckParent = parentMap.get(it.category)
                    deckParent?.children?.add(DeckChild(it, false))
                }
            emit(Resource.success(parentMap.values.toList()))
        } catch (e: Exception) {
            emit(Resource.error("加载出错", null))
        }
    }

}