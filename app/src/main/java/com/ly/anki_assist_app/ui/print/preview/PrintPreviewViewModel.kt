package com.ly.anki_assist_app.ui.print.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ly.anki_assist_app.ankidroid.api.CardApi
import com.ly.anki_assist_app.ankidroid.api.DeckApi
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ankidroid.ui.CardAppearance
import com.ly.anki_assist_app.utils.Resource
import kotlinx.coroutines.delay
import java.lang.Exception

class PrintPreviewViewModel : ViewModel() {

    val dueCardsString = liveData<Resource<String>> {
        emit(Resource.loading(null))

        try {
            val ankiCardList = arrayListOf<AnkiCard>()
            val dueDeckList = DeckApi.asynGetDueDeckList()
            for (deck in dueDeckList) {
                ankiCardList.addAll(CardApi.asynGetDueCards(deck.deckId, 100))
            }
            emit(Resource.success(CardAppearance.displayString(ankiCardList)))
        } catch (e: Exception) {
            emit(Resource.error("Cards Loading Error", null))
        }
    }

}