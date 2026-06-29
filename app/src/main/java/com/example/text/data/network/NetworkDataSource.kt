package com.example.text.data.network

import com.example.text.data.network.dto.CourseSuggestionDto
import com.example.text.data.network.dto.SemesterInfoDto
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkDataSource {
    private val client = OkHttpClient.Builder()
        .addInterceptor(MockInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://mock.api/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiService::class.java)

    suspend fun searchCourseSuggestions(query: String): Result<List<CourseSuggestionDto>> = runCatching {
        api.searchCourseSuggestions(query)
    }

    suspend fun getSemesterInfo(): Result<SemesterInfoDto> = runCatching {
        api.getSemesterInfo()
    }
}
