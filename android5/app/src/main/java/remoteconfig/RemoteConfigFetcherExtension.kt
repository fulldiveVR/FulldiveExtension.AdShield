package remoteconfig

fun IRemoteConfigFetcher.isAdShieldProLimited() =
    getRemoteBoolean("is_adshield_pro_limited")

fun IRemoteConfigFetcher.isAdShieldLegalDescriptionEnabled() =
    getRemoteBoolean("is_adshield_legal_description_enabled")