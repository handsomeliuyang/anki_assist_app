package com.ly.anki_assist_app.ui.home

import android.text.Html
import android.text.Spanned
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.printroom.DeckEntity
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeViewModel : ViewModel() {
    private val _checkResult = MutableLiveData<Boolean>(false)
    private val _checkReason = MutableLiveData<String>("检测中...")
    val checkResult: LiveData<Boolean> = _checkResult
    val checkReason: LiveData<String> = _checkReason

    fun updateCheckResult(checkResult: Boolean, checkReason: String){
        _checkResult.value = checkResult
        _checkReason.value = checkReason
    }

    private val _dueAnkiDeck = _checkResult.switchMap {checkResult ->
        liveData {
            if(checkResult) {
                val result = try {
                    // 查询所需要复习的卡片
                    val dueDeckList = DeckApi.asynGetDueDeckList()
                    Resource.success(dueDeckList)
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
//                    val list = PrintUtils.asynPrintsByDate(Date())
                    // 查询所有的数据
                    val list = PrintUtils.asynGetPrints()
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

    val overView:LiveData<Overview> = MediatorLiveData<Overview>().apply {
        fun update(){
            val dueDecks = _dueAnkiDeck.value?.data ?: return
            val prints = printList.value?.data ?: emptyList()

            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar[Calendar.MILLISECOND] = 0
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.HOUR_OF_DAY] = 0
            val todayStart = calendar.time.time.toLong()
            calendar[Calendar.MILLISECOND] = 0
            calendar[Calendar.SECOND] = 59
            calendar[Calendar.MINUTE] = 59
            calendar[Calendar.HOUR_OF_DAY] = 23
            val todayEnd = calendar.time.time.toLong()

            val printDeckList = prints
                .filter { // 获取今天内的打印记录
                    val time = it.printEntity.time?.time ?: return@filter false
                    time in todayStart until todayEnd
                }
                .flatMap {
                it.printEntity.deckEntitys
            }

            val notPrintList = dueDecks.filter {
                for (printDeck in printDeckList) {
                    if(it.name == printDeck.name) {
                        return@filter false
                    }
                }
                return@filter true
            }

            value = Overview(notPrintList, printDeckList)
        }
        addSource(_dueAnkiDeck){update()}
        addSource(printList){update()}
    }

    fun deletePrint(printEntity: PrintEntity){
        viewModelScope.launch {
            PrintUtils.asynDeletePrint(printEntity)
            // 目的是为了刷新
            _checkResult.value = _checkResult.value
        }
    }

    private val _messageLiveData = MutableLiveData<String>()
    val messageLiveData: LiveData<String> = _messageLiveData

    fun clearHistory() {
        viewModelScope.launch {
            try {
                PrintUtils.asynClearHistoryBeforeDate(Date())
                _messageLiveData.value = "清除成功"
                // 目的是为了刷新
                _checkResult.value = _checkResult.value
            } catch (e: Exception) {
                _messageLiveData.value = "清除失败"
            }
        }
    }
}

data class Overview(
    val dueDeckList: List<AnkiDeck>,
    val printDeckList: List<DeckEntity>
) {
    companion object {
        fun empty(): Overview{
            return Overview(emptyList(), emptyList())
        }
    }
    fun getDueDeckText(): Spanned {
        if(dueDeckList.isEmpty()) {
            return Html.fromHtml("今日已全部打印完，继续保持，加油！！！")
        }

        val list = dueDeckList.map {
            "${it.name}(${it.deckDueCounts.getTotal()}张)"
        }

        return Html.fromHtml("今日还需打印复习 <font color='#FF0000'>${list.joinToString(" ")}</font>")
    }
    fun getPrintDeckText(): Spanned {
        if(printDeckList.isEmpty()) {
            return Html.fromHtml("今日已打印 <font color='#FF0000'>0</font> 张")
        }

        val list = printDeckList.map {
            "${it.name}(${it.total}张)"
        }
        return Html.fromHtml("今日已打印 ${list.joinToString(" ")}")
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
        if(!printEntity.hasCheckAndSyncAnki) {
            return "未检查"
        }
        if(printEntity.strengthenMemoryCounts <= 0) {
            return "完成"
        }
        if(!printEntity.hasStrengthenMemory) {
            return "未辅导"
        }
        return "完成"
    }
    fun getEnable(): Boolean {
        if (!printEntity.hasCheckAndSyncAnki) {
            return false
        }
        if(printEntity.strengthenMemoryCounts <= 0) {
            return false
        }
        return true
    }
    fun getStatInfo(): String {
        var totalCounts = 0
        var newCounts = 0
        var reviewCounts = 0
        var reinforceCounts = 0
        printEntity.deckEntitys
            .flatMap { it.cards }
            .map {
                totalCounts ++
                if (it.buttonCount == 4) {
                    reviewCounts ++
                } else {
                    newCounts ++
                }
                if(it.hasStrengthenMemory) reinforceCounts++
            }
        if (printEntity.hasStrengthenMemory) {
            return "总/${totalCounts} 新/${newCounts} 复/${reviewCounts} 记忆加强/${reinforceCounts}"
        }
        return "总/${totalCounts} 新/${newCounts} 复/${reviewCounts}"
    }

    fun getPrintInfo(): String {
        val about = printEntity.deckEntitys.map {
            "${it.name}(${it.total}张)"
        }
        return about.joinToString(" ")
    }
}