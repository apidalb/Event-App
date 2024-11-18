package com.example.dicodingevent.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dicodingevent.data.local.favorite.RoomDBFavoriteEvent


@Dao
interface FavoriteEventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemFavorite(event: RoomDBFavoriteEvent)

    @Query("DELETE FROM favorite_db_event WHERE eventID = :eventId")
    suspend fun deleteItemFavorite(eventId: String)

    @Query("SELECT * FROM favorite_db_event")
    suspend fun getAllListFavorite(): List<RoomDBFavoriteEvent>
}