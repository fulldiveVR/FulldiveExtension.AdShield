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
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import org.adshield.R
import ui.BottomSheetFragment
import ui.StatsViewModel
import ui.app
import ui.utils.CircleProgressBar
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
        webView.isVisible = false
        circleProgressView.isVisible = true
        var isLoaded = false

        var appJsonConfig = ""

        statsVM.customBlocklistConfig.observe(viewLifecycleOwner) { config ->
            if (!isLoaded) {
                appJsonConfig = Gson().toJson(config)
                webView.isVisible = true
                circleProgressView.isVisible = false

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
            override fun onPageFinished(view: WebView?, url: String) {
                webView.loadUrl("javascript:loadHosts('$appJsonConfig')")
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
