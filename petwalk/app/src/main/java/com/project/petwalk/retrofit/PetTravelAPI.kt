package com.project.petwalk.retrofit

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PetTravelAPI:Application() {
    var networkService: INetworkService
    val retrofit:Retrofit

        get()= Retrofit.Builder()
            .baseUrl("http://www.pettravel.kr/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    init {
        networkService=retrofit.create(INetworkService::class.java)
    }
}