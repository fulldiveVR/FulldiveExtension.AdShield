package service

import android.app.Activity
import android.net.VpnService
import utils.Logger

object VpnPermissionService {

    private val log = Logger("VpnPerm")
    private val context = ContextService

    var onPermissionGranted = {}

    fun hasPermission(): Boolean {
        return VpnService.prepare(context.requireContext()) == null
    }

    fun askPermission() {
        log.w("Asking for VPN permission")
        val activity = context.requireContext()
        if (activity !is Activity) {
            log.e("No activity context available")
            return
        }

        VpnService.prepare(activity)?.let { intent ->
            activity.startActivityForResult(intent, 0)
        } ?: onPermissionGranted()
    }

    fun resultReturned(resultCode: Int) {
        if (resultCode == -1) onPermissionGranted()
        else log.w("VPN permission not granted, returned code $resultCode")
    }

}