package com.example.harumub_front

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    var isExistBlank = false // 빈칸이 있는지 확인
    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        var j_id = findViewById<EditText>(R.id.id)
        var j_pw = findViewById<EditText>(R.id.pw)
        var j_name = findViewById<EditText>(R.id.name)
        var j_email = findViewById<EditText>(R.id.email)
        var j_code = findViewById<EditText>(R.id.code)

        var btnReq = findViewById<Button>(R.id.btn_request)
        var btnJoin = findViewById<Button>(R.id.btn_join)
        var btnAuth = findViewById<Button>(R.id.btn_auth)

        // 아이디 특수 문자 금지
        var inputLayout = findViewById<TextInputLayout>(R.id.input_layout)
        var check_id = inputLayout.getEditText()

        if (check_id != null) {
            check_id.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // text가 변경된 후 호출
                    // s에는 변경 후의 문자열이 담겨 있다.
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // text가 변경되기 전 호출
                    // s에는 변경 전 문자열이 담겨 있다.
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // text가 바뀔 때마다 호출
                    if (s.toString().contains("#")) {
                        inputLayout.setError("특수 문자는 사용할 수 없습니다.");
                    } else {
                        inputLayout.setError(null); // null은 에러 메시지를 지워주는 기능
                    }

                }
            })
        }

        // 비밀번호 암호화(*)
//        var inputLayout = findViewById<TextInputLayout>(R.id.input_layout)
//        inputLayout.setPasswordVisibilityToggleEnabled(true)

        // 이메일 인증을 위한 변수 선언
        var result : EmailResult?
        var getcode : String? = null

        // 이메일 검사 정규식
        val emailValidation = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

        // 이메일 검사 함수
        fun checkEmail():Boolean{
            var e_mail = j_email.text.toString().trim() // 텍스트 변환 및 공백제거
            val pattern = Pattern.matches(emailValidation, e_mail) // 서로 패턴이 맞는지 확인
            if (pattern) {
                //이메일 형태가 정상일 경우
                j_email.setTextColor(R.color.black.toInt())
                btnReq.setEnabled(true)
                return true
            } else { // 이메일 형태가 정상이 아닐 경우 - 빨간 텍스트로 표시
                j_email.setTextColor(-65536) // R.color.red.toInt()
                return false
            }
        }

        // EditText item에 TextWatcher 연결
        j_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // text가 변경된 후 호출
                // s에는 변경 후의 문자열이 담겨 있다.
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // text가 변경되기 전 호출
                // s에는 변경 전 문자열이 담겨 있다.
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // text가 바뀔 때마다 호출
                // 실제로는 함수가 사용됨
                checkEmail()
            }
        })


        // 이메일 (인증코드) 요청 버튼 누르면 -> 노드에 map 전송
        btnReq.setOnClickListener(object : View.OnClickListener{

            var result : EmailResult? = null
            var getcode : String? = null

            override fun onClick(v: View?) {
                var map = HashMap<String, String>()
                map.put("email", j_email.text.toString()) // 'paramId' of 백 코드

                var call = retrofitInterface.executeEmail(map)

                call!!.enqueue(object : Callback<EmailResult?>{
                    override fun onResponse( call: Call<EmailResult?>, response: Response<EmailResult?>) {
                        if(response.code() == 200){
                            result = response.body()
                            getcode = result?.code
                            Toast.makeText(this@SignupActivity, "getCode : " + getcode, Toast.LENGTH_SHORT).show()
                            btnAuth.setEnabled(true)

                            Toast.makeText(this@SignupActivity, "email send successfully", Toast.LENGTH_SHORT).show()
                        }
                        else if (response.code() == 400){
                            Toast.makeText(this@SignupActivity, "email send error", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<EmailResult?>, t: Throwable) {
                        Toast.makeText(this@SignupActivity, t.message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })

        // 사용자가 받은 이메일에서 '인증코드'입력 후 -> 인증코드 확인 버튼 누르면
        btnAuth.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(j_code.getText().toString() == getcode){
                    Toast.makeText(this@SignupActivity, "emailcode good", Toast.LENGTH_SHORT).show()
                    btnJoin.setEnabled(true)
                }
                else {
                    Toast.makeText(this@SignupActivity, getcode + "emailcode wrong", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // 하단 JOIN 버튼 클릭 - 회원 가입 정보 연동 및 액티비티 종료 ->로그인 페이지 호출
        btnJoin.setOnClickListener {
            // 이메일 형식 확인
            if(!checkEmail()){ //틀린 경우
                Toast.makeText(applicationContext,"이메일 형식에 맞게 다시 입력하세요!",Toast.LENGTH_LONG).show()
            }

            // 사용자가 입력한 값들을 String으로 받아오기
            val id = j_id.text.toString()
            val pw = j_pw.text.toString()
            val name = j_name.text.toString()
            val email = j_email.text.toString().trim() //공백제거

            // 유저가 항목을 다 채우지 않았을 경우
            if(id.isEmpty() || pw.isEmpty() || name.isEmpty() || email.isEmpty()){
                isExistBlank = true
                // 작성 경고 토스트 메세지
                Toast.makeText(this, "입력란을 모두 작성바랍니다.", Toast.LENGTH_SHORT).show()
            }
            // 유저가 모든 항목을 다 채웠을 경우
            //if(!isExistBlank){
            else {
                // 회원정보 retrofit 연동
                val map = HashMap<String, String>()
                map.put("id", id)
                map.put("password", pw)
                map.put("name", name)

                val call = retrofitInterface.executeSignup(map)

                call!!.enqueue(object : Callback<Void?> {
                    override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                        if (response.code() == 200) {
                            Toast.makeText(this@SignupActivity,
                                "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show()
                            val intent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(intent)
                        }
                        else if (response.code() == 400) {
                            Toast.makeText(this@SignupActivity, "이미 가입된 정보입니다.",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<Void?>, t: Throwable) {
                        Toast.makeText(this@SignupActivity, t.message,
                            Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }
}