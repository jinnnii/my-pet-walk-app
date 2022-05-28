package com.project.petwalk.walk

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityWalkTravelDetailBinding
import com.project.petwalk.model.TravelDetail
import java.io.File


class WalkTravelDetailActivity : AppCompatActivity() {
    lateinit var binding:ActivityWalkTravelDetailBinding
    lateinit var travelDetail:TravelDetail

    lateinit var title: TextView
    lateinit var images:ImageView

    lateinit var address:TextView
    lateinit var tel:TextView
    lateinit var usedTime:TextView
    lateinit var keyword:TextView

    lateinit var content:TextView
    lateinit var mainFacility:TextView
    lateinit var usedCost:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityWalkTravelDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        title=binding.travelTitle
        images=binding.travelImage
        address=binding.travelAddress
        tel=binding.travelTel
        usedTime=binding.travelUsedTime
        keyword=binding.travelKeyword
        content=binding.travelContent
        mainFacility=binding.travelMainFactory
        usedCost=binding.travelUsedCost

        getExtra()
    }


    /**
     * 받아온 정보 담기
     */
    private fun getExtra(){
        if(intent.hasExtra("detail")){
            travelDetail = intent.getSerializableExtra("detail") as TravelDetail

            // todo glide 이미지 넣기
            Glide.with(this)
                .load(travelDetail.imageList[0]["image"].toString()) //로드할 이미지
                .placeholder(R.drawable.ic_loading) //이미지 불러오기 전 이미지
                .error(R.drawable.ic_error) //로딩 에러 발생 시 이미지
                .fallback(R.drawable.default_image) //로드할 url이 비어있을 시 표시할 이미지
                .into(images)

            //todo 주요 시설 및 요금 리스트화
            val facList = travelDetail.mainFacility.split("- ")
            val costList = travelDetail.usedCost.split("- ")
            var facText=""
            var costText=""
            for(fac in facList){
                facText+=fac+"\n"
            }
            for(cost in costList){
                costText+=cost+"\n"
            }
            mainFacility.text=facText
            usedCost.text=costText
            
            // 나머지
            title.text=travelDetail.title

            address.text = travelDetail.address
            tel.text=travelDetail.tel
            usedTime.text=travelDetail.usedTime
            keyword.text=travelDetail.keyword

            content.text=travelDetail.content

        }
    }
}