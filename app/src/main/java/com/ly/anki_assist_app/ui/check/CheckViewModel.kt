package com.ly.anki_assist_app.ui.check

import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ankidroid.model.AnkiCardQA
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.CardEntity
import com.ly.anki_assist_app.printroom.DeckEntity
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.launch

const val ACTION_NEXT = 1
const val ACTION_NONE = 0
const val ACTION_PREV = -1

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


    private val _action = MutableLiveData<Int>()
    private fun emitAction(action: Int){
        _action.value = action
    }

    val checkCardLiveData: LiveData<Resource<CheckCard>> = MediatorLiveData<Resource<CheckCard>>().apply {
        fun update(isReset: Boolean){
            val printEntity = print.value?.data ?: return

            var curDeckIndex = value?.data?.deckIndex ?: 0
            var curCardIndex = value?.data?.cardIndex ?: 0

            if(isReset) {
                curCardIndex = 0
                curCardIndex = 0
            } else {
                value?.data?.let {
                    val action = _action.value ?: ACTION_NONE
                    when(action){
                        ACTION_NEXT -> {
                            curCardIndex++
                            if(curCardIndex >= printEntity.deckEntitys.get(curDeckIndex).cards.size) {
                                curCardIndex = 0
                                curDeckIndex ++
                            }
                            if(curDeckIndex >= printEntity.deckEntitys.size) {
                                value = Resource.error("Index has end", value?.data)
                                return
                            }
                        }
                        ACTION_PREV -> {
                            curCardIndex--
                            if(curCardIndex < 0) {
                                curCardIndex = 0
                                curDeckIndex --
                            }
                            if(curDeckIndex < 0) {
                                value = Resource.error("Index has begin", value?.data)
                                return
                            }
                        }
                    }
                    ""
                }
            }

            val deckEntity = printEntity.deckEntitys.get(curDeckIndex)
            val cardEntity = deckEntity.cards.get(curCardIndex)

            value = Resource.success(
                CheckCard(
                    curDeckIndex,
                    curCardIndex,
                    deckEntity,
                    cardEntity
                )
            )
        }

        addSource(_action) {update(false)}
        addSource(print){update(true)}
    }

    val checkCardString = checkCardLiveData.switchMap { resource ->
        liveData<Resource<String>> {
            if (resource.status == Status.ERROR) {
                emit(Resource.error(resource.message ?: "Error", null))
                return@liveData
            }
            if(resource.status == Status.LOADING) {
                emit(Resource.loading("加载中...", null))
                return@liveData
            }

            val cardEntity = resource.data?.cardEntity ?: return@liveData

            try {
                val ankiCardQA: AnkiCardQA = CardApi.asynGetQuestionAndAnswer(
                    cardEntity.noteId,
                    cardEntity.cardOrd,
                )
                emit(Resource.success(CardAppearance.displayCheckString(ankiCardQA.answerContent)))
            } catch (e: Exception) {
                emit(Resource.error("Cards String Loading Error", null))
            }
        }
    }

    fun prevAction(){
        emitAction(ACTION_PREV)
    }
    fun nextAction(){
        emitAction(ACTION_NEXT)
    }

    fun answerCardOnError() {
        val index = checkCardLiveData.value?.data?.errorButtonIndex ?: return
        answerCard(index, false)
    }

    fun answerCardOnRight() {
        val index = checkCardLiveData.value?.data?.rightButtonIndex ?: return
        answerCard(index, true)
    }

    fun answerCardOnEasy() {
        val index = checkCardLiveData.value?.data?.easyButtonIndex ?: return
        answerCard(index, true)
    }

    private fun answerCard(buttonIndex: Int, isRight: Boolean) {
        if (buttonIndex == -1) return
        val cardEntity = checkCardLiveData.value?.data?.cardEntity ?: return
        val printEntity = print.value?.data ?: return

        viewModelScope.launch {
            // TODO-ly 先修改Anki的状态
//            CardApi.asynAnswerCard(cardEntity.noteId, cardEntity.cardOrd, buttonIndex)

            // TODO-ly 再修改本地数据库的状态
//            checkCard.cardEntity.studyState = if(isRight) CardEntity.STUDY_STATE_RIGHT else CardEntity.STUDY_STATE_ERROR
//            checkCard.cardEntity.parentState = CardEntity.PARENT_STATE_CHECKED
//            PrintUtils.asynUpdate(printEntity)

            // 下一个
            emitAction(ACTION_NEXT)
        }
    }
}

data class CheckCard(
    // 下标
    val deckIndex: Int,
    val cardIndex: Int,
    // 当前结点对象
    val deckEntity: DeckEntity,
    val cardEntity: CardEntity,

    var errorButtonIndex: Int = 0,
    var rightButtonIndex: Int = 0,
    var easyButtonIndex: Int = 0,
) {
    init {
        val buttonCound = cardEntity.buttonCount
        if(buttonCound == 2) {
            errorButtonIndex = 0
            rightButtonIndex = 1
            easyButtonIndex = -1
        } else if (buttonCound == 3) {
            errorButtonIndex = 0
            rightButtonIndex = 1
            easyButtonIndex = 2
        } else if(buttonCound == 4) {
            // TODO-ly 缺乏一个识别逻辑，是否超过20
            errorButtonIndex = 1
            rightButtonIndex = 2
            easyButtonIndex = 3
        }
    }
    fun processShow(): String {
        return "${deckEntity.name}: ${cardIndex + 1} / ${deckEntity.cards.size}"
    }
    fun errorBtnShow(): String {
        if(errorButtonIndex == -1) {
            return "错误\n异常"
        }

        if (cardEntity.buttonCount == 3 || cardEntity.buttonCount == 2) {
            return "错误\n重来"
        }
        if(cardEntity.buttonCount == 4) {
            return "错误\n<20天 困难 ${cardEntity.nextReviewTimes[1]}\n>20天 重来"
        }
        return "错误\n异常"
    }
    fun rightBtnShow(): String {
        if(rightButtonIndex == -1) {
            return "正确\n异常"
        }

        return "正确\n${cardEntity.nextReviewTimes[rightButtonIndex]}"
    }
    fun easyBtnShow(): String {
        if(easyButtonIndex == -1) {
            return "简单\n异常"
        }
        return "简单\n${cardEntity.nextReviewTimes[easyButtonIndex]}"
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