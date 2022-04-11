package com.example.harumub_front

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main1.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class MainActivity1 : AppCompatActivity() ,NavigationView.OnNavigationItemSelectedListener {
    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface

    lateinit var main_this : androidx.drawerlayout.widget.DrawerLayout
    lateinit var drawer_button : ImageButton
    lateinit var recent_button: ImageButton
    lateinit var drawer_view : NavigationView
    lateinit var btnRecord : Button

    private val id = intent.getStringExtra("user_id")
    private val name = intent.getStringExtra("user_name")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        // 로그인 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id") && intent.hasExtra("user_name")) {
            Log.d("MainActivity1", "로그인에서 받아온 id : $id, name : $name")
        } else {
            Log.e("MainActivity1", "가져온 데이터 없음")
        }

        main_this = findViewById(R.id.main_drawer)
        drawer_button = findViewById(R.id.drawer_button) // 드로어 열기(메뉴버튼)
        drawer_view = findViewById(R.id.drawer_view) // 드로어
        val drawerHeader = drawer_view.getHeaderView(0) // 드로어 헤더
        recent_button = findViewById(R.id.recent) // 최근 감상기록 버튼


        // 드로어 버튼 클릭 -> 드로어 메뉴 열기
        drawer_button.setOnClickListener{
            main_this.openDrawer(GravityCompat.START) // START = left, END : right (드로어가 나오는 방향지정)
        }
        // 네비게이션 메뉴 아이템에 클릭 속성 부여
        drawer_view.setNavigationItemSelectedListener(this)

        // 최근 감상 기록 (시계) 버튼 클릭 -> 드로어 메뉴 열기
        recent_button.setOnClickListener{
            val intent = Intent(this, WatchListActivity::class.java)
            intent.putExtra("user_id", id)
            startActivity(intent)
        }

        // 감상하기 버튼
        btnRecord = findViewById(R.id.btnRecord)
        btnRecord.setOnClickListener { // 감상하기 버튼 클릭 시 영화 검색 페이지로 이동
            //val intent = Intent(this, SearchActivity::class.java) // 영화 검색 페이지
            val intent = Intent(this, MainActivity2::class.java) // 메인 2 페이지
            intent.putExtra("user_id", id)
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { // 네비게이션 메뉴 아이템 클릭 시 수행
        when(item.itemId){ // 드로어 메뉴 눌렸을 시 수행. 수정 필요
            R.id.drawer_UserRecord -> {
                // fragment manager 가져와서 fragment transaction 생성
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "사용자 기록보기", Toast.LENGTH_SHORT).show()

                    var intent = Intent(applicationContext, WatchListActivity::class.java)
                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 0) // + 결과값 전달 // requestCode: 액티비티 식별값 - 원하는 값
                    commit()
                }
            }
            R.id.drawer_WatchAlone -> {
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "혼자 보기", Toast.LENGTH_SHORT).show()

                    var intent = Intent(applicationContext, SearchActivity::class.java)
                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 0) // + 결과값 전달 // requestCode: 액티비티 식별값 - 원하는 값
                    commit()
                }
            }
            R.id.drawer_WatchTogether -> {
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "같이 보기", Toast.LENGTH_SHORT).show()

                    var intent = Intent(applicationContext, EnterActivity::class.java)
                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 0) // + 결과값 전달 // requestCode: 액티비티 식별값 - 원하는 값
                    commit()
                }
            }
            R.id.drawer_Help -> {
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "도움말", Toast.LENGTH_SHORT).show()

                    var intent = Intent(applicationContext, HelpActivity::class.java)
                    startActivityForResult(intent, 0)
                    commit()
                } // 프래그먼트 트랜잭션 변경 후 commit() 호출해야 변경 내용 적용
            }
            R.id.drawer_Logout -> {
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "로그아웃합니다..", Toast.LENGTH_SHORT).show()
                    val map = HashMap<String, String>()

                    val call = retrofitInterface.executeLogout(map)
                    call!!.enqueue(object : Callback<Void?> {
                        override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                            if (response.code() == 200) {
                                val result = response.body()

                                var intent = Intent(applicationContext, LoginActivity::class.java) // 두번째 인자에 이동할 액티비티

                                Toast.makeText(this@MainActivity1, "로그아웃합니다..", Toast.LENGTH_LONG).show()
                                startActivity(intent)
                            }
                        }

                        override fun onFailure(call: Call<Void?>, t: Throwable) {
                            Toast.makeText(this@MainActivity1, t.message,
                                Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
        main_this.closeDrawers() // 네비게이션 뷰 닫기
        return false
    }
}
