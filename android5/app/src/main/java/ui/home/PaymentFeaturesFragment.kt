package ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.adshield.R
import ui.AccountViewModel
import ui.BottomSheetFragment
import ui.app
import ui.settings.SettingsFragmentDirections
import utils.Links

class PaymentFeaturesFragment : BottomSheetFragment(skipCollapsed = false) {

    private lateinit var vm: AccountViewModel

    companion object {
        fun newInstance() = PaymentFeaturesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(AccountViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_payment_features, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
            val fragment = PaymentFragment.newInstance()
            fragment.show(parentFragmentManager, null)
        }

        val paymentContinue: View = root.findViewById(R.id.payment_continue)
        paymentContinue.setOnClickListener {
            dismiss()
            val fragment = PaymentFragment.newInstance()
            fragment.show(parentFragmentManager, null)
        }

        return root
    }

}