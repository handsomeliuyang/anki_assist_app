package com.ly.anki_assist_app.printroom

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

const val PRINT_STATE_NONE_CHECK = 1

@Entity(tableName = "print_table")
data class PrintEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val time: Date?,
    val state: Int?,
    val category: String?,
    val reviewCount: Int?,
    val cardIdAndStateList: List<CardIdAndState>
)

data class CardIdAndState(
    val noteId: Long,
    val cardOrd: Int,
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

