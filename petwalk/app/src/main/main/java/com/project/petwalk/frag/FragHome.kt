package com.project.petwalk.frag

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.project.petwalk.R
import com.project.petwalk.databinding.FragmentFragHomeBinding
import com.project.petwalk.home.retrofit.ITEM
import com.project.petwalk.home.retrofit.WEATHER
import com.project.petwalk.home.retrofit.WeatherAPI
import com.project.petwalk.model.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat


class FragHome : Fragment(){

    private var locationManager: LocationManager? = null
    private val REQUEST_CODE_LOCATION = 2

    lateinit var binding: FragmentFragHomeBinding
    lateinit var weather: Weather

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

        // 실시간 위치 nx,ny 받기-----------------------------------------------------------------------------------------------------------


        //사용자의 현재 위치
        getWeather()

    }
    /**
     * 사용자의 위치를 수신
     */
    private fun getMyLocation() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } !== PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } !== PackageManager.PERMISSION_GRANTED
        ) {
            //println("////////////사용자에게 권한을 요청해야함")
            activity?.let {
                ActivityCompat.requestPermissions(
                    it, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION
                )
            }
            getMyLocation() //권한 승인하면 즉시 위치값 받아오려고 사용. 필수X
        } else {
            //사용자의 위치 수신을 위한 세팅
            locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L,0F,listener)
        }
    }

    private val listener: LocationListener = object:LocationListener{
        override fun onLocationChanged(location: Location) {
            nx=location.latitude.toInt().toString()
            ny=location.longitude.toInt().toString()
            getWeather()
            locationManager?.removeUpdates(this)
        }

    }
    //---------------------------------------------------------------------------------------------------------

    // 날씨 api에서 날씨데이터 받와서 필요한데이터만 추출하기------------------------------------------------------------
    @SuppressLint("SimpleDateFormat")
    private fun getWeather(){

        val sdf = SimpleDateFormat("yyyyMMdd")
        val hdf = SimpleDateFormat("HH")

        val currentTime = sdf.format(System.currentTimeMillis())
        val curHour = (hdf.format(System.currentTimeMillis()).toInt()-1).toString()
        val timeStr = curHour+"00"
        Log.d("kej", "weather >>>>> $currentTime, $timeStr, $nx, $ny")


        val userListCall=WeatherAPI.networkService.getWeather(60,1,"JSON",currentTime, timeStr, "35", "128" )
        userListCall.enqueue(object : Callback<WEATHER> {
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if(response.isSuccessful){
                    if (response.body()!!.response.header.resultCode==1){
                        Log.d("kej", response.body().toString())
                        return
                    }
                    Log.d("kej", response.body().toString())
                    val it: List<ITEM> = response.body()!!.response.body.items.item

                    val weatherArr = arrayOf(Weather(), Weather(), Weather(), Weather(), Weather(), Weather())

                    var index = 0
                    val totalCount = response.body()!!.response.body.totalCount - 1
                    for (i in 0..totalCount) {
                        index %= 6
                        when(it[i].category) {
                            "PTY" -> weatherArr[index].rainType = it[i].fcstValue     // 강수 형태
                            "SKY" -> weatherArr[index].sky = it[i].fcstValue          // 하늘 상태
                            "T1H" -> weatherArr[index].temp = it[i].fcstValue         // 기온
                            else -> continue
                        }
                        index++
                    }
                    //날짜 배열 시간 설정
                    for (i in 0..5) weatherArr[i].fcstTime = it[i].fcstTime
                    weather = weatherArr[0]
                    // 각 날짜 배열 시간 설정
                    Log.d("pet",">>>>>>>>>>>>>>>>>>"+weatherArr[0].toString())
                    getRainImage(weather.rainType!!, weather.sky!!)
                    changeWeatherImage()
                }

            }

            override fun onFailure(call: Call<WEATHER>, t: Throwable) {

            }


        })
    }
    //---------------------------------------------------------------------------------

    // 현재 날씨별 아이콘변경부분 ---------------------------------------------------------------------------------------------------------------
//        weather.rainType = "3" // 날씨 조작
    fun changeWeatherImage() {
        if (weather!!.rainType == "2" || weather!!.rainType == "1") { //비
            binding.weatherAnimationView.setAnimationFromUrl("https://assets8.lottiefiles.com/private_files/lf30_orqfuyox.json")
        } else if (weather!!.rainType == "3") { //눈
            binding.weatherAnimationView.setAnimationFromUrl("https://assets6.lottiefiles.com/temp/lf20_WtPCZs.json")
        } else if (weather!!.sky == "3" || weather!!.sky == "4") { //구름
            binding.weatherAnimationView.setAnimationFromUrl("https://assets5.lottiefiles.com/packages/lf20_wfx6naii.json")
        } else {
        }//해

    }
    //        ---


    // 강수 형태
    fun getRainImage(rainType : String, sky: String) {
        var str = when(rainType) {
            "0" -> getWeatherImage(sky)
            "1" -> "날씨 : 비"
            "2" -> "날씨 : 헤일"
            "3" -> "날씨 : 눈"
            "4" -> "날씨 : 소나기"
            else -> getWeatherImage(sky)
        }
        binding.tvSky.text=str
    }

    fun getWeatherImage(sky : String):String {
        // 하늘 상태
        var str=""
        when(sky) {
            "1" -> str="날씨 : 맑음"                      // 맑음
            "3" ->  str="날씨 : 구름많음"                     // 구름 많음
            "4" -> str="날씨 : 흐림"                 // 흐림
            else -> R.drawable.ic_launcher_foreground   // 오류
        }
        return str
    }
}