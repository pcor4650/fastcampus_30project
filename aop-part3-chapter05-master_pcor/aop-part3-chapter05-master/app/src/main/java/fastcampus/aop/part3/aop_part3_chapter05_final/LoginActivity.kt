package fastcampus.aop.part3.aop_part3_chapter05_final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth     //FirebaseAuth에 인스턴스를 저장하기 위해 전역에 변수 선언
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth   //FirebaseAuth.getInstance()와 같은 코드이다

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val facebookLoginButton = findViewById<LoginButton>(R.id.facebookLoginButton)

        //메소드 만들기 단축키? 범위지엉 후 ctrl + alt + m,  initLoginButton
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            //singInWithEmailAndPassword로 이메일 로그인 사용
            //?응 어떻게 사용?
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        successLogin()
                    } else {
                        Toast.makeText(this, "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        //이메일, 패스워드가 공란일 때 회원가입 및 로그인 버튼 비활성화 하기기
       emailEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty() //이메일, 패스워드 모두 공란이 아니어야 true가 됨
            loginButton.isEnabled = enable      //버튼.inEnabled로
            signUpButton.isEnabled = enable
        }

        passwordEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            //아래 메소드를 통해 회원가입 할 수 있다
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "회원가입을 성공했습니다. 로그인 버튼을 눌러 로그인해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "이미 가입한 이메일이거나, 회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

        }

        callbackManager = CallbackManager.Factory.create()
        //페이스북 로그인버튼
        facebookLoginButton.setPermissions("email", "public_profile")
        facebookLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            //로그인 성공공
           override fun onSuccess(loginResult: LoginResult) {
                Log.d("Facebook", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("Facebook", "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d("Facebook", "facebook:onError", error)
                Toast.makeText(this@LoginActivity, "페이스북 로그인에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        })

    }

    //디프리케이트 된 메소드 아님?
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    successLogin()
                } else {
                    Toast.makeText(this, "페이스북 로그인에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //handleSuccessLogin이름이 더 적절
    private fun successLogin() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        //realTime database에 데이터 저장 방법
        val userId: String = auth.currentUser?.uid.orEmpty()
        val currentUserDb = Firebase.database.reference.child("Users").child(userId)
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        currentUserDb.updateChildren(user)

        finish()  //finish()하면 어떻게 됨?
    }
}