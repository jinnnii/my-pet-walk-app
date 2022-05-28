package com.project.petwalk.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.project.petwalk.MainActivity
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityUserLoginBinding
import com.project.petwalk.model.Images
import com.project.petwalk.model.User

class UserLoginActivity : AppCompatActivity() {
    lateinit var firebaseAuth: FirebaseAuth
    val GOOGLE_REQUEST_CODE = 99

    lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        // 구글 로그인을 통해 앱에서 요구하는 사용자의 데이터를 요청한다.
        // GoogleSignInOptions 개체를 만들고 DEFAULT_SIGN_IN 을 매개변수로 주면 사용자의 기본적인 정보(userID, basic profile)를 얻을 수 있다.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // gso를 통해 가져올 클라이언트의 정보를 담을 GoogleSignInClient 개체를 만든다.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // signInButton 변수에 xml에서 작성한 버튼을 넣어주었고 버튼을 누르면 signIn() 함수를 호출하며 드디어 로그인을 하게 된다.
        binding.googleSignInBtn.setOnClickListener {
            signIn()
        }

        // 가입 버튼이 눌리면
        binding.ClickRegister.setOnClickListener {
            // 회원가입 액티비티를 실행
            startActivity(Intent(this@UserLoginActivity, UserRegisterActivity::class.java))
        }

        // 로그인 버튼이 눌리면
        binding.btnLogin.setOnClickListener {
            // 로그인 정보를 받아서 String형으로 변환
            val email = binding.emailEt.getText().toString()
            val pwd = binding.pwdEt.getText().toString()
            if (email.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this@UserLoginActivity, "로그인 정보를 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                // 파이어베이스 인증의 로그인 메소드를 사용하여 로그인
                firebaseAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(
                        this@UserLoginActivity
                    ) { task ->
                        // 로그인이 성공하면 마이페이지 액티비티를 실행
                        if (task.isSuccessful) {
                            val intent = Intent(this@UserLoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            // 로그인이 실패하면 로그인 오류라는 토스트 메시지 생성
                        } else {
                            Toast.makeText(this@UserLoginActivity, "로그인 오류", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }
    }

    // 로그인 화면으로 넘어가기 위해 getSignInIntent 를 가져온 후 로그인 액티비티를 실행한다.
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE)
    }

    // startActivityForResult 함수를 사용했기 때문에 결과값을 받아 사용할 onActivityResult 함수를 만들어야 한다.
    // 이 함수 안에는 로그인 intent를 시작함에 대한 결과를 작성한다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // GoogleSignInApi.getSignInInent(...)에서 Intent를 실행한 후 반환된 결과입니다.
        if (requestCode == GOOGLE_REQUEST_CODE) {
            // 실행한 액티비티가 로그인에 대한 액티비티이므로 로그인한 사용자에 대한 정보를 활용하기 위해 task 변수에 getSignedInAccountFromIntent 로 가져와 담는다.
            // 만약 밑에 구문에서 오류가 나면 파이어베이스에서 생성한 프로젝트에 SHA1키의 값이 다르기 때문이다.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // 구글 로그인에 성공하면 파이어베이스로 인증
                val account = task.getResult(ApiException::class.java)!!
                Log.d("google", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // 로그인 실패 시 토스트 메시지 출력
                Log.w("google", "Google sign in failed", e)
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 구글 로그인으로 파이어베이스 인증 받아오기
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        // 파이어베이스의 인증 메소드를 이용하여 파이어베이스로 부터 인증을 받는다
        firebaseAuth.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val database = FirebaseDatabase.getInstance()
                    // 로그인에 성공하면 로그인한 사용자의 이메일을 받아서 loginSuccess함수 실행
                    Log.d("google", "로그인 성공")
                    val user = firebaseAuth.currentUser!!
                    val uid = user.uid
                    val name = user.displayName.toString()
                    val phone = user.phoneNumber.toString()
                    // 파이어베이스로 인증한 현재 유저의 이메일을 받아서 String형으로 변환
                    val email = user.email.toString()
                    // "@" 기준으로 잘라서 temp에 저장
                    val temp = email.split("@")
                    // "@"의 앞쪽 부분을 id에 저장
                    val id = temp[0]
                    // 회원 정보 저장
                    database.getReference().child("Users").child(id)
                        .setValue(User(uid, email, name, phone))
                    // 프로필 사진 초기값으로 데이터베이스에 경로 생성
                    database.getReference().child("profileImages").child(id).setValue(Images(""))
                    /*database.getReference().get().addOnSuccessListener {
                        // dataSnapshot이 it으로 반환됨.
                        val Urimap = it.child("images").child(id).getValue() as HashMap<String, Any>
                        val profileUrl = Urimap.get("imageUrl").toString()
                        // 구글 로그인을 할 때 프로필 사진 경로를 생성하지 않았으면
                        if (profileUrl != "") {
                            database.getReference().child("images").child(id).setValue(ProfileImages(""))
                        }
                    }*/
                    loginSuccess()
                } else {
                    // 로그인에 실패하면 예외처리 및 실패 로그 출력
                    Log.w("google", "signInWithCredential:failure", task.exception)
                }
            }
    }

    // 로그인이 성공하면 파이어베이스로 인증된 메인 액티비티를 실행한다.
    private fun loginSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
