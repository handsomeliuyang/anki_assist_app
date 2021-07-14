package com.ly.anki_assist_app.ui.print.preview

import android.os.Parcelable
import androidx.lifecycle.*
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
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

const val PRINT_CARD_MAX = 30

class PrintPreviewViewModel : ViewModel() {

    private val _planDecks = MutableLiveData<List<PrintDeck>>()

    fun setPlanDecks(planDecks: List<PrintDeck>) {
        _planDecks.value = planDecks
    }

    private val printDeckCardsList = _planDecks.switchMap { planDecks ->
        liveData {
            emit(Resource.loading("加载中...", null))

            try {
                val list = arrayListOf<PrintDeckCards>()

                var remainNums = PRINT_CARD_MAX

                for (planDeck in planDecks) {
                    if(remainNums <= 0) {
                        break
                    }
                    val nums = Math.min(planDeck.total, remainNums)
                    val cards = CardApi.asynGetDueCards(planDeck.deckId, nums)

                    list.add(
                        PrintDeckCards(
                            PrintDeck(planDeck.deckId, planDeck.name, nums),
                            cards
                        )
                    )

                    remainNums -= cards.size
                }

                emit(Resource.success(list))
            } catch (e: Exception) {
                Timber.e(e)
                emit(Resource.error("Cards Loading Error", null))
            }
        }
    }

    val dueCardsString = printDeckCardsList.switchMap { resource ->
        liveData<Resource<String>> {
            try {
                when (resource.status) {
                    Status.LOADING -> emit(Resource.loading("加载中...", null))
                    Status.ERROR -> emit(Resource.error(resource.message ?: "Error", null))
                    Status.SUCCESS -> emit(Resource.success(CardAppearance.displayPrintString(resource.data ?: emptyList())))
                }
            } catch (e: Exception) {
                emit(Resource.error("Cards String Loading Error", null))
            }
        }
    }

    fun savePrintdata(printName: String) {
        viewModelScope.launch {

            val status = printDeckCardsList.value?.status ?: Status.LOADING
            if (status == Status.SUCCESS) {

                val printList = printDeckCardsList.value?.data ?: return@launch
                val deckEntitys = printList.map {
                    val cardEntitys = it.cards.map { ankiCard->
                        CardEntity(
                            ankiCard.noteId,
                            ankiCard.cardOrd,
                            ankiCard.buttonCount,
                            ankiCard.nextReviewTimesString,
                            CardEntity.STUDY_STATE_INIT,
                            CardEntity.PARENT_STATE_INIT
                        )
                    }
                    DeckEntity(
                        it.printDeck.deckId,
                        it.printDeck.name,
                        it.printDeck.total,
                        cardEntitys
                    )
                }

                val printEntity = PrintEntity(
                    0,
                    printName,
                    Date(),
                    PrintEntity.STATE_NONE_CHECK,
                    deckEntitys
                )

                PrintUtils.asynSavePrint(printEntity)
            }
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

data class PrintDeckCards(
    val printDeck: PrintDeck,
    val cards: List<AnkiCard>
)