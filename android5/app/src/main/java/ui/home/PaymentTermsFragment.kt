package ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.adshield.R
import ui.AccountViewModel
import ui.BottomSheetFragment
import ui.app
import utils.Links

class PaymentTermsFragment : BottomSheetFragment() {

    private lateinit var vm: AccountViewModel

    companion object {
        fun newInstance() = PaymentTermsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(AccountViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_payment_terms, container, false)
        val nav = findNavController()

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val cancel: View = root.findViewById(R.id.cancel)
        cancel.setOnClickListener {
            dismiss()
        }

        val privacy: View = root.findViewById(R.id.payment_privacy)
        privacy.setOnClickListener {
            nav.navigate(
                HomeFragmentDirections.actionNavigationHomeToWebFragment(
                    Links.privacy, getString(R.string.payment_action_policy)
                )
            )
            dismiss()
        }

        return root
    }

}