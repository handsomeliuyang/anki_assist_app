package com.ly.anki_assist_app.ankidroid.model

import org.json.JSONArray

data class AnkiDeck(
    val deckId: Long,
    val fullName: String,
    val rootDir: String,
    val isSubDeck: Boolean,
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
            var rootDir = ""
            var isSubDeck = false
            var name = deckName
            if (dirs.size > 1) {
                isSubDeck = true
                name = dirs[dirs.size - 1]
                rootDir = dirs[0]
            }

            return AnkiDeck(deckId, deckName, rootDir, isSubDeck, name, parseDeckCounts(deckCounts))
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
