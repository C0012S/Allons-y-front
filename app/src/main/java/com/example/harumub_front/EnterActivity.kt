package com.example.harumub_front

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_enter.*
import kotlinx.android.synthetic.main.dialog_entercode.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class EnterActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var enter_this : androidx.drawerlayout.widget.DrawerLayout
    lateinit var drawer_button : ImageButton
    lateinit var recent_button: ImageButton
    lateinit var drawer_view : NavigationView

    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface
    // 현재 로그인하고 있는 사용자 아이디, 이름
    private val id = intent.getStringExtra("user_id")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        // 메인 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id")) {
            Log.e("EnterActivity", "메인에서 받아온 id : $id")
        } else {
            Log.e("EnterActivity", "가져온 데이터 없음")
        }

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        enter_this = findViewById(R.id.enter_drawer)
        drawer_button = findViewById(R.id.drawer_button) // 드로어 열기(메뉴버튼)
        drawer_view = findViewById(R.id.drawer_view) // 드로어
        val drawerHeader = drawer_view.getHeaderView(0) // 드로어 헤더
        recent_button = findViewById(R.id.recent) // 최근 감상기록 버튼

        // 드로어 버튼 클릭 -> 드로어 메뉴 열기
        drawer_button.setOnClickListener{
            enter_this.openDrawer(GravityCompat.START) // START = left, END : right (드로어가 나오는 방향지정)
        }
        // 네비게이션 메뉴 아이템에 클릭 속성 부여
        drawer_view.setNavigationItemSelectedListener(this)

        // 최근 감상 기록 (시계) 버튼 클릭 -> 페이지 이동
        recent_button.setOnClickListener{
            val intent = Intent(this, WatchListActivity::class.java)
            intent.putExtra("user_id", id)
            startActivity(intent)
        }

        // 방 생성 버튼
        createNewroom.setOnClickListener{ // 새로운 방 생성 버튼 클릭 시 같이보기 페이지로 이동
            val map = HashMap<String, String>()
            val call = retrofitInterface.executeMakeRoom(map)
            call!!.enqueue(object : Callback<MakeRoomResult?> {
                override fun onResponse(call: Call<MakeRoomResult?>, response: Response<MakeRoomResult?>) {
                    if (response.code() == 200) {
                        val result = response.body()
                        val builder1 = androidx.appcompat.app.AlertDialog.Builder(this@EnterActivity)
                        builder1.setTitle("방 생성 성공, 초대코드 : " + result!!.roomCode)
                        builder1.show()

                        var intent = Intent(applicationContext, TogetherActivity::class.java) // 두번째 인자에 이동할 액티비티
                        intent.putExtra("roomCode", result.roomCode)
                        intent.putExtra("user_id", id)
                        startActivityForResult(intent, 0)
                    }
                    else if (response.code() == 400) {
                        Toast.makeText(this@EnterActivity, "정의되지 않음", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<MakeRoomResult?>, t: Throwable) {
                    Toast.makeText(this@EnterActivity, t.message,
                        Toast.LENGTH_LONG).show()
                }
            })

//            var intent = Intent(applicationContext, TogetherActivity::class.java)
//            startActivityForResult(intent, 0)
        }

        // 초대 코드 입력 버튼
        writeCode.setOnClickListener() { // 초대코드 입장 버튼 클릭 시 다이얼로그 띄워 줌
            val dig = AlertDialog.Builder(this)

            val dialogView = View.inflate(this, R.layout.dialog_entercode, null)
            dig.setView(dialogView)
            dig.setPositiveButton("확인") { dialog, which ->
                Toast.makeText(this@EnterActivity,
                    "확인 누름", Toast.LENGTH_LONG).show()
                val map = HashMap<String, String>()

                var codeEdit = dialogView.findViewById<EditText>(R.id.code_edittext)

                var getroomCode = codeEdit.text.toString()
                map.put("roomCode", getroomCode)

                val call = retrofitInterface.executeEnterRoom(map)

                call!!.enqueue(object : Callback<Void?> {
                    override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                        if (response.code() == 200) {
                            Toast.makeText(this@EnterActivity,
                                "방 코드 : " + getroomCode + " 에 입장합니다.", Toast.LENGTH_LONG).show()
                            val intent = Intent(applicationContext, TogetherActivity::class.java)
                            intent.putExtra("user_id", id)
                            startActivityForResult(intent, 0)

                        } else if (response.code() == 400) {
                            Toast.makeText(this@EnterActivity, "잘못된 방 코드 입니다.",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<Void?>, t: Throwable) {
                        Toast.makeText(this@EnterActivity, t.message,
                            Toast.LENGTH_LONG).show()
                    }
                })
/*
                val intent = Intent(applicationContext, TogetherActivity::class.java)
                startActivityForResult(intent, 0)
*/
            } // 확인 버튼 클릭 시 같이보기 페이지로 이동
            dig.setNegativeButton("취소") { dialog, which ->
                Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_LONG).show()
            } // 취소 버튼 클릭 시 취소되었다는 토스트 메세지를 보여 줌
            dig.show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {// 네비게이션 메뉴 아이템 클릭 시 수행
        when(item.itemId){ // 드로어 메뉴 눌렸을 시 수행. 수정 필요
            R.id.drawer_UserRecord -> {
                // fragment manager 가져와서 fragment transaction 생성
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "사용자 기록보기", Toast.LENGTH_SHORT).show()

                    val intent = Intent(applicationContext, WatchListActivity::class.java)
                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 0) // + 결과값 전달 // requestCode: 액티비티 식별값 - 원하는 값
                    commit()
                }
            }
            R.id.drawer_WatchAlone -> {
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "혼자 보기", Toast.LENGTH_SHORT).show()

                    val intent = Intent(applicationContext, SearchActivity::class.java)
                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 0) // + 결과값 전달 // requestCode: 액티비티 식별값 - 원하는 값
                    commit()
                }
            }
            R.id.drawer_WatchTogether -> {
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "같이 보기", Toast.LENGTH_SHORT).show()

                    val intent = Intent(applicationContext, EnterActivity::class.java)
                    intent.putExtra("user_id", id)
                    startActivityForResult(intent, 0) // + 결과값 전달 // requestCode: 액티비티 식별값 - 원하는 값
                    commit()
                }
            }
            R.id.drawer_Help -> {
                with(supportFragmentManager.beginTransaction()) {
                    Toast.makeText(applicationContext, "도움말", Toast.LENGTH_SHORT).show()

                    val intent = Intent(applicationContext, HelpActivity::class.java)
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

                                Toast.makeText(this@EnterActivity, "로그아웃합니다..",
                                    Toast.LENGTH_LONG).show()
                                startActivity(intent)
                            }
                        }

                        override fun onFailure(call: Call<Void?>, t: Throwable) {
                            Toast.makeText(this@EnterActivity, t.message,
                                Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
        enter_this.closeDrawers() // 네비게이션 뷰 닫기
        return false
    }
}