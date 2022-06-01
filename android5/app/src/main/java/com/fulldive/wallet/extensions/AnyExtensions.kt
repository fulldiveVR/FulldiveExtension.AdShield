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

import android.os.Bundle
import timber.log.Timber

fun Any?.isNotNull(): Boolean {
    return null != this
}

fun Bundle?.orEmpty(): Bundle = this ?: Bundle.EMPTY
fun Boolean?.orFalse() = this ?: false
fun Boolean?.orTrue() = this ?: true
inline fun <T> Boolean?.ifTrue(block: () -> T) = if (this == true) block.invoke() else null
inline fun <T> Boolean?.ifFalse(block: () -> T) = if (this == false) block.invoke() else null

fun <T> T?.or(value: T) = this ?: value
inline fun <T> T?.or(block: () -> T) = this ?: block.invoke()
inline fun <T, R> T?.letOr(block: (T) -> R, blockElse: () -> R): R {
    return if (this != null) block.invoke(this) else blockElse.invoke()
}

fun String?.orNull(): String? = if (this.isNullOrEmpty()) null else this
fun CharSequence?.orEmpty(): CharSequence = this ?: ""
fun CharSequence?.orEmptyString(): String = this.orEmpty().toString()

inline fun <R> safe(block: () -> R?): R? {
    return try {
        block.invoke()
    } catch (ex: Exception) {
        Timber.e(ex)
        null
    }
}

inline fun <reified T> unsafeLazy(noinline initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)

fun <A, B> combine(a: A, b: B) = a to b
fun <A, B, C> combine(a: A, b: B, c: C) = Triple(a, b, c)

fun <A> concat(a: List<A>, b: List<A>) = a + b

fun String.withPrefix(prefix: String) = if (startsWith(prefix)) this else "$prefix$this"
