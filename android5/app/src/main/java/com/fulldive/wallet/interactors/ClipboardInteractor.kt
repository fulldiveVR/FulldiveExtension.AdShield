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

package com.fulldive.wallet.interactors

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.fulldive.wallet.di.modules.DefaultInteractorsModule
import com.fulldive.wallet.extensions.safeCompletable
import com.fulldive.wallet.extensions.safeSingle
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedBy(DefaultInteractorsModule::class)
class ClipboardInteractor @Inject constructor(
    private val context: Context
) {
    fun getClip(): Single<String> {
        return safeSingle {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                ?.primaryClip
                ?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)
                ?.coerceToText(context)
                ?.toString()
                ?.trim()
        }
    }

    fun copyToClipboard(text: String): Completable {
        return safeCompletable {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                ?.setPrimaryClip(
                    ClipData.newPlainText(
                        PLAIN_TEXT_LABEL,
                        text
                    )
                )
        }
    }

    companion object {
        private const val PLAIN_TEXT_LABEL = "data"
    }
}