package com.ly.anki_assist_app.ankidroid.ui

import com.ly.anki_assist_app.App
import com.ly.anki_assist_app.ankidroid.model.AnkiCard
import com.ly.anki_assist_app.ui.print.preview.PrintDeckCards
import com.ly.anki_assist_app.utils.Utils

class CardAppearance {
    companion object {

        fun displayCheckString(answerContent: String): String {
            // 加载卡片布局模板
            val printTemplate = loadPrintTemplate()

            val content = StringBuffer()

            // 添加头部
            content.append(
                "<table class=\"print_table\">\n"
            )

            // 添加内容
            content.append(
                "<tr class=\"print_tr\">" +
                    "<td class=\"print_td\">" +
                        answerContent +
                    "</td>" +
                "</tr>"
            )

            // 添加table的尾部
            content.append("</table>")

            return printTemplate.replace("::content::", content.toString())
        }

        fun displayPrintString(list: List<PrintDeckCards>): String {
            // 加载卡片布局模板
            val printTemplate = loadPrintTemplate()

            val content = StringBuffer()

            // 添加头部
            content.append("<h2>温故而知新</h2>\n" +
                    "<h3>苟有恒，何必三更起五更眠；最无益，莫过一日曝十日寒</h3>\n" +
                    "<table class=\"print_table\">\n" +
                    "   <tr class=\"print_tr\">\n" +
                    "       <th width=\"50\" class=\"print_th\">序号</th>\n" +
                    "       <th width=\"100%\" class=\"print_th\">问题</th>\n" +
                    "   </tr>\n")

            for (printDeckCards in list) {
                for ((index, card) in printDeckCards.cards.withIndex()) {
                    content.append(
                        printCard(
                            printDeckCards.printDeck.name,
                            index,
                            printDeckCards.cards.size,
                            card.cardQA.questionContent,
                            card.cardQA.answerContent
                        )
                    )
                }
            }

            // 添加table的尾部
            content.append("</table>")

            return printTemplate.replace("::content::", content.toString())
        }

        private fun printCard(deckName: String, index: Int, total: Int, question: String, answer: String): String {
            return "<tr class=\"print_tr\">" +
                        "<td class=\"print_td\">${deckName}<br>${index+1}/${total}</td>" +
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