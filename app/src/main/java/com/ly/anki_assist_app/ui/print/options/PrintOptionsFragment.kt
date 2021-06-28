package com.ly.anki_assist_app.ui.print.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ly.anki_assist_app.databinding.FragmentPrintOptionsBinding

class PrintOptionsFragment : Fragment() {

    private lateinit var viewModel: PrintOptionsViewModel
    private var _binding: FragmentPrintOptionsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(PrintOptionsViewModel::class.java)

        _binding = FragmentPrintOptionsBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = this@PrintOptionsFragment.viewLifecycleOwner
        }
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // View创建后，再设置lifecycle，才会开始监听LiveData
//        binding.lifecycleOwner = this.viewLifecycleOwner
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}