package com.ly.anki_assist_app.ui.print.options

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.bignerdranch.expandablerecyclerview.ChildViewHolder
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter
import com.bignerdranch.expandablerecyclerview.ParentViewHolder
import com.bignerdranch.expandablerecyclerview.model.Parent
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.ankidroid.model.AnkiDeck
import com.ly.anki_assist_app.databinding.ItemDeckBinding

class DeckAadpter(context: Context) :
    ExpandableRecyclerAdapter<DeckParent, AnkiDeck, DeckParentViewHolder, DeckChildViewHolder>(
        emptyList()
    ) {

    private val mExpandImage: Drawable
    private val mCollapseImage: Drawable
    private val mNoExpander: Drawable = ColorDrawable(Color.TRANSPARENT)

    init {
        mExpandImage = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_expand_more_black_24dp, context.theme)!!
        mCollapseImage = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_chevron_right_black_24dp, context.theme)!!
    }

    override fun onCreateParentViewHolder(
        parentViewGroup: ViewGroup,
        viewType: Int
    ): DeckParentViewHolder {
        return DeckParentViewHolder(
            mExpandImage, mCollapseImage,
            ItemDeckBinding.inflate(
                LayoutInflater.from(
                    parentViewGroup.context
                ),
                parentViewGroup,
                false
            )
        )
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup,
        viewType: Int
    ): DeckChildViewHolder {
        return DeckChildViewHolder(
            ItemDeckBinding.inflate(
                LayoutInflater.from(childViewGroup.context),
                childViewGroup,
                false
            )
        )
    }

    override fun onBindParentViewHolder(
        parentViewHolder: DeckParentViewHolder,
        parentPosition: Int,
        parent: DeckParent
    ) {
        parentViewHolder.bind(parent)
    }

    override fun onBindChildViewHolder(
        childViewHolder: DeckChildViewHolder,
        parentPosition: Int,
        childPosition: Int,
        child: AnkiDeck
    ) {
        childViewHolder.bind(child)
    }

}

class DeckParent(val deck: AnkiDeck, val children: MutableList<AnkiDeck>) : Parent<AnkiDeck> {

    override fun getChildList(): MutableList<AnkiDeck> {
        return children
    }

    override fun isInitiallyExpanded(): Boolean {
        return false
    }

}

class DeckParentViewHolder(val expandImage: Drawable, val collapseImage: Drawable, val binding: ItemDeckBinding) :
    ParentViewHolder<DeckParent, AnkiDeck>(binding.root) {
    fun bind(deckParent: DeckParent) {

        binding.deck = deckParent.deck

        if (deckParent.children.isEmpty()) {
            binding.deckpickerExpander.visibility = View.INVISIBLE
        } else {
            binding.deckpickerExpander.visibility = View.VISIBLE
            binding.deckpickerExpander.setOnClickListener {
                if (isExpanded) {
                    collapseView()
                } else {
                    expandView()
                }
            }
        }
        binding.deckpickerExpander.setImageDrawable(collapseImage)
    }

    override fun onExpansionToggled(expanded: Boolean) {
        super.onExpansionToggled(expanded)
        val drawable = if (expanded) collapseImage else expandImage
        binding.deckpickerExpander.setImageDrawable(drawable)
    }
}

class DeckChildViewHolder(val binding: ItemDeckBinding) :
    ChildViewHolder<AnkiDeck>(binding.root) {
    fun bind(deck: AnkiDeck) {
        binding.deck = deck
        binding.deckpickerExpander.visibility = View.INVISIBLE
    }
}