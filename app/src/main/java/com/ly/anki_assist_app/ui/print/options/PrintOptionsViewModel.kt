package com.ly.anki_assist_app.ui.print.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.ui.home.Overview
import com.ly.anki_assist_app.utils.Resource

class PrintOptionsViewModel : ViewModel() {

    val items: LiveData<Resource<List<AnkiDeck>>> = liveData {
        emit(Resource.loading("加载中...", null))
        try {

            kotlinx.coroutines.delay(3000)

            val dueDeckList = DeckApi.asynGetDueDeckList()
            // TODO-ly 补充主目录和子目录的数据结构
            emit(Resource.success(dueDeckList))
        } catch (e: Exception) {
            emit(Resource.error("加载出错", null))
        }
    }

}