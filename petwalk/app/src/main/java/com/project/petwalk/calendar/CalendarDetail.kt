package com.project.petwalk.calendar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityCalendarDetailBinding

class CalendarDetail : AppCompatActivity() {
    lateinit var binding:ActivityCalendarDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityCalendarDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}