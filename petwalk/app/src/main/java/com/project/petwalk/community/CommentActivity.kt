package com.project.petwalk.community

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.project.petwalk.Adapter.CommentRecyclerviewAdapter
import com.project.petwalk.model.Comment
import com.project.petwalk.Post
import com.project.petwalk.databinding.ActivityCommentBinding
import com.project.petwalk.firebase.FirebaseCommentHelper
import com.project.petwalk.firebase.FirebaseImageHelper
import com.project.petwalk.firebase.FirebasePostHelper
import com.project.petwalk.firebase.FirebaseUserHelper
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.User
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid.toString()
    lateinit var binding: ActivityCommentBinding

    val userList:ArrayList<User> = arrayListOf()
    val commentList:ArrayList<Comment> = arrayListOf()

    val fireDatabase = FirebaseDatabase.getInstance() //f -> 파이어베이스데이터베이스의 자료를 담음
    var databaseReference = FirebaseDatabase.getInstance().getReference() // 데이터베이스레퍼런스를 위한 인스턴트 생성
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val postid = intent.getStringExtra("postId").toString()

        FirebaseUserHelper().readUser(object:FirebaseUserHelper.DataStatus{
            override fun DataIsLoaded(user: User?) {
                for(img in user!!.profile){
                    FirebaseImageHelper().readImage(object:FirebaseImageHelper.DataStatus{
                        override fun DataIsLoaded(image: ImageModel) {
                            if (this@CommentActivity.isFinishing)return;
                            Glide.with(this@CommentActivity)
                                .load(image.imageUrl)
                                .centerCrop()
                                .into(binding.profileImg)
                        }
                    },img.key)
                }
            }
        },currentUserUID)

        FirebasePostHelper().readCommentList(postid, postListener)

        // 댓글 작성
        binding.commentSubmit.setOnClickListener {
            val commentText = CommentText.text.toString()
            val regDate = System.currentTimeMillis()
            val userUID = FirebaseAuth.getInstance().currentUser?.uid.toString()
            CommentText.setText("")
            val comment = Comment("", postid, userUID, commentText, regDate)
            FirebaseCommentHelper().addComment(comment,postid, listener)
        }
    }

    val postListener = object :FirebasePostHelper.DataStatus{
        override fun DataIsLoaded(post: Post) {}
        override fun ImageIsLoaded(img: ImageModel) { }
        override fun DataIsInserted() {}
        override fun DataIsUpdated() {}
        override fun DataIsDeleted() {}
        override fun NodeIsLoaded(comments: ArrayList<String?>?) {
            userList.clear()
            commentList.clear()

            for(commentKey in comments!!){
                Log.d("kej", "readCommentUID >>>>> $commentKey")
                FirebaseCommentHelper().readComments(listener, commentKey!!)
            }
            binding.commentRecyclerView.adapter =
                CommentRecyclerviewAdapter(commentList, userList)
            binding.commentRecyclerView.layoutManager = LinearLayoutManager(this@CommentActivity)
        }

    }
    val listener = object:FirebaseCommentHelper.DataStatus{
        @SuppressLint("NotifyDataSetChanged")
        override fun DataIsLoaded(comment: Comment, user: User) {
            commentList.add(comment)
            userList.add(user)
            binding.commentRecyclerView.adapter?.notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun DataIsInserted() {
            binding.commentRecyclerView.adapter?.notifyDataSetChanged()
        }

        override fun DataIsUpdated() {
            TODO("Not yet implemented")
        }

        override fun DataIsDeleted() {
            TODO("Not yet implemented")
        }

    }

}