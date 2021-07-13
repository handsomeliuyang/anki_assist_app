package com.ly.anki_assist_app.printroom

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ichi2.anki.FlashCardsContract
import java.util.*

@Entity(tableName = "print_table")
data class PrintEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val time: Date?,
    val state: Int?,
    val reviewCount: Int?,
    val cardIdAndStateList: List<CardIdAndState>
) {

    companion object {
        const val STATE_NONE_CHECK = 1
        const val STATE_NONE_COACH = 2
        const val STATE_FINISHED = 3
    }

    fun getStateText(): String{
        return when(state) {
            STATE_NONE_CHECK -> "未检查"
            STATE_NONE_COACH -> "未辅导"
            STATE_FINISHED -> "完成"
            else -> "异常"
        }
    }
}

data class CardIdAndState(
    val noteId: Long,
    val cardOrd: Int,
    val buttonCount: Int,
    val nextReviewTimesString: String,

    val studyState: Int,
    val parentState: Int
) {
    companion object {
        const val STUDY_STATE_INIT = 0
        const val STUDY_STATE_ERROR = 1
        const val STUDY_STATE_RIGHT = 2

        const val PARENT_STATE_INIT = 0
        const val PARENT_STATE_CHECKED = 1
        const val PARENT_STATE_COACH = 2
    }
}

