package remoteconfig

fun IRemoteConfigFetcher.isAdShieldProLimited() =
    getRemoteBoolean("is_adshield_pro_limited")

fun IRemoteConfigFetcher.isAdShieldLegalDescriptionEnabled() =
    getRemoteBoolean("is_adshield_legal_description_enabled")

fun IRemoteConfigFetcher.isAdShieldWebAppSettingsLimited() =
    getRemoteBoolean("is_adshield_web_apps_settings_limited")

fun IRemoteConfigFetcher.isAdShieldWebCustomSettingsLimited() =
    getRemoteBoolean("is_adshield_web_custom_settings_limited")

fun IRemoteConfigFetcher.isAdShieldAdsCounterLimited() =
    getRemoteBoolean("is_adshield_ads_counter_limited")

fun IRemoteConfigFetcher.getCurrentVersion() =
    getRemoteLong("adshield_current_version").toInt()

fun IRemoteConfigFetcher.getIsRewardsLimited() =
    getRemoteBoolean("is_adshield_rewards_limited")

fun IRemoteConfigFetcher.getIsStatsLimited() =
    getRemoteBoolean("is_adshield_stats_limited")