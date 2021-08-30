package ui.advanced.apps

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.adshield.R
import ui.BottomSheetFragment

class AppsFilterFragment : BottomSheetFragment() {

    companion object {
        fun newInstance() = AppsFilterFragment()
    }

    private lateinit var viewModel: AppsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.run {
            viewModel = ViewModelProvider(this).get(AppsViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_apps_filter, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val cancel: View = root.findViewById(R.id.cancel)
        cancel.setOnClickListener {
            dismiss()
        }

        val all: View = root.findViewById(R.id.app_filterall)
        all.setOnClickListener {
            viewModel.filter(AppsViewModel.Filter.ALL)
            dismiss()
        }

        val blocked: View = root.findViewById(R.id.app_filterbypassed)
        blocked.setOnClickListener {
            viewModel.filter(AppsViewModel.Filter.BYPASSED)
            dismiss()
        }

        val allowed: View = root.findViewById(R.id.app_filternotbypassed)
        allowed.setOnClickListener {
            viewModel.filter(AppsViewModel.Filter.NOT_BYPASSED)
            dismiss()
        }

        val toggle: View = root.findViewById(R.id.app_togglesystem)
        toggle.setOnClickListener {
            viewModel.switchBypassForAllSystemApps()
            dismiss()
        }

        return root
    }

}