package com.project.petwalk.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.project.petwalk.R
import com.project.petwalk.community.CommunityModifyActivity
import com.project.petwalk.databinding.CardPostBinding
import com.project.petwalk.databinding.PetListBinding
import com.project.petwalk.frag.FragCommunity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_background.view.*
import kotlinx.android.synthetic.main.card_post.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// RecyclerView 에서 사용하는 View 홀더 클래스
class  CommunityViewHolder(val binding: CardPostBinding) : RecyclerView.ViewHolder(binding.root)

// RecyclerView 의 어댑터 클래스
class CommunityListAdapter(
    val context:Context,
    val arrPostImages :ArrayList<String>,
    val arrPostId: ArrayList<String>,
    val arrMessage: ArrayList<String>,
    val arrWriterId: ArrayList<String>,
    val arrWriteTime: ArrayList<Long>,
    val arrCommentCount: ArrayList<Long>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    // RecyclerView 에서 각 Row(행)에서 그릴 ViewHolder 를 생성할때 불리는 메소드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CommunityViewHolder(
            CardPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    // 각 행의 포지션에서 그려야할 ViewHoler UI 에 데이터를 적용하는 메소드
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CommunityViewHolder).binding
        binding.contentsText.text=arrMessage[position]
        binding.timeTextView.text= getDiffTimeText(arrWriteTime[position])
        binding.commentCountText.text= arrCommentCount[position].toString()
        val into = Glide.with(binding.root.context).load(arrPostImages[position])
            .into(binding.imageView)

        Log.d("kej", "${arrWriterId[position]}, ${FirebaseAuth.getInstance().currentUser?.uid}" )


        binding.contentsText.setOnClickListener {
            val intent = Intent(binding.root.context,CommunityModifyActivity::class.java)
            intent.putExtra("postId","${arrPostId[position]}")
            context.startActivity(intent)
        }
    }

    // RecyclerView 에서 몇개의 행을 그릴지 기준이 되는 메소드
    override fun getItemCount(): Int {
        return arrMessage.size
    }

    // 글이 쓰여진 시간을 "방금전", "시간전", "yyyy년 MM월 dd일 HH:mm" 포맷으로 변환해주는 메소드
    private fun getDiffTimeText(targetTime: Long): String{
        val curDateTime = DateTime()
        val targetDateTime = DateTime().withMillis(targetTime)

        val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
        val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
        val diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).minutes
        if(diffDay == 0) {
            if(diffHours == 0 && diffMinutes == 0){
                return "방금 전"
            }
            return if(diffHours > 0){
                ""+ diffHours + "시간 전"
            } else "" + diffMinutes + "분 전"
        } else {
            val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
            return format.format(Date(targetTime)).toString()
        }
    }

}