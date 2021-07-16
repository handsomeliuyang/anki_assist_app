package com.ly.anki_assist_app.printroom

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ichi2.anki.FlashCardsContract
import java.util.*

@Entity(tableName = "print_table")
data class PrintEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val time: Date?,
    val state: Int?,
    val deckEntitys: List<DeckEntity>
) {

    companion object {
        const val STATE_NONE_CHECK = 1
        const val STATE_NONE_COACH = 2
        const val STATE_FINISHED = 3
    }
}

data class DeckEntity(
    val deckId: Long,
    val name: String,
    val total: Int,
    val cards: List<CardEntity>
)

data class CardEntity(
    val noteId: Long,
    val cardOrd: Int,
    val buttonCount: Int,
    val nextReviewTimes: List<String>,

    var answerEasy: Int = -1,
    var hasStrengthenMemory: Boolean = false,
    var hasSyncAnki: Boolean = false
)

