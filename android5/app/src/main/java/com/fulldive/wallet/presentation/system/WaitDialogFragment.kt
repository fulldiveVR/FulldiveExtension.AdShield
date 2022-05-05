package com.fulldive.wallet.presentation.system

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fulldive.wallet.presentation.base.BaseMvpDialogFragment
import org.adshield.databinding.DialogWaitBinding

class WaitDialogFragment : BaseMvpDialogFragment<DialogWaitBinding>() {

    override fun getViewBinding() = DialogWaitBinding.inflate(layoutInflater)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        fun newInstance() = WaitDialogFragment()
    }
}