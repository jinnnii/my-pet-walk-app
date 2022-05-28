package com.project.petwalk.calendar

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityCalendarDetailBinding
import com.project.petwalk.firebase.FirebaseImageHelper
import com.project.petwalk.firebase.FirebaseUserHelper
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.LocationModel
import com.project.petwalk.model.User
import com.project.petwalk.model.Walk
import kotlinx.android.synthetic.main.item_calendar.view.*
import java.text.SimpleDateFormat

class CalendarDetail : AppCompatActivity() {
    lateinit var binding:ActivityCalendarDetailBinding
    //지도 띄우기
    private lateinit var mMap: GoogleMap

    //location 정보 firebase
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val locReference: DatabaseReference =database.getReference("locations")
    lateinit var user: User


    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityCalendarDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUserProfile()

//        val mapFragment =supportFragmentManager
//            .findFragmentById(R.id.detailMap) as SupportMapFragment
//        mapFragment.getMapAsync(this)


        //데이터 들고 오기 ==init
        toolbarAction()
        val walk = getWalkData()
        getLocationsData(walk!!)
        setData(walk)

    }

    fun setUserProfile(){
        FirebaseUserHelper().readUser(object:FirebaseUserHelper.DataStatus{
            override fun DataIsLoaded(user: User) {
                var imageUID: String? =null
                for(uid in user.profile){
                    imageUID = uid.key
                }

                FirebaseImageHelper().readImage(object:FirebaseImageHelper.DataStatus{
                    override fun DataIsLoaded(image: ImageModel) {
                        this@CalendarDetail.user =user
                        Glide
                            .with(this@CalendarDetail)
                            .load(image.imageUrl)
                            .fallback(R.drawable.default_image) //로드할 url이 비어있을 시 표시할 이미지
                            .into(binding.userProfile)
                        binding.userId.text=user.email.split("@").get(0)
                    }

                },imageUID.toString())

            }
            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
            override fun NodeIsLoaded(b: ArrayList<Boolean>?, keys: ArrayList<String?>?) {}

        },FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    @SuppressLint("SimpleDateFormat")
    private fun setData(walk:Walk) {
        val timeFmt = SimpleDateFormat("mm:ss")
        val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val time = timeFmt.format(walk.usedTime)
        val startDate = dateFmt.format(walk.startTime)
        val endDate = dateFmt.format(walk.endTime)

        binding.tvUsedTime.text = time
        binding.tvDistanceKm.text= walk.distance.toString()
        binding.tvStartDate.text = startDate
        binding.tvEndDate.text=endDate
    }

    fun toolbarAction(){
        //툴바 활성화
        setSupportActionBar(binding.toolBar)
        //툴바 홈버튼 활성화 / 타이틀 비활성화
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //툴바 홈버튼의 이미지 변경
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24)
    }
    fun getWalkData():Walk?{
        if(intent.hasExtra("walk")){
            return intent.getSerializableExtra("walk") as Walk
        }
        return null
    }
    fun getLocationsData(walk:Walk){
        for(location in walk.locations){
            locReference.child(location.key)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val locations = arrayListOf<LocationModel>()
                        for(snap in snapshot.children){
                            val location = snap.getValue(LocationModel::class.java)!!
                            snap.getValue()
                            location.latitude
                            locations.add(location)
                            Log.d("pet", ">>>>>>>>locationList$locations")
                        }

                        mapSetting(locations)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //
                    }
                })
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when(id){
            android.R.id.home->onBackPressed()

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    fun mapSetting(locations: List<LocationModel>){
        val mapFragment = SupportMapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.detailMap,mapFragment)
            .commit()
        mapFragment.getMapAsync {
            mMap = it
            drawPoly(locations)
        }
    }

    fun drawPoly(locations:List<LocationModel>){

        val polylineOptions = PolylineOptions().width(25F)
            .color(Color.parseColor("#99B3B1"))
            .geodesic(true)

        Log.d("pet", "위치정보배열:::$locations")
        if(locations.isNotEmpty()){
            val cameraPosition=CameraPosition.Builder()
                .target(LatLng(locations[0].latitude, locations[0].longitude))
                .zoom(17F)
                .build()
            for(location in locations){
                polylineOptions.add(LatLng(location.latitude,location.longitude))
            }
            mMap.addPolyline(polylineOptions)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

    }


}