package com.example.harumub_front

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main2.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class MainActivity2 : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface

    lateinit var main2_this : androidx.drawerlayout.widget.DrawerLayout
    lateinit var drawer_button : ImageButton
    lateinit var recent_button: ImageButton
    lateinit var drawer_view : NavigationView

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecommendAdapter1.ViewHolder>? = null

    private var layoutManager2: RecyclerView.LayoutManager? = null
    private var adapter2: RecyclerView.Adapter<RecommendAdapter2.ViewHolder>? = null

    // 현재 로그인하고 있는 사용자 아이디, 이름
    private val id = intent.getStringExtra("user_id")
    private val name = intent.getStringExtra("user_name")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // 로그인 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id") && intent.hasExtra("user_name")) {
            Log.d("MainActivity2", "로그인에서 받아온 id : $id, name : $name")
        } else {
            Log.e("MainActivity2", "가져온 데이터 없음")
        }

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        main2_this = findViewById(R.id.main2_drawer)
        drawer_button = findViewById(R.id.drawer_button) // 드로어 열기(메뉴버튼)
        drawer_view = findViewById(R.id.drawer_view) // 드로어
        val drawerHeader = drawer_view.getHeaderView(0) // 드로어 헤더
        recent_button = findViewById(R.id.recent) // 최근 감상기록 버튼

        // 현재 로그인하고 있는 사용자 아이디 (수정 필요) --수민 작성
        // var userid = ""
        var result : List<Recommend2Result>
        var userIds : Array<String> = emptyArray()
        var titles : Array<String> = emptyArray()
        var posters : Array<String> = emptyArray()

        // 추천 1, 들어가면 토스트 메시지로 추천 영화 10개 전달되는 것 확인되게 코드 작성해둠
        val map1 = HashMap<String, String>()
        map1.put("id", id!!)

        val call1 = retrofitInterface.executeRecommend1(map1)
        call1!!.enqueue(object : Callback<List<String>?> {
            override fun onResponse(call: Call<List<String>?>, response: Response<List<String>?>) {
                if (response.code() == 200) {
                    val result = response.body()

                    Toast.makeText(this@MainActivity2, "추천 영화 목록 : "+result, Toast.LENGTH_LONG).show()
                }
                else if (response.code() == 400) {
                    Toast.makeText(this@MainActivity2, "정의되지 않음", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<String>?>, t: Throwable) {
                Toast.makeText(this@MainActivity2, t.message,
                    Toast.LENGTH_LONG).show()
            }
        })

        // 추천 2
        val map2 = HashMap<String, String>()
        map2.put("id", id!!)

        val call = retrofitInterface.executeRecommend2(map2)
        call!!.enqueue(object : Callback<List<Recommend2Result?>> {
            override fun onResponse(call: Call<List<Recommend2Result?>>, response: Response<List<Recommend2Result?>>) {
                if (response.code() == 200) {
                    val result = response.body()

                    for(i in 0..result?.size!!-1){
                        // (userid) title 과 Poster url 은 배열에 저장. -> 리사이클러뷰에 넣어야 함 -- 수민 작성
                        userIds[i] = result.get(i)!!.userId
                        titles[i] = result.get(i)!!.title
                        posters[i] = result.get(i)!!.poster

                        Toast.makeText(this@MainActivity2, "유사 사용자 추천 성공", Toast.LENGTH_SHORT).show()
                    }
                }
                else if (response.code() == 400) {
                    Toast.makeText(this@MainActivity2, "정의되지 않음", Toast.LENGTH_LONG).show()
                }
//                else if (response.code() == 404) {
//                    Toast.makeText(this@MainActivity2, "404 오류", Toast.LENGTH_LONG).show()
//                }
            }
            override fun onFailure(call: Call<List<Recommend2Result?>>, t: Throwable) {
                Toast.makeText(this@MainActivity2, t.message,
                    Toast.LENGTH_LONG).show()
            }
        })

        // 드로어 버튼 클릭 -> 드로어 메뉴 열기
        drawer_button.setOnClickListener{
            main2_this.openDrawer(GravityCompat.START) // START = left, END : right (드로어가 나오는 방향지정)
        }
        // 네비게이션 메뉴 아이템에 클릭 속성 부여
        drawer_view.setNavigationItemSelectedListener(this)

        // 최근 감상 기록 (시계) 버튼 클릭 -> 페이지 이동
        recent_button.setOnClickListener{
            val intent = Intent(this, WatchListActivity::class.java)
            intent.putExtra("user_id", id)
            startActivity(intent)
        }

        // '내가 좋아하는' 영화 목록 RecyclerView와 RecommendAdapter1 연결
        layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        adapter = RecommendAdapter1()
        recyclerView.adapter = adapter

        // '다른 사용자가 좋아하는' 영화 목록 RecyclerView와 RecommendAdapter2 연결
        layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView2.layoutManager = layoutManager2
        adapter2 = RecommendAdapter2()
        recyclerView2.adapter = adapter2

//        var text = findViewById<TextView>(R.id.textView3) // '다른 사용자가 좋아하는' 텍스트
//        // 원래는 이미지버튼에서 넘어가야 함
//        text.setOnClickListener { // '다른 사용자가 좋아하는' 텍스트 클릭 시 다른 사용자 감상기록 페이지로 이동. 수정 필요
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.main2, UserMovieListFragment())
//                .commit()
//            Log.d("text : ", "선택")
//        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {// 네비게이션 메뉴 아이템 클릭 시 수행
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

                                Toast.makeText(this@MainActivity2, "로그아웃합니다..",
                                    Toast.LENGTH_LONG).show()
                                startActivity(intent)
                            }
                        }

                        override fun onFailure(call: Call<Void?>, t: Throwable) {
                            Toast.makeText(this@MainActivity2, t.message,
                                Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
        main2_this.closeDrawers() // 네비게이션 뷰 닫기
        return false
    }

}