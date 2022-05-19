package com.project.petwalk

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.project.petwalk.databinding.ActivityMainBinding
import com.project.petwalk.frag.*

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding


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