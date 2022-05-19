package com.project.petwalk

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.project.petwalk.databinding.ActivityMainBinding
import com.project.petwalk.frag.*
import com.project.petwalk.home.retrofit.ITEM
import com.project.petwalk.home.retrofit.WEATHER
import com.project.petwalk.home.retrofit.WeatherAPI
import com.project.petwalk.model.Weather
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private val REQUEST_CODE_LOCATION = 2
    var weather: Weather?=null

    fun changeFragment(frag: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame, frag)
            .commit()

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.run {
            setOnItemSelectedListener { item->
                when(item.itemId){
                    R.id.action_community->{
                        changeFragment(FragCommunity())
                    }
                    R.id.action_calendar->{
                        changeFragment(FragCalendar())
                    }
                    R.id.action_home->{
                        changeFragment(FragHome())

                    }
                    R.id.action_walking->{
                        changeFragment(FragWalk())
                    }
                    R.id.action_my_page->{
                        changeFragment(FragMypage())
                    }


                }
                true
            }
            selectedItemId=R.id.action_home
        }




    }
}