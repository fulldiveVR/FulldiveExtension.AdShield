/*
 * This file is part of Blokada.
 *
 * Blokada is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Blokada is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright © 2020 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import model.HistoryEntryType
import org.blokada.R
import ui.StatsViewModel
import ui.app
import ui.advanced.packs.OptionView
import ui.utils.AndroidUtils


class StatsDetailFragment : Fragment() {

    companion object {
        fun newInstance() = StatsDetailFragment()
    }

    private val args: StatsDetailFragmentArgs by navArgs()

    private lateinit var viewModel: StatsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            viewModel = ViewModelProvider(it.app()).get(StatsViewModel::class.java)
        }

        val root =  inflater.inflate(R.layout.fragment_stats_detail, container, false)

        val icon: ImageView = root.findViewById(R.id.activity_icon)
        val name: TextView = root.findViewById(R.id.activity_name)
        val comment: TextView = root.findViewById(R.id.activity_comment)
        val fullName: TextView = root.findViewById(R.id.activity_fullname)
        val time: TextView = root.findViewById(R.id.activity_fulltime)
        val counter: TextView = root.findViewById(R.id.activity_occurrences)
        val primaryAction: OptionView = root.findViewById(R.id.activity_primaryaction)
        val copyAction: OptionView = root.findViewById(R.id.activity_copyaction)

        viewModel.history.observe(viewLifecycleOwner, Observer {
            viewModel.get(args.historyId)?.run {
                when (this.type) {
                    HistoryEntryType.passed_allowed -> {
                        icon.setImageResource(R.drawable.ic_shield_off_outline)
                        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
                        comment.text = getString(R.string.activity_request_allowed_whitelisted)
                    }
                    HistoryEntryType.blocked_denied -> {
                        icon.setImageResource(R.drawable.ic_shield_off_outline)
                        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red))
                        comment.text = getString(R.string.activity_request_blocked_blacklisted)
                    }
                    HistoryEntryType.passed -> {
                        icon.setImageResource(R.drawable.ic_shield_outline)
                        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
                        comment.text = getString(R.string.activity_request_allowed)
                    }
                    else -> {
                        icon.setImageResource(R.drawable.ic_shield_outline)
                        icon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red))
                        comment.text = getString(R.string.activity_request_blocked)
                    }
                }

                when {
                    viewModel.isDenied(this.name) -> {
                        primaryAction.name = getString(R.string.activity_action_added_to_blacklist)
                        primaryAction.active = true
                        primaryAction.setOnClickListener {
                            viewModel.undeny(this.name)
                        }
                    }
                    viewModel.isAllowed(this.name) -> {
                        primaryAction.name = getString(R.string.activity_action_added_to_whitelist)
                        primaryAction.active = true
                        primaryAction.setOnClickListener {
                            viewModel.unallow(this.name)
                        }
                    }
                    this.type == HistoryEntryType.passed -> {
                        primaryAction.name = getString(R.string.activity_action_add_to_blacklist)
                        primaryAction.active = false
                        primaryAction.setOnClickListener {
                            viewModel.deny(this.name)
                        }
                    }
                    else -> {
                        primaryAction.name = getString(R.string.activity_action_add_to_whitelist)
                        primaryAction.active = false
                        primaryAction.setOnClickListener {
                            viewModel.allow(this.name)
                        }
                    }
                }

                counter.text = requests.toString()
                name.text = this.name
                fullName.text = this.name
                time.text = this.time.toString()

                copyAction.setOnClickListener {
                    AndroidUtils.copyToClipboard(this.name)
                }
            }
        })

        return root
    }

}