package com.project.petwalk.user

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.project.petwalk.MainActivity
import com.project.petwalk.databinding.ActivityUserMyInfoBinding
import com.project.petwalk.firebase.FirebaseUserHelper
import com.project.petwalk.model.User


class UserMyInfoActivity : AppCompatActivity() {
    lateinit var databaseRef: DatabaseReference
    lateinit var binding:ActivityUserMyInfoBinding
    lateinit var user:User

    val userUID = FirebaseAuth.getInstance().currentUser?.uid

    val callback = object:FirebaseUserHelper.DataStatus{
        @SuppressLint("SetTextI18n")
        override fun DataIsLoaded(user: User) {

            this@UserMyInfoActivity.user =user

            // 이메일 부분의 "@"앞쪽 부분을 잘라서 temp에 저장 (즉, 아이디 부분 저장)
            val temp = user.email.split("@")
            // 생성한 temp를 id에 저장
            val id = temp[0]

            // 해당 회원의 모든 정보를 불러옴
            binding.tvMyinfo.text = "${id}님의 정보수정"
            // 현재 유저의 email
            binding.editEmail.setText(user.email)
            binding.editName.setText(user.name)
            binding.editPhone.setText(user.phone)
        }

        override fun DataIsInserted() {
            TODO("Not yet implemented")
        }

        override fun DataIsUpdated() {
            val intent =
                Intent(this@UserMyInfoActivity, MainActivity:: class.java)
            startActivity(intent)
            Toast.makeText(
                this@UserMyInfoActivity,
                "정보수정에 성공하셨습니다.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        override fun DataIsDeleted() {
            TODO("Not yet implemented")
        }

        override fun NodeIsLoaded(b: ArrayList<Boolean>?, keys: ArrayList<String?>?) {
            TODO("Not yet implemented")
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserMyInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        FirebaseUserHelper().readUser(callback, userUID!!)


        //업데이트 버튼 클릭
        binding.button.setOnClickListener {

            if(binding.editEmail.text.isEmpty()
                || binding.editName.text.isEmpty() || binding.editPhone.text.isEmpty()
            ){
                Toast.makeText(
                    this@UserMyInfoActivity,
                    "입력되지 않은 항목이 있습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                // 수정된 data로 User객체를 새로 만든다.
                val data = User(
                    user.uid,
                    binding.editEmail.text.toString(),
                    binding.editName.text.toString(),
                    binding.editPhone.text.toString(),
                    walkList = user.walkList,
                    petList = user.petList,
                    profile = user.profile
                )
                // 새로 만들어진 User객체를 User밑에 id경로에 저장
                FirebaseUserHelper().updateUser(user.uid, data, callback)
            }

        }
    }
}