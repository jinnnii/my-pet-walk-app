package com.project.petwalk.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityCommunityModifyBinding

class CommunityModifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCommunityModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val databaseReference = FirebaseDatabase.getInstance().getReference()
        val key= intent.getStringExtra("postId").toString()
        Log.d("test","${key}")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.child("posts").child(key).getValue() as HashMap<String, Any>
                //val imageMap = snapshot.child("postImages").child(key).getValue() as HashMap<String, Any>
                // 해당 회원의 모든 정보를 불러옴
                val message = map.get("message").toString()
                //val image = imageMap.get("imageUrl").toString()

                binding.modInput.setText(message)
                //Glide.with(this@CommunityModifyActivity).load(image).into(binding.writeBackground)

            }
            override fun onCancelled(error: DatabaseError) {
                println("loadItem:onCancelled : ${error.toException()}")
            }
        })


    }
}