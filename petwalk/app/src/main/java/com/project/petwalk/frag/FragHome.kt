package com.project.petwalk.frag

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.petwalk.databinding.FragmentFragHomeBinding
import com.project.petwalk.home.retrofit.ITEM
import com.project.petwalk.home.retrofit.WEATHER
import com.project.petwalk.home.retrofit.WeatherAPI
import com.project.petwalk.model.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class FragHome : Fragment() {

    lateinit var binding: FragmentFragHomeBinding
    lateinit var weather: Weather
//    var mLocationManager: LocationManager? = null
//    var mLocationListener: LocationListener? = null
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//    }
    private var base_date = "20220510"  // 발표 일자
    private var base_time = "1400"      // 발표 시각
    private var nx = "55"               // 예보지점 X 좌표
    private var ny = "127"              // 예보지점 Y 좌표


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFragHomeBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weather=Weather()

        binding.animationView.setOnClickListener {
            //binding.animationView.isAnimating
            binding.animationView.playAnimation()
        }

//
        weather.rainType = "3" // 날씨 조작
        if(weather!!.rainType=="2" || weather!!.rainType=="1"){ //비
            binding.weatherAnimationView.setAnimationFromUrl("https://assets8.lottiefiles.com/private_files/lf30_orqfuyox.json")
        } else if(weather!!.rainType=="3"){ //눈
            binding.weatherAnimationView.setAnimationFromUrl("https://assets6.lottiefiles.com/temp/lf20_WtPCZs.json")
        }else if(weather!!.sky == "3" || weather!!.sky == "4"){ //구름
            binding.weatherAnimationView.setAnimationFromUrl("https://assets5.lottiefiles.com/packages/lf20_wfx6naii.json")
        } else //해

        getWeather()

    }
    @SuppressLint("SimpleDateFormat")
    private fun getWeather(){
        val networkService=(activity?.applicationContext as WeatherAPI).networkService

        val sdf = SimpleDateFormat("yyyyMMdd")
        val hdf = SimpleDateFormat("HH")

        val currentTime = sdf.format(System.currentTimeMillis())
        val curHour = hdf.format(System.currentTimeMillis())
        val timeStr = curHour+"00"
        Log.d("pet",timeStr)


        val userListCall=networkService.getWeather(60,1,"JSON",currentTime, timeStr, "55","127"   )
        userListCall.enqueue(object : Callback<WEATHER> {
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if(response.isSuccessful){
                    val it: List<ITEM> = response.body()!!.response.body.items.item
                    weather=Weather()
                    var index = 0
                    when(it[0].category) {
                        "PTY" -> weather!!.rainType = it[0].fcstValue     // 강수 형태
                        "SKY" -> weather!!.sky = it[0].fcstValue          // 하늘 상태
                        "T1H" -> weather!!.temp = it[0].fcstValue         // 기온
                    }
                        index++

                    // 각 날짜 배열 시간 설정
                    weather!!.fcstTime = it[0].fcstTime

                    Log.d("pet",">>>>>>>>>>>>>>>>>>"+ weather!!.rainType)

                }

            }

            override fun onFailure(call: Call<WEATHER>, t: Throwable) {

            }


        })
    }
}


