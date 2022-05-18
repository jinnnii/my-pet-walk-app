package com.project.petwalk.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.project.petwalk.Comment
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityCommentBinding

class CommentActivity : AppCompatActivity() {

    lateinit var binding: ActivityCommentBinding
    val comment = Comment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fireDatabase = FirebaseDatabase.getInstance() //f -> 파이어베이스데이터베이스의 자료를 담음
        var database = Firebase.database.reference // 데이터베이스레퍼런스를 위한 인스턴트 생성


    }
}