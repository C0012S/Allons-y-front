package com.example.harumub_front

import retrofit2.Call
import retrofit2.http.*
import java.util.*
import kotlin.collections.HashMap

interface RetrofitInteface {
    @POST("/login")
    fun executeLogin(@Body map: HashMap<String, String>): Call<LoginResult?>?

    @POST("/signup")
    fun executeSignup(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/makeRoom")
    fun executeMakeRoom(@Body map: HashMap<String, String>): Call<MakeRoomResult?>?

    @POST("/recommend1")
    fun executeRecommend1(@Body map: HashMap<String, String>): Call<List<String>?>?

    @POST("/sceneAnalyze")
    fun executeSceneAnalyze(@Body map: HashMap<String, String>): Call<List<String>?>?

    @POST("/email")
    fun executeEmail(@Body map: HashMap<String, String>): Call<EmailResult?>?

    @POST("/watchList")
    fun executeWatchList(@Body map: HashMap<String, String>): Call<List<WatchListResult?>>?

    @POST("/watchresult")
    fun executeWatchResult(@Body map: HashMap<String, String>): Call<WatchResult?>?

    @POST("/enterroom")
    fun executeEnterRoom(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/recommend2")
    fun executeRecommend2(@Body map: HashMap<String, String>): Call<List<Recommend2Result?>>?

    @POST("/watchAloneStart")
    fun executeWatchAloneStart(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/watchAloneEnd")
    fun executeWatchAloneEnd(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("addReview") // 추가: 리뷰 작성 후 버튼 클릭시 데이터 전달
    fun executeAddReview(@Body map: HashMap<String, String>): Call<Void?>?

    @POST("/logout")
    fun executeLogout(@Body map: HashMap<String, String>): Call<Void?>?

}