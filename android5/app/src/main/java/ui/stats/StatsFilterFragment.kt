package ui.stats

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.adshield.R
import ui.BottomSheetFragment
import ui.StatsViewModel
import ui.app

class StatsFilterFragment : BottomSheetFragment() {

    companion object {
        fun newInstance() = StatsFilterFragment()
    }

    private lateinit var viewModel: StatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            viewModel = ViewModelProvider(it.app()).get(StatsViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_stats_filter, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val cancel: View = root.findViewById(R.id.cancel)
        cancel.setOnClickListener {
            dismiss()
        }

        val all: View = root.findViewById(R.id.activity_filterall)
        all.setOnClickListener {
            viewModel.filter(StatsViewModel.Filter.ALL)
            dismiss()
        }

        val blocked: View = root.findViewById(R.id.activity_filterblocked)
        blocked.setOnClickListener {
            viewModel.filter(StatsViewModel.Filter.BLOCKED)
            dismiss()
        }

        val allowed: View = root.findViewById(R.id.activity_filterallowed)
        allowed.setOnClickListener {
            viewModel.filter(StatsViewModel.Filter.ALLOWED)
            dismiss()
        }

        return root
    }

}