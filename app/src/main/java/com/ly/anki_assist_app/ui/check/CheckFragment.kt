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
import com.ly.anki_assist_app.printroom.CardEntity
import com.ly.anki_assist_app.ui.home.ARGUMENT_PRINT_ID
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        _binding = FragmentCheckBinding.inflate(inflater, container, false).apply {
            this.checkViewModel = viewModel
            this.lifecycleOwner = this@CheckFragment.viewLifecycleOwner
        }

        viewModel.checkCardLiveData.observe(this.viewLifecycleOwner, Observer {
            val checkCard = it.data ?: return@Observer
//            if (it.status == Status.SUCCESS) {
//                if (it.data == null) {
//                    // 检查结束
//                    val context = this.context ?: return@Observer
//                    MaterialAlertDialogBuilder(context)
//                        .setTitle("提示")
//                        .setMessage("已检查完，辛苦了！！！")
//                        .setPositiveButton("确定") { dialog, which ->
//                            this.findNavController().popBackStack()
//                        }
//                        .show()
//                    return@Observer
//                }
                _binding?.checkCard = checkCard
                _binding?.executePendingBindings()
//            }
        })

        viewModel.checkCardString.observe(viewLifecycleOwner, Observer {
            if(it.status == Status.SUCCESS) {
                displayCardQuestion(it.data ?: "")
            }
        })

        // 获取参数
        val printId = arguments?.getInt(ARGUMENT_PRINT_ID, -1) ?: -1
        viewModel.setPrintId(printId)

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        GlobalScope.launch {
            viewModel.savePrint()
        }
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