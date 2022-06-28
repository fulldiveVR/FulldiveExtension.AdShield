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

package com.fulldive.wallet.presentation.base.subscription

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.fulldive.wallet.extensions.fromHtmlToSpanned
import com.fulldive.wallet.extensions.getHexColor
import com.fulldive.wallet.extensions.unsafeLazy
import org.adshield.R
import ui.BottomSheetFragment
import ui.TunnelViewModel
import ui.app
import ui.utils.CustomImageSpan
import java.util.regex.Pattern

class SubscriptionSuccessDialogFragment : BottomSheetFragment() {

    private lateinit var tunnelVM: TunnelViewModel

    companion object {
        fun newInstance() = SubscriptionSuccessDialogFragment()
    }

    private val tutorialColor by unsafeLazy {
        requireContext().getHexColor(R.color.colorAccent)
    }

    private val tutorial by unsafeLazy {
        SpannableString.valueOf(requireContext().getString(R.string.str_subscription_tutorial_checkmarks))
            .apply {
                process(
                    Pattern.compile(Pattern.quote(SubscriptionTutorialFragment.CHECK_MARK_TAG)),
                    R.drawable.ic_check_mark,
                    requireContext(),
                    this,
                    DynamicDrawableSpan.ALIGN_BASELINE
                )
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            tunnelVM = ViewModelProvider(it.app()).get(TunnelViewModel::class.java)
        }

        val root = inflater.inflate(R.layout.fragment_subscription_success, container, false)

        val back: View = root.findViewById(R.id.back)
        back.setOnClickListener {
            dismiss()
        }
        val tutorialTextView = root.findViewById<TextView>(R.id.tutorialTextView)
        val titleTextView = root.findViewById<TextView>(R.id.titleTextView)
        tutorialTextView.text = tutorial
        titleTextView.text = fromHtmlToSpanned(
            getString(R.string.str_subscription_congrats_title).replace(
                "%tutorialTextColor%",
                tutorialColor
            )
        )
        return root
    }

    fun process(
        key: Pattern,
        value: Int,
        context: Context,
        spannable: Spannable,
        alignment: Int
    ): Boolean {
        var hasChanges = false
        val matcher = key.matcher(spannable)
        while (matcher.find()) {
            var changed = true
            for (span in spannable.getSpans(
                matcher.start(),
                matcher.end(),
                ImageSpan::class.java
            )) {
                if (spannable.getSpanStart(span) >= matcher.start() &&
                    spannable.getSpanEnd(span) <= matcher.end()
                )
                    spannable.removeSpan(span)
                else {
                    changed = false
                    break
                }
            }
            if (changed) {
                hasChanges = true
                spannable.setSpan(
                    CustomImageSpan(context, value, alignment),
                    matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return hasChanges
    }
}
