package com.ly.anki_assist_app.ui.home

import android.text.Html
import android.text.Spanned
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel : ViewModel() {
    private val _checkResult = MutableLiveData<Boolean>(false)
    private val _checkReason = MutableLiveData<String>("检测中...")
    val checkResult: LiveData<Boolean> = _checkResult
    val checkReason: LiveData<String> = _checkReason

    fun updateCheckResult(checkResult: Boolean, checkReason: String){
        _checkResult.value = checkResult
        _checkReason.value = checkReason
    }

    val dueOverview = _checkResult.switchMap {checkResult ->
        liveData {
            if(checkResult) {
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

    val printList = _checkResult.switchMap { checkResult ->
        liveData {
            if(checkResult) {

                Timber.d("liuyang printList begin")

                val result = try {
                    val list = PrintUtils.asynGetAllPrint()
                    Resource.success(list)
                } catch (e: Exception) {
                    Resource.error(e.message ?: "", null)
                }

                Timber.d("liuyang printList success %s", result)

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
) {
    companion object {
        fun empty(): Overview{
            return Overview(emptyList(), 0, 0)
        }
    }
}