package com.ly.anki_assist_app.ui.print.preview

import android.os.Parcelable
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ankidroid.model.AnkiCardQA
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.printroom.*
//import com.ly.anki_assist_app.room.PrintEntity
//import com.ly.anki_assist_app.room.PrintRoomDatabase
import com.ly.anki_assist_app.utils.Resource
import com.ly.anki_assist_app.utils.Status
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.util.*

const val PRINT_CARD_MAX = 50

class PrintPreviewViewModel : ViewModel() {

    private val _planDecks = MutableLiveData<List<PrintDeck>>()

    fun setPlanDecks(planDecks: List<PrintDeck>) {
        _planDecks.value = planDecks
    }

    private val deckEntitysLiveData = _planDecks.switchMap { planDecks ->
        liveData {
            emit(Resource.loading("加载中...", null))

            try {
                val deckEntitys = planDecks.map {
                    val cardEntitys = CardApi.asynGetDueCards(it.deckId, it.total)
                        .map {ankiCard->
                            CardEntity(
                                ankiCard.noteId,
                                ankiCard.cardOrd,
                                ankiCard.buttonCount,
                                ankiCard.nextReviewTimes
                            )
                        }
                    DeckEntity(
                        it.deckId,
                        it.name,
                        it.total,
                        cardEntitys
                    )
                }
                emit(Resource.success(deckEntitys))
            } catch (e: Exception) {
                Timber.e(e)
                emit(Resource.error("Cards Loading Error", null))
            }
        }
    }

    val deckEntitysStringLiveData = deckEntitysLiveData.switchMap { resource ->
        liveData<Resource<String>> {
            if (resource.status == Status.ERROR) {
                emit(Resource.error(resource.message ?: "Error", null))
                return@liveData
            }
            if(resource.status == Status.LOADING) {
                emit(Resource.loading("加载中...", null))
                return@liveData
            }

            val deckEntity = resource.data ?: return@liveData

            try {
                val deckNameList = deckEntity.flatMap {
                    it.cards.map { cardEntity ->
                        it.name
                    }
                }
                val qaList = deckEntity.flatMap {
                    it.cards.map {cardEntity ->
                        CardApi.asynGetQuestionAndAnswer(
                            cardEntity.noteId,
                            cardEntity.cardOrd,
                        )
                    }
                }
                emit(Resource.success(CardAppearance.displayPrintString(deckNameList, qaList)))
            } catch (e: Exception) {
                emit(Resource.error("Cards String Loading Error", null))
            }
        }
    }

    fun savePrintdata(printName: String) {
        val deckEntitys = deckEntitysLiveData.value?.data ?: return

        viewModelScope.launch {
            val printEntity = PrintEntity(
                0,
                printName,
                Date(),
                deckEntitys
            )

            PrintUtils.asynSavePrint(printEntity)
        }
    }
}

@Parcelize
data class PrintDeck(
    val deckId: Long,
    val name: String,
    val total: Int,
) : Parcelable {
    companion object {
        fun from(ankiDeck: AnkiDeck): PrintDeck {
            return PrintDeck(ankiDeck.deckId, ankiDeck.name, ankiDeck.deckDueCounts.getTotal())
        }
    }
}