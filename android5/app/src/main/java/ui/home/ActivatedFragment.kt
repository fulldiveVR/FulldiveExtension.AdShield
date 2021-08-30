package ui.home

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.adshield.R
import ui.ActivationViewModel
import ui.BottomSheetFragment

class ActivatedFragment : BottomSheetFragment() {

    private lateinit var vm: ActivationViewModel

    companion object {
        fun newInstance() = ActivatedFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it).get(ActivationViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_activated, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val proceed: View = root.findViewById(R.id.activated_continue)
        proceed.setOnClickListener {
            dismiss()
            val fragment = LocationFragment.newInstance()
            fragment.show(parentFragmentManager, null)
        }

        return root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        vm.setInformedUserAboutActivation()
    }

}