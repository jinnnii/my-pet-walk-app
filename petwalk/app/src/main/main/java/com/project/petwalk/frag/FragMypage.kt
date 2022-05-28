package com.project.petwalk.frag

import android.app.Activity
import com.project.petwalk.R
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.common.io.Files.getFileExtension
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.petwalk.databinding.FragmentFragMypageBinding
import com.project.petwalk.model.Images
import com.project.petwalk.model.User
import com.project.petwalk.user.*


class FragMypage : Fragment() {
    lateinit var binding: FragmentFragMypageBinding
    private val firebaseAuth = FirebaseAuth.getInstance()


    private val REQUEST_CODE = 0
    var imageUri: Uri? = null
    lateinit var mDialog: ProgressDialog

    // 파이어베이스의 저장소
    private val storagereference: StorageReference = FirebaseStorage.getInstance().reference

    // 파이어베이스의 데이터베이스
    private val databaseRef = FirebaseDatabase.getInstance().getReference()

    lateinit var user:User

    lateinit var profileUID:String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFragMypageBinding.inflate(inflater, container, false)

        // 로그인하고 있는 회원의 이메일을 받아서 출력한다.
        val userAuth = firebaseAuth.currentUser
        val userUID = firebaseAuth.currentUser?.uid.toString()

        val email = userAuth?.email.toString()
        // 이메일 부분의 "@"앞쪽 부분을 잘라서 temp에 저장 (즉, 아이디 부분 저장)
        val temp = email.split("@")
        // 생성한 temp를 id에 저장
        val id = temp[0]
        binding.tvResult.text = "${id}님의 마이페이지"

        databaseRef.child("Users").child(userUID)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java) as User
                    setView(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        if (id == "admin") {
            binding.btnUserList.setOnClickListener {
                val intent = Intent(activity, UserAdminPageActivity::class.java)
                startActivity(intent)
            }
        } else {
            binding.btnUserList.visibility = View.GONE
        }


        // 로그아웃
        binding.btnLogout.setOnClickListener {
            // 로그인 화면으로
            val intent = Intent(activity, UserLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            firebaseAuth.signOut()
        }

        // 버튼을 누를 경우 내정보 수정 페이지로 간다.
        binding.btnMyinfo.setOnClickListener {
            val intent = Intent(activity, UserMyInfoActivity::class.java)
            startActivity(intent)
        }

        // 비밀번호 변경
        binding.btnModify.setOnClickListener {
            var editTextNewPassword = EditText(activity)

            // 비밀번호 변경 인스턴스 생성
            editTextNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()

            // 변경 창 생성
            var alertDialog = AlertDialog.Builder(requireActivity())
            alertDialog.setTitle("패스워드 변경")
            alertDialog.setMessage("변경하고 싶은 패스워드를 입력하세요")
            alertDialog.setView(editTextNewPassword)

            // 긍정 버튼을 생성하고 클릭할 경우 새로운 비밀번호를 매개변수로 넘겨주어 비밀번호 변경 함수 실행
            alertDialog.setPositiveButton(
                "변경",
                { dialogInterface, i -> changePassword(editTextNewPassword.text.toString()) })

            // 부정버튼을 생성하고 클릭하면 변경 창을 보여주지 않는다.
            alertDialog.setNegativeButton("취소", { dialogInterface, i -> dialogInterface.dismiss() })
            alertDialog.show()
        }

        // 회원탈퇴
        binding.btnBan.setOnClickListener {
            // 인증된 현재 계정 삭제
            firebaseAuth.getCurrentUser()!!.delete()
            // 실시간 데이터베이스에 저장된 해당 계정 정보 삭제
            databaseRef.child("Users").child(id).removeValue()
            databaseRef.child("images").child(id).removeValue()
            // 성공 메시지 출력
            Toast.makeText(activity, "성공적으로 탈퇴되었습니다.", Toast.LENGTH_LONG).show()
            // 로그인 액티비티로 이동
            val intent = Intent(activity, UserLoginActivity::class.java)
            startActivity(intent)
        }

        binding.button2.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, REQUEST_CODE)
        }

        // 프로필 사진 업로드
        binding.button3.setOnClickListener {
            if (imageUri != null) {
                mDialog = ProgressDialog(this.requireActivity())
                mDialog.setMessage("적용중입니다...")
                mDialog.show()
                uploadToFirebase(imageUri!!);
            } else {
                Toast.makeText(activity, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
            }
        }

        binding.btnPetList.setOnClickListener {
            val intent = Intent(context, UserPetListActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    fun setView(user:User){
        // 프로필 사진을 불러옴

        // dataSnapshot이 it으로 반환됨.
        for(uid in user.profile.keys){
            profileUID = uid
        }
        Log.d("kej","profileUID>>>>>$profileUID")

        databaseRef.child("profileImages").child(profileUID)
            .get().addOnSuccessListener {
                val Urimap = it.getValue(Images::class.java)
                val profileUrl = Urimap?.imageUrl

                if(profileUrl==""){
                    binding.imageView3.setImageResource(R.drawable.profile1)
                }else{
                    Glide
                        .with(this)
                        .load(profileUrl)
                        .centerCrop()
                        .into(binding.imageView3)
                }
            }
//            val Urimap = it.child("profileImages").child(profileUID).getValue(Images::class.java)
        //만약 프로필 사진이 없으면



        databaseRef.get().addOnSuccessListener {
            // dataSnapshot이 it으로 반환됨.
            // 파이어베이스의 실시간 데이터베이스에 Users밑에 해당하는 id의 값에 값을 받아온다.
            val value = it.child("Users").child(user.uid).value
            // 만약 값이 없으면
            if (value == null) {
                // 내정보 보기 및 비밀번호 변경을 숨김
                binding.btnMyinfo.visibility = View.GONE
                binding.btnModify.visibility = View.GONE
                binding.tvGoogle.text = "올바르지 않는 유저는 내 정보 수정 및 비밀번호 변경이 불가능 합니다."
            } else {
                binding.tvGoogle.visibility = View.GONE
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
        if (requestCode == REQUEST_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                try {
                    imageUri = data!!.data!!
                    Glide.with(this.requireActivity().applicationContext).load(imageUri)
                        .into(binding.imageView3) //다이얼로그 이미지사진에 넣기
                } catch (e: Exception) {
                }
            } else if (resultCode == AppCompatActivity.RESULT_CANCELED) { // 취소시 호출할 행동 쓰기
            }
        }
    }

    // 비밀번호 변경
    fun changePassword(password: String) {
        // 파이어베이스 인증 인스턴스를 생성 후 현재 유저의 비밀번호를 업데이트 한다.
        FirebaseAuth.getInstance().currentUser!!.updatePassword(password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "비밀번호가 변경되었습니다.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(activity, task.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    // 프로필 사진 업로드
    private fun uploadToFirebase(uri: Uri) {
        // 파일 이름 설정
        val fileRef: StorageReference =
            storagereference.child(
                System.currentTimeMillis().toString() + "." + getFileExtension(uri.toString())
            )
        fileRef.putFile(uri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // 이미지 Uri를 데이터베이스에 넣는다
//                val temp = firebaseAuth.currentUser?.email.toString().split("@")
//                val id = temp[0]
//                databaseRef.child("profileImages").child(id).setValue(Images(uri.toString()))

                databaseRef.child("profileImages").child(profileUID)
                    .setValue(Images(uri.toString()))
                //프로그래스바 숨김
                mDialog.dismiss()
                Toast.makeText(activity, "적용이 완료되었습니다", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(activity, "적용에 완료되었습니다", Toast.LENGTH_SHORT).show()
        }
    }
}

