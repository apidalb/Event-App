package com.example.dicodingevent.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dicodingevent.data.local.favorite.RoomDBFavoriteEvent

@Database(entities = [RoomDBFavoriteEvent::class], version = 1, exportSchema = false)
abstract class AppDatabaseRoomEvent : RoomDatabase() {
    abstract fun favoriteEventDao(): FavoriteEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabaseRoomEvent? = null

        fun getDatabaseInstance(context: Context): AppDatabaseRoomEvent {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabaseRoomEvent::class.java,
                    "favorite_db_event"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
