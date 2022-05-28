package com.project.petwalk.user

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.common.io.Files
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.petwalk.databinding.ActivityUserPetPageBinding
import com.project.petwalk.firebase.FirebasePetHelper
import com.project.petwalk.model.Pet

class UserPetPageActivity : AppCompatActivity() {
    private val REQUEST_CODE = 0
    var imageUri: Uri? = null
    lateinit var mDialog: ProgressDialog
    lateinit var binding: ActivityUserPetPageBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance().getReference()
    private val storagereference: StorageReference = FirebaseStorage.getInstance().getReference()

    lateinit var userUID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserPetPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인하고 있는 회원의 이메일을 받아서 출력한다.
        val firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        userUID=user?.uid.toString()

        val email = user?.email.toString()
        // 이메일 부분의 "@"앞쪽 부분을 잘라서 temp에 저장 (즉, 아이디 부분 저장)
        val temp = email.split("@")
        // 생성한 temp를 id에 저장
        val id = temp[0]

        binding.button2.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE)
        }

        // 프로필 사진 업로드
        binding.btnPetAdd.setOnClickListener {
            if (imageUri != null) {
                mDialog = ProgressDialog(this)
                mDialog.setMessage("적용중입니다...")
                mDialog.show()
                uploadToFirebase(imageUri!!);
            } else {
                Toast.makeText(this, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    fun setPetData(petURI:String){
        val animal = binding.editKind.text.toString()
        val kind=binding.editKind2.text.toString()
        val name=binding.editName.text.toString()

        if (animal.isEmpty() || kind.isEmpty() || name.isEmpty()){
            Toast.makeText(
                this@UserPetPageActivity,
                "펫 정보를 입력해주세요.",
                Toast.LENGTH_SHORT
            ).show()
            mDialog.dismiss()
        } else {
            val pet = Pet(animal, kind,name, petURI)
            FirebasePetHelper().addPet(pet, userUID, object : FirebasePetHelper.DataStatus {
                override fun DataIsLoaded(pet: Pet) {}
                override fun DataIsInserted() {

                }
                override fun DataIsUpdated() {}
                override fun DataIsDeleted() {}
            })

            mDialog.dismiss()


            val intent = Intent();
            setResult(RESULT_OK, intent);
            finish();

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        // 뒤로가기 버튼이 눌렸을시
        return super.onSupportNavigateUp() // 뒤로가기 버튼
    }

    // 갤러리에 선택한 사진 가져오기
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                try {
                    imageUri = data!!.data!!
                    Glide.with(this).load(imageUri)
                        .into(binding.imageView3) //다이얼로그 이미지사진에 넣기
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
                // 이미지 Uri
                val petProfileURI = uri.toString()
                setPetData(petProfileURI)
//                Toast.makeText(this, "적용이 완료되었습니다", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "적용에 완료되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
}