package com.ly.anki_assist_app.ankidroid.api

import android.util.Log
import com.ichi2.anki.FlashCardsContract
import com.ly.anki_assist_app.App
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeckApi {
    companion object {

        suspend fun asynGetDueDeckList(): List<AnkiDeck> {
            return withContext(Dispatchers.IO) {
                return@withContext getDueDeckList()
            }
        }

        /**
         * 获取到期的卡片列表
         */
        private fun getDueDeckList(): List<AnkiDeck> {
            val deckList = arrayListOf<AnkiDeck>()

            val decksCursor = App.context.contentResolver.query(
                FlashCardsContract.Deck.CONTENT_ALL_URI,
                null,
                null,
                null,
                null
            ) ?: return deckList

            decksCursor.use { it ->
                if (it.moveToFirst()) {
                    do {
                        val deckId = it.getLong(it.getColumnIndex(FlashCardsContract.Deck.DECK_ID))
                        val deckName = it.getString(it.getColumnIndex(FlashCardsContract.Deck.DECK_NAME))

                        val deckCounts = it.getString(it.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS))
                        val deckDueCounts = AnkiDeck.parseDeckCounts(deckCounts)

                        // 过滤掉 Default 类别
                        if (deckName == "Default") {
                            continue
                        }

                        // 过滤掉没有需要复用的类别
                        if (deckDueCounts.getTotal() == 0) {
                            continue
                        }

                        deckList.add(AnkiDeck(deckId, deckName, deckDueCounts))
                    } while (it.moveToNext())
                }

                Log.d("AnkiDroidHelper", "${deckList}")
            }

            return deckList
        }
    }
}