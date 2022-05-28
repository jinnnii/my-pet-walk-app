package com.project.petwalk.frag

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorSpace
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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

    lateinit var mapFragment:SupportMapFragment

    lateinit var pathPoly:PolylineOptions

    //지도
    private lateinit var mMap: GoogleMap

    //동반 시설 분류 코드
    val PART_CODE = arrayOf("PC01", "PC02", "PC03", "PC04", "PC05")
    val MARKER = arrayOf(R.drawable.ic_mark_cafe, R.drawable.ic_mark_bed, R.drawable.ic_mark_tour, R.drawable.ic_mark_exp, R.drawable.ic_mark_hospital)

    //현재위치
    var manager: LocationManager?= null


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
    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        Log.d("pet", mMap.toString())

        if(locations.size!=0){
            val userLocation = locations[0]
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(userLocation.latitude, userLocation.longitude))
                .zoom(19F)
                .build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            mMap.isMyLocationEnabled=true
        }

    }

    /**
     * 동반가능 시설 위치 마커 찍기
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun drawMark(travelList:List<Travel>, code:String, mk:Int){
        for(travel in travelList){
            val lat= travel.latitude.toDouble()
            val lng= travel.longitude.toDouble()
            val title= travel.title
            val id = travel.contentSeq


            val position = LatLng(lat, lng)
            val markerOptions = MarkerOptions()
            markerOptions.icon(bitmapDescriptorFromVector(context, mk))
            val marker: Marker? = mMap.addMarker(markerOptions.position(position).title(title))

            marker?.tag= "$id,$code"
        }
        mMap.setOnMarkerClickListener(this)
    }

    /**
     * 마커 아이콘 Bitmap으로 변경
     */
    private fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor? {
        return context?.let {
            ContextCompat.getDrawable(it, vectorResId)?.run {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
                draw(Canvas(bitmap))
                BitmapDescriptorFactory.fromBitmap(bitmap)
            }
        }
    }



    /**
     *
     * 경로 그리기
     */
    private fun drawPath(){
        if(locations.size>1){
            val size= locations.size-1
//            val start = LatLng(locations[size-1].latitude, locations[size-1].longitude)
//            val end = LatLng(locations[size].latitude, locations[size].longitude)
//            val path:PolylineOptions  = PolylineOptions().add(start).width(60F).color(R.color.path_green).geodesic(true)
            val lastLoc = LatLng(locations[size].latitude, locations[size].longitude)
            pathPoly.add(lastLoc)
            mMap.addPolyline(pathPoly)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLoc, 19F))
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

    private val initListener :LocationListener=object :LocationListener{
        override fun onLocationChanged(location: Location) {
            locations.add(LocationModel(location.latitude, location.longitude, location.time))
            val location = locations[0]

            Log.d("pet", "실시간 좌표 위치 첫번째>>>$locations")
            mapFragment.getMapAsync(callback)
            pathPoly = PolylineOptions().add(LatLng(location.latitude, location.longitude)).width(50F).color(Color.parseColor("#99B3B1")).geodesic(true)

            manager?.removeUpdates(this)
        }

    }
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

    @SuppressLint("MissingPermission")
    private fun initMap(){
        manager= activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        val location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        manager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, initListener)
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

        Log.d("pet", "메인에서 받아온 walkList::::"+ arguments?.getSerializable("walkListtest").toString())
        mapFragment = (childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?)!!


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
            initMap()
            init()
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



            // todo  최소 10초, 최소 10m 마다 위치 확인
            manager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 0f, listener)

            /**
             * 반려동물 동반 장소 api 사용
             */
//            val travelNetService = (activity?.applicationContext as PetTravelAPI).networkService

            for(idx in PART_CODE.indices) {
//                val travelListCall = travelNetService.doGetTravelList("1", "133", PART_CODE[idx])
                val travelListCall = PetTravelAPI.networkService.doGetTravelList("1", "133", PART_CODE[idx])

                travelListCall.enqueue(object : Callback<List<TravelList>> {
                    override fun onResponse(
                        call: Call<List<TravelList>>,
                        response: Response<List<TravelList>>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("pet", response.body()?.get(0)?.resultList.toString())
                            val travelList: List<Travel> = response.body()?.get(0)?.resultList!!
                            drawMark(travelList, PART_CODE[idx], MARKER[idx])

                        } else {
                            Log.d("pet", "failed")
                        }
                    }

                    override fun onFailure(call: Call<List<TravelList>>, t: Throwable) {
                        call.cancel()
                    }


                })
            }
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


            val timeSec = binding.chronometer.drawingTime-binding.chronometer.base
            Log.d("kej", "timeSec::::${timeSec.toString()}")
            val walk = Walk(initDistance, startDate, endDate, timeSec, mapOf(),"","")


            init()

            // todo walk 객체 전송
//            val intent = Intent(context, WalkResultActivity::class.java)
//            intent.putExtra("walk", walk)
//            intent.putExtra("locations", locations)
//            startActivityForResult(intent,3000)

        }

    }

    fun init(){
        pauseTime=0L
        initDistance=0.0

        binding.chronometer.base=SystemClock.elapsedRealtime()
        binding.chronometer.stop()

        binding.beforeFrame.visibility=View.VISIBLE
        binding.ingFrame.visibility=View.GONE
        binding.parseBtn.visibility=View.VISIBLE
        binding.parseLayout.visibility=View.GONE
        binding.distance.text="0.0"

        manager?.removeUpdates(listener)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("pet",">>>>>>>>>>>>>>>>>>>>>>>>>"+resultCode.toString())
        initMap()
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
//        val travelNetService = (activity?.applicationContext as Tr.networkService
        val travelDetailCall = PetTravelAPI.networkService.doGetTravelDetail(code,number)

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