package com.ly.anki_assist_app.utils

import android.text.Html
import android.text.Spanned
import com.ly.anki_assist_app.ui.home.Overview

class HtmlTextUtils {

    companion object {
        fun getOverviewText(overview: Overview): Spanned {
            return Html.fromHtml("今日需复习 <font color='#FF0000'>${overview.reviewNums}</font> 张，需学习新卡片 <font color='#FF0000'>${overview.newNums}</font> 张")
        }

        fun getPrintInfo(reviewNums: Int): Spanned {
            return Html.fromHtml("简介：复习 <font color='#FF0000'>${reviewNums}</font> 张")
        }
    }
}