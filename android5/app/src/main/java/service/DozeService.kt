package service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import model.BlokadaException
import utils.Logger

object DozeService {

    private val log = Logger("Doze")
    private lateinit var powerManager: PowerManager

    var onDozeChanged = { isDoze: Boolean -> }

    fun setup(ctx: Context) {
        ctx.registerReceiver(DozeReceiver(), IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED))
        powerManager = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        log.v("Registered DozeReceiver")
    }

    fun isDoze() = powerManager.isDeviceIdleMode

    internal fun dozeChanged() {
        val doze = powerManager.isDeviceIdleMode
        log.v("Doze changed: $doze")
        onDozeChanged(doze)
    }

    fun ensureNotDoze() {
        if (isDoze()) throw BlokadaException("Doze mode detected")
    }
}

class DozeReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, p1: Intent) {
        DozeService.dozeChanged()
    }
}
