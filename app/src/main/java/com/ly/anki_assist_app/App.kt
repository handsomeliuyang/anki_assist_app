package com.ly.anki_assist_app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.ly.anki_assist_app.utils.AnkiDroidHelper

class App : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        App.context = this.applicationContext

        AnkiDroidHelper.instance.init(this.applicationContext)
    }

}