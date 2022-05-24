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

package ui.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHost
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.adshield.R
import ui.BottomSheetFragment
import ui.advanced.packs.PackDetailFragmentDirections
import ui.advanced.packs.PacksViewModel
import ui.web.ForwardingListExtension.Companion.EXTENSION_NAME

class WebFragment : BottomSheetFragment() {

    private lateinit var packsVM: PacksViewModel

    private val args: WebFragmentArgs by navArgs()

    private lateinit var currentUrl: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            packsVM = ViewModelProvider(it)[PacksViewModel::class.java]
        }

        var packsConfiguration = emptyList<PacksViewModel.PackEntity>()

        val root = inflater.inflate(R.layout.fragment_web_view, container, false)
        val webView: WebView = root.findViewById(R.id.webView)
        var jsonConfig = "[]"
        var isLoaded = false
        packsVM.packs.observe(viewLifecycleOwner, Observer { packs ->
            if (!isLoaded) {
                packsConfiguration = packsVM.mapPacksToEntities(packs)
                jsonConfig = Gson().toJson(packsConfiguration)
                webView.loadUrl(args.url)
                isLoaded = true
            }
        })

        currentUrl = args.url

        val extension = ForwardingListExtension().apply {
            onBlocklistChangeListener = { jsonConfig ->
                val type = object : TypeToken<List<PacksViewModel.PackEntity>>() {}.type
                val config: List<PacksViewModel.PackEntity> = Gson().fromJson(jsonConfig, type)
                packsVM.onPacksConfigChanged(config)
                val nav = findNavController()
                nav.popBackStack()
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(extension, EXTENSION_NAME)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String) {
                webView.loadUrl("javascript:loadConfig('$jsonConfig')")
            }
        }
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.web_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        fun newInstance() = WebFragment()
    }
}
