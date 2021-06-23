package com.ly.anki_assist_app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ly.anki_assist_app.utils.AnkiDroidHelper

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    fun loadDecks() {

        AnkiDroidHelper.instance.getDueDeckList()

    }
}