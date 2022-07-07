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

package com.fulldive.wallet.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.fulldive.wallet.models.AppIcon
import io.reactivex.Single

@Dao
interface AppIconDao : BaseDao<AppIcon> {

    @Query("SELECT * FROM AppIcon")
    fun getAll(): List<AppIcon>

    @Query("SELECT iconUrl FROM AppIcon WHERE appId=:appId")
    fun getAppIconByAppId(appId: String): Single<String>
}