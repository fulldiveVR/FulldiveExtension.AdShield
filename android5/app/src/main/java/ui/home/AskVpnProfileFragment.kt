/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2022 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import org.adshield.R
import service.VpnPermissionService
import ui.BottomSheetFragment
import ui.TunnelViewModel
import ui.app

class AskVpnProfileFragment : BottomSheetFragment() {

    companion object {
        fun newInstance() = AskVpnProfileFragment()
    }

    private val vpnPerm = VpnPermissionService
    private lateinit var vm: TunnelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            vm = ViewModelProvider(it.app()).get(TunnelViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_vpnprofile, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }

        val vpnContinue: View = root.findViewById(R.id.vpnperm_continue)
        vpnContinue.setOnClickListener {
            vpnPerm.askPermission()
        }

        vpnPerm.onPermissionGranted = {
            vm.turnOn()
            dismiss()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vpnPerm.onPermissionGranted = {}
    }

}