package com.ly.anki_assist_app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import timber.log.Timber

class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        App.context = this.applicationContext

        // 初始化日志库
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else CrashReportingTree())
    }
}

private class CrashReportingTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }
        // TODO-ly 把Error和Warn日志上报到后台
    }

}