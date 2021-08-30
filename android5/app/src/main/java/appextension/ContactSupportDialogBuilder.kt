package appextension

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import org.adshield.R

object ContactSupportDialogBuilder {

    fun show(context: Context, onPositiveClicked: () -> Unit) {
        val dialog = AlertDialog
            .Builder(context)
            .setTitle(R.string.support_title)
            .setMessage(R.string.support_description)
            .setPositiveButton(R.string.support_submit) { _, _ ->
                onPositiveClicked.invoke()
            }
            .setNegativeButton(R.string.maybe_later) { _, _ -> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(context.getColor(R.color.textColorAccent))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(context.getColor(R.color.textColorSecondary))
        }
        dialog.show()
    }
}