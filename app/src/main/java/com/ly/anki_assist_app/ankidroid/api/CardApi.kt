package com.ly.anki_assist_app.ankidroid.api

import android.content.ContentValues
import android.net.Uri
import com.ichi2.anki.FlashCardsContract
import com.ly.anki_assist_app.App
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ankidroid.model.AnkiCardQA
import com.ly.anki_assist_app.ankidroid.model.AnkiNoteInfo
import com.ly.anki_assist_app.ankidroid.model.Ease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.HashMap

class CardApi {
    companion object {

        suspend fun asynGetDueCard(nodeId: Long, cardOrd: Int, buttonCount: Int, nextReviewTimesString: String): AnkiCard {
            return withContext(Dispatchers.IO) {
                return@withContext getDueCard(nodeId, cardOrd, buttonCount, nextReviewTimesString)
            }
        }

        private fun getDueCard(noteId: Long, cardOrd: Int, buttonCount: Int, nextReviewTimesString: String): AnkiCard {
            val card = retrieveCard(noteId, cardOrd)
            card.setReviewData(buttonCount, nextReviewTimesString)

            return card
        }

        /**
         * 异步获取卡片详情
         */
        suspend fun asynGetDueCards(deckId: Long, numCards: Int): List<AnkiCard> {
            return withContext(Dispatchers.IO) {
                return@withContext getDueCards(deckId, numCards)
            }
        }

        private fun getDueCards(deckId: Long, numCards: Int): List<AnkiCard> {
            val ankiCards = arrayListOf<AnkiCard>()

            val cursor = App.context.contentResolver.query(
                FlashCardsContract.ReviewInfo.CONTENT_URI,
                null,
                "limit=?, deckID=?",
                arrayOf(numCards.toString(), deckId.toString()),
                null
            ) ?: return ankiCards

            cursor.use { it ->
                if(it.moveToFirst()) {
                    do {
                        val noteId = it.getLong(it.getColumnIndex(FlashCardsContract.ReviewInfo.NOTE_ID))
                        val cardOrd = it.getInt(it.getColumnIndex(FlashCardsContract.ReviewInfo.CARD_ORD))
                        val buttonCount = it.getInt(it.getColumnIndex(FlashCardsContract.ReviewInfo.BUTTON_COUNT))

                        val nextReviewTimes = it.getString(it.getColumnIndex(FlashCardsContract.ReviewInfo.NEXT_REVIEW_TIMES))

                        val mediaFiles = it.getString(it.getColumnIndex(FlashCardsContract.ReviewInfo.MEDIA_FILES))

                        Timber.d("mediaFiles=%s", mediaFiles)

                        val card = retrieveCard(noteId, cardOrd)
                        card.setReviewData(buttonCount, nextReviewTimes)
                        ankiCards.add(card)
                    } while (it.moveToNext())
                }
            }
            return ankiCards
        }

        private fun retrieveCard(noteId: Long, cardOrd: Int): AnkiCard {
            val ankiCardQA = getQuestionAndAnswer(noteId, cardOrd)

            val ankiNoteInfo = getNoteInfo(noteId)

            val fieldNames = getModelInfo(ankiNoteInfo.modelId)

            // fieldNames 转换为 Key-Value
            val fieldMap = HashMap<String, String>()
            for (i in fieldNames.indices) {
                val fieldName = fieldNames[i]
                var fieldValue = ""
                if (i < ankiNoteInfo.fieldValues.size) {
                    fieldValue = ankiNoteInfo.fieldValues[i]
                }
                fieldMap[fieldName] = fieldValue
            }

            val cardTemplateName = getCardTemplateName(ankiNoteInfo.modelId, cardOrd)

            val ankiCard = AnkiCard()
            ankiCard.noteId = noteId
            ankiCard.cardOrd = cardOrd
            ankiCard.modelId = ankiNoteInfo.modelId
            ankiCard.cardTemplateName = cardTemplateName
            ankiCard.fieldMap = fieldMap
            ankiCard.tags = ankiNoteInfo.tagSet
            ankiCard.cardQA = ankiCardQA
            return ankiCard
        }

        private fun getQuestionAndAnswer(noteId: Long, cardOrd: Int): AnkiCardQA {
            // 获取 Card 的 question 和 answer
            val ankiCardQA = AnkiCardQA("", "")

            val noteUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, noteId.toString())
            val cardsUri = Uri.withAppendedPath(noteUri, "cards")
            val specificCardUri = Uri.withAppendedPath(cardsUri, cardOrd.toString())
            val cardCursor = App.context.contentResolver.query(
                specificCardUri,
                arrayOf(FlashCardsContract.Card.QUESTION, FlashCardsContract.Card.ANSWER),
                null,
                null,
                null
            ) ?: return ankiCardQA

            cardCursor.use { it ->
                if (it.moveToFirst()) {
                    ankiCardQA.questionContent = it.getString(it.getColumnIndex(FlashCardsContract.Card.QUESTION))
                    ankiCardQA.answerContent = it.getString(it.getColumnIndex(FlashCardsContract.Card.ANSWER))
                }
            }

            return ankiCardQA
        }

        private fun getNoteInfo(noteId: Long): AnkiNoteInfo {
            // 获取 note 信息
            val ankiNoteInfo = AnkiNoteInfo()

            val noteInfoUri = Uri.withAppendedPath(FlashCardsContract.Note.CONTENT_URI, noteId.toString());
            val noteCursor = App.context.contentResolver.query(
                noteInfoUri,
                null,
                null,
                null,
                null
            ) ?: return ankiNoteInfo

            noteCursor.use { it ->
                if(it.moveToFirst()) {
                    ankiNoteInfo.modelId = it.getLong(it.getColumnIndex(FlashCardsContract.Note.MID))

                    val fields = it.getString(it.getColumnIndex(FlashCardsContract.Note.FLDS))
                    ankiNoteInfo.fieldValues = fields.split("\\x1f").toTypedArray()

                    val tags = it.getString(it.getColumnIndex(FlashCardsContract.Note.TAGS))
                    val cardTags = tags.split(" ").toTypedArray()
                    for (tag in cardTags) {
                        ankiNoteInfo.tagSet.add(tag)
                    }
                }
            }

            return ankiNoteInfo
        }

        private fun getModelInfo(modelId: Long): Array<String>{
            // 获取 mode 信息
            var fieldNames = arrayOf<String>()

            val modelUri = Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, modelId.toString())
            val modelCursor = App.context.contentResolver.query(
                modelUri,
                null,
                null,
                null,
                null
            ) ?: return fieldNames

            modelCursor.use { it ->
                if (it.moveToFirst()) {
                    val fieldNamesStr = it.getString(it.getColumnIndex(
                        FlashCardsContract.Model.FIELD_NAMES))
                    fieldNames = fieldNamesStr.split("\\x1f").toTypedArray()
                }
            }
            return fieldNames
        }

        private fun getCardTemplateName(modelId: Long, cardOrd: Int): String {
            // 获取 template 信息

            var cardTemplateName = ""

            val uri1 = Uri.withAppendedPath(FlashCardsContract.Model.CONTENT_URI, modelId.toString())
            val uri2 = Uri.withAppendedPath(uri1, "templates")
            val cardTemplateUri = Uri.withAppendedPath(uri2, cardOrd.toString())
            val cardTemplateCursor = App.context.contentResolver.query(
                cardTemplateUri,
                null,  // projection
                null,  // selection is ignored for this URI
                null,  // selectionArgs is ignored for this URI
                null // sortOrder is ignored for this URI
            ) ?: return cardTemplateName
            cardTemplateCursor.use { it ->
                if (it.moveToFirst()) {
                    cardTemplateName = it.getString(it.getColumnIndex(
                        FlashCardsContract.CardTemplate.NAME))
                }
            }

            return cardTemplateName
        }

        /**
         * 异步回答卡片
         */
        suspend fun asynAnswerCard(card: AnkiCard, ease: Ease, timeTaken: Long){
            withContext(Dispatchers.IO) {
                answerCard(card, ease, timeTaken)
            }
        }

        private fun answerCard(card: AnkiCard, ease: Ease, timeTaken: Long){
            val values = ContentValues()
            values.put(FlashCardsContract.ReviewInfo.NOTE_ID, card.noteId)
            values.put(FlashCardsContract.ReviewInfo.CARD_ORD, card.cardOrd)
            values.put(FlashCardsContract.ReviewInfo.EASE, ease.value)
            values.put(FlashCardsContract.ReviewInfo.TIME_TAKEN, timeTaken)

            App.context.contentResolver.update(
                FlashCardsContract.ReviewInfo.CONTENT_URI,
                values,
                null,
                null
            )
        }

    }
}