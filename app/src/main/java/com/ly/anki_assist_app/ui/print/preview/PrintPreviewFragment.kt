package com.ly.anki_assist_app.ui.print.preview

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.*
import androidx.core.os.EnvironmentCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.ankidroid.api.AnkiAppApi
import com.ly.anki_assist_app.databinding.PrintPreviewFragmentBinding
import com.ly.anki_assist_app.utils.Status
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

const val BASE_URL = "file:///storage/emulated/0/__viewer__.html"
const val ARGUMENT_PRINT_DECKID_ARRAY = "print_deckid_array"

class PrintPreviewFragment : Fragment() {

    private val BASE_URL = AnkiAppApi.getAnkiMediaUri()
    private lateinit var viewModel: PrintPreviewViewModel
    private var _binding: PrintPreviewFragmentBinding? = null

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

        _binding = PrintPreviewFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initWebView(binding.webview)

        viewModel.dueCardsString.observe(viewLifecycleOwner, Observer {
            when(it.status) {
                Status.SUCCESS -> displayCardQuestion(it.data ?: "")
                Status.ERROR -> showMessage(it.message ?: "请求出错")
                Status.LOADING -> showMessage("加载中...")
            }
        })

        // 获取参数
        val array = arguments?.getLongArray(ARGUMENT_PRINT_DECKID_ARRAY)?.toTypedArray() ?: emptyArray()
        viewModel.setPrintList(array)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.print_preview, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return when(item.itemId) {
            R.id.action_print -> printWebView()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun printWebView(): Boolean {
        val webView = _binding?.webview ?: return true
        val jobName = "print_due_cards_${SimpleDateFormat("yyyy-MM-dd").format(Date())}"
        val printManager = this.activity?.getSystemService(Context.PRINT_SERVICE) as PrintManager? ?: return true
        val printAdapter = webView.createPrintDocumentAdapter(jobName)
        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        return true
    }

    private fun displayCardQuestion(displayString: String) {
        _binding?.webview?.visibility = View.VISIBLE
        _binding?.messageTitle?.visibility = View.GONE

        Timber.d("webview load url = %s", BASE_URL)

        // 显示所有卡片的问题
        _binding?.webview?.loadDataWithBaseURL(
            BASE_URL + "__viewer__.html",
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

    private fun initWebView(webview: MyWebView) {
        webview.settings.apply {
            this.displayZoomControls = false
            this.builtInZoomControls = true
            this.setSupportZoom(true)
            this.loadWithOverviewMode = true
            this.javaScriptEnabled = true
        }
        webview.scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        webview.isScrollbarFadingEnabled = true
        webview.setBackgroundColor(Color.argb(1, 0, 0, 0))
        webview.webViewClient = CardViewerWebClient()
        webview.webChromeClient = AnkiDroidWebChromeClient()
        //        webview.addJavascriptInterface()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}