/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.fulldive.wallet.di.modules

import android.content.Context
import remoteconfig.FirebaseConfigurationFetcher
import remoteconfig.IRemoteConfigFetcher
import com.fulldive.wallet.rx.AppSchedulers
import com.fulldive.wallet.rx.ISchedulersProvider
import com.joom.lightsaber.Module
import com.joom.lightsaber.Provide
import javax.inject.Singleton

@Singleton
@Module
class ApplicationConfigurationModule(private val context: Context) {

    @Provide
    fun getAppContext(): Context = context

    @Singleton
    @Provide
    fun provideSchedulersProvider(): ISchedulersProvider = AppSchedulers

    @Singleton
    @Provide
    fun getConfigurationFetcher(): IRemoteConfigFetcher = FirebaseConfigurationFetcher()
}
