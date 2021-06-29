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
            val childMap = mutableMapOf<String, MutableList<AnkiDeck>>()

            for (dueDeck in dueDeckList) {
                val dirs = dueDeck.deckName.split("::")
                if(dirs.size == 1) {
                    val deckParent = DeckParent(dueDeck, mutableListOf<AnkiDeck>())
                    deckParentList.add(deckParent)
                    parentMap.put(dirs[0], deckParent)
                } else {
                    val list = childMap.get(dirs[0]) ?: mutableListOf<AnkiDeck>()
                    list.add(dueDeck)
                    childMap.put(dirs[0], list)
                }
            }

            for ((key, value) in childMap.entries) {
                val deckParent = parentMap.get(key) ?: continue
                deckParent.children.addAll(value)
            }

            emit(Resource.success(deckParentList))
        } catch (e: Exception) {
            emit(Resource.error("加载出错", null))
        }
    }

}