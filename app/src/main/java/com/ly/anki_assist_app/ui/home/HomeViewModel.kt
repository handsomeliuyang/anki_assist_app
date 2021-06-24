package com.ly.anki_assist_app.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    fun loadDecks() {
        viewModelScope.launch {
            // 加载所有到期的类别
            val dueDeckList = DeckApi.asynGetDueDeckList()
            Log.d("liuyang", "all due deck list: ${dueDeckList}")

            // 获取每个Deck下的到期的Card
            for (deck in dueDeckList) {
                val dueCards = CardApi.asynGetDueCards(deck.deckId, 20)

                Log.d("liuyang", "${deck}=${dueCards}")
            }
        }
    }
}