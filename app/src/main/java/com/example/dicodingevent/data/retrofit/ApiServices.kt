package com.example.dicodingevent.data.retrofit

import com.example.dicodingevent.data.response.EventDetailResponse
import com.example.dicodingevent.data.response.EventResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServices {
    @GET("events")
    suspend fun getHomeActiveEvent(
        @Query("active") active: Int = 1
    ): Response<EventResponse>

    @GET("events")
    suspend fun getFinishedEvent(
        @Query("active") active: Int = 0
    ): Response<EventResponse>

    @GET("events/{id}")
    suspend fun getDetailEvent(
        @Path("id") id: Int
    ): Response<EventDetailResponse>

    @GET("events")
    suspend fun getNearestActiveEvent(
        @Query("active") active: Int = -1,
        @Query("limit") limit: Int = 1
    ): Response<EventResponse>
}