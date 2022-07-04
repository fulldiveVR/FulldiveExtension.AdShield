package com.fulldive.wallet.di.modules

import analytics.FulldiveActionTracker
import analytics.IActionTracker
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import com.joom.lightsaber.Module
import com.joom.lightsaber.Provide
import analytics.ITagReader
import javax.inject.Singleton

@Singleton
@Module
class ApplicationInfrastructureModule(private val context: Context) {

    @Singleton
    @Provide
    fun getResources(): Resources = context.resources

    @Singleton
    @Provide
    fun getAssetManager(): AssetManager = context.assets

    @Singleton
    @Provide
    fun getContentResolver(): ContentResolver = context.contentResolver

    @Provide
    @Singleton
    fun getActionTracker(
        tagReader: ITagReader
    ): IActionTracker = FulldiveActionTracker(context, tagReader)
}
