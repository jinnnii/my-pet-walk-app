package com.project.petwalk.user

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.project.petwalk.databinding.ActivityUserMyPageBinding

class UserMyPageActivity : AppCompatActivity() {
    lateinit var databaseRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val auth = Firebase.auth

        // 로그인하고 있는 회원의 이메일을 받아서 출력한다.
        val firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        val email = user?.email.toString()
        // 이메일 부분의 "@"앞쪽 부분을 잘라서 temp에 저장 (즉, 아이디 부분 저장)
        val temp = email.split("@")
        // 생성한 temp를 id에 저장
        val id = temp[0]
        binding.tvResult.text = "${email}님의 마이페이지"

        // 로그아웃
        binding.btnLogout.setOnClickListener {
            // 로그인 화면으로
            val intent = Intent(this, UserLoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            auth?.signOut()
        }
        databaseRef = FirebaseDatabase.getInstance().getReference()
        databaseRef.get().addOnSuccessListener {
            // dataSnapshot이 it으로 반환됨.
            // 파이어베이스의 실시간 데이터베이스에 Users밑에 해당하는 id의 값에 값을 받아온다.
            var value = it.child("Users").child(id).getValue()
            // 만약 값이 없으면
            if (value == null) {
                // 내정보 보기 및 비밀번호 변경을 숨김
                binding.btnMyinfo.visibility = View.GONE
                binding.btnModify.visibility = View.GONE
                binding.tvGoogle.text = "올바르지 않는 유저는 정보 수정 및 비밀번호 변경이 불가능 합니다."
            } else {
                binding.tvGoogle.visibility = View.GONE
            }
        }

        if (id=="admin"){
            binding.btnUserList.setOnClickListener {
                val intent = Intent(this@UserMyPageActivity , UserAdminPageActivity::class.java)
                startActivity(intent)
            }
        } else{
            binding.btnUserList.visibility=View.GONE
        }

        // 버튼을 누를 경우 내정보 수정 페이지로 간다.
        binding.btnMyinfo.setOnClickListener {
            val intent = Intent(this, UserMyInfoActivity::class.java)
            startActivity(intent)
        }

        // 비밀번호 변경
        binding.btnModify.setOnClickListener {
            var editTextNewPassword = EditText(this)

            // 비밀번호 변경 인스턴스 생성
            editTextNewPassword.transformationMethod = PasswordTransformationMethod.getInstance()

            // 변경 창 생성
            var alertDialog = AlertDialog.Builder(this)
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
            auth.currentUser!!.delete()
            // 실시간 데이터베이스에 저장된 해당 계정 정보 삭제
            databaseRef.child("Users").child(id).removeValue()
            // 성공 메시지 출력
            Toast.makeText(this, "성공적으로 탈퇴되었습니다.", Toast.LENGTH_LONG).show()
            // 로그인 액티비티로 이동
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
        }
    }


    // 비밀번호 변경
    fun changePassword(password: String) {
        // 파이어베이스 인증 인스턴스를 생성 후 현재 유저의 비밀번호를 업데이트 한다.
        FirebaseAuth.getInstance().currentUser!!.updatePassword(password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }
}