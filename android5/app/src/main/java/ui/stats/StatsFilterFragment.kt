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

package ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProvider
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

        val radioGroup: RadioGroup = root.findViewById(R.id.radioGroup)
        val currentFilter = viewModel.getFilter()
        val activeButton: RadioButton = root.findViewById(
            when (currentFilter) {
                StatsViewModel.Filter.BLOCKED -> R.id.activity_filterblocked
                StatsViewModel.Filter.ALLOWED -> R.id.activity_filterallowed
                else -> R.id.activity_filterall
            }
        )
        activeButton.isChecked = true
        val cancel: View = root.findViewById(R.id.cancel)
        cancel.setOnClickListener {
            val filter = when (radioGroup.checkedRadioButtonId) {
                R.id.activity_filterblocked -> StatsViewModel.Filter.BLOCKED
                R.id.activity_filterallowed -> StatsViewModel.Filter.ALLOWED
                else -> StatsViewModel.Filter.ALL
            }
            viewModel.filter(filter)
            dismiss()
        }

        return root
    }
}