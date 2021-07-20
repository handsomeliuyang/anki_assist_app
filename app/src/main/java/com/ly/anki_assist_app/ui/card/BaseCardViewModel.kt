package com.ly.anki_assist_app.ui.card

import android.view.View
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCardQA
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.CardEntity
import com.ly.anki_assist_app.printroom.DeckEntity
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status
import timber.log.Timber

const val ACTION_NEXT = 1
const val ACTION_REFRESH = 0
const val ACTION_PREV = -1

abstract class BaseCardViewModel() : ViewModel() {
    private val _printId = MutableLiveData<Int>()
    fun setPrintId(printId: Int) {
        _printId.value = printId
    }
    val printLiveData = _printId.switchMap { printId ->
        liveData {
            if(printId != -1) {
                val result = try {
                    val printEntity = PrintUtils.asynGetPrintById(printId)
                    Resource.success(printEntity)
                } catch (e: Exception) {
                    Timber.e(e)
                    Resource.error(e.message ?: "", null)
                }
                emit(result)
            } else {
                emit(Resource.error("printid 不能为-1", null))
            }
        }
    }

    val uiCardsLiveData: LiveData<Resource<List<UICard>>> = printLiveData.switchMap {
        liveData {
            val printEntity = it.data ?: return@liveData emit(Resource.error("no print data", null))

            val checkCards = printEntityToUICard(printEntity)

            emit(Resource.success(checkCards))
        }
    }

    abstract fun printEntityToUICard(printEntity: PrintEntity): List<UICard>

    private val _action = MutableLiveData<Int>()
//    val action: LiveData<Int> = _action
    fun emitAction(action: Int){
        _action.value = action
    }

    val curUICardLiveData: LiveData<Resource<UICard>> = MediatorLiveData<Resource<UICard>>().apply {
        fun update(isReset: Boolean){
            val uiCardCards = uiCardsLiveData.value?.data ?: return

            var curIndex = value?.data?.curIndex ?: 0

            if(isReset) {
                curIndex = 0
            } else {
                value?.data?.let {
                    val action = _action.value ?: ACTION_REFRESH
                    when(action){
                        ACTION_NEXT -> {
                            curIndex++
                            if(curIndex >= uiCardCards.size) {
                                value = Resource.error("Index has end", value?.data)
                                return
                            }
                        }
                        ACTION_PREV -> {
                            curIndex--
                            if(curIndex < 0) {
                                value = Resource.error("Index has begin", value?.data)
                                return
                            }
                        }
                    }
                    ""
                }
            }

            value = Resource.success(uiCardCards.get(curIndex))
        }

        addSource(_action) {update(false)}
        addSource(uiCardsLiveData){update(true)}
    }

    val curUICardString = curUICardLiveData.switchMap { resource ->
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

    fun getUICardsCount(): Int {
        return uiCardsLiveData.value?.data?.size ?: 0
    }

    suspend fun savePrint(){
        val printEntity = printLiveData.value?.data ?: return
        PrintUtils.asynUpdate(printEntity)
    }

}

data class UICard(
    var curIndex: Int = -1,

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
            easyButtonIndexMap.put(4, 3)
        }
    }

    fun getCheckMsg(): String {
        val index = easyButtonIndexMap.get(cardEntity.answerEasy) ?: return "复习中..."
        return answerButtons.get(index).text
    }

    fun processShow(total: Int): String{
        return "${curIndex + 1} / ${total} ${deckEntity.name}"
    }

    fun needCoach():Boolean {
        // 针对 重复 和 困难 需要加强一下记忆
        val index = easyButtonIndexMap.get(cardEntity.answerEasy) ?: return false
        return index <= 1
    }

    fun isShowAnswerBtnLayout(): Boolean{
        return cardEntity.answerEasy == -1
    }
}

data class AnswerButton(
    val visible: Int = View.GONE,
    val easy: Int = -1,
    val text: String = ""
)