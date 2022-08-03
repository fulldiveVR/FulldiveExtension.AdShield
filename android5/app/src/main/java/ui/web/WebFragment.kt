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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.adshield.R
import ui.BottomSheetFragment
import ui.advanced.packs.PacksViewModel
import ui.utils.CircleProgressBar
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

        val root = inflater.inflate(R.layout.fragment_web_view, container, false)
        val webView: WebView = root.findViewById(R.id.webView)
        val circleProgressView: CircleProgressBar = root.findViewById(R.id.circleProgressView)
        val searchBar: SearchView = root.findViewById(R.id.searchBar)
        val webBackButton: AppCompatImageView = root.findViewById(R.id.webBackButton)
        val webRefreshButton: AppCompatImageView = root.findViewById(R.id.webRefreshButton)
        webBackButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
            webView.postDelayed(
                { searchBar.setQuery(webView.url, false) },
                200
            )
        }

        webRefreshButton.setOnClickListener {
            webView.url?.let { it1 -> webView.loadUrl(it1) }
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (WebService.isUrl(query)) {
                    webView.loadUrl(query)
                } else {
                    webView.loadUrl("https://www.google.com/search?q=$query")
                }

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return newText.isNotEmpty()
            }
        })

        webView.isVisible = false
        circleProgressView.isVisible = true
        var jsonConfig = "[]"
        var isLoaded = false
        packsVM.packs.observe(viewLifecycleOwner) { packs ->
            if (!isLoaded) {
                val packsConfiguration = packsVM.mapPacksToEntities(packs)
                jsonConfig = Gson().toJson(packsConfiguration)
                webView.isVisible = true
                circleProgressView.isVisible = false
                searchBar.setQuery(args.url, false)
                webView.loadUrl(args.url)
                isLoaded = true
            }
        }

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
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.isNotEmpty()) {
                    searchBar.setQuery(url, false)
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (currentUrl == url) {
                    webView.loadUrl("javascript:loadConfig('$jsonConfig')")
                }
                webBackButton.isGone = !webView.canGoBack()
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
