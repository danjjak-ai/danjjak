package com.danjjak.data.remote

import com.danjjak.data.remote.dto.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface DanjjakApi {
    @POST("api/journal")
    suspend fun saveJournal(@Body request: JournalRequest): JournalResponse

    @GET("api/nudge")
    suspend fun getAdvice(): AdviceResponse

    @POST("api/feedback")
    suspend fun sendFeedback(@Body request: FeedbackRequest): FeedbackResponse

    @POST("api/meal")
    suspend fun saveMeal(@Body request: MealRequest): MealResponse
}

object ApiService {
    // 안드로이드 에뮬레이터에서 로컬호스트로 접속하는 주소 (실기기 테스트시에는 IP 변경 필요)
    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: DanjjakApi = retrofit.create(DanjjakApi::class.java)
}
