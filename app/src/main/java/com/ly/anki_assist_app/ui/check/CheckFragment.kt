package com.ly.anki_assist_app.ui.check

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.ankidroid.ui.MyWebView
import com.ly.anki_assist_app.databinding.FragmentCheckBinding
import com.ly.anki_assist_app.ui.home.ARGUMENT_PRINT_ID
import com.ly.anki_assist_app.ui.print.preview.ARGUMENT_PRINT_DECKID_ARRAY
import com.ly.anki_assist_app.utils.Status

class CheckFragment : Fragment() {

    private lateinit var viewModel: CheckViewModel
    private var _binding: FragmentCheckBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(CheckViewModel::class.java)
        _binding = FragmentCheckBinding.inflate(inflater, container, false)

        viewModel.print.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                viewModel.resetIndex()
            }
        })

        viewModel.checkCard.observe(this.viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                _binding?.processText?.text = it.data?.processShow()
                _binding?.errorBtn?.text = it.data?.errorBtnShow()
                _binding?.rightBtn?.text = it.data?.rightBtnShow()
                _binding?.easyBtn?.text = it.data?.easyBtnShow()
            }
        })

        viewModel.checkCardString.observe(viewLifecycleOwner, Observer {
            if(it.status == Status.SUCCESS) {
                displayCardQuestion(it.data ?: "")
            }
        })

        // 出错的Btn
        binding.errorBtn.setOnClickListener {

        }
        // 正确的Btn
        binding.rightBtn.setOnClickListener {

        }
        // 容易的Btn
        binding.easyBtn.setOnClickListener {

        }

        // 获取参数
        val printId = arguments?.getInt(ARGUMENT_PRINT_ID, -1) ?: -1
        viewModel.setPrintId(printId)

        return binding.root
    }

    private fun displayCardQuestion(displayString: String) {
        // 显示所有卡片的问题
        _binding?.webview?.loadDataWithBaseURL(
            MyWebView.BASE_URL + "__viewer__.html",
            displayString,
            "text/html",
            "utf-8",
            null
        )
    }


}