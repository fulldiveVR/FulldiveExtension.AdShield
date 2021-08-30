package ui.settings.leases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import model.Lease
import org.adshield.R
import ui.AccountViewModel
import ui.TunnelViewModel
import ui.app

class LeasesFragment : Fragment() {

    private lateinit var vm: LeasesViewModel
    private lateinit var accountVM: AccountViewModel
    private lateinit var tunnelVM: TunnelViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it).get(LeasesViewModel::class.java)
            accountVM = ViewModelProvider(it.app()).get(AccountViewModel::class.java)
            tunnelVM = ViewModelProvider(it.app()).get(TunnelViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_leases, container, false)

        val recycler: RecyclerView = root.findViewById(R.id.recyclerview)
        recycler.layoutManager = LinearLayoutManager(context)

        accountVM.account.observe(viewLifecycleOwner, Observer { account ->
            val adapter = LeasesAdapter(interaction = object : LeasesAdapter.Interaction {
                override fun onDelete(lease: Lease) {
                    vm.delete(account.id, lease)
                }

                override fun isThisDevice(lease: Lease): Boolean {
                    return tunnelVM.isMe(lease.public_key)
                }
            })
            recycler.adapter = adapter

            vm.leases.observe(viewLifecycleOwner, Observer {
                adapter.swapData(it)
            })

            vm.fetch(account.id)
        })

        return root
    }
}