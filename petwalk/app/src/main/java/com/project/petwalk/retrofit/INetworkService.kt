package com.project.petwalk.retrofit

import com.project.petwalk.model.Travel
import com.project.petwalk.model.TravelDetail
import com.project.petwalk.model.TravelDetailList
import com.project.petwalk.model.TravelList
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

interface INetworkService {
    @GET("listPart.do")
    fun doGetTravelList(@Query("page") page:String,
                      @Query("pageBlock") pageBlock:String,
                      @Query("partCode")partCode:String):Call<List<TravelList>>
    @GET("detailSeqPart.do")
    fun doGetTravelDetail(@Query("partCode")partCode: String,
                        @Query("contentNum")contentNum:String):Call<List<TravelDetailList>>
}