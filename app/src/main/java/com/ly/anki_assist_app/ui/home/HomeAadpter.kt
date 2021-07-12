package com.ly.anki_assist_app.ui.home

import android.content.Context
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.databinding.HomeItemOverviewBinding
import com.ly.anki_assist_app.databinding.HomeItemPrintBinding
import com.ly.anki_assist_app.printroom.PrintEntity
import com.ly.anki_assist_app.utils.HtmlTextUtils
import timber.log.Timber

class HomeAadpter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_OVERVIEW = 1
        const val VIEW_TYPE_PRINT = 2
    }

    private var overview: Overview = Overview.empty()
    private var list: List<PrintEntity> = emptyList()

    fun updateOverview(overview: Overview){
        this.overview = overview
        notifyItemChanged(0)
    }

    fun updatePrint(list: List<PrintEntity>){
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_OVERVIEW) {
            return OverviewViewHolder(HomeItemOverviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
        return PrintViewHolder(HomeItemPrintBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == VIEW_TYPE_OVERVIEW) {
            (holder as OverviewViewHolder).bind(overview)
        } else {
            (holder as PrintViewHolder).bind(list[position - 1])
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0) {
            return VIEW_TYPE_OVERVIEW
        }
        return VIEW_TYPE_PRINT
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    private inner class OverviewViewHolder(val binding: HomeItemOverviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(overview: Overview) {
            binding.htmlTextUtilsStatic = HtmlTextUtils.Companion
            binding.overView = overview
            binding.printBtn.setOnClickListener {
                it.findNavController().navigate(R.id.action_home_to_print_options)
            }
            binding.executePendingBindings()
        }

    }

    private inner class PrintViewHolder(val binding: HomeItemPrintBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(printEntity: PrintEntity) {
            binding.htmlTextUtilsStatic = HtmlTextUtils.Companion
            binding.printEntity = printEntity
            binding.executePendingBindings()
        }

    }

}

