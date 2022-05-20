package com.project.petwalk.Adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.petwalk.R
import com.project.petwalk.community.CommunityWriteActivity
import com.project.petwalk.databinding.CardBackgroundBinding
import com.project.petwalk.databinding.ItemImageBinding
import com.project.petwalk.databinding.PetListBinding
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_community_write.*
import kotlinx.android.synthetic.main.card_background.view.*


// RecyclerView 에서 사용하는 View 홀더 클래스
class ImageHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

// RecyclerView 의 어댑터 클래스
class ImageAdapter(
    val bgList: MutableList<String>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //RecyclerView 에서 사용하는 ViewHolder 클래스를 card_background 레이아웃 리소스 파일을 사용하도록 생성한다.
        return ImageHolder(
            ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // 각 행의 포지션에서 그려야할 ViewHolder UI 에 데이터를 적용하는 메소드
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val binding = (holder as ImageHolder).binding
        val writeBackground = binding.root.findViewById<ImageView>(R.id.writeBackground)
        // 이미지 로딩 라이브러리인 피카소 객체로 뷰홀더에 존재하는 imageView 에 이미지 로딩
        Glide.with(binding.root.context).load(bgList[position])
            .into(binding.image)

        // 각 배경화면 행이 클릭된 경우에 이벤트 리스너 설정
        binding.image.setOnClickListener {
            // 이미지 로딩 라이브러리인 피카소 객체에 뷰홀더에 존재하는 글쓰기 배경 이미지뷰에 이미지 로딩
            Glide.with(binding.root.context).load(bgList[position])
                .into(writeBackground)
        }
    }

    // RecyclerView 에서 몇개의 행을 그릴지 기준이 되는 메소드
    override fun getItemCount(): Int {
        return bgList.size
    }

}