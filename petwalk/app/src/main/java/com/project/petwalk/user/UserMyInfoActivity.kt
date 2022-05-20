package com.project.petwalk.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.petwalk.MainActivity
import com.project.petwalk.databinding.ActivityUserMyInfoBinding
import com.project.petwalk.model.User


class UserMyInfoActivity : AppCompatActivity() {
    lateinit var databaseRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserMyInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // 파이어베이스 인증 인스턴스 생성
        val firebaseAuth = FirebaseAuth.getInstance()
        // 현재 접속 중인 유저의 정보
        val user = firebaseAuth.currentUser
        // 현재 유저의 uid
        val uid = user?.uid.toString()
        // 현재 유저의 email
        val email = user?.email.toString()
        // 이메일 부분의 "@"앞쪽 부분을 잘라서 temp에 저장 (즉, 아이디 부분 저장)
        val temp = email.split("@")
        // 생성한 temp를 id에 저장
        val id = temp[0]

        binding.tvMyinfo.text = "${email}님의 정보수정"
        databaseRef = FirebaseDatabase.getInstance().getReference()
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.child("Users").child(id).getValue() as HashMap<String, Any>
                // 해당 회원의 모든 정보를 불러옴
                val name = map.get("name").toString()
                val phone = map.get("phone").toString()

                // 텍스트 편집창에 이름과 휴대폰 번호를 불러옴
                binding.editEmail.setText(email)
                binding.editName.setText(name)
                binding.editPhone.setText(phone)
            }
            override fun onCancelled(error: DatabaseError) {
                println("loadItem:onCancelled : ${error.toException()}")
            }
        })
        binding.button.setOnClickListener {
            // 수정된 data로 User객체를 새로 만든다.
            val data = User(
                uid,
                binding.editEmail.text.toString(),
                binding.editName.text.toString(),
                binding.editPhone.text.toString()
            )
            // 새로 만들어진 User객체를 User밑에 id경로에 저장
            databaseRef.child("Users").child(id).setValue(data)
            val intent =
                Intent(this@UserMyInfoActivity, MainActivity:: class.java)
            startActivity(intent)
            Toast.makeText(
                this@UserMyInfoActivity,
                "정보수정에 성공하셨습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}