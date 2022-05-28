package com.project.petwalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.project.petwalk.databinding.ActivityMainBinding
import com.project.petwalk.frag.*
import com.project.petwalk.home.retrofit.WeatherAPI
import com.project.petwalk.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
interface FirebaseCallback{
    fun success(data:Any)
    fun fail(error:String)
}

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private val REQUEST_CODE_LOCATION = 2

    lateinit var user:User
    lateinit var walkList:ArrayList<Walk>
    lateinit var petList:ArrayList<Pet>
    lateinit var profile:Images


    fun changeFragment(frag: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame, frag)
            .commit()

    }

    fun setMenu(){
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setMenu()
    }



}