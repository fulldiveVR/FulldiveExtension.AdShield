package remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.reactivex.Completable
import org.adshield.R
import java.util.concurrent.CancellationException

class FirebaseConfigurationFetcher : IRemoteConfigFetcher {
    private val remoteConfig = FirebaseRemoteConfig
        .getInstance()
        .apply {
            setDefaultsAsync(R.xml.config_defaults)
        }

    override fun fetch(force: Boolean): Completable {
        return Completable.create { emitter ->
            try {
                val remoteConfigSettings = FirebaseRemoteConfigSettings
                    .Builder()
                    .setFetchTimeoutInSeconds(if (force) 0L else 3600L)
                    .build()

                remoteConfig.setConfigSettingsAsync(remoteConfigSettings)

                remoteConfig
                    .fetchAndActivate()
                    .addOnSuccessListener {
                        emitter.onComplete()
                    }
                    .addOnFailureListener {
                        emitter.tryOnError(it)
                    }
                    .addOnCanceledListener {
                        emitter.tryOnError(CancellationException())
                    }
            } catch (ex: Exception) {
                emitter.tryOnError(ex)
            }
        }
    }

    override fun getRemoteBoolean(value: String) = remoteConfig.getBoolean(value)

    override fun getRemoteString(value: String) = remoteConfig.getString(value)

    override fun getRemoteLong(value: String) = remoteConfig.getLong(value)

    override fun getRemoteDouble(value: String) = remoteConfig.getDouble(value)

    override fun getAll(): Map<String, String> {
        val result = HashMap<String, String>()
        val pairs = remoteConfig.all
        pairs.keys.forEach { key ->
            result[key] = pairs[key]?.asString().orEmpty()
        }
        val info = remoteConfig.info
        result["lastFetchStatus"] = info.lastFetchStatus.toString()
        result["fetchTimeMillis"] = info.fetchTimeMillis.toString()
        return result
    }
}
