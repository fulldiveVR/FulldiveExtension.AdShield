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

package repository

import com.fulldive.wallet.di.modules.DefaultRepositoryModule
import com.fulldive.wallet.extensions.safeCompletable
import com.fulldive.wallet.local.dao.AdshieldDatabase
import com.fulldive.wallet.models.AppIcon
import com.joom.lightsaber.ProvidedBy
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@ProvidedBy(DefaultRepositoryModule::class)
class AppIconLocalDataSource @Inject constructor(database: AdshieldDatabase) {

    val appIconDao = database.getAppIconDao()

    fun getAllAppIcons(): List<AppIcon> {
        return appIconDao.getAll()
    }

    fun getAppIconByAppId(appId: String): Single<String> {
        return appIconDao.getAppIconByAppId(appId)
    }

    fun saveAppIcon(appIcon: AppIcon): Completable {
        return safeCompletable { appIconDao.insert(appIcon) }
    }
}