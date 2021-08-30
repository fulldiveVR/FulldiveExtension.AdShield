package service

import android.os.Build
import model.DeviceId
import org.adshield.BuildConfig

object EnvironmentService {

    fun getDeviceAlias(): String {
        val brand = Build.MANUFACTURER
        val model = Build.MODEL
        return "$brand $model"
    }

    fun getUserAgent(): String {
        val version = BuildConfig.VERSION_NAME
        val androidVersion = Build.VERSION.SDK_INT
        val flavor = getFlavor()
        val type = BuildConfig.BUILD_TYPE
        val arch = Build.SUPPORTED_ABIS[0]
        val brand = Build.MANUFACTURER
        val device = Build.DEVICE
        val touch = if (isSupportingTouch()) "touch" else "donttouch"
        val compatible = if (isCompatible()) "compatible" else "incompatible"
        return if (isFdroid())
            "adshield/5.x.x (android-droid droid droid droid droid droid droid compatible)"
        else
            "adshield/$version (android-$androidVersion $flavor $type $arch $brand $device $touch api $compatible)"
    }

    fun isPublicBuild(): Boolean {
        return BuildConfig.BUILD_TYPE == "release"
    }

    fun isSlim(): Boolean {
        return BuildConfig.FLAVOR == "google" && !escaped
    }

    fun isFdroid(): Boolean {
        return BuildConfig.FLAVOR == "droid"
    }

    fun getFlavor(): String {
        return if (escaped) "escaped" else BuildConfig.FLAVOR
    }

    fun getBuildName(): String {
        return "${getFlavor()}${BuildConfig.BUILD_TYPE.capitalize()}"
    }

    fun getVersionCode() = BuildConfig.VERSION_CODE

    fun getDeviceId(): DeviceId {
        return getDeviceAlias() // TODO: more unique
    }

    fun isCompatible(): Boolean {
        val device = Build.MANUFACTURER.toLowerCase()
        return when {
            device.startsWith("realme") -> false
            device.startsWith("oppo") -> false
            !isSupportingTouch() -> false
            else -> true
        }
    }

    fun isSupportingTouch(): Boolean {
        val ctx = ContextService.requireContext()
        return ctx.packageManager.hasSystemFeature("android.hardware.touchscreen")
    }

    var escaped = false

}
