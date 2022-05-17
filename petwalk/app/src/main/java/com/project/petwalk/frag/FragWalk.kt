package com.project.petwalk.frag

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.project.petwalk.databinding.FragmentFragWalkBinding
import com.project.petwalk.model.*
import com.project.petwalk.retrofit.PetTravelAPI
import com.project.petwalk.walk.WalkResultActivity
import com.project.petwalk.walk.WalkTravelDetailActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

class FragWalk : Fragment(), GoogleMap.OnMarkerClickListener {

    lateinit var binding: FragmentFragWalkBinding

    //지도
    private lateinit var mMap: GoogleMap

    //동반 시설 분류 코드
    val PART_CODE = arrayOf("PC01", "PC02", "PC03", "PC04", "PC05")

    //현재위치
    lateinit var manager: LocationManager

    //파이어베이스
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val reference: DatabaseReference =database.getReference("locations")
    val walkReference:DatabaseReference = database.getReference("walk")

    //시작 시, 데이터베이스에 저장할 key 값
    lateinit var key:String

    // 위치 정보를 저장할 리스트
    lateinit var locations:ArrayList<LocationModel>

    // walk 객체에 저장할 데이터 초기화
    var initTime=0L
    var pauseTime = 0L
    var initDistance= 0.0
    var startDate=0L
    var endDate=0L

    /**
     * 처음 지도 화면 (현재 위치로 카메라 이동)
     */
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        if(locations.size!=0){
            val userLocation = locations[0]
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(userLocation.latitude, userLocation.longitude))
                .zoom(19F)
                .build()
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
//        else {
//            val cameraPosition= CameraPosition.Builder()
//                .target(LatLng(37.747381503046,127.630154310527))
//                .zoom(19F)
//                .build()
//            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
//        }
    }

    /**
     * 동반가능 시설 위치 마커 찍기
     */
    private fun drawMark(travelList:List<Travel>, code:String){
        for(travel in travelList){
            val lat= travel.latitude.toDouble()
            val lng= travel.longitude.toDouble()
            val title= travel.title
            val id = travel.contentSeq


            val position = LatLng(lat, lng)

            val markerOptions = MarkerOptions()
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            val marker: Marker? = mMap.addMarker(markerOptions.position(position).title(title))

            marker?.tag= "$id,$code"
        }
        mMap.setOnMarkerClickListener(this)
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
     *  두 위도 경도 사이의 거리 구하고 누적하기
     */
    private fun addDistance(lat1:Double, lat2:Double, lon1:Double, lon2:Double){
        val R = 6372.8*1000
        val dLat = Math.toRadians(lat2-lat1)
        val dLon = Math.toRadians(lon2-lon1)
        val a = sin(dLat/2)
            .pow(2.0)+sin(dLon/2)
            .pow(2.0)*cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2))
        val c=2*asin(sqrt(a))
        val distance = String.format("%.2f",R*c*0.001).toDouble()

        initDistance+=distance
        binding.distance.text=(initDistance.toString())
    }


    /**
     * 실시간 위치 좌표 리스트에 넣기
     */
    private val listener: LocationListener = object:LocationListener
    {
        override fun onLocationChanged(location: Location) {
            locations.add(LocationModel(location.latitude, location.longitude, location.time))
            
            // 경로 업데이트
            drawPath()
            
            // 이동거리 업데이트
            val size= locations.size-1
            addDistance(
                locations[size-1].latitude,
                locations[size].latitude,
                locations[size-1].longitude,
                locations[size].longitude)
            
            
            Log.d("pet","${location.latitude},${location.longitude}, ${location.time}")
            Log.d("pet", locations.toString())
        }

    }

    /**
     * 파이어베이스에 위치정보 저장
     */
    private fun sendLocation(idx:Int, key:String, lat:Double, lon:Double, time:Long){
        val location= LocationModel( lat, lon, time)
        reference.child(key).child(idx.toString()).setValue(location)
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
            initTime=SystemClock.elapsedRealtime()
            binding.chronometer.base=SystemClock.elapsedRealtime()+pauseTime
            binding.chronometer.start()

            //현재 시각 저장
            startDate = System.currentTimeMillis()


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
                        for(code in PART_CODE){
                            drawMark(travelList,code)
                        }

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
            endDate=System.currentTimeMillis()

            //파이어베이스에 위치 정보 저장하기
            val locKey = reference.push().key!!
            for( i in 0 until locations.size){
                sendLocation(
                    i,
                    locKey,
                    locations[i].latitude,
                    locations[i].longitude,
                    locations[i].time)
            }

            //walk 객체 저장
            val walkKey = walkReference.push().key!!
            val location = mapOf(locKey to true)
            val timeSec = binding.chronometer.drawingTime-binding.chronometer.base

            val walk = Walk(walkKey,initDistance, startDate, endDate, timeSec, location)


            pauseTime=0L
            initDistance=0.0

            binding.chronometer.base=SystemClock.elapsedRealtime()
            binding.chronometer.stop()

            binding.beforeFrame.visibility=View.VISIBLE
            binding.ingFrame.visibility=View.GONE
            binding.parseBtn.visibility=View.VISIBLE
            binding.parseLayout.visibility=View.GONE
            binding.distance.text="0.0"

            manager.removeUpdates(listener)


            val intent = Intent(context, WalkResultActivity::class.java)
            intent.putExtra("walk", walk)
            startActivity(intent)

        }

    }

    /**
     * 마커 클릭 시, 
     * 1...동반 시설 디테일 정보 가져오기
     * 2...액티비티에 정보 보내기
     */
    override fun onMarkerClick(p0: Marker): Boolean {
        val tagList = p0.tag.toString().split(",")
        val number = tagList[0]
        val code = tagList[1]
        var travelDetail:TravelDetail?=null

        
        
        // 동반 시설 디테일 정보 가져오기
        val travelNetService = (activity?.applicationContext as PetTravelAPI).networkService
        val travelDetailCall = travelNetService.doGetTravelDetail(code,number)

        travelDetailCall.enqueue(object: Callback<List<TravelDetailList>> {
            override fun onResponse(
                call: Call<List<TravelDetailList>>,
                response: Response<List<TravelDetailList>>
            ) {
                if(response.isSuccessful){
                    Log.d("pet", response.body()?.get(0)?.resultList.toString())
                    travelDetail = response.body()?.get(0)?.resultList!!

                    val intent = Intent(context, WalkTravelDetailActivity::class.java)
                    intent.putExtra("detail",travelDetail)
                    startActivity(intent)

                }else{
                    Log.d("pet", "failed")
                }
            }

            override fun onFailure(call: Call<List<TravelDetailList>>, t: Throwable) {
                Log.d("pet", "failed : get travel detail data")
            }

        })
        return true
    }

}

