package com.project.petwalk.frag

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.petwalk.R
import com.project.petwalk.databinding.FragmentFragMypageBinding
import com.project.petwalk.databinding.FragmentFragWalkBinding
import com.project.petwalk.model.LocationModel
import com.project.petwalk.model.Travel
import com.project.petwalk.model.TravelList
import com.project.petwalk.retrofit.PetTravelAPI
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FragWalk : Fragment() {

    lateinit var binding: FragmentFragWalkBinding

    //지도
    private lateinit var mMap: GoogleMap

    //현재위치
    lateinit var manager: LocationManager

    //파이어베이스
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val reference: DatabaseReference =database.getReference("locations")

    //시작 시, 데이터베이스에 저장할 key 값
    lateinit var key:String
    // 위치 정보를 저장할 리스트
    lateinit var locations:ArrayList<LocationModel>

    var initTime = 0L
    var pauseTime = 0L

    /**
     * 처음 지도 화면 (현재 위치로 카메라 이동)
     */
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        if(locations!=null){
            val userLocation = locations[0]
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(userLocation.latitude, userLocation.longitude))
                .zoom(19F)
                .build()
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    /**
     * 동반가능 시설 위치 마커 찍기
     */
    private fun drawMark(travelList:List<Travel>){
        for(travel in travelList){
            val lat= travel.latitude.toDouble()
            val lng= travel.longitude.toDouble()
            val title= travel.title

            val position = LatLng(lat, lng)

            val markerOptions = MarkerOptions()
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))


            mMap.addMarker(markerOptions.position(position).title(title))
        }
    }

    /**
     * 경로 그리기
     */
    private fun drawPath(){
        if(locations.size>1){
            val size= locations.size-1
            val start = LatLng(locations[size-1].latitude, locations[size-1].longitude)
            val end = LatLng(locations[size].latitude, locations[size].longitude)
            val options:PolylineOptions  = PolylineOptions().add(start).add(end).width(60F).color(R.color.path_green).geodesic(true);
            mMap.addPolyline(options)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(end, 19F))
        }
    }

    /**
     * 실시간 위치 좌표 저장하기
     */
    private val listener: LocationListener = object:LocationListener
    {
        override fun onLocationChanged(location: Location) {
            sendLocation(key, location.latitude, location.longitude, location.time)
            locations.add(LocationModel(location.latitude, location.longitude, location.time))
            drawPath()
            Log.d("pet","${location.latitude},${location.longitude}, ${location.time}")
            Log.d("pet", locations.toString())
        }

    }

    /**
     * 파이어베이스에 위치정보 저장
     */
    private fun sendLocation(key:String, lat:Double, lon:Double, time:Long){
        val location= LocationModel( lat, lon, time)
        reference.child(key).child((locations.size-1).toString()).setValue(location)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFragWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locations = ArrayList()
        manager= activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?


        /**
         *  위치 권한 승락/거부 시 동작
         */
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
            if(it){
                Log.d("pet","access...")
            }else{
                Toast.makeText(this.context,
                    "권한 설정이 거부되었습니다.\n앱을 사용하시려면 다시 실행해주세요.",
                    Toast.LENGTH_SHORT).show()
                    activity?.finish()
            }
        }

        /**
         * 위치 권한 확인
         */
        if(this.context?.let { ContextCompat.checkSelfPermission(it, "android.permission.ACCESS_FINE_LOCATION") } == PackageManager.PERMISSION_GRANTED){
            val location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let {
                locations.add(LocationModel(location.latitude, location.longitude, location.time))
                mapFragment?.getMapAsync(callback)
            }
        }else{
            permissionLauncher.launch("android.permission.ACCESS_FINE_LOCATION")
        }



        binding.startBtn.setOnClickListener {

            //동작 화면 넘어가기 ( 시간 및 거리 측정 시작)
            binding.beforeFrame.visibility= View.GONE
            binding.ingFrame.visibility=View.VISIBLE

            //시간 측정
            binding.chronometer.base=SystemClock.elapsedRealtime()+pauseTime
            binding.chronometer.start()

            //저장할 키 생성
            key= reference.push().key!!

            // todo  최소 10초, 최소 10m 마다 위치 확인
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 0f, listener)

            /**
             * 반려동물 동반 장소 api 사용
             */
            val travelNetService = (activity?.applicationContext as PetTravelAPI).networkService
            val travelListCall = travelNetService.doGetTravelList("1","133","PC01")

            travelListCall.enqueue(object: Callback<List<TravelList>> {
                override fun onResponse(
                    call: Call<List<TravelList>>,
                    response: Response<List<TravelList>>
                ) {
                    if(response.isSuccessful){
                        Log.d("pet", response.body()?.get(0)?.resultList.toString())
                        val travelList:List<Travel> = response.body()?.get(0)?.resultList!!
                        drawMark(travelList)


                    }else{
                        Log.d("pet", "failed")
                    }
                }

                override fun onFailure(call: Call<List<TravelList>>, t: Throwable) {
                    call.cancel()
                }


            })
        }

        /**
         * todo 일시정지
         */
        binding.parseBtn.setOnClickListener{
            //중지
            pauseTime=binding.chronometer.base-SystemClock.elapsedRealtime()
            binding.chronometer.stop()

            binding.parseLayout.visibility=View.VISIBLE
            binding.parseBtn.visibility=View.GONE
        }
        
        /**
         * todo 다시시작
         */
        binding.restartBtn.setOnClickListener{
            binding.chronometer.base=SystemClock.elapsedRealtime()+pauseTime
            binding.chronometer.start()

            binding.parseLayout.visibility=View.GONE
            binding.parseBtn.visibility=View.VISIBLE
        }

        /**
         * todo 중지
         */
        binding.endBtn.setOnClickListener{
            pauseTime=0L
            binding.chronometer.base=SystemClock.elapsedRealtime()
            binding.chronometer.stop()

            binding.beforeFrame.visibility=View.VISIBLE
            binding.ingFrame.visibility=View.GONE
            binding.parseBtn.visibility=View.VISIBLE
            binding.parseLayout.visibility=View.GONE
            manager.removeUpdates(listener)
        }

    }

}

