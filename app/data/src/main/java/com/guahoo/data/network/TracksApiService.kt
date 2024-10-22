package com.guahoo.data.network

import com.guahoo.data.response.OverpassResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TracksApiService {
    @GET("api/interpreter")
    suspend fun getTracksByArea(
        @Query("data") tracks: String
    ): OverpassResponse

}