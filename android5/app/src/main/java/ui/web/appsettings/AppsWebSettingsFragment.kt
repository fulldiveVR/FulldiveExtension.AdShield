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

package ui.web.appsettings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import appextension.dialogs.PopupManager
import com.google.gson.Gson
import model.App
import org.adshield.R
import ui.BottomSheetFragment
import ui.advanced.apps.AppsViewModel
import ui.advanced.packs.PacksViewModel
import ui.utils.CircleProgressBar
import utils.Links

class AppsWebSettingsFragment : BottomSheetFragment() {

    private lateinit var packsVM: PacksViewModel
    private lateinit var appsVM: AppsViewModel

    private val appsSettingsUrl get() = Links.appsSettings

    private lateinit var currentUrl: String

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            packsVM = ViewModelProvider(it)[PacksViewModel::class.java]
            appsVM = ViewModelProvider(it)[AppsViewModel::class.java]
        }

        var appsConfiguration = emptyList<App>()

        val root = inflater.inflate(R.layout.fragment_web_view, container, false)
        val webView: WebView = root.findViewById(R.id.webView)
        val circleProgressView: CircleProgressBar = root.findViewById(R.id.circleProgressView)
        webView.isVisible = false
        circleProgressView.isVisible = true

        var isLoaded = false
        var appJsonConfig = "[]"

        appsVM.apps.observe(viewLifecycleOwner) { apps ->
            if (!isLoaded) {
                appsConfiguration = apps
                appJsonConfig = Gson().toJson(apps)
                webView.isVisible = true
                circleProgressView.isVisible = false
                PopupManager.showAppSettingsPermissionDialog(requireContext()) { isGranted ->
                    if (isGranted) {
                        webView.loadUrl(appsSettingsUrl)
                    } else {
                        findNavController().popBackStack()
                    }
                }
                isLoaded = true
            }
        }
        currentUrl = appsSettingsUrl

        val extension = AppsSettingsExtension().apply {
            onAppStateChangeListener = { appId, _ ->
                appsVM.switchBypass(appId)
            }
        }
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(extension, AppsSettingsExtension.EXTENSION_NAME)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String) {
                webView.loadUrl("javascript:loadConfig('$appJsonConfig')")

            }
        }
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.web_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    companion object {
        fun newInstance() = AppsWebSettingsFragment()
    }
}
