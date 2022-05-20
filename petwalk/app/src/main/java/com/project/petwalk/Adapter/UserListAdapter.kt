package com.project.petwalk.Adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.project.petwalk.databinding.UserListBinding
import com.project.petwalk.user.UserAdminPageActivity


class UserListHolder(val binding: UserListBinding) : RecyclerView.ViewHolder(binding.root)

class UserListAdapter(
    val arrEmail: ArrayList<String>,
    val arrName: ArrayList<String>,
    val arrPhone: ArrayList<String>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserListHolder(
            UserListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as UserListHolder).binding
        binding.textView6.text = arrEmail[position]
        binding.tvName.text = arrName[position]
        binding.tvPhone.text = arrPhone[position]

        binding.btnKick.setOnClickListener {
            val databaseRef = FirebaseDatabase.getInstance().getReference()
            val auth = FirebaseAuth.getInstance()
            val temp = arrName[position].split("@")
            val id = temp[0]
            // 인증된 계정 삭제

            // 실시간 데이터베이스에 저장된 해당 계정 정보 삭제
            databaseRef.child("Users").child(id).removeValue()

            val context = UserAdminPageActivity()
            val intent = (context as Activity).intent
            (context as Activity).finish() //현재 액티비티 종료 실시

            (context as Activity).overridePendingTransition(0, 0) //효과 없애기

            (context as Activity).startActivity(intent) //현재 액티비티 재실행 실시

            (context as Activity).overridePendingTransition(0, 0) //효과 없애기
        }
    }

    override fun getItemCount(): Int {
        return arrName.size
    }
}