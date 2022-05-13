package com.project.petwalk.frag

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.project.petwalk.R
import com.project.petwalk.databinding.FragmentFragMypageBinding
import com.project.petwalk.databinding.FragmentFragWalkBinding
import com.project.petwalk.model.Travel
import com.project.petwalk.model.TravelList
import com.project.petwalk.retrofit.PetTravelAPI
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class FragWalk : Fragment() {
    lateinit var binding: FragmentFragWalkBinding
    private lateinit var mMap: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        val seoul = LatLng(35.4257183247, 128.029859528)
        mMap.addMarker(MarkerOptions().position(seoul).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seoul))
//        mMap.animateCamera(
//            CameraUpdateFactory.newLatLngZoom(
//                seoul,10.0f
//            )
//        )
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFragWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


        binding.startBtn.setOnClickListener {


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
                        binding.tvList.text = "failed"
                    }
                }

                override fun onFailure(call: Call<List<TravelList>>, t: Throwable) {
                    call.cancel()
                }


            })
        }


    }

}

