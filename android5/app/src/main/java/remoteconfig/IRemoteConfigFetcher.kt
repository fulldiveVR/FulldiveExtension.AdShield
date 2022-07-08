package remoteconfig

import io.reactivex.Completable

interface IRemoteConfigFetcher {
    fun fetch(force: Boolean): Completable
    fun getRemoteBoolean(value: String): Boolean
    fun getRemoteString(value: String): String
    fun getRemoteLong(value: String): Long
    fun getRemoteDouble(value: String): Double
    fun getAll(): Map<String, String>
}