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
import android.view.LayoutInflater
import org.adshield.R

object UpdateAppDialog {

    fun show(
        context: Context
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.app_update_available_dialog_layout, null)

        val dialog = AlertDialog.Builder(context, R.style.AppTheme_DialogStyle)
            .setView(view)
            .setPositiveButton(R.string.str_ok) { _, _ ->
            }
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