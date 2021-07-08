package com.ly.anki_assist_app.ui.print.preview

import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.CardIdAndState
import com.ly.anki_assist_app.printroom.PRINT_STATE_NONE_CHECK
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.printroom.PrintUtils
//import com.ly.anki_assist_app.room.PrintEntity
//import com.ly.anki_assist_app.room.PrintRoomDatabase
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class PrintPreviewViewModel : ViewModel() {

    private val _printDecks = MutableLiveData<Array<Long>>()

    fun setPrintList(printArray: Array<Long>) {
        _printDecks.value = printArray
    }

    val dueCards = _printDecks.switchMap { printArray ->
        liveData<Resource<List<AnkiCard>>> {
            emit(Resource.loading("加载中...", null))

            try {
                val ankiCardList = arrayListOf<AnkiCard>()

                printArray.map { deckId ->
                    ankiCardList.addAll(CardApi.asynGetDueCards(deckId, 20))
                }

                emit(Resource.success(ankiCardList))
            } catch (e: Exception) {
                emit(Resource.error("Cards Loading Error", null))
            }
        }
    }

    val dueCardsString = dueCards.switchMap { resource ->
        liveData<Resource<String>> {
            try {
                when (resource.status) {
                    Status.LOADING -> emit(Resource.loading("加载中...", null))
                    Status.ERROR -> emit(Resource.error(resource.message ?: "Error", null))
                    Status.SUCCESS -> emit(Resource.success(CardAppearance.displayString(resource.data ?: emptyList())))
                }
            } catch (e: Exception) {
                emit(Resource.error("Cards Loading Error", null))
            }
        }
    }

    fun savePrintdata(printName: String) {
        viewModelScope.launch {

            val status = dueCards.value?.status ?: Status.LOADING
            if (status == Status.SUCCESS) {

                val list = arrayListOf<CardIdAndState>()

                val ankiCardList = dueCards.value?.data ?: return@launch
                ankiCardList.map {
                    list.add(CardIdAndState(
                        noteId = it.noteId,
                        cardOrd = it.cardOrd,
                        studyState = CardIdAndState.STUDY_STATE_INIT,
                        parentState = CardIdAndState.PARENT_STATE_INIT
                    ))
                }

                val printEntity = PrintEntity(
                    name = printName,
                    time = Date(),
                    state = PRINT_STATE_NONE_CHECK,
                    category = "类别",
                    reviewCount = ankiCardList.size,
                    cardIdAndStateList = list
                )

                PrintUtils.asynSavePrint(printEntity)
            }
        }
    }

}