package com.ly.anki_assist_app.ui.card.coach

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.ankidroid.ui.MyWebView
import com.ly.anki_assist_app.databinding.FragmentCoachBinding
import com.ly.anki_assist_app.ui.card.CARD_STATE_ANSWER
import com.ly.anki_assist_app.ui.card.CARD_STATE_QUESTION
import com.ly.anki_assist_app.ui.home.ARGUMENT_PRINT_ID
import com.ly.anki_assist_app.utils.Status
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CoachFragment : Fragment() {

    private lateinit var viewModel: CoachViewModel
    private var _binding: FragmentCoachBinding? = null

    private val binding get() = _binding!!

    private var _hasStrengthenMemory: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(CoachViewModel::class.java)
        _binding = FragmentCoachBinding.inflate(inflater, container, false).apply {
            this.coachViewModel = viewModel
            this.lifecycleOwner = this@CoachFragment.viewLifecycleOwner
        }

        viewModel.printLiveData.observe(this.viewLifecycleOwner, Observer {
            _hasStrengthenMemory = it.data?.hasStrengthenMemory ?: false
            this.activity?.invalidateOptionsMenu()
        })

        viewModel.curUICardLiveData.observe(viewLifecycleOwner, Observer {
            val uiCard = it.data ?: return@Observer
            _binding?.uiCard = uiCard
            _binding?.executePendingBindings()
        })

        viewModel.curUICardString.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                displayCardQuestion(it.data ?: "")
            }
        })

        viewModel.curCardState.observe(this.viewLifecycleOwner, Observer {
            val cardState = it ?: CARD_STATE_QUESTION
            _binding?.cardSwitch?.text = if (cardState == CARD_STATE_ANSWER) "??????" else "??????"
        })

        viewModel.coachFinishLivedata.observe(viewLifecycleOwner, Observer {
            when(it.status){
                Status.ERROR -> showError(it.message?:"")
                Status.LOADING -> showLoading()
                Status.SUCCESS -> showSuccess()
            }
        })

        // ????????????
        val printId = arguments?.getInt(ARGUMENT_PRINT_ID, -1) ?: -1
        viewModel.setPrintId(printId)

        return _binding?.root
    }

    private fun displayCardQuestion(displayString: String) {
        // ???????????????????????????
        _binding?.webview?.loadDataWithBaseURL(
            MyWebView.BASE_URL + "__viewer__.html",
            displayString,
            "text/html",
            "utf-8",
            null
        )
    }

    private var _alertDialog: AlertDialog? = null
    private fun showSuccess() {
        val context = this.context ?: return
        if (_alertDialog?.isShowing == true) {
            _alertDialog?.dismiss()
        }
        _alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("??????")
            .setMessage("?????????????????????????????????????????????")
            .setPositiveButton("??????") { dialog, which ->
                this.findNavController().popBackStack()
            }
            .show()
    }

    private fun showLoading() {
        val context = this.context ?: return
        if (_alertDialog?.isShowing == true) {
            _alertDialog?.dismiss()
        }
        _alertDialog = AlertDialog.Builder(context)
            .setView(R.layout.dialog_loading)
            .setCancelable(true)
            .show()
    }

    private fun showError(errorMsg: String) {
        val context = this.context ?: return
        if (_alertDialog?.isShowing == true) {
            _alertDialog?.dismiss()
        }
        _alertDialog = MaterialAlertDialogBuilder(context)
            .setTitle("??????")
            .setMessage(errorMsg)
            .setPositiveButton("??????") { dialog, which ->
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.coach, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val finishItem = menu.findItem(R.id.action_finish)

        if(_hasStrengthenMemory) {
            finishItem.setEnabled(false)
            finishItem.setTitle("???????????????")
        } else {
            finishItem.setEnabled(true)
            finishItem.setTitle("??????")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_finish -> finish()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun finish(): Boolean {
        viewModel.coachFinished()
        return true
    }

}