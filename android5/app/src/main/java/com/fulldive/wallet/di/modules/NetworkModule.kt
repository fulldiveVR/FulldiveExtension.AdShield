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

import com.fulldive.wallet.remote.FullDiveApi
import com.fulldive.wallet.remote.FulldiveRestApiProvider
import com.joom.lightsaber.Module
import com.joom.lightsaber.Provide
import org.adshield.BuildConfig
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provide
    @Singleton
    fun provideFulldiveRestApiProvider(): FulldiveRestApiProvider {
        return FulldiveRestApiProvider()
    }

    @Singleton
    @Provide
    fun provideFullDiveApi(fulldiveRestApiProvider: FulldiveRestApiProvider): FullDiveApi {
        return fulldiveRestApiProvider.getRetrofitApi("${BuildConfig.API_URL}/")
    }
}