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

package appextension.dialogs

import android.app.AlertDialog
import android.content.Context
import org.adshield.R

object ContactSupportDialogBuilder {

    fun show(context: Context, onPositiveClicked: () -> Unit) {
        val dialog = AlertDialog
            .Builder(context, R.style.AppTheme_DialogStyle)
            .setTitle(R.string.support_title)
            .setMessage(R.string.support_description)
            .setPositiveButton(R.string.support_submit) { _, _ ->
                onPositiveClicked.invoke()
            }
            .setNegativeButton(R.string.maybe_later) { _, _ -> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(context.getColor(R.color.colorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(context.getColor(R.color.textColorSecondary))
        }
        dialog.show()
    }
}