package com.ly.anki_assist_app.ui.check

import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status

class CheckViewModel : ViewModel() {

    private val _printId = MutableLiveData<Int>()

    fun setPrintId(printId: Int) {
        _printId.value = printId
    }

    val print = _printId.switchMap { printId ->
        liveData {
            if(printId != -1) {
                val result = try {
                    val printEntity = PrintUtils.asynGetPrintById(printId)
                    Resource.success(printEntity)
                } catch (e: Exception) {
                    Resource.error(e.message ?: "", null)
                }
                emit(result)
            } else {
                emit(Resource.error("printid 不能为-1", null))
            }
        }
    }

    private val _next = MutableLiveData<Int>()
    private var _currentIndex = Pair<Int, Int>(0, -1)
//    private val _index = MutableLiveData<Pair<Int, Int>>()

    fun resetIndex(){
        _next.value = 0
        _currentIndex = Pair(0, -1)
    }

    fun increaseIndex(){
        _next.value = _next.value?.plus(1)
    }

    val checkCard = _next.switchMap { index ->
        liveData {
            if(print.value?.status != Status.SUCCESS){
                emit(Resource.error("打印记录加载失败", null))
                return@liveData
            }
            val deckEntitys = print.value?.data?.deckEntitys ?: emptyList()

            var first = _currentIndex.first
            var second = _currentIndex.second

            // 计算下一个
            second++

            if(second >= deckEntitys.get(_currentIndex.first).cards.size) {
                first ++
                second = 0
            }

            if (first >= deckEntitys.size) {
                emit(Resource.success(null))
                return@liveData
            }

            _currentIndex = Pair(first, second)
            val deckEntity = deckEntitys.get(first)
            val card = deckEntity.cards.get(second)

            // 查询问题内容及
            val process = index + 1
            val total = deckEntity.cards.size

            try {
                val ankiCard: AnkiCard = CardApi.asynGetDueCard(
                    card.noteId,
                    card.cardOrd,
                    card.buttonCount,
                    card.nextReviewTimesString
                )
                emit(Resource.success(CheckCard(deckEntity.name, process, total, ankiCard)))
            } catch (e: Exception) {
                emit(Resource.error(e.message ?: "", null))
            }
        }
    }

    val checkCardString = checkCard.switchMap { resource ->
        liveData<Resource<String>> {
            try {
                when (resource.status) {
                    Status.LOADING -> emit(Resource.loading("加载中...", null))
                    Status.ERROR -> emit(Resource.error(resource.message ?: "Error", null))
                    Status.SUCCESS -> emit(Resource.success(CardAppearance.displayCheckString(resource.data?.ankiCard)))
                }
            } catch (e: java.lang.Exception) {
                emit(Resource.error("Cards Loading Error", null))
            }
        }
    }

    fun answerCard() {

    }

}

data class CheckCard(
    val deckName: String,
    val process: Int,
    val total: Int,
    val ankiCard: AnkiCard
) {
    fun processShow(): String {
        return "${deckName}: ${process} / ${total}"
    }
    fun errorBtnShow(): String {
        if (ankiCard.buttonCount == 3) {
            return "错误\n重来"
        }
        if(ankiCard.buttonCount == 4) {
            return "错误\n<20天 困难 ${ankiCard.nextReviewTimes[1]}月\n>20天 重来"
        }
        return "错误\n异常"
    }
    fun rightBtnShow(): String {
        if (ankiCard.buttonCount == 3) {
            return "正确\n${ankiCard.nextReviewTimes[1]}"
        }
        if(ankiCard.buttonCount == 4) {
            return "正确\n${ankiCard.nextReviewTimes[2]}"
        }
        return "正确\n异常"
    }
    fun easyBtnShow(): String {
        if (ankiCard.buttonCount == 3) {
            return "简单\n${ankiCard.nextReviewTimes[2]}"
        }
        if(ankiCard.buttonCount == 4) {
            return "简单\n${ankiCard.nextReviewTimes[3]}"
        }
        return "简单\n异常"
    }
}