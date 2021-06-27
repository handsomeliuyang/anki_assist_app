package com.ly.anki_assist_app.utils

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

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

        fun convertStreamToString(inputStream: InputStream?): String {
            return try {
                val sb = StringBuilder()

                val rd = BufferedReader(InputStreamReader(inputStream), 4096)
                var line: String?
                while (rd.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                rd.close()

                sb.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}