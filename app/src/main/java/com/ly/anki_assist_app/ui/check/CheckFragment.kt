package com.ly.anki_assist_app.ui.check

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ly.anki_assist_app.R

class CheckFragment : Fragment() {

    companion object {
        fun newInstance() = CheckFragment()
    }

    private lateinit var viewModel: CheckViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_check, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CheckViewModel::class.java)
        // TODO: Use the ViewModel
    }

}