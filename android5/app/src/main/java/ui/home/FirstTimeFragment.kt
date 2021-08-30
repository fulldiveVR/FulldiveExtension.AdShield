package ui.home

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import org.adshield.R
import ui.BottomSheetFragment
import utils.Links

class FirstTimeFragment : BottomSheetFragment() {

    companion object {
        fun newInstance() = FirstTimeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_firsttime, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val more: View = root.findViewById(R.id.firsttime_more)
        more.setOnClickListener {
            dismiss()
            val nav = findNavController()
            nav.navigate(
                HomeFragmentDirections.actionNavigationHomeToWebFragment(
                    Links.intro, getString(R.string.intro_header)
                )
            )
        }

        val firstTimeContinue: View = root.findViewById(R.id.firsttime_continue)
        firstTimeContinue.setOnClickListener {
            dismiss()
        }

        return root
    }

}