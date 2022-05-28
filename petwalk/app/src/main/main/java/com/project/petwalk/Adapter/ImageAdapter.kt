package com.project.petwalk.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.petwalk.R
import com.project.petwalk.databinding.CardBackgroundBinding
import kotlinx.android.synthetic.main.activity_community_modify.view.*
import kotlinx.android.synthetic.main.activity_community_write.*
import kotlinx.android.synthetic.main.activity_community_write.view.*
import kotlinx.android.synthetic.main.card_background.view.*


// RecyclerView 에서 사용하는 View 홀더 클래스
class ImageHolder(val binding: CardBackgroundBinding) : RecyclerView.ViewHolder(binding.root)

// RecyclerView 의 어댑터 클래스
class ImageAdapter(
    val bgList: MutableList<String>,
    val listener:ItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    lateinit var parent:ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //RecyclerView 에서 사용하는 ViewHolder 클래스를 card_background 레이아웃 리소스 파일을 사용하도록 생성한다.
        this.parent = parent
        return ImageHolder(
            CardBackgroundBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // 각 행의 포지션에서 그려야할 ViewHolder UI 에 데이터를 적용하는 메소드
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val binding = (holder as ImageHolder).binding
        val writeBackground = parent.rootView.findViewById<ImageView>(R.id.write_back)

        // 이미지 로딩 라이브러리인 피카소 객체로 뷰홀더에 존재하는 imageView 에 이미지 로딩
        Glide.with(binding.root.context)
            .load(bgList[position])
            .into(binding.imageView)

        // 각 배경화면 행이 클릭된 경우에 이벤트 리스너 설정
        binding.imageView.setOnClickListener {
//             이미지 로딩 라이브러리인 피카소 객체에 뷰홀더에 존재하는 글쓰기 배경 이미지뷰에 이미지 로딩
            Glide
                .with(binding.root.context)
                .load(bgList[position])
                .into(writeBackground)

            listener.onItemClick(position)
        }
    }

    // RecyclerView 에서 몇개의 행을 그릴지 기준이 되는 메소드
    override fun getItemCount(): Int {
        return bgList.size
    }

    interface ItemClickListener{
        fun onItemClick(position: Int)
    }
}