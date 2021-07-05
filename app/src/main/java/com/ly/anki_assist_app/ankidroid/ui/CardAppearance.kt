package com.ly.anki_assist_app.ankidroid.ui

import com.ly.anki_assist_app.App
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.utils.Utils

class CardAppearance {
    companion object {

        fun displayString(cardList: List<AnkiCard>): String {
            // 加载卡片布局模板
            val printTemplate = loadPrintTemplate()

            val content = StringBuffer()
            for ((index, card) in cardList.withIndex()) {
                content.append(
                    printCard(
                        index,
                        cardList.size,
                        card.cardQA.questionContent,
                        card.cardQA.answerContent
                    )
                )
            }

            return printTemplate.replace("::content::", content.toString())
        }

        private fun printCard(index: Int, total: Int, question: String, answer: String): String {
//            return " <tr class=\"print_tr\">" +
//                        "<td class=\"print_td\">${index+1}/${total}</td>" +
//                        "<td class=\"print_td\">${question}</td>" +
//                        "<td class=\"print_td\"><div class=\"answer_td\">${answer}</div></td>" +
//                    "</tr>"
            return "<tr class=\"print_tr\">" +
                        "<td class=\"print_td\">${index+1}/${total}</td>" +
                        "<td class=\"print_td\">" +
                            question +
                            "<div style=\"height: 70px;\"></div>" +
                        "</td>" +
                    "</tr>"
        }

        private fun loadPrintTemplate(): String{
            return Utils.convertStreamToString(App.context.assets.open("print_template.html"))
        }

    }
}