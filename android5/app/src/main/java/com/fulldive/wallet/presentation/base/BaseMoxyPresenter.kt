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

package com.fulldive.wallet.presentation.base

import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import io.reactivex.disposables.CompositeDisposable
import moxy.MvpPresenter
import org.adshield.R
import timber.log.Timber
import java.io.IOException
import java.net.UnknownHostException

abstract class BaseMoxyPresenter<View : BaseMoxyView>
    : MvpPresenter<View>(), ICompositable {

    override val compositeDisposable by lazy { CompositeDisposable() }

    override val defaultOnErrorConsumer: (Throwable) -> Unit by lazy { OnErrorConsumer() }

    @CallSuper
    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    open inner class OnErrorConsumer : BaseOnErrorConsumer() {

        protected fun logError(error: Throwable) {
            val errorMessage = error.message.orEmpty()
            Timber.w("$errorMessage >> ${Log.getStackTraceString(error)}")
        }

        @MainThread
        override fun onError(error: Throwable) {
            logError(error)
            when (error) {
                is UnknownHostException,
                is IOException -> onNoConnectionError()
                else -> error.message?.let(viewState::showMessage)
            }
        }

        @MainThread
        override fun onNoConnectionError() {
            viewState.showMessage(R.string.error_unknown)
        }
    }
}