package com.project.petwalk.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.project.petwalk.databinding.ActivityUserRegisterBinding
import com.project.petwalk.model.Images
import com.project.petwalk.model.User

class UserRegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //액션 바 등록하기
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setTitle("Create Account")
        actionBar?.setDisplayHomeAsUpEnabled(true) //뒤로가기버튼
        actionBar?.setDisplayShowHomeEnabled(true) //홈 아이콘
        //파이어베이스 접근 설정
        val firebaseAuth = FirebaseAuth.getInstance()
        //가입버튼 클릭리스너   -->  firebase에 데이터를 저장한다.
        binding.btnSignUp.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //가입 정보 가져오기
                val email = binding.editId.getText().toString()
                val pwd = binding.editPw.getText().toString()
                val pwdcheck = binding.editPwCheck.getText().toString()
                // 이메일과 비밀번호를 입력하지 않았을 경우
                if (email.isEmpty() || pwd.isEmpty()) {
                    // 회원가입 정보를 입력해달라는 메시지 출력
                    Toast.makeText(
                        this@UserRegisterActivity,
                        "회원가입 정보를 입력해주세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                // 회원가입 정보가 정상적으로 입력하였을 때
                } else {
                    // 비밀번호와 비밀번호 확인이 일치할 경우
                    if (pwd == pwdcheck) {
                        Log.d(TAG, "등록 버튼 $email , $pwd")
                        // 가입중이라는 로딩 라이얼로그 출력
                        val mDialog = ProgressDialog(this@UserRegisterActivity)
                        mDialog.setMessage("가입중입니다...")
                        mDialog.show()
                        //파이어베이스에 신규계정 등록하기
                        firebaseAuth.createUserWithEmailAndPassword(email, pwd)
                            .addOnCompleteListener(this@UserRegisterActivity,
                                OnCompleteListener { task ->
                                    //가입 성공시
                                    if (task.isSuccessful) {
                                        // 가입중 표시를 안보이게 하기
                                        mDialog.dismiss()
                                        // 현재 유저
                                        val user = firebaseAuth.currentUser
                                        // 현재 유저의 uid값을 String으로 변환하여 uid의 변수에 저장
                                        val uid = user?.uid.toString()
                                        // 현재 유저의 email값을 String으로 변환하여 uid의 변수에 저장
                                        val email = user?.email.toString()
                                        val temp = email.split("@")
                                        val id = temp[0]
                                        // 회원가입 레이아웃에서 휴대폰 번호를 얻어옴
                                        val phone = binding.editPhone.getText().toString()
                                        // 회원가입 레이아웃에서 이름을 얻어옴
                                        val name = binding.editName.getText().toString()
                                        // User형으로 생성자를 만들어서 파이어베이스 실시간 데이터베이스에 저장


                                        val users = User(uid, email, name, phone)
                                        val model = Images("")


                                        // 파이어베이스의 실시간 데이터베이스 인스턴스 생성
                                        val database = FirebaseDatabase.getInstance()
                                        val userReference=database.reference.child("Users")
                                        val profileReference = database.reference.child("profileImages")

                                        val profileUID = profileReference.push().key.toString()
                                        profileReference.child(profileUID).setValue(model)
                                        userReference.child(uid).setValue(users)
                                        userReference.child(uid).child("profile").setValue(
                                            mapOf(profileUID to true))



                                        //가입이 이루어져을시 가입 화면을 빠져나가서 로그인 화면을 출력.
                                        val intent =
                                            Intent(this@UserRegisterActivity, UserLoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                        // 회원가입에 성공하였다는 토스트 메시지 출력
                                        Toast.makeText(
                                            this@UserRegisterActivity,
                                            "회원가입에 성공하셨습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    // 회원가입에 실패하면 오류메시지를 띄운다



                                    } else {
                                        mDialog.dismiss()
                                        Toast.makeText(
                                            this@UserRegisterActivity,
                                            "회원가입 오류.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        //해당 메소드 진행을 멈추고 빠져나감.
                                        return@OnCompleteListener
                                    }
                                })
                    //비밀번호와 비밀번호 확인이 틀릴 경우
                    } else {
                        Toast.makeText(
                            this@UserRegisterActivity,
                            "비밀번호가 틀렸습니다. 다시 입력해 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        // 뒤로가기 버튼이 눌렸을시
        return super.onSupportNavigateUp() // 뒤로가기 버튼
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}