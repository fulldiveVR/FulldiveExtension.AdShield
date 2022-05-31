package com.fulldive.wallet.di.modules

import android.content.Context
import com.fulldive.wallet.local.dao.AdshieldDatabase
import com.joom.lightsaber.Module
import com.joom.lightsaber.Provide
import javax.inject.Singleton

@Module
class DatabaseModule(val context: Context) {

    @Provide
    @Singleton
    fun provideAdshieldBaseData(): AdshieldDatabase {
        return AdshieldDatabase.getInstance(context)
            ?: throw IllegalStateException("AdshieldDatabase can't be null")
    }
}
