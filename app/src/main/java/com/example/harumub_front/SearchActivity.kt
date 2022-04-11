package com.example.harumub_front

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() , TextWatcher {
    var recyclerView: RecyclerView? = null
    var editText: EditText? = null
    var adapter: SearchAdapter? = null
    var items = ArrayList<String>()
    var poster = arrayOf(R.drawable.about, R.drawable.gucci, R.drawable.spider)
    var title = arrayOf("About Times", "Gucci", "Spider Man3")
    // 현재 로그인하고 있는 사용자 아이디
    private val id = intent.getStringExtra("user_id")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // 메인 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id")) {
            Log.d("SearchActivity", "메인에서 받아온 id : $id")
        } else {
            Log.e("SearchActivity", "가져온 데이터 없음")
        }

        recyclerView = findViewById<View>(R.id.recylcerview) as RecyclerView
        editText = findViewById<View>(R.id.search_edt) as EditText
        editText!!.addTextChangedListener(this)

        // items 배열에 영화 제목 넣기
        for(i: Int in 0..poster.size-1) {
            items.add(title[i]) // item 배열에 영화 제목 추가
        }

        //
        adapter = SearchAdapter(applicationContext, items)
        recyclerView!!.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView!!.adapter = adapter

        // 혼자보기 입장 버튼 클릭 시 혼자보기 페이지로 이동
        watch_alone_enter_button.setOnClickListener {
            var intent = Intent(applicationContext, WatchAloneActivity::class.java)
            intent.putExtra("user_id", id)
            // intent.putExtra("movie_title", movie_title)
            startActivity(intent)
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        //
        adapter?.getFilter()?.filter(charSequence)
    }

    override fun afterTextChanged(editable: Editable) {}
}