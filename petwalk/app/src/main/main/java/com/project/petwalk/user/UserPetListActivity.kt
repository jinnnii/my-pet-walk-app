package com.project.petwalk.user

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.petwalk.Adapter.PetListAdapter
import com.project.petwalk.Adapter.UserListAdapter
import com.project.petwalk.databinding.ActivityPetListPageBinding
import com.project.petwalk.databinding.ActivityUserAdminPageBinding
import com.project.petwalk.firebase.FirebasePetHelper
import com.project.petwalk.firebase.FirebaseUserHelper
import com.project.petwalk.model.Pet
import com.project.petwalk.model.User


class UserPetListActivity : AppCompatActivity() {
    // 회원 정보를 담는 배열 변수 선언
    lateinit var recycler:RecyclerView

    lateinit var user:User
    val userUID = FirebaseAuth.getInstance().currentUser?.uid

    var petList:ArrayList<Pet>?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPetListPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recycler=binding.recyclerView
        getPetListData()

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, UserPetPageActivity::class.java)
            startActivityForResult(intent,1000)
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    fun setAdapter(petList:ArrayList<Pet>){
//      이 액티비티에 레이아웃 매니저 설정
        recycler.layoutManager = LinearLayoutManager(this@UserPetListActivity)
        // 리사이클러 뷰의 어댑터를 UserListAdapter로 설정
        recycler.adapter = PetListAdapter(petList, userUID!!)
        // 어댑터로 생성된 아이템 리스트를 수직방향으로 나열함
        recycler.addItemDecoration(
            DividerItemDecoration(
                this@UserPetListActivity,
                LinearLayoutManager.VERTICAL
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getPetListData(){
        petList = arrayListOf<Pet>()
        FirebaseUserHelper().readUserPetList(object:FirebaseUserHelper.DataStatus{
            override fun DataIsLoaded(user: User) {}
            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
            override fun NodeIsLoaded(b: ArrayList<Boolean>?, keys: ArrayList<String?>?) {
                petList!!.clear()
                for(petURI in keys!!){
                    FirebasePetHelper().readPet(object:FirebasePetHelper.DataStatus{
                        override fun DataIsLoaded(pet: Pet) {
                            petList!!.add(pet)
                            recycler.adapter?.notifyDataSetChanged()
                        }
                        override fun DataIsInserted() {}
                        override fun DataIsUpdated() {}
                        override fun DataIsDeleted() {}

                    }, petURI.toString())
                }
                setAdapter(petList!!)
                Log.d("kej", petList.toString())
            }

        }, userUID!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            Log.d("kej","this is onActivityResult....")
            Log.d("kej","return petList::: $petList")
        }
    }


}
