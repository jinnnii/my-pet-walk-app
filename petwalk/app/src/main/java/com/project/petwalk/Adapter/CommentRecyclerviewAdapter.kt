package com.project.petwalk.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.project.petwalk.model.Comment
import com.project.petwalk.R
import com.project.petwalk.databinding.CardCommentBinding
import com.project.petwalk.databinding.ItemImageBinding
import com.project.petwalk.firebase.FirebaseImageHelper
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.User
import kotlinx.android.synthetic.main.card_comment.view.*
import java.text.SimpleDateFormat

class CustomViewHolder(val binding: CardCommentBinding) :
    RecyclerView.ViewHolder(binding.root) // 리싸이클러 뷰홀더를 상속받고 뷰 넘겨주기

class CommentRecyclerviewAdapter(
    val commentList: ArrayList<Comment>,
    val userList: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomViewHolder(
            CardCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CustomViewHolder).binding

        Log.d("kej", "comment list >>> $commentList,,,\n ,,,$userList")

        val date = SimpleDateFormat("yy-MM-dd HH:mm").format(commentList[position].writeTime)
        binding.commentNick.text = userList[position].name //닉네임 매핑
        binding.CommentTextView.text = commentList[position].message
        binding.commentDate.text=date

        var profileURL = ""
        for(profile in userList[position].profile){
            profileURL=profile.key
            FirebaseImageHelper().readImage(object:FirebaseImageHelper.DataStatus{
                override fun DataIsLoaded(image: ImageModel) {
                    Glide
                        .with(binding.root.context)
                        .load(image.imageUrl)
                        .centerCrop()
                        .into(binding.commentProfile)
                }

            },profileURL)
            Log.d("kej", "comment image::::$profileURL")

        }

    }

    override fun getItemCount(): Int {
        return commentList.size
    }
}