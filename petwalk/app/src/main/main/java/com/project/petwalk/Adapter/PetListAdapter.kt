package com.project.petwalk.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.project.petwalk.databinding.PetListBinding
import com.project.petwalk.firebase.FirebasePetHelper
import com.project.petwalk.firebase.FirebaseUserHelper
import com.project.petwalk.model.Pet
import com.project.petwalk.model.User


class PetListHolder(val binding: PetListBinding) : RecyclerView.ViewHolder(binding.root)

class PetListAdapter(
    val petList: ArrayList<Pet>,
    val userUID:String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val databaseRef = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PetListHolder(
                PetListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val binding = (holder as PetListHolder).binding
        Glide.with(binding.root.context).load(petList[position].profile)
            .into(binding.imgPet)
        binding.textView6.text = petList[position].animal
        binding.tvName.text = petList[position].name
        binding.tvPhone.text = petList[position].kind

        binding.btnKick.setOnClickListener {
//            val databaseRef = FirebaseDatabase.getInstance().getReference()
//            val auth = FirebaseAuth.getInstance()
//            val temp = arrName[position].split("@")
//            val id = temp[0]
//            // 인증된 계정 삭제

            val petUID = petList[position].uid
            // 실시간 데이터베이스에 저장된 해당 계정 정보 삭제
            FirebaseUserHelper().deleteUserPet(userUID,petUID,object :FirebaseUserHelper.DataStatus{
                override fun DataIsLoaded(user: User) {}
                override fun DataIsInserted() {}
                override fun DataIsUpdated() {}
                override fun DataIsDeleted() {
                    FirebasePetHelper().deletePet(petUID,userUID,object:FirebasePetHelper.DataStatus{
                        override fun DataIsLoaded(pet: Pet) {}
                        override fun DataIsInserted() {}
                        override fun DataIsUpdated() {}
                        override fun DataIsDeleted() {
//                            notifyItemRemoved(position)
                        }

                    })
                }
                override fun NodeIsLoaded(
                    b: ArrayList<Boolean>?,
                    keys: ArrayList<String?>?
                ) {}
            })


//            val context = UserPetListActivity()
//            val intent = (context as Activity).intent
//            (context as Activity).finish() //현재 액티비티 종료 실시
//            (context as Activity).overridePendingTransition(0, 0) //효과 없애기
//            (context as Activity).startActivity(intent) //현재 액티비티 재실행 실시
//            (context as Activity).overridePendingTransition(0, 0) //효과 없애기

        }
    }

    override fun getItemCount(): Int {
        return petList.size
    }
}