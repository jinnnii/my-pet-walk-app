package com.project.petwalk.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.project.petwalk.Comment
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityCommentBinding
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {

    lateinit var binding: ActivityCommentBinding
    val comment = Comment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fireDatabase = FirebaseDatabase.getInstance() //f -> 파이어베이스데이터베이스의 자료를 담음
        var database = Firebase.database.reference // 데이터베이스레퍼런스를 위한 인스턴트 생성

        binding.commentSubmit.setOnClickListener {
            comment.writerId = FirebaseAuth.getInstance().currentUser?.email.toString()
            comment.message = CommentText.text.toString()
            comment.writeTime = System.currentTimeMillis()
            comment.commentId // 이 코멘트의 고유값
            comment.postId // 지금 들어와있는 글의 id

            fireDatabase.getReference("Comments").child(comment.postId).setValue(comment)
        }
    }
}