package com.project.petwalk.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.petwalk.Adapter.PetListAdapter
import com.project.petwalk.Adapter.UserListAdapter
import com.project.petwalk.databinding.ActivityPetListPageBinding
import com.project.petwalk.databinding.ActivityUserAdminPageBinding


class UserPetListActivity : AppCompatActivity() {
    // 회원 정보를 담는 배열 변수 선언
    lateinit var arrAnimalImages: ArrayList<String>
    lateinit var arrAnimal: ArrayList<String>
    lateinit var arrName: ArrayList<String>
    lateinit var arrKind: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPetListPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this,UserPetPageActivity::class.java)
            startActivity(intent)
        }


        val databaseRef = FirebaseDatabase.getInstance().getReference()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 배열 변수 초기화
                arrAnimalImages = arrayListOf()
                arrAnimal = arrayListOf()
                arrName = arrayListOf()
                arrKind = arrayListOf()
                // 파이어베이스 데이터베이스의 Users경로 밑에 데이터를 순회하면서 list에 담음
                for (list in snapshot.child("pets").children.iterator()) {
                    // list에 저장된 데이터를 map에 HashMap형태로 저장
                    var map = list.getValue() as HashMap<String, Any>
                    // map에 저장된 데이터 중 해당하는 키 값의 데이터를 배열 변수에 추가
                    arrAnimal.add(map.get("animal").toString())
                    arrName.add(map.get("name").toString())
                    arrKind.add(map.get("kind").toString())
                }
                for (list in snapshot.child("petImages").children.iterator()) {
                    // list에 저장된 데이터를 map에 HashMap형태로 저장
                    var map = list.getValue() as HashMap<String, Any>
                    // map에 저장된 데이터 중 해당하는 키 값의 데이터를 배열 변수에 추가
                    arrAnimalImages.add(map.get("imageUrl").toString())
                }

                // 이 액티비티에 레이아웃 매니저 설정
                binding.recyclerView.layoutManager = LinearLayoutManager(this@UserPetListActivity)
                // 리사이클러 뷰의 어댑터를 UserListAdapter로 설정
                binding.recyclerView.adapter = PetListAdapter(arrAnimalImages,arrAnimal, arrName, arrKind)
                // 어댑터로 생성된 아이템 리스트를 수직방향으로 나열함
                binding.recyclerView.addItemDecoration(
                    DividerItemDecoration(
                        this@UserPetListActivity,
                        LinearLayoutManager.VERTICAL
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                println("loadItem:onCancelled : ${error.toException()}")
            }
        })
    }

}
