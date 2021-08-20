package appextension

sealed class AppExtensionState(val id: String) {
    object START: AppExtensionState("start")
    object STOP: AppExtensionState("stop")
    object FAILURE: AppExtensionState("failure")
}