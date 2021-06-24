package com.ly.anki_assist_app.ankidroid.model

import org.json.JSONArray

data class AnkiDeck(
    val deckId: Long,
    val deckName: String,
    val deckDueCounts: DeckDueCounts
) {
    companion object {
        fun parseDeckCounts(deckCounts: String): DeckDueCounts {
            return try {
                val jsonArray = JSONArray(deckCounts)
                DeckDueCounts(
                    jsonArray.getInt(0),
                    jsonArray.getInt(1),
                    jsonArray.getInt(2)
                )
            } catch (e: Exception) {
                DeckDueCounts(0, 0, 0)
            }
        }
    }
}

data class DeckDueCounts(
    val learnCount: Int,
    val reviewCount: Int,
    val newCount: Int
) {
    fun getTotal(): Int {
        return learnCount + reviewCount + newCount
    }
}
