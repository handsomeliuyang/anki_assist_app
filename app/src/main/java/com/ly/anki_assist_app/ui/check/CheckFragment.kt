package com.ly.anki_assist_app.ui.check

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ly.anki_assist_app.ankidroid.ui.MyWebView
import com.ly.anki_assist_app.databinding.FragmentCheckBinding
import com.ly.anki_assist_app.ui.home.ARGUMENT_PRINT_ID
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
                if (it.data == null) {
                    // 检查结束
                    val context = this.context ?: return@Observer
                    MaterialAlertDialogBuilder(context)
                        .setTitle("提示")
                        .setMessage("已检查完，辛苦了！！！")
                        .setPositiveButton("确定") { dialog, which ->
                            this.findNavController().popBackStack()
                        }
                        .show()
                    return@Observer
                }
                val checkCard = it.data

                _binding?.processText?.text = checkCard.processShow()
                _binding?.errorBtn?.text = checkCard.errorBtnShow()
                _binding?.rightBtn?.text = checkCard.rightBtnShow()

                if(checkCard.easyButtonIndex == -1) {
                    _binding?.easyBtn?.visibility = View.GONE
                } else {
                    _binding?.easyBtn?.visibility = View.VISIBLE
                    _binding?.easyBtn?.text = checkCard.easyBtnShow()
                }
            }
        })

        viewModel.checkCardString.observe(viewLifecycleOwner, Observer {
            if(it.status == Status.SUCCESS) {
                displayCardQuestion(it.data ?: "")
            }
        })

        // 出错的Btn
        binding.errorBtn.setOnClickListener {
            viewModel.answerCardOnError()
        }
        // 正确的Btn
        binding.rightBtn.setOnClickListener {
            viewModel.answerCardOnRight()
        }
        // 容易的Btn
        binding.easyBtn.setOnClickListener {
            viewModel.answerCardOnEasy()
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