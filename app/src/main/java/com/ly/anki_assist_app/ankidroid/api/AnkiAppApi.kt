package com.ly.anki_assist_app.ankidroid.api

import android.content.Intent
import com.ichi2.anki.api.AddContentApi
import com.ly.anki_assist_app.App

const val ANKI_PACKAGE_NAME = "com.ichi2.anki"

class AnkiAppApi {
    companion object {
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
    }
}