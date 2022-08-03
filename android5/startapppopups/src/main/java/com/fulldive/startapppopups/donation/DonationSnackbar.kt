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

package com.fulldive.startapppopups.donation

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.fulldive.startapppopups.R
import com.google.android.material.snackbar.Snackbar


class DonationSnackbar {

    private var snackbar: Snackbar? = null

    fun showSnackBar(
        view: View,
        onDonateClicked: () -> Unit,
        onCloseClicked: () -> Unit,
        bottomMargin: Int
    ) {
        val snackView = View.inflate(view.context, R.layout.layout_donate, null)
        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
        snackbar?.view?.let { rootView ->
            (rootView as? ViewGroup)?.removeAllViews()
            (rootView as? ViewGroup)?.addView(snackView)
            rootView.setPadding(0, 0, 0, 0)

            val params = rootView.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(
                params.leftMargin,
                params.topMargin,
                params.rightMargin,
                bottomMargin + params.bottomMargin
            )
            rootView.layoutParams = params
            rootView.elevation = 0f
            snackbar?.setBackgroundTint(
                ContextCompat.getColor(
                    view.context,
                    android.R.color.transparent
                )
            )
            val idoAnnouncementCardView =
                snackView.findViewById<CardView>(R.id.idoAnnouncementCardView)
            idoAnnouncementCardView.setOnClickListener {
                onDonateClicked.invoke()
            }
            val crossButton = snackView.findViewById<ImageView>(R.id.closePopupButton)
            crossButton.setOnClickListener {
                onCloseClicked.invoke()
                dismiss()
            }
        }

        snackbar?.show()
    }

    fun dismiss() {
        snackbar?.dismiss()
    }
}