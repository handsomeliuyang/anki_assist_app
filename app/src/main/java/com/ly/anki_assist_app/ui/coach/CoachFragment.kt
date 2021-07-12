package com.ly.anki_assist_app.ui.coach

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ly.anki_assist_app.R

class CoachFragment : Fragment() {

    companion object {
        fun newInstance() = CoachFragment()
    }

    private lateinit var viewModel: CoachViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_coach, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CoachViewModel::class.java)
        // TODO: Use the ViewModel
    }

}