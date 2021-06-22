package com.ly.anki_assist_app.utils

import android.content.Context
import java.lang.Exception

class Utils {
    companion object {

        fun isAppInstalled(context: Context?, packageName: String): Boolean {
            try {
                context?.packageManager?.getApplicationInfo(packageName, 0) ?: return false;
                return true;
            } catch (e: Exception) {
                return false;
            }
        }
    }
}