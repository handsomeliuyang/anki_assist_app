package com.ly.anki_assist_app.ui.print.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.ui.home.Overview
import com.ly.anki_assist_app.utils.Resource
import timber.log.Timber

class PrintOptionsViewModel : ViewModel() {

    val items: LiveData<Resource<List<DeckParent>>> = liveData {
        emit(Resource.loading("加载中...", null))
        try {
            kotlinx.coroutines.delay(3000)
            val dueDeckList = DeckApi.asynGetDueDeckList()

            val deckParentList = mutableListOf<DeckParent>()
            val parentMap = mutableMapOf<String, DeckParent>()

            // 先创建一级目录
            dueDeckList
                .filter { !it.isSubDeck }
                .map {
                    val deckParent = DeckParent(it, true, mutableListOf<DeckChild>())
                    deckParentList.add(deckParent)
                    parentMap.put(it.name, deckParent)
                }

            // 归类二级目录
            dueDeckList
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