package com.fulldive.wallet.local.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fulldive.wallet.models.LeaderEntity

@Database(
    entities = [
        LeaderEntity::class
    ], version = 1
)

abstract class AdshieldDatabase : RoomDatabase() {

    abstract fun getLeaderEntityDao(): LeaderEntityDao

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
                        // .addMigrations()

                        INSTANCE = builder.build()
                    }
                }
            }
            return INSTANCE
        }
    }
}