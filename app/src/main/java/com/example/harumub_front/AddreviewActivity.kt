package com.example.harumub_front

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.CubeGrid
import kotlinx.android.synthetic.main.fragment_addreview.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.HashMap
import kotlin.properties.Delegates

class AddreviewActivity : AppCompatActivity() {
    // 로딩 다이얼로그
    private lateinit var progressDialog : ProgressDialog

    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface

    private lateinit var myTitle: TextView
    private lateinit var myPoster: ImageView

    // 현재 로그인하고 있는 사용자 아이디, 선택한 영화 아이디
    private val id = intent.getStringExtra("user_id")
    private val movie_title = intent.getStringExtra("movie_title")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_addreview)
        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        // 검색 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id")&&intent.hasExtra("movie_title")) {
            Log.d("WatchAloneActivity", "검색에서 받아온 id : $id , movie title : $movie_title")
        } else {
            Log.e("WatchAloneActivity", "가져온 데이터 없음")
        }

        // 감상했던 영화 정보 출력 및 불러오기
        myTitle = findViewById<TextView>(R.id.title)
        myPoster = findViewById<ImageView>(R.id.poster)

        myTitle.setText(movie_title)

        var myUrl = "" // 서버에서 poster url 받아와야 함!! -- 수정 필요
        var result = "https://image.tmdb.org/t/p/w500"
        var image_task : URLtoBitmapTask = URLtoBitmapTask().apply {
            url = URL(result+myUrl)
        }
        var bitmap : Bitmap = image_task.execute().get()
        myPoster.setImageBitmap(bitmap)


        // 별점 - 람다식을 사용하여 처리
        ratingBar.setOnRatingBarChangeListener{ ratingBar, rating, fromUser ->
            ratingBar.rating = rating
            Toast.makeText(applicationContext, "별점: ${rating}", Toast.LENGTH_SHORT).show()
        }

        // 로딩창 선언
        progressDialog = ProgressDialog(this)
        progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 백그라운드를 투명하게

        // 등록하기 버튼 클릭시 별점, 한줄평 >> 결과 페이지&서버에 전달, 로딩 다이얼로그 출력
        btnAdd.setOnClickListener {
            // 로딩창 실행
            // progressDialog.setCancelable(false) // 외부 클릭으로 다이얼로그 종료 X - 실행 위해 임시로 주석 처리
            progressDialog.show() // 로딩화면 보여주기

            var user_comment = comment.text.toString() // EditText 입력값을 텍스트로

            // 현재 로그인하고 있는 사용자 아이디 (수정 필요) --수민 작성
            //var userid = ""

            var map = HashMap<String, String>()
            map.put("id", id!!)

            // val call = retrofitInterface.executeSceneAnalyze(map)
            val call = retrofitInterface.executeAddReview(map)
            call!!.enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    if (response.code() == 200) {
                        Toast.makeText(this@AddreviewActivity, "리뷰 보내기 성공", Toast.LENGTH_SHORT).show()
                        // val result = response.body()

                        if(response.code() == 200) {
                            // 서버에서 감상 결과를 불러오는 데 성공한 신호(응답)를 받으면 로딩창 종료
                            progressDialog.dismiss()

                            var intent = Intent(applicationContext, ResultActivity::class.java)
                            intent.putExtra("user_id", id)
                            intent.putExtra("movie_title", movie_title)
                            intent.putExtra("user_rating", ratingBar.rating)
                            intent.putExtra("user_comment", user_comment)
                            startActivityForResult(intent, 0)
                        }
                    }
                    else if (response.code() == 400) {
                        Toast.makeText(this@AddreviewActivity, "오류 발생", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<Void?>, t: Throwable) {
                    Toast.makeText(this@AddreviewActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
        }

    }
}

class ProgressDialog(context: Context?) : Dialog(context!!) {
    init {
        // 다이얼 로그 제목을 안보이게 설정
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_spinkit)

        // 라이브러리 로딩 이미지 사용 - CubeGrid
        val progressBar = findViewById<View>(R.id.spin_kit) as ProgressBar
        val cubeGrid: Sprite = CubeGrid()
        progressBar.indeterminateDrawable = cubeGrid
    }
}