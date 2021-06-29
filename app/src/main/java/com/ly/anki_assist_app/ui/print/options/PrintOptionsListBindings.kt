package com.ly.anki_assist_app.ui.print.options

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter("binding:items")
fun setItems(listView: RecyclerView, items: List<DeckParent>?) {
    items?.let {
        (listView.adapter as DeckAadpter).setParentList(items, false)
    }
}