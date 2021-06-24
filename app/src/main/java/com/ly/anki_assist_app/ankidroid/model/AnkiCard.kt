package com.ly.anki_assist_app.ankidroid.model

import androidx.collection.arrayMapOf
import androidx.collection.arraySetOf
import com.ichi2.anki.FlashCardsContract
import org.json.JSONArray

data class AnkiCard(
    var noteId: Long = 0,
    var cardOrd: Int = 0,
    var modelId: Long = 0,
    var cardTemplateName: String = "",

    var fieldMap: Map<String, String> = arrayMapOf(),
    var tags: Set<String> = arraySetOf<String>(),

    var buttonCount: Int = 0,
    var nextReviewTimes: List<String> = arrayListOf(),

    var cardQA: AnkiCardQA = AnkiCardQA()
    ) {

    fun setReviewData(buttonCount: Int, nextReviewTimesString: String) {
        this.buttonCount = buttonCount

        val mutableList = arrayListOf<String>()
        val reviewTimes = JSONArray(nextReviewTimesString)
        for (i in 0 until reviewTimes.length()) {
            val nextReviewTime: String = reviewTimes.getString(i)
            mutableList.add(nextReviewTime)
        }
        this.nextReviewTimes = mutableList
    }

}

data class AnkiCardQA(
    var questionContent: String = "",
    var answerContent: String = "",
)

data class AnkiNoteInfo(
    var fieldValues: Array<String> = arrayOf<String>(),
    var tagSet: HashSet<String> = HashSet<String>(),
    var modelId: Long = 0
)