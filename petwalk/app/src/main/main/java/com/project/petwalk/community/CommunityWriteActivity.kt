package com.project.petwalk.community

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.common.io.Files
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.petwalk.Adapter.ImageAdapter
import com.project.petwalk.model.Comment
import com.project.petwalk.Post
import com.project.petwalk.databinding.ActivityCommunityWriteBinding
import com.project.petwalk.firebase.FirebasePostHelper
import com.project.petwalk.frag.FragCommunity
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.Images
import kotlinx.android.synthetic.main.activity_community_write.*
import java.sql.Types.TIMESTAMP


class CommunityWriteActivity : AppCompatActivity(){
    //    배경 리스트 데이터
//    res/drawable 디렉토리에 있는 배경 이미지를 uri 주소로 사용한다.
//    uri 주소로 사용하면 추후 웹에 있는 이미지 URL 도 바로 사용이 가능하다.
    val bgList = mutableListOf(
        "android.resource://com.project.petwalk/drawable/default_bg",
        "android.resource://com.project.petwalk/drawable/bg2",
        "android.resource://com.project.petwalk/drawable/bg3",
        "android.resource://com.project.petwalk/drawable/bg4",
        "android.resource://com.project.petwalk/drawable/bg5",
        "android.resource://com.project.petwalk/drawable/bg6",
        "android.resource://com.project.petwalk/drawable/bg7",
        "android.resource://com.project.petwalk/drawable/bg8",
        "android.resource://com.project.petwalk/drawable/bg9"
    )

    // 현재 선택된 배경이미지의 포지션을 저장하는 변수
    var imageKey: String = ""
    var imageUri: Uri? = null
    lateinit var mDialog: ProgressDialog
    lateinit var binding: ActivityCommunityWriteBinding
    private val databaseRef = FirebaseDatabase.getInstance().getReference()
    private val storagereference: StorageReference = FirebaseStorage.getInstance().getReference()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // actionbar 의 타이틀을 "글쓰기"로 변경
        supportActionBar?.title = "글쓰기"

        // 이미지 리스트 불러오기
        val layoutManager = LinearLayoutManager(this@CommunityWriteActivity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = ImageAdapter(bgList,listener)


        Glide.with(this)
            .load(bgList[0])
            .into(binding.writeBack)

        imageUri=Uri.parse(bgList[0])


        // 배경사진 추가 (갤러리이동)
        binding.photoButton.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, 0)
        }


        // 공유하기
        binding.sendButton.setOnClickListener {
            // 메세지가 없는 경우 토스트 메세지로 알림.
            if (TextUtils.isEmpty(input.text)) {
                Toast.makeText(applicationContext, "메세지를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }
            Log.d("kej","select Image uri ::: $imageUri")
            if (imageUri != null) {
                mDialog = ProgressDialog(this)
                mDialog.setMessage("적용중입니다...")
                mDialog.show()
                uploadToFirebase(imageUri!!)

            } else {
                Toast.makeText(this, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 갤러리에 선택한 사진 가져오기
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                try {
                    imageUri = data!!.data!!

                    Log.d("kej", "select gallery .... :: $imageUri")
                    Glide.with(this)
                        .load(imageUri)
                        .into(binding.writeBack) //다이얼로그 이미지사진에 넣기
                } catch (e: Exception) {
                }
            } else if (resultCode == AppCompatActivity.RESULT_CANCELED) { // 취소시 호출할 행동 쓰기
            }
        }
    }

    // 사진 업로드
    private fun uploadToFirebase(uri: Uri) {
        // 파일 이름 설정
        val fileRef: StorageReference =
            storagereference.child(
                System.currentTimeMillis().toString() + "." + Files.getFileExtension(uri.toString())
            )

        fileRef.putFile(uri).addOnSuccessListener {

            fileRef.downloadUrl.addOnSuccessListener { uri ->

                // 이미지 Uri를 데이터베이스에 넣는다
                imageKey = FirebaseDatabase.getInstance().getReference().child("postImages")
                    .push().key.toString()
                databaseRef.child("postImages").child(imageKey)
                    .setValue(Images(uri.toString()))

                val message = binding.input.text.toString()
                val writerId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val commentCount = 0
                val writeTime = System.currentTimeMillis()

                val post = Post(
                    uid = "",
                    writerId=writerId,
                    message = message,
                    writeTime = writeTime,
                    imageList = mapOf(imageKey to true),
                    commentCount=commentCount)


                FirebasePostHelper().addPost(post, writerId, object: FirebasePostHelper.DataStatus{
                    override fun DataIsLoaded(post: Post) {}
                    override fun ImageIsLoaded(img: ImageModel) {}
                    override fun DataIsInserted() {
                        //프로그래스바 숨김
                        mDialog.dismiss()
                        // 저장성공 토스트 알림을 보여주고 Activity 종료
                        Toast.makeText(applicationContext, "공유되었습니다", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    override fun DataIsUpdated() {}
                    override fun DataIsDeleted() {}
                    override fun NodeIsLoaded(comments: ArrayList<String?>?) {}

                })

            }
        }.addOnFailureListener {
            Toast.makeText(this, "적용에 완료되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    val listener = object:ImageAdapter.ItemClickListener{
        override fun onItemClick(position: Int) {
            imageUri=Uri.parse(bgList[position])
            Log.d("kej","bg click event ::: ${imageUri}")
        }
    }
}