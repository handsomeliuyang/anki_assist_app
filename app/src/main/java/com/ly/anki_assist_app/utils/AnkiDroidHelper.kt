package com.ly.anki_assist_app.utils

import android.content.Context
import android.util.Log
import com.ichi2.anki.api.AddContentApi
import com.ly.anki_assist_app.App

class AnkiDroidHelper private constructor() {

    companion object {
        val instance: AnkiDroidHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { AnkiDroidHelper() }
    }

    private var mAnkiApi: AddContentApi? = null

    fun init(context: Context) {
        mAnkiApi = AddContentApi(context)
    }

    fun getDeckList(){
        val deckList = mAnkiApi?.deckList

        Log.d("AnkiDroidHelper", "${deckList}")
    }

}