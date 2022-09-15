/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ui.web.customsettings

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
import com.google.gson.Gson
import org.adshield.R
import ui.BottomSheetFragment
import ui.StatsViewModel
import ui.app
import ui.utils.CircleProgressBar
import ui.web.WebService
import utils.Links

class CustomWebSettingsFragment : BottomSheetFragment() {

    private lateinit var statsVM: StatsViewModel

    private val customSettingsUrl get() = Links.customSettings

    private lateinit var currentUrl: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            statsVM = ViewModelProvider(it.app())[StatsViewModel::class.java]
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
        var isLoaded = false

        var customBlocklistsJsonConfig = ""

        statsVM.customBlocklistConfig.observe(viewLifecycleOwner) { config ->
            if (!isLoaded) {
                customBlocklistsJsonConfig = Gson().toJson(config)
                webView.isVisible = true
                circleProgressView.isVisible = false
                searchBar.setQuery(customSettingsUrl, false)
                webView.loadUrl(customSettingsUrl)
                isLoaded = true
            }
        }

        currentUrl = customSettingsUrl

        val extension = CustomSettingsExtension().apply {
            onCustomSettingsStateChangeListener = { config ->
                statsVM.onConfigUpdate(config)
                findNavController().popBackStack()
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(extension, CustomSettingsExtension.EXTENSION_NAME)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.isNotEmpty()) {
                    searchBar.setQuery(url, false)
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String) {
                if (currentUrl == url) {
                    webView.loadUrl("javascript:loadHosts('$customBlocklistsJsonConfig')")
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
        fun newInstance() = CustomWebSettingsFragment()
    }
}
