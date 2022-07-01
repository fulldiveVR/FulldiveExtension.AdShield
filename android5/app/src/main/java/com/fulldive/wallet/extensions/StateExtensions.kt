package com.fulldive.wallet.extensions

import com.google.gson.annotations.SerializedName
import retrofit2.Response

fun Response<ResponseData>.getSuccessState(): State<Boolean> = getBodyState { (success) ->
    success.toSuccess()
}

inline fun <T : Any, R : Any> Response<T>.getBodyState(onSuccess: (T) -> State<R>): State<R> =
    this.runCatching { this.body()!! }.fold(onSuccess, onFailure = {
        it.toFailure()
    })

fun <T : Any> T?.catchNullState(): State<T> = this?.toSuccess()
    ?: NullPointerException().toFailure()

fun Response<Any?>.getDeleteState(): State<Boolean> {
    return true.takeIf { this.code() == 204 }?.toSuccess() ?: IllegalStateException().toFailure()
}

fun <T : Throwable> T.toFailure() = failureState(message = this.message.orEmptyString(), ex = this)
fun <T : Any> T.toSuccess() = State.Success(this)

fun failureState(message: String, ex: Throwable, code: Int = -1) =
    State.Failure(message = message, code = code, ex = ex)

suspend inline fun <reified T : Any, reified R : Any> State<T>.mapTo(inHandler: suspend (T) -> R?): State<R> {
    return when (this) {
        is State.Success<T> -> inHandler(this.data).catchNullState()
        is State.Failure -> this
        is State.Loading -> this
        is State.Refresh -> this
    }
}

suspend inline fun <reified T : Any, reified R : Any> State<List<T>>.mapEachTo(inHandler: suspend (T) -> R?): State<List<R>> {
    return when (this) {
        is State.Success<List<T>> -> this.data.mapNotNull { inHandler(it) }.toSuccess()
        is State.Failure -> this
        is State.Loading -> this
        is State.Refresh -> this
    }
}

inline fun <reified T : Any> T?.getStateData() = this.runCatching { this!! }.fold(onSuccess = {
    it.toSuccess()
}, onFailure = {
    it.toFailure()
})

suspend fun <T : Any> State<T>.applyIfSuccess(inHandler: suspend (T) -> Unit): State<T> {
    if (this is State.Success<T>) inHandler(this.data)
    return this
}

inline fun <reified T : Any> State<T>.fold(
    noinline onSuccess: ((T) -> Unit)? = null,
    noinline onFailure: ((Throwable) -> Unit)? = null,
    noinline onLoading: (() -> Unit)? = null
) {
    when (this) {
        is State.Success<T> -> onSuccess?.invoke(this.data)
        is State.Failure -> onFailure?.invoke(this.ex)
        is State.Loading -> onLoading?.invoke()
        else -> {}
    }
}

data class ResponseData(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String?
)