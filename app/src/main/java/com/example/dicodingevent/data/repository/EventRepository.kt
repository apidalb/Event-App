package com.example.dicodingevent.data.repository

import com.example.dicodingevent.data.local.database.FavoriteEventDao
import com.example.dicodingevent.data.local.favorite.RoomDBFavoriteEvent
import com.example.dicodingevent.data.response.EventDetailResponse
import com.example.dicodingevent.data.response.EventResponse
import com.example.dicodingevent.data.response.ListEventsItem
import com.example.dicodingevent.data.retrofit.ApiServices
import kotlinx.coroutines.delay
import retrofit2.Response

class EventRepository(
    private val apiServices: ApiServices, private val favoriteEventDao: FavoriteEventDao) {
    private suspend fun delay() {
        delay(0)
    }

    suspend fun getActiveEvents(): Response<EventResponse> {
        delay()
        return apiServices.getHomeActiveEvent()
    }

    suspend fun getFinishedEvents(): Response<EventResponse> {
        delay()
        return apiServices.getFinishedEvent()
    }

    suspend fun getEventDetail(eventId: Int): Response<EventDetailResponse> {
        delay()
        return apiServices.getDetailEvent(eventId)
    }

    suspend fun insertItemFavorite(event: RoomDBFavoriteEvent) {
        favoriteEventDao.insertItemFavorite(event)
    }

    suspend fun deleteFavoriteEvent(eventId: String) {
        favoriteEventDao.deleteItemFavorite(eventId)
    }

    suspend fun getFavoriteEvents(): List<RoomDBFavoriteEvent> {
        return favoriteEventDao.getAllListFavorite()
    }

    suspend fun fetchNearestActiveEvent(): ListEventsItem? {
        val response = apiServices.getNearestActiveEvent()
        return if (response.isSuccessful) {
            response.body()?.listEvents?.firstOrNull()
        } else {
            null
        }
    }
}
