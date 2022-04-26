package com.fulldive.wallet.presentation.accounts

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fulldive.wallet.presentation.accounts.create.CreateAccountActivity
import com.fulldive.wallet.presentation.accounts.mnemonic.RestoreMnemonicActivity
import com.fulldive.wallet.presentation.accounts.privatekey.RestorePrivateKeyActivity
import com.fulldive.wallet.presentation.base.BaseMvpDialogFragment
import org.adshield.databinding.DialogAddAccountBinding

class AddAccountDialogFragment : BaseMvpDialogFragment<DialogAddAccountBinding>() {

    override fun getViewBinding() = DialogAddAccountBinding.inflate(layoutInflater)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDialogCreated(alertDialog: AlertDialog) {
        super.onDialogCreated(alertDialog)

        binding {
            restoreWithPrivateKey.setOnClickListener {
                showActivity(RestorePrivateKeyActivity::class.java)
            }
            restoreWithMnemonic.setOnClickListener {
                showActivity(RestoreMnemonicActivity::class.java)
            }
            createButton.setOnClickListener {
                showActivity(CreateAccountActivity::class.java)
            }
        }
    }

    private fun showActivity(clazz: Class<*>) {
        val activity = requireActivity()
        val intent = Intent(activity, clazz)
        activity.startActivity(intent)
        dismiss()
    }

    companion object {

        fun newInstance(): AddAccountDialogFragment {
            return AddAccountDialogFragment()
        }
    }
}