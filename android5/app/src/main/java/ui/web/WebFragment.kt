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
import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.BaseColumns
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CursorAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import appextension.dialogs.PopupManager
import com.fulldive.wallet.extensions.or
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.adshield.R
import ui.BottomSheetFragment
import ui.StatsViewModel
import ui.advanced.apps.AppsViewModel
import ui.advanced.packs.PacksViewModel
import ui.app
import ui.utils.CircleProgressBar
import ui.web.appsettings.AppsSettingsExtension
import ui.web.customsettings.CustomSettingsExtension
import utils.Links

class WebFragment : BottomSheetFragment() {

    private val suggestions = WebSettings.getWebSettings()

    private lateinit var packsVM: PacksViewModel
    private lateinit var appsVM: AppsViewModel
    private lateinit var statsVM: StatsViewModel
    private lateinit var webView: WebView
    private lateinit var circleProgressView: CircleProgressBar
    private lateinit var searchBar: SearchView
    private lateinit var webBackButton: AppCompatImageView
    private lateinit var webRefreshButton: AppCompatImageView

    private val args: WebFragmentArgs by navArgs()

    private var currentSettings: WebSettings = WebSettings.Empty

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        packsVM = ViewModelProvider(this)[PacksViewModel::class.java]
        appsVM = ViewModelProvider(this)[AppsViewModel::class.java]
        activity?.let {
            statsVM = ViewModelProvider(it.app())[StatsViewModel::class.java]
        }

        val root = inflater.inflate(R.layout.fragment_web_view, container, false)
        webView = root.findViewById(R.id.webView)
        circleProgressView = root.findViewById(R.id.circleProgressView)
        searchBar = root.findViewById(R.id.searchBar)
        webBackButton = root.findViewById(R.id.webBackButton)
        webRefreshButton = root.findViewById(R.id.webRefreshButton)

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

        circleProgressView.isVisible = true
        if (args.url == Links.dnsSettings) {
            var isLoaded = false
            packsVM.packs.observe(viewLifecycleOwner) { packs ->
                if (!isLoaded) {
                    webView.isVisible = true
                    circleProgressView.isVisible = false
                    searchBar.setQuery(args.url, false)
                    webView.loadUrl(args.url)
                    isLoaded = true
                }
            }
        }

        initSearchBar()
        initWebView()
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.web_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initSearchBar() {
        val suggestionsAdapter = SimpleCursorAdapter(
            requireContext(),
            R.layout.layout_suggestion_list_item,
            null,
            arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1),
            intArrayOf(R.id.titleTextView),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        searchBar.suggestionsAdapter = suggestionsAdapter

        searchBar.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = searchBar.suggestionsAdapter.getItem(position) as Cursor
                val index = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2_URL)
                return if (index >= 0) {
                    val selection = cursor.getString(index)
                    searchBar.setQuery(selection, true)
                    true
                } else {
                    false
                }
            }
        })

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
                val cursor = MatrixCursor(
                    arrayOf(
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_TEXT_2_URL
                    )
                )
                suggestions.forEach { suggestion ->
                    cursor.addRow(
                        arrayOf(
                            suggestion.id,
                            getString(suggestion.titleRes),
                            suggestion.url
                        )
                    )
                }

                suggestionsAdapter.changeCursor(cursor)
                return newText.isNotEmpty()
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val blocklistExtension = ForwardingListExtension().apply {
            onBlocklistChangeListener = { jsonConfig ->
                val type = object : TypeToken<List<PacksViewModel.PackEntity>>() {}.type
                val config: List<PacksViewModel.PackEntity> = Gson().fromJson(jsonConfig, type)
                packsVM.onPacksConfigChanged(config)
            }
        }
        val appExtension = AppsSettingsExtension().apply {
            onAppStateChangeListener = { appId, _ ->
                appsVM.switchBypass(appId)
            }
        }

        val customSettingsExtension = CustomSettingsExtension().apply {
            onCustomSettingsStateChangeListener = { config ->
                statsVM.onConfigUpdate(config)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(blocklistExtension, ForwardingListExtension.EXTENSION_NAME)
        webView.addJavascriptInterface(appExtension, AppsSettingsExtension.EXTENSION_NAME)
        webView.addJavascriptInterface(
            customSettingsExtension,
            CustomSettingsExtension.EXTENSION_NAME
        )

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.isNotEmpty()) {
                    searchBar.setQuery(url, false)
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                currentSettings = suggestions
                    .firstOrNull { it.url == url }
                    .or { WebSettings.Empty }
            }

            override fun onPageFinished(view: WebView, url: String) {
                processSettingsPage()
                webBackButton.isGone = !webView.canGoBack()
            }
        }
    }

    private fun processSettingsPage() {
        packsVM.packs.removeObservers(viewLifecycleOwner)
        appsVM.apps.removeObservers(viewLifecycleOwner)
        statsVM.customBlocklistConfig.removeObservers(viewLifecycleOwner)
        when (currentSettings) {
            WebSettings.BlockListsSettings -> {
                loadPacksConfig()
            }
            WebSettings.AppsListsSettings -> {
                loadAppsConfig()
            }
            WebSettings.CustomBlockListsSettings -> {
                loadCustomConfig()
            }
            else -> {}
        }
    }

    private fun loadPacksConfig() {
        var isLoaded = false
        packsVM.packs.observe(viewLifecycleOwner) { packs ->
            if (!isLoaded && packs != null) {
                val packsConfiguration = packsVM.mapPacksToEntities(packs)
                val jsonConfig = Gson().toJson(packsConfiguration)
                webView.isVisible = true
                circleProgressView.isVisible = false
                webView.loadUrl("javascript:loadConfig('$jsonConfig')")
                isLoaded = true
            }
        }
    }

    private fun loadAppsConfig() {
        var isLoaded = false
        appsVM.apps.observe(viewLifecycleOwner) { apps ->
            if (!isLoaded) {
                val jsonConfig = Gson().toJson(apps)
                webView.isVisible = true
                circleProgressView.isVisible = false
                PopupManager.showAppSettingsPermissionDialog(requireContext()) { isGranted ->
                    if (isGranted) {
                        webView.loadUrl("javascript:loadConfig('$jsonConfig')")
                    } else {
                        findNavController().popBackStack()
                    }
                }
                isLoaded = true
            }
        }
    }

    private fun loadCustomConfig() {
        var isLoaded = false
        statsVM.customBlocklistConfig.observe(viewLifecycleOwner) { config ->
            if (!isLoaded) {
                val jsonConfig = Gson().toJson(config)
                webView.isVisible = true
                circleProgressView.isVisible = false
                webView.loadUrl("javascript:loadHosts('$jsonConfig')")
                isLoaded = true
            }
        }
    }

    companion object {
        fun newInstance() = WebFragment()
    }
}
