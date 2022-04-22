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