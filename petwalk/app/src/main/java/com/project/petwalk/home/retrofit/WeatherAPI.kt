package com.project.petwalk.home.retrofit

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherAPI{
    var networkService: INetworkService
    val retrofit: Retrofit= Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    init {
        networkService=retrofit.create(INetworkService::class.java)
    }

}