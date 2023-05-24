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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fulldive.wallet.models.AppIcon
import com.fulldive.wallet.models.LeaderEntity

@Database(
    entities = [
        LeaderEntity::class,
        AppIcon::class
    ], version = 2
)

abstract class AdshieldDatabase : RoomDatabase() {

    abstract fun getLeaderEntityDao(): LeaderEntityDao
    abstract fun getAppIconDao(): AppIconDao

    companion object {
        private const val TAG = "AdshieldDatabase"

        @Volatile
        private var INSTANCE: AdshieldDatabase? = null
        private const val DATABASE_NAME = "Adshield.db"

        fun getInstance(context: Context): AdshieldDatabase? {
            if (INSTANCE == null) {
                synchronized(AdshieldDatabase::class.java) {
                    if (INSTANCE == null) {

                        val builder = Room
                            .databaseBuilder(
                                context.applicationContext,
                                AdshieldDatabase::class.java,
                                DATABASE_NAME
                            )
                            .addMigrations(
                                MIGRATION_1_2
                            )

                        INSTANCE = builder.build()
                    }
                }
            }
            return INSTANCE
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE AppIcon (" +
                            "appId TEXT PRIMARY KEY NOT NULL," +
                            "iconUrl TEXT NOT NULL" +
                            ")"
                )
            }
        }
    }
}