package com.okane.paperaero.lobby

import com.okane.paperaero.network.RetrofitClient
import com.okane.paperaero.test.TestApiService
import com.okane.paperaero.test.UserResponse
import retrofit2.http.GET

interface LobbyApiService {

    @GET("test/dummyNumber")
    suspend fun getDummyNumber(): String
}
val lobbyApi: LobbyApiService by lazy {
    RetrofitClient.instance.create(LobbyApiService::class.java)
}