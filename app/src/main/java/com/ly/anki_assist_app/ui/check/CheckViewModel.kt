package com.ly.anki_assist_app.ui.check

import android.view.View
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCardQA
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.CardEntity
import com.ly.anki_assist_app.printroom.DeckEntity
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val ACTION_NEXT = 1
const val ACTION_REFRESH = 0
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
                    val action = _action.value ?: ACTION_REFRESH
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

    suspend fun savePrint(){
        val printEntity = print.value?.data ?: return
        PrintUtils.asynUpdate(printEntity)
    }

    fun resetAnswer(){
        val cardEntity = checkCardLiveData.value?.data?.cardEntity ?: return
        cardEntity.answerEasy = -1
        emitAction(ACTION_REFRESH)
    }

    fun answerCard(easy: Int){
        val cardEntity = checkCardLiveData.value?.data?.cardEntity ?: return
        val printEntity = print.value?.data ?: return

        viewModelScope.launch {
            // TODO-ly 先修改Anki的状态
//            CardApi.asynAnswerCard(cardEntity.noteId, cardEntity.cardOrd, buttonIndex)

            // 再修改本地数据库的状态
            cardEntity.answerEasy = easy
//            PrintUtils.asynUpdate(printEntity)

            // 下一个
            emitAction(ACTION_NEXT)
        }
    }


    private val _syncAnkiLiveData = MutableLiveData<Resource<Boolean>>()
    val syncAnkiLivedata: LiveData<Resource<Boolean>> = _syncAnkiLiveData
    fun syncAnki() {
        val printEntity = print.value?.data ?: return

        if (printEntity.hasCheckAndSyncAnki) {
            return
        }

        val noneAnswerDecks = printEntity.deckEntitys.flatMap {
            it.cards
        }.filter {
            it.answerEasy == -1
        }

        if (noneAnswerDecks.isNotEmpty()) {
            // 还有未完成的卡片
            _syncAnkiLiveData.value = Resource.error("${noneAnswerDecks.size} 张卡片未检查", null)
            return
        }

        _syncAnkiLiveData.value = Resource.loading("同步中...", null)



        viewModelScope.launch {

            delay(3000)

            // 先同步Anki
            val results = printEntity.deckEntitys.flatMap {
                it.cards
            }.map {
                CardApi.asynAnswerCard(it.noteId, it.cardOrd, it.answerEasy)
            }

            // 修改本地数据库的状态
            printEntity.hasCheckAndSyncAnki = true
            savePrint()

            _syncAnkiLiveData.value = Resource.success(true)
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

    val answerButtons: ArrayList<AnswerButton> = ArrayList(),
    val easyButtonIndexMap: MutableMap<Int, Int> = mutableMapOf()
) {
    init {
        val buttonCount = cardEntity.buttonCount

        if(buttonCount == 2) {
            answerButtons.add(AnswerButton(View.VISIBLE, 1,cardEntity.nextReviewTimes.get(0) + '\n' + "重来"))
            answerButtons.add(AnswerButton())
            answerButtons.add(AnswerButton(View.VISIBLE,2,  cardEntity.nextReviewTimes.get(1) + '\n' + "一般"))
            answerButtons.add(AnswerButton())
            easyButtonIndexMap.put(1, 0)
            easyButtonIndexMap.put(2, 2)
        } else if (buttonCount == 3) {
            answerButtons.add(AnswerButton(View.VISIBLE,1, cardEntity.nextReviewTimes.get(0) + '\n' + "重来"))
            answerButtons.add(AnswerButton())
            answerButtons.add(AnswerButton(View.VISIBLE,2, cardEntity.nextReviewTimes.get(1) + '\n' + "一般" ))
            answerButtons.add(AnswerButton(View.VISIBLE,3, cardEntity.nextReviewTimes.get(2) + '\n' + "简单"))
            easyButtonIndexMap.put(1, 0)
            easyButtonIndexMap.put(2, 2)
            easyButtonIndexMap.put(3, 3)
        } else {
            answerButtons.add(AnswerButton(View.VISIBLE,1,  cardEntity.nextReviewTimes.get(0) + '\n' + "重来"))
            answerButtons.add(AnswerButton(View.VISIBLE,2,  cardEntity.nextReviewTimes.get(1) + '\n' + "困难"))
            answerButtons.add(AnswerButton(View.VISIBLE,3,  cardEntity.nextReviewTimes.get(2) + '\n' + "一般"))
            answerButtons.add(AnswerButton(View.VISIBLE,4, cardEntity.nextReviewTimes.get(3) + '\n' + "简单"))
            easyButtonIndexMap.put(1, 0)
            easyButtonIndexMap.put(2, 1)
            easyButtonIndexMap.put(3, 2)
            easyButtonIndexMap.put(3, 3)
        }
    }
    fun processShow(): String {
        return "${deckEntity.name}: ${cardIndex + 1} / ${deckEntity.cards.size}"
    }

    fun isShowAnswerBtnLayout(): Boolean{
        return cardEntity.answerEasy == -1
    }

    fun getCheckMsg(): String {
        val index = easyButtonIndexMap.get(cardEntity.answerEasy) ?: return "复习中..."
        return answerButtons.get(index).text
    }
}

data class AnswerButton(
    val visible: Int = View.GONE,
    val easy: Int = -1,
    val text: String = ""
)