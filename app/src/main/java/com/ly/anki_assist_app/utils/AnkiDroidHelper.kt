package com.ly.anki_assist_app.utils

import android.content.Context
import android.util.Log
import com.ichi2.anki.api.AddContentApi
import com.ly.anki_assist_app.App

const val ANKI_PACKAGE_NAME = "com.ichi2.anki"

class AnkiDroidHelper private constructor() {

    companion object {
        val instance: AnkiDroidHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { AnkiDroidHelper() }
    }

    private var mAnkiApi: AddContentApi? = null

    fun init(context: Context) {
        mAnkiApi = AddContentApi(context)
    }

    fun isApiAvailable(): Boolean{
        return AddContentApi.getAnkiDroidPackageName(App.context) != null
    }

    fun startAnkiDroid(): Boolean{
        val manager = App.context.getPackageManager()
        val intent = manager.getLaunchIntentForPackage(ANKI_PACKAGE_NAME)
        if(intent == null){
            return false
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        App.context.startActivity(intent)
        return true
    }

    fun getDeckList(){
        val deckList = mAnkiApi?.deckList

        Log.d("AnkiDroidHelper", "${deckList}")
    }

}