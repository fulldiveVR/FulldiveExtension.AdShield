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

package appextension

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.adshield.BuildConfig
import org.adshield.R

object EmailHelper {
    private const val MIME_TYPE_PLAIN_TEXT = "text/plain"
    private const val SUPPORT_EMAIL = "support@fulldive.com"

    fun sendEmailToSupport(context: Context) {
        sendEmail(
            context,
            "",
            "${context.getString(R.string.app_name)}-${BuildConfig.VERSION_NAME}",
            SUPPORT_EMAIL
        )
    }

    fun sendEmail(
        context: Context,
        message: String,
        subject: String,
        email: String
    ) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
            .apply {
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                type = MIME_TYPE_PLAIN_TEXT
            }
        var success = false
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(
                    Intent.createChooser(
                        intent, context.getString(R.string.flat_fraud_intent_chooser)
                    )
                )
                success = true
            }
        } catch (error: ActivityNotFoundException) {
        }
        if (!success) {
            sendIntent(context, message, subject, email)
        }
    }

    private fun sendIntent(
        context: Context,
        message: String,
        subject: String,
        email: String
    ): Boolean {
        val intent = Intent(Intent.ACTION_SEND, Uri.fromParts("mailto", email, null)).apply {
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            type = MIME_TYPE_PLAIN_TEXT
        }
        var success = false
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        context.getString(R.string.flat_fraud_intent_chooser)
                    )
                )
                success = true
            }
        } catch (error: ActivityNotFoundException) {
        }
        return success
    }
}
