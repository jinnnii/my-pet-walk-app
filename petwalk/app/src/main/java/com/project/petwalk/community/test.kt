package com.project.petwalk.community

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.petwalk.Post
import com.project.petwalk.databinding.ActivityCommunityWriteBinding

class test {

    class CommunityWriteActivity : AppCompatActivity() {
//        var mode = "post"
        var imageUri : Uri? =null
        lateinit var binding: ActivityCommunityWriteBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding= ActivityCommunityWriteBinding.inflate(layoutInflater)
            setContentView(binding.root)

//            binding.photoButton.setOnClickListener {
//                val intent = Intent()
//                intent.type = "image/*"
//                intent.action = Intent.ACTION_GET_CONTENT
//                startActivityForResult(intent, 0)
//            }


            binding.sendButton.setOnClickListener {
                // 메세지가 없는 경우 토스트 메세지로 알림.
//                if(TextUtils.isEmpty(binding.input.text)){
//                    Toast.makeText(applicationContext, "메세지를 입력하세요.", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
                if(mode == "post") {
                    uploadToFirebase(imageUri!!)
                    val temp = FirebaseAuth.getInstance().currentUser?.email.toString().split("@")
                    val id = temp[0]
                    // Post 객체 생성
                    val post = Post()
                    // Firebase 의 Posts 참조에서 객체를 저장하기 위한 새로운 키를 생성하고 참조를 newRef 에 저장
                    val newRef = FirebaseDatabase.getInstance().getReference("Posts").child(id)
                    // 글이 쓰여진 시간은 Firebase 서버의 시간으로 설정
//            val time = System.currentTimeMillis()
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
//            val curTime = dateFormat.format(Date(time)).toString()
                    post.writeTime = ServerValue.TIMESTAMP
                    //post.writeTime = SimpleDateFormat("yyyy-MM-dd hh:mm")
                    // 배경 Uri 주소를 현재 선택된 배경의 주소로 할당
                    Log.d("test","$imageUri")
                    // 메세지는 input EditText 의 텍스트 내용을 할당
                    post.message = binding.input.text.toString()
                    // 글쓴 사람의 ID 는 디바이스의 아이디로 할당
                    post.writerId = FirebaseAuth.getInstance().currentUser?.email.toString()
                    // 글의 ID 는 새로 생성된 파이어베이스 참조의 키로 할당
                    post.postId = newRef.key.toString()
                    // Post 객체를 새로 생성한 참조에 저장
                    newRef.setValue(post)
                    // 저장성공 토스트 알림을 보여주고 Activity 종료
                    Toast.makeText(this, "공유되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
//                val comment = ContentModel.Comment()
//                // Firebase 의 Posts 참조에서 객체를 저장하기 위한 새로운 키를 생성하고 참조를 newRef 에 저장
//                val newRef = FirebaseDatabase.getInstance().getReference("Comments/$postId").push()
//                comment.writeTime = ServerValue.TIMESTAMP
//                comment.bgUri = bgList[currentBgPosition]
//                // 메세지는 input EditText 의 텍스트 내용을 할당
//                comment.message = input.text.toString()
//                // 글쓴 사람의 ID는 디바이스의 아이디로 할당
//                comment.writerId = getMyId()
//                // 글의 ID 는 새로 생성된 파이어베이스 참조의 키로 할당
//                comment.commentId = newRef.key.toString()
//                // 댓글이 속한 글의 ID
//                comment.postId = postId
//                newRef.setValue(comment)
//                // 저장성공 토스트 알림을 보여주고 Activity 종료
                    Toast.makeText(applicationContext, "공유되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            @Nullable data: Intent?
        ) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == 0) {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    try {
                        imageUri = data!!.data!!
                        Glide.with(this@CommunityWriteActivity).load(imageUri)
                            .into(binding.writeBackground) //다이얼로그 이미지사진에 넣기
                    } catch (e: Exception) {
                    }
                } else if (resultCode == AppCompatActivity.RESULT_CANCELED) { // 취소시 호출할 행동 쓰기
                }
            }
        }

        // 프로필 사진 업로드
        private fun uploadToFirebase(uri: Uri) {
            // 파일 이름 설정
            val fileRef: StorageReference =
                FirebaseStorage.getInstance().getReference().child(
                    System.currentTimeMillis().toString() + "." + Files.getFileExtension(uri.toString())
                )
            fileRef.putFile(uri).addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    // 이미지 Uri를 데이터베이스에 넣는다
                    val temp = FirebaseAuth.getInstance().currentUser?.email.toString().split("@")
                    val id = temp[0]
                    FirebaseDatabase.getInstance().getReference().child("postImages").child(id).setValue(
                        MediaStore.Images(uri.toString())
                    )
                    //프로그래스바 숨김
                }
            }
        }
    }
}