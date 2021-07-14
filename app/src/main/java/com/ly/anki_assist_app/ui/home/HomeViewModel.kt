package com.ly.anki_assist_app.ui.home

import android.text.Html
import android.text.Spanned
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

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
                    // 查询所需要复习的卡片
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
                val result = try {
                    // 查询今日的打印记录
                    val list = PrintUtils.asynPrintsByDate(Date())
                    val result: List<PrintItem> = list.map {
                        PrintItem(it)
                    }
                    Resource.success(result)
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
    fun getOverviewText(): Spanned {
        return Html.fromHtml("今日需复习 <font color='#FF0000'>${reviewNums}</font> 张，需学习新卡片 <font color='#FF0000'>${newNums}</font> 张")
    }
}

data class PrintItem(
    val printEntity: PrintEntity
) {
    fun showDate(): String{
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(printEntity.time)
    }
    fun getStateText(): String{
        return when(printEntity.state) {
            PrintEntity.STATE_NONE_CHECK -> "未检查"
            PrintEntity.STATE_NONE_COACH -> "未辅导"
            PrintEntity.STATE_FINISHED -> "完成"
            else -> "异常"
        }
    }
    fun getPrintInfo(): String {
        val about = printEntity.deckEntitys.map {
            "${it.name}(${it.total}张)"
        }
        return about.joinToString(" ")
    }
}