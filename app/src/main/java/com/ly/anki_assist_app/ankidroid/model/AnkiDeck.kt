package com.ly.anki_assist_app.ankidroid.model

import org.json.JSONArray

data class AnkiDeck(
    val deckId: Long,
    val category: String,
    val name: String,
    val deckDueCounts: DeckDueCounts
) {
    companion object {
        private fun parseDeckCounts(deckCounts: String): DeckDueCounts {
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

        fun fromString(deckId: Long, deckName: String, deckCounts: String): AnkiDeck{
            val dirs = deckName.split("::")
            var category = ""
            var name = deckName
            if (dirs.size > 1) {
                name = dirs[dirs.size - 1]
                category = dirs[0]
            }

            return AnkiDeck(deckId, category, name, parseDeckCounts(deckCounts))
        }

        fun fromName(name: String): AnkiDeck {
            return AnkiDeck(-1, "", name, DeckDueCounts(0, 0, 0))
        }
    }
}

data class DeckDueCounts(
    var learnCount: Int,
    var reviewCount: Int,
    var newCount: Int
) {
    fun getTotal(): Int {
        return learnCount + reviewCount + newCount
    }

    fun add(deckDueCounts: DeckDueCounts) {
        this.newCount += deckDueCounts.newCount
        this.learnCount += deckDueCounts.learnCount
        this.reviewCount += deckDueCounts.reviewCount
    }
}
