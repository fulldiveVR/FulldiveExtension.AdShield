package com.fulldive.wallet.extensions

import com.fulldive.wallet.presentation.base.BaseMoxyPresenter
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

inline fun <reified R : Any> BaseMoxyPresenter<*>.tryLaunch(
    noinline onStart: (() -> Unit)? = null,
    noinline onSuccess: ((R) -> Unit)? = null,
    noinline onFailure: (Throwable) -> Unit = defaultOnErrorConsumer,
    noinline onFinish: (() -> Unit)? = null,
    noinline tryBlock: suspend () -> State<R>
) {
    launch {
        onStart?.invoke()
        try {
            val state = withContext(Dispatchers.IO) { tryBlock.invoke() }
            state.fold(onSuccess = onSuccess, onFailure = onFailure)
        } catch (e: Throwable) {
            onFailure.invoke(e)
        } finally {
            onFinish?.invoke()
        }
    }
}

suspend inline fun <reified R : Any> tryAsync(noinline tryBlock: suspend () -> State<R>): State<R> =
    withContext(Dispatchers.IO) {
        try {
            tryBlock()
        } catch (e: Throwable) {
            e.toFailure()
        }
    }

suspend inline fun <reified R : Any?, T : Any> trySuspend(
    noinline tryBlock: () -> Call<R>,
    noinline onSuccess: Response<R>.() -> State<T>
): State<T> = suspendCoroutine { continuation ->
    try {
        continuation.resume(tryBlock().execute().onSuccess())
    } catch (e: Throwable) {
        continuation.resume(e.toFailure())
    }
}

sealed class State<out T : Any> {
    data class Success<T : Any>(
        @SerializedName("data")
        val data: T
    ) : State<T>()

    data class Failure(
        @SerializedName("message")
        val message: String = "",
        @SerializedName("code")
        val code: Int = -1,
        @SerializedName("ex")
        val ex: Throwable
    ) : State<Nothing>()

    object Loading : State<Nothing>()
    object Refresh : State<Nothing>()
}
