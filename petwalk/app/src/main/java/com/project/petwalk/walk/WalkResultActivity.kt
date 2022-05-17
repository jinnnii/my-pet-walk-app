package com.project.petwalk.walk

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityWalkResultBinding
import com.project.petwalk.model.TravelDetail
import com.project.petwalk.model.Walk
import java.text.SimpleDateFormat

class WalkResultActivity : AppCompatActivity() {
    lateinit var binding: ActivityWalkResultBinding
    lateinit var walk: Walk

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWalkResultBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getExtra()
        val sdf = SimpleDateFormat("mm분 ss초")
        val time = sdf.format(walk.usedTime)
        binding.result.text = "$time 초 동안\n${walk.distance}km 산책을 했어요"
    }

    /**
     * 받아온 walk 객체 정보 출력
     */
    private fun getExtra() {
        if (intent.hasExtra("walk")) {
            walk = intent.getSerializableExtra("walk") as Walk
        }
    }




}