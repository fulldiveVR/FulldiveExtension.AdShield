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

import io.reactivex.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

interface ICompositable {

    val compositeDisposable: CompositeDisposable
    val defaultOnErrorConsumer: (Throwable) -> Unit

    fun Completable.compositeSubscribe(
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)
        .composite()

    fun <T : Any> Flowable<T>.compositeSubscribe(
        onNext: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer,
        onComplete: () -> Unit = {}
    ) = subscribe(onNext, onError, onComplete)
        .composite()

    fun <T : Any> Single<T>.compositeSubscribe(
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)
        .composite()

    fun <T : Any> Maybe<T>.compositeSubscribe(
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)
        .composite()

    fun <T : Any> Observable<T>.compositeSubscribe(
        onNext: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer,
        onComplete: () -> Unit = {}
    ) = subscribe(onNext, onError, onComplete)
        .composite()

    fun Completable.justSubscribe(
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)

    fun <T : Any> Single<T>.justSubscribe(
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)

    private fun Disposable.composite(): Disposable {
        compositeDisposable.add(this)
        return this
    }

    fun onDestroy()
}
