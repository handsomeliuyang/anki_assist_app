package com.ly.anki_assist_app.ui.check

import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.CardEntity
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.launch

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

    fun resetIndex(){
        _currentIndex = Pair(0, -1)
        // 注意：livedata的监听是马上执行，而不是等当前方法执行完后
        _next.value = 0
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

                var errorButtonIndex: Int = -1
                var rightButtonIndex: Int = -1
                var easyButtonIndex: Int = -1
                if(ankiCard.buttonCount == 2) {
                    errorButtonIndex = 0
                    rightButtonIndex = 1
                    easyButtonIndex = -1
                } else if (ankiCard.buttonCount == 3) {
                    errorButtonIndex = 0
                    rightButtonIndex = 1
                    easyButtonIndex = 2
                } else if(ankiCard.buttonCount == 4) {
                    // TODO-ly 缺乏一个识别逻辑，是否超过20
                    errorButtonIndex = 1
                    rightButtonIndex = 2
                    easyButtonIndex = 3
                }

                emit(Resource.success(CheckCard(deckEntity.name, process, total, ankiCard, card, errorButtonIndex, rightButtonIndex, easyButtonIndex)))
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
                    Status.SUCCESS -> {
                        if (resource.data != null) {
                            emit(Resource.success(CardAppearance.displayCheckString(resource.data?.ankiCard)))
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                emit(Resource.error("Cards Loading Error", null))
            }
        }
    }

    fun answerCardOnError() {
        val index = checkCard.value?.data?.errorButtonIndex ?: return
        answerCard(index, false)
    }

    fun answerCardOnRight() {
        val index = checkCard.value?.data?.rightButtonIndex ?: return
        answerCard(index, true)
    }

    fun answerCardOnEasy() {
        val index = checkCard.value?.data?.easyButtonIndex ?: return
        answerCard(index, true)
    }

    private fun answerCard(buttonIndex: Int, isRight: Boolean) {
        if (buttonIndex == -1) return
        val checkCard = checkCard.value?.data ?: return
        val printEntity = print.value?.data ?: return


        viewModelScope.launch {
            // 先修改Anki的状态
            CardApi.asynAnswerCard(checkCard.ankiCard, buttonIndex)

            // 再修改本地数据库的状态
            checkCard.cardEntity.studyState = if(isRight) CardEntity.STUDY_STATE_RIGHT else CardEntity.STUDY_STATE_ERROR
            checkCard.cardEntity.parentState = CardEntity.PARENT_STATE_CHECKED
            PrintUtils.asynUpdate(printEntity)

            // 下一个
            increaseIndex()
        }
    }
}

data class CheckCard(
    val deckName: String,
    val process: Int,
    val total: Int,
    val ankiCard: AnkiCard,
    val cardEntity: CardEntity,
    val errorButtonIndex: Int,
    val rightButtonIndex: Int,
    val easyButtonIndex: Int
) {
    fun processShow(): String {
        return "${deckName}: ${process} / ${total}"
    }
    fun errorBtnShow(): String {
        if(errorButtonIndex == -1) {
            return "错误\n异常"
        }

        if (ankiCard.buttonCount == 3 || ankiCard.buttonCount == 2) {
            return "错误\n重来"
        }
        if(ankiCard.buttonCount == 4) {
            return "错误\n<20天 困难 ${ankiCard.nextReviewTimes[1]}\n>20天 重来"
        }
        return "错误\n异常"
    }
    fun rightBtnShow(): String {
        if(rightButtonIndex == -1) {
            return "正确\n异常"
        }

        return "正确\n${ankiCard.nextReviewTimes[rightButtonIndex]}"
    }
    fun easyBtnShow(): String {
        if(easyButtonIndex == -1) {
            return "简单\n异常"
        }
        return "简单\n${ankiCard.nextReviewTimes[easyButtonIndex]}"
    }
    fun isShowAnswerBtnLayout(): Boolean{
        return cardEntity.parentState == CardEntity.PARENT_STATE_INIT
    }
    fun getCheckMsg(): String {

        val studyResult = when(cardEntity.studyState) {
            CardEntity.STUDY_STATE_ERROR -> "答题：错误"
            CardEntity.STUDY_STATE_RIGHT -> "答题：正确"
            else -> "答题中..."
        }

        val parentResult = when(cardEntity.parentState){
            CardEntity.PARENT_STATE_CHECKED -> "家长已检查"
            CardEntity.PARENT_STATE_COACH -> "此题已辅导"
            else -> "待检验..."
        }

        return "${studyResult}\n${parentResult}"
    }
}