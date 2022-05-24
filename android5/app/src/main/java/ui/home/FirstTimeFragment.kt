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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import org.adshield.BuildConfig
import org.adshield.R
import ui.BottomSheetFragment
import java.util.*

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

        val intro: TextView = root.findViewById(R.id.intro_text)
        val vpnString = getString(R.string.intro_text)
        val vpnStringSpanned = getTextWithLink(vpnString) {
            startActivity(getIntentForVpnProfile())
        }
        intro.text = vpnStringSpanned
        intro.movementMethod = LinkMovementMethod.getInstance()

        val batteryStringSpanned = if (Build.MANUFACTURER.lowercase(Locale.ENGLISH) == "meizu") {
            val batteryString = getString(R.string.first_start_battery_exception_meizu)
            getTextWithLink(batteryString) {
                activity?.let { openFlymeSecurityApp(it) }
            }
        } else {
            val batteryString = getString(R.string.first_start_battery_exception)
             getTextWithLink(batteryString) {
                checkDoze()
            }
        }

        val more: TextView = root.findViewById(R.id.firsttime_more)
        more.text = batteryStringSpanned
        more.movementMethod = LinkMovementMethod.getInstance()

        val firstTimeContinue: View = root.findViewById(R.id.firsttime_continue)
        firstTimeContinue.setOnClickListener {
            dismiss()
        }

        return root
    }

    private fun checkDoze() {
        val settings = Intent(
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        )
        if (context?.packageManager?.resolveActivity(
                settings,
                0
            ) != null
        ) try {
            startActivity(settings)
        } catch (ex: Throwable) {
        }
    }

    fun setTextColor(
        context: Context,
        string: SpannableString,
        @ColorRes colorRes: Int,
        startIndex: Int = 0,
        endIndex: Int = string.length
    ): SpannableString {
        if (string.isNotEmpty()) {
            string.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        colorRes
                    )
                ),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return string
    }

    private fun getSubstring(string: String): SubString {
        val openTag = "<u>"
        val closeTag = "</u>"
        val startIndex = string.indexOf(openTag)
        val substring = string.replace(openTag, "")
        val endIndex = substring.indexOf(closeTag)
        return SubString(substring.replace(closeTag, ""), startIndex, endIndex)
    }

    private fun getTextWithLink(text: String, callback: () -> Unit): SpannableString {
        val (substring, startIndex, endIndex) = getSubstring(text)
        val stringSpanned = SpannableString(substring)
        if (startIndex >= 0) {
            stringSpanned.setSpan(
                UnderlineSpan(),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            context?.let {
                setTextColor(
                    it,
                    stringSpanned,
                    R.color.colorAccent,
                    startIndex,
                    endIndex
                )
            }
            stringSpanned.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        callback.invoke()
                    }
                },
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return stringSpanned
    }

    private fun getIntentForVpnProfile() = Intent().apply {
        action = "android.net.vpn.SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    private fun openFlymeSecurityApp(context: Activity) {
        val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class SubString(
        val text: String,
        val startIndex: Int,
        val endIndex: Int
    )
}