package com.ly.anki_assist_app.ui.print.options

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.databinding.FragmentPrintOptionsBinding
import com.ly.anki_assist_app.ui.print.preview.ARGUMENT_PRINT_DECKID_ARRAY
import com.ly.anki_assist_app.ui.print.preview.PrintPreviewFragment
import timber.log.Timber

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

        binding.recyclerView.adapter = DeckAadpter(this.requireContext())

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.print_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_options_ok -> navPrintPreview()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navPrintPreview(): Boolean {

        val deckAdapter = binding.recyclerView.adapter as DeckAadpter

        val deckIdList = ArrayList<Long>()
        deckAdapter.parentList
            .filter { it.checked }
            .map { deckParent ->
                deckIdList.add(deckParent.deck.deckId)
                deckParent.children
                    .filter { it.checked }
                    .map { deckChild ->
                        deckIdList.add(deckChild.deck.deckId)
                    }
            }

        Timber.d("selected decks %s", deckIdList.toString())

        val bundle = Bundle()
        bundle.putLongArray(ARGUMENT_PRINT_DECKID_ARRAY, deckIdList.toLongArray())
        NavHostFragment.findNavController(this).navigate(R.id.action_print_options_to_print_preview, bundle)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}