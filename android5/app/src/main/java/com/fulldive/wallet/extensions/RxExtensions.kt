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

package com.fulldive.wallet.extensions

import com.fulldive.wallet.rx.AppSchedulers
import io.reactivex.*
import timber.log.Timber

fun <T> Single<T>.withUiDefaults(): Single<T> {
    return compose {
        it.subscribeOn(AppSchedulers.ui())
            .observeOn(AppSchedulers.ui())
            .doOnError(Timber::e)
    }
}

fun <T> Single<T>.withDefaults(): Single<T> {
    return compose {
        it.subscribeOn(AppSchedulers.io())
            .observeOn(AppSchedulers.ui())
            .doOnError(Timber::e)
    }
}

fun <T> Flowable<T>.withDefaults(): Flowable<T> {
    return compose {
        it.subscribeOn(AppSchedulers.io())
            .observeOn(AppSchedulers.ui())
            .doOnError(Timber::e)
    }
}

fun <T> Maybe<T>.withDefaults(): Maybe<T> {
    return compose {
        it.subscribeOn(AppSchedulers.io())
            .observeOn(AppSchedulers.ui())
            .doOnError(Timber::e)
    }
}

fun <T> Observable<T>.withDefaults(): Observable<T> {
    return compose {
        it.subscribeOn(AppSchedulers.io())
            .observeOn(AppSchedulers.ui())
            .doOnError(Timber::e)
    }
}

fun Completable.withDefaults(): Completable {
    return compose {
        it.subscribeOn(AppSchedulers.io())
            .observeOn(AppSchedulers.ui())
            .doOnError(Timber::e)
    }
}

inline fun <reified R> singleCallable(noinline callable: () -> R): Single<R> {
    return Single.fromCallable(callable)
}

inline fun <reified R> safeSingle(noinline callable: () -> R?): Single<R> {
    return Single.create { emitter ->
        try {
            val result = callable()
            if (result == null) {
                emitter.tryOnError(NullPointerException())
            } else {
                emitter.onSuccess(result)
            }
        } catch (ex: Exception) {
            emitter.tryOnError(ex)
        }
    }
}

inline fun <reified R> safeMaybe(noinline callable: () -> R?): Maybe<R> {
    return Maybe.create { emitter ->
        try {
            val result = callable()
            if (result == null) {
                emitter.tryOnError(NullPointerException())
            } else {
                emitter.onSuccess(result)
            }
        } catch (ex: Exception) {
            emitter.tryOnError(ex)
        }
    }
}

fun completeCallable(callable: () -> Unit): Completable {
    return Completable.fromCallable(callable)
}

fun safeCompletable(callable: () -> Unit): Completable {
    return Completable.create { emitter ->
        try {
            callable()
            emitter.onComplete()
        } catch (ex: Exception) {
            emitter.tryOnError(ex)
        }
    }
}

fun <T> T.toSingle(): Single<T> = Single.just(this)
