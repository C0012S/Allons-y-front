package com.example.harumub_front

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class LoginActivity : AppCompatActivity() {
    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        var l_id = findViewById<EditText>(R.id.myid)
        var l_pw = findViewById<EditText>(R.id.mypw)
        var btnLogin = findViewById<Button>(R.id.btn_login)
        var btnSignup = findViewById<Button>(R.id.btn_signup)

        // 단방향 페이지 이동
        // 로그인 버튼 클릭 - 액티비티 종료 및 메인페이지 호출
        btnLogin.setOnClickListener {
            // 에뮬레이터 실행용
//            var intent = Intent(applicationContext, MainActivity2::class.java)
//            startActivity(intent)


            // 사용자가 입력한 값들을 String으로 받아오기
            val id = l_id.text.toString()
            val pw = l_pw.text.toString()

            val map = HashMap<String, String>()
            map.put("id", id)
            map.put("password", pw)

            val call = retrofitInterface.executeLogin(map)
            call!!.enqueue(object : Callback<LoginResult?> {
                override fun onResponse(call: Call<LoginResult?>, response: Response<LoginResult?>) {
                    if (response.code() == 200) {
                        val result = response.body()
                        val builder1 = AlertDialog.Builder(this@LoginActivity)
                        builder1.setTitle("로그인 성공")
                        builder1.setMessage(result!!.name + "님 환영합니다!")
                        builder1.show()

                        // 서버에서 전달받은 감상결과 record=false => 메인1로 이동
                        if(result.record == false) {
                            var intent = Intent(applicationContext, MainActivity1::class.java)
                            intent.putExtra("user_id", result.id)
                            intent.putExtra("user_name", result.name)
                            startActivityForResult(intent, 0)
                        }
                        // 서버에서 전달받은 감상결과 record=true => 메인2로 이동
                         if (result.record == true) {
                             var intent = Intent(applicationContext, MainActivity2::class.java)
                             intent.putExtra("user_id", result.id)
                             intent.putExtra("user_name", result.name)
                             startActivityForResult(intent, 0)
                         }
                    }
                    else if (response.code() == 400) {
                        Toast.makeText(this@LoginActivity, "정의되지 않은 사용자", Toast.LENGTH_LONG).show()
                    }
                    else if (response.code() == 404) {
                        Toast.makeText(this@LoginActivity, "404 오류", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<LoginResult?>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, t.message,
                        Toast.LENGTH_LONG).show()
                }
            })
        }

        // 양방향 액티비티 (회원가입 <-> 로그인)
        // 회원가입 버튼 클릭 - 회원가입 페이지로 이동
        btnSignup.setOnClickListener {
            var intent2 = Intent(applicationContext, SignupActivity::class.java)
            startActivity(intent2)
        }
    }


}