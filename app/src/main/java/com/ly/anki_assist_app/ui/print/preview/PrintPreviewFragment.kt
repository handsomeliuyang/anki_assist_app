package com.ly.anki_assist_app.ui.print.preview

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.ankidroid.ui.MyWebView
import com.ly.anki_assist_app.databinding.FragmentPrintPreviewBinding
import com.ly.anki_assist_app.utils.Status
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

const val ARGUMENT_PRINT_DECKS = "print_decks"

class PrintPreviewFragment : Fragment() {

    private lateinit var viewModel: PrintPreviewViewModel
    private var _binding: FragmentPrintPreviewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(this).get(PrintPreviewViewModel::class.java)

        _binding = FragmentPrintPreviewBinding.inflate(inflater, container, false)

        viewModel.deckEntitysStringLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> displayCardQuestion(it.data ?: "")
                Status.ERROR -> showMessage(it.message ?: "请求出错")
                Status.LOADING -> showMessage("加载中...")
            }
        })

        // 获取参数
        val printDecks = arguments?.getParcelableArrayList<PrintDeck>(ARGUMENT_PRINT_DECKS)
            ?: emptyList<PrintDeck>()
        viewModel.setPlanDecks(printDecks)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.print_preview, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_print -> printWebView()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun printWebView(): Boolean {
        val jobName = "print_due_cards_${SimpleDateFormat("yyyy-MM-dd").format(Date())}"

        viewModel.savePrintdata(jobName)

        val webView = _binding?.webview ?: return true

        val printManager =
            this.activity?.getSystemService(Context.PRINT_SERVICE) as PrintManager? ?: return true
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        return true
    }

    private fun displayCardQuestion(displayString: String) {
        _binding?.webview?.visibility = View.VISIBLE
        _binding?.messageTitle?.visibility = View.GONE

        Timber.d("webview load url = %s", MyWebView.BASE_URL)

        // 显示所有卡片的问题
        _binding?.webview?.loadDataWithBaseURL(
            MyWebView.BASE_URL + "__viewer__.html",
            displayString,
            "text/html",
            "utf-8",
            null
        )
    }

    private fun showMessage(msg: String) {
        _binding?.webview?.visibility = View.GONE
        _binding?.messageTitle?.visibility = View.VISIBLE

        _binding?.messageTitle?.text = msg
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}