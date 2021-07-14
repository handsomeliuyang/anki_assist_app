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
import com.ly.anki_assist_app.ankidroid.model.DeckDueCounts
import com.ly.anki_assist_app.databinding.ItemDeckBinding
import timber.log.Timber
import java.util.*

class DeckAadpter(context: Context) :
    ExpandableRecyclerAdapter<DeckParent, DeckChild, DeckParentViewHolder, DeckChildViewHolder>(
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
            this, mExpandImage, mCollapseImage,
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
            this,
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
        parentViewHolder.bind(parent, parentPosition)
    }

    override fun onBindChildViewHolder(
        childViewHolder: DeckChildViewHolder,
        parentPosition: Int,
        childPosition: Int,
        child: DeckChild
    ) {
        childViewHolder.bind(parentList.get(parentPosition), child, parentPosition)
    }

}

class DeckParent(val deck: AnkiDeck, var checked: Boolean = false, val children: MutableList<DeckChild>) : Parent<DeckChild> {

    fun getDueCounts(): DeckDueCounts {
        val dueCounts = DeckDueCounts(0, 0, 0)
        dueCounts.add(deck.deckDueCounts)
        children.map {
            dueCounts.add(it.deck.deckDueCounts)
        }
        return dueCounts
    }

    override fun getChildList(): MutableList<DeckChild> {
        return children
    }

    override fun isInitiallyExpanded(): Boolean {
        return false
    }

}

class DeckChild(val deck: AnkiDeck, var checked: Boolean){

}

class DeckParentViewHolder(val adpter: DeckAadpter, val expandImage: Drawable, val collapseImage: Drawable, val binding: ItemDeckBinding) :
    ParentViewHolder<DeckParent, DeckChild>(binding.root) {
    fun bind(deckParent: DeckParent, parentPosition: Int) {
        binding.checked = deckParent.checked
        binding.deck = deckParent.deck
        binding.dueCounts = deckParent.getDueCounts()

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

        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) { // 只处理点击后的状态改变
                deckParent.checked = isChecked
                deckParent.children.map {
                    it.checked = isChecked
                }
                adpter.notifyChildRangeChanged(parentPosition, 0, deckParent.children.size)
            }
        }
    }

    override fun onExpansionToggled(expanded: Boolean) {
        super.onExpansionToggled(expanded)
        val drawable = if (expanded) collapseImage else expandImage
        binding.deckpickerExpander.setImageDrawable(drawable)
    }
}

class DeckChildViewHolder(val adpter: DeckAadpter, val binding: ItemDeckBinding) :
    ChildViewHolder<DeckChild>(binding.root) {
    fun bind(deckParent: DeckParent, deckChild: DeckChild, parentPosition: Int) {
        binding.checked = deckChild.checked
        binding.deck = deckChild.deck
        binding.dueCounts = deckChild.deck.deckDueCounts
        binding.deckpickerExpander.visibility = View.INVISIBLE

        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) { // 只处理点击后的状态改变
                deckChild.checked = isChecked

                if(!deckParent.checked && isChecked) {
                    deckParent.checked = true
                    adpter.notifyParentChanged(parentPosition)
                }
            }
        }
    }
}