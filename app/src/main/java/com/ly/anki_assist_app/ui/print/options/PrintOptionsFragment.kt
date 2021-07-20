package com.ly.anki_assist_app.ui.print.options

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.databinding.FragmentPrintOptionsBinding
import com.ly.anki_assist_app.ui.print.preview.ARGUMENT_PRINT_DECKS
import com.ly.anki_assist_app.ui.print.preview.PrintDeck
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
        val parentPrintDecks = deckAdapter.parentList
            .filter { it.checked }
            .map {
                PrintDeck.from(it.deck)
            }
        val childPrintDecks = deckAdapter.parentList
            .flatMap { it.children }
            .filter { it.checked }
            .map {
                PrintDeck.from(it.deck)
            }

        val printDecks = ArrayList<PrintDeck>()
        printDecks.addAll(parentPrintDecks)
        printDecks.addAll(childPrintDecks)

        val bundle = Bundle()
        bundle.putParcelableArrayList(ARGUMENT_PRINT_DECKS, printDecks)
        NavHostFragment.findNavController(this).navigate(R.id.action_print_options_to_print_preview, bundle)
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}