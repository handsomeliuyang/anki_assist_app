package com.ly.anki_assist_app.ui.card.check

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.ankidroid.api.AnkiAppApi
import com.ly.anki_assist_app.ankidroid.ui.MyWebView
import com.ly.anki_assist_app.databinding.FragmentCheckBinding
import com.ly.anki_assist_app.ui.card.CARD_STATE_ANSWER
import com.ly.anki_assist_app.ui.home.ARGUMENT_PRINT_ID
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class CheckFragment : Fragment() {

    private lateinit var viewModel: CheckViewModel
    private var _binding: FragmentCheckBinding? = null

    private val binding get() = _binding!!

    private var _hasCheckAndSyncAnki: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this).get(CheckViewModel::class.java)
        _binding = FragmentCheckBinding.inflate(inflater, container, false).apply {
            this.checkViewModel = viewModel
            this.lifecycleOwner = this@CheckFragment.viewLifecycleOwner
        }

        viewModel.printLiveData.observe(this.viewLifecycleOwner, Observer {
            _hasCheckAndSyncAnki = it.data?.hasCheckAndSyncAnki ?: false
            this.activity?.invalidateOptionsMenu()
        })

        viewModel.curUICardLiveData.observe(this.viewLifecycleOwner, Observer {
            val uiCard = it.data ?: return@Observer
            _binding?.uiCard = uiCard
            _binding?.executePendingBindings()
        })

        viewModel.curUICardString.observe(this.viewLifecycleOwner, Observer {
            if(it.status == Status.SUCCESS) {
                displayCardQuestion(it.data ?: "")
            }
        })

        viewModel.curCardState.observe(this.viewLifecycleOwner, Observer {
            val cardState = it ?: CARD_STATE_ANSWER
            _binding?.cardSwitch?.text = if (cardState == CARD_STATE_ANSWER) "问题" else "答案"
        })

        // 监听同步状态，显示弹窗
        viewModel.syncAnkiLivedata.observe(this.viewLifecycleOwner, Observer {
            when(it.status){
                Status.ERROR -> showSyncError(it.message?:"")
                Status.LOADING -> showSyncLoading()
                Status.SUCCESS -> showSyncSuccess()
            }
        })

        // 获取参数
        val printId = arguments?.getInt(ARGUMENT_PRINT_ID, -1) ?: -1
        viewModel.setPrintId(printId)

        return binding.root
    }

    private var _syncAlertDialog: AlertDialog? = null

    private fun showSyncState(){
        val context = this.context ?: return
        if (_syncAlertDialog?.isShowing == true) {
            _syncAlertDialog?.dismiss()
        }
        _syncAlertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("声明")
            .setMessage("同步之前，请确保已打开Anki ？")
            .setNegativeButton("同步") { dialog, which ->
                // 同步数据库，同步Anki
                viewModel.syncAnki()
            }
            .setPositiveButton("调起Anki") { dialog, which ->
                AnkiAppApi.startAnkiDroid()
            }
            .show()
        // 重写listener，防止dismiss
        _syncAlertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            AnkiAppApi.startAnkiDroid()
        }
    }

    private fun showSyncSuccess() {
        val context = this.context ?: return
        if (_syncAlertDialog?.isShowing == true) {
            _syncAlertDialog?.dismiss()
        }
        _syncAlertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("成功")
            .setMessage("检查完成，同步成功！！！\n请求打开Anki同步，并把学习中的卡片点掉。")
            .setNegativeButton("回到首页") { dialog, which ->
                this.findNavController().popBackStack()
            }
            .setPositiveButton("调起Anki") { dialog, which ->
                AnkiAppApi.startAnkiDroid()
            }
            .show()
        // 重写listener，防止dismiss
        _syncAlertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            AnkiAppApi.startAnkiDroid()
        }
    }

    private fun showSyncLoading() {
        val context = this.context ?: return
        if (_syncAlertDialog?.isShowing == true) {
            _syncAlertDialog?.dismiss()
        }
        _syncAlertDialog = AlertDialog.Builder(context)
            .setView(R.layout.dialog_loading)
            .setCancelable(true)
            .show()
    }

    private fun showSyncError(errorMsg: String) {
        val context = this.context ?: return
        if (_syncAlertDialog?.isShowing == true) {
            _syncAlertDialog?.dismiss()
        }
        _syncAlertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("错误")
            .setMessage(errorMsg)
            .setPositiveButton("确定") { dialog, which ->
                dialog.dismiss()
            }
            .show()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.check, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val finishItem = menu.findItem(R.id.action_check_finish)

        if(_hasCheckAndSyncAnki) {
            finishItem.setEnabled(false)
            finishItem.setTitle("已检查")
        } else {
            finishItem.setEnabled(true)
            finishItem.setTitle("完成")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_check_finish -> checkFinish()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkFinish(): Boolean {
        showSyncState()
        return true
    }
}