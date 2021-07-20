package com.ly.anki_assist_app.ui.card.coach

import androidx.lifecycle.*
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.ui.card.ACTION_NEXT
import com.ly.anki_assist_app.ui.card.BaseCardViewModel
import com.ly.anki_assist_app.ui.card.UICard
import com.ly.anki_assist_app.utils.Resource
import kotlinx.coroutines.launch

class CoachViewModel : BaseCardViewModel() {

    override fun printEntityToUICard(printEntity: PrintEntity): List<UICard> {
        return printEntity.deckEntitys
            .flatMap { deckEntity ->
                deckEntity.cards.map {
                    UICard(deckEntity = deckEntity, cardEntity = it)
                }
            }
            .filter {
                it.needCoach()
            }
            .mapIndexed { index, uiCard ->
                uiCard.curIndex = index
                uiCard
            }
    }

    fun coachCardFinish() {
        val uiCard = curUICardLiveData.value?.data ?: return
        uiCard.cardEntity.hasStrengthenMemory = true
        emitAction(ACTION_NEXT)
    }

    private val _coachFinishLiveData = MutableLiveData<Resource<Boolean>>()
    val coachFinishLivedata: LiveData<Resource<Boolean>> = _coachFinishLiveData
    fun coachFinished() {
        val printEntity = printLiveData.value?.data ?: return
        val uiCards = uiCardsLiveData.value?.data ?: return

        if (printEntity.hasStrengthenMemory) {
            return
        }

        val noneCoachCards = uiCards.filter {
            !it.cardEntity.hasStrengthenMemory
        }

        if (noneCoachCards.isNotEmpty()) {
            // 还有未完成的卡片
            _coachFinishLiveData.value = Resource.error("${noneCoachCards.size} 张卡片未加强记忆", null)
            return
        }

        _coachFinishLiveData.value = Resource.loading("同步中...", null)
        viewModelScope.launch {
            // 修改本地数据库的状态
            printEntity.hasStrengthenMemory = true
            savePrint()
            _coachFinishLiveData.value = Resource.success(true)
        }
    }


}