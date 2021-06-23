package com.ly.anki_assist_app.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.ichi2.anki.FlashCardsContract
import com.ichi2.anki.api.AddContentApi
import com.ly.anki_assist_app.App
import com.ly.anki_assist_app.model.AnkiDeck

const val ANKI_PACKAGE_NAME = "com.ichi2.anki"

class AnkiDroidHelper private constructor() {

    companion object {
        val instance: AnkiDroidHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { AnkiDroidHelper() }
    }

//    private var mAnkiApi: AddContentApi? = null
//
//    fun init(context: Context) {
//        mAnkiApi = AddContentApi(context)
//    }

    fun isApiAvailable(): Boolean{
        return AddContentApi.getAnkiDroidPackageName(App.context) != null
    }

    fun startAnkiDroid(): Boolean{
        val manager = App.context.packageManager
        val intent = manager.getLaunchIntentForPackage(ANKI_PACKAGE_NAME) ?: return false
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        App.context.startActivity(intent)
        return true
    }

    /**
     * 获取到期的卡片列表
     */
    fun getDueDeckList(){
        val decksCursor = App.context.contentResolver.query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null, null) ?: return
        try {

            val deckList = arrayListOf<AnkiDeck>()

            while (decksCursor.moveToNext()) {
                val deckId = decksCursor.getLong(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID))
                val deckName = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME))

                val deckCounts = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS))
                val deckDueCounts = AnkiDeck.parseDeckCounts(deckCounts)

                // 过滤掉 Default 类别
                if(deckName == "Default") {
                    continue
                }

                // 过滤掉没有需要复用的类别
                if( deckDueCounts.getTotal() == 0) {
                    continue
                }

                deckList.add(AnkiDeck(
                    deckId,
                    deckName,
                    deckDueCounts
                ))
            }
            Log.d("AnkiDroidHelper", "${deckList}")
        } finally {
            decksCursor.close()
        }
    }

}