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
    val deckEntitys: List<DeckEntity>,
    var hasCheckAndSyncAnki: Boolean = false,

    var strengthenMemoryCounts: Int = 0,
    var hasStrengthenMemory: Boolean = false
)

data class DeckEntity(
    val deckId: Long,
    val name: String,
    val total: Int,
    var cards: List<CardEntity>
)

data class CardEntity(
    val noteId: Long,
    val cardOrd: Int,
    val buttonCount: Int,
    val nextReviewTimes: List<String>,

    var answerEasy: Int = -1,
    var hasStrengthenMemory: Boolean = false
)

