package com.project.petwalk.community

import android.content.Intent
import android.net.UrlQuerySanitizer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.petwalk.Post
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityCommunityModifyBinding
import com.project.petwalk.firebase.FirebaseImageHelper
import com.project.petwalk.firebase.FirebasePostHelper
import com.project.petwalk.model.ImageModel
import kotlinx.android.synthetic.main.fragment_frag_community.*

class CommunityModifyActivity : AppCompatActivity() {
    lateinit var binding: ActivityCommunityModifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = intent.getSerializableExtra("post") as Post

        for (image in post.imageList) {
            FirebasePostHelper().readImage(object : FirebasePostHelper.DataStatus {
                override fun ImageIsLoaded(img: ImageModel) {
                    Log.d("kej","road image ::: ${img.imageUrl}")
                    Glide
                        .with(this@CommunityModifyActivity)
                        .load(img.imageUrl)
                        .centerCrop()
                        .into(binding.modifyBack)
                }

            }, image.key)
        }

        binding.modifyInput.setText(post.message)

        binding.modifyButton.setOnClickListener {
            post.message=binding.modifyInput.text.toString()
            updatePost(post)
        }

    }
    fun updatePost(post: Post) {
        FirebasePostHelper().updatePost(post, object : FirebasePostHelper.DataStatus {
            override fun DataIsUpdated() {
                finish()
            }

        })
    }

}