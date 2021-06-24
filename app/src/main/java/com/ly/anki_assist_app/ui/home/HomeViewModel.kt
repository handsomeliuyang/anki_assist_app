package com.ly.anki_assist_app.ui.home

import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class CheckStatus {
    CHECKING,
    ANKI_UNSTALL,
    NOT_PERMISSION,
    SUCCESS
}

class HomeViewModel : ViewModel() {

    private val _checkStatus = MutableLiveData<CheckStatus>().apply {
        value = CheckStatus.CHECKING
    }
    val checkStatus: LiveData<CheckStatus> = _checkStatus

    fun updateCheckStatus(status: CheckStatus){
        _checkStatus.value = status
    }

    val dueOverview = _checkStatus.switchMap {
        liveData {
            if(it == CheckStatus.SUCCESS) {
                val result = try {
                    val dueDeckList = DeckApi.asynGetDueDeckList()
                    var reviewNumbs = 0
                    var newNumbs = 0
                    for (dueDeck in dueDeckList) {
                        reviewNumbs += dueDeck.deckDueCounts.learnCount + dueDeck.deckDueCounts.reviewCount
                        newNumbs += dueDeck.deckDueCounts.newCount
                    }
                    Resource.success(Overview(dueDeckList, reviewNumbs, newNumbs))
                } catch (e: Exception) {
                    Resource.error("加载出错", null)
                }
                emit(result)
            } else {
                emit(Resource.error("check 失败", null))
            }
        }
    }
}

data class Overview(
    val dueDeckList: List<AnkiDeck>,
    val reviewNums: Int,
    val newNums: Int,
)