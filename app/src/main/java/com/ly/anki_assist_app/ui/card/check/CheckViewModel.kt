package com.ly.anki_assist_app.ui.card.check

import android.view.View
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCardQA
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.CardEntity
import com.ly.anki_assist_app.printroom.DeckEntity
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.printroom.PrintUtils
import com.ly.anki_assist_app.ui.card.*
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class CheckViewModel : BaseCardViewModel(CARD_STATE_ANSWER) {

    override fun printEntityToUICard(printEntity: PrintEntity): List<UICard> {
        return printEntity.deckEntitys
            .flatMap { deckEntity ->
                deckEntity.cards.map {
                    UICard(deckEntity = deckEntity, cardEntity = it)
                }
            }
            .mapIndexed { index, uiCard ->
                uiCard.curIndex = index
                uiCard
            }
    }

    fun resetAnswer(){
        val cardEntity = curUICardLiveData.value?.data?.cardEntity ?: return
        cardEntity.answerEasy = -1
        emitAction(ACTION_REFRESH)
    }

    fun answerCard(easy: Int){
        val cardEntity = curUICardLiveData.value?.data?.cardEntity ?: return
        viewModelScope.launch {
            // 再修改本地数据库的状态
            cardEntity.answerEasy = easy
            // 下一个
            emitAction(ACTION_NEXT)
        }
    }

    private val _syncAnkiLiveData = MutableLiveData<Resource<Boolean>>()
    val syncAnkiLivedata: LiveData<Resource<Boolean>> = _syncAnkiLiveData
    
    fun syncAnki() {
        val printEntity = printLiveData.value?.data ?: return
        val uiCards = uiCardsLiveData.value?.data ?: return

        if (printEntity.hasCheckAndSyncAnki) {
            return
        }

        val noneAnswerDecks = uiCards.filter {
            it.cardEntity.answerEasy == -1
        }

        if (noneAnswerDecks.isNotEmpty()) {
            // 还有未完成的卡片
            _syncAnkiLiveData.value = Resource.error("${noneAnswerDecks.size} 张卡片未检查", null)
            return
        }

        _syncAnkiLiveData.value = Resource.loading("同步中...", null)
        viewModelScope.launch {
            // 先同步Anki,同时查询是否需要加强记忆
            var strengthenMemoryCounts = 0
            uiCards.map {
                CardApi.asynAnswerCard(it.cardEntity.noteId, it.cardEntity.cardOrd, it.cardEntity.answerEasy)
                if (it.needCoach()) {
                    strengthenMemoryCounts++
                }
            }

            // 修改本地数据库的状态
            printEntity.hasCheckAndSyncAnki = true
            printEntity.strengthenMemoryCounts = strengthenMemoryCounts
            savePrint()

            _syncAnkiLiveData.value = Resource.success(true)
        }
    }


}