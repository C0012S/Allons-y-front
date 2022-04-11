package com.example.harumub_front

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_watch_alone.*
import kotlinx.android.synthetic.main.fragment_addreview.*
import kotlinx.android.synthetic.main.fragment_result.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.ArrayList
import java.util.HashMap

class ResultActivity : AppCompatActivity() {
    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface: RetrofitInteface

    // 현재 로그인하고 있는 사용자 아이디, 선택한 영화 아이디, 별점 평가, 한줄평
    private val id = intent.getStringExtra("user_id")
    private val movie_title = intent.getStringExtra("movie_title")
    private val ratings = intent.getStringExtra("user_rating")
    private val comments = intent.getStringExtra("user_comment")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_result)

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        // 리뷰 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id") && intent.hasExtra("movie_title")
            && intent.hasExtra("user_rating") && intent.hasExtra("user_comment")
        ) {
            Log.d(
                "WatchAloneActivity",
                "리뷰에서 받아온 id : $id , movie title : $movie_title \n rating : $ratings , comment : $comments "
            )
        } else {
            Log.e("WatchAloneActivity", "가져온 데이터 없음")
        }

        var myTitle = findViewById<TextView>(R.id.title)
        var myPoster = findViewById<ImageView>(R.id.poster)
        var myGenres = findViewById<TextView>(R.id.genres)
        var myConPer = findViewById<TextView>(R.id.concentration)
        var myHlTime = findViewById<TextView>(R.id.highlight)

        var myRating = findViewById<RatingBar>(R.id.user_rating)
        var myComment = findViewById<TextView>(R.id.user_comment)

        // var myEmotion = arrayListOf<Any>(3) // 배열에 setImageResource() 적용 안 됨
        var emotion1 = findViewById<ImageView>(R.id.emotion1)
        var emotion2 = findViewById<ImageView>(R.id.emotion2)
        var emotion3 = findViewById<ImageView>(R.id.emotion3)

        var myChart = findViewById<LineChart>(R.id.chart)
        var myHighlight = findViewById<ImageView>(R.id.img_highlight)

        var btnMain = findViewById<Button>(R.id.back2main)
        var btnList = findViewById<Button>(R.id.back2list)


        var map = HashMap<String, String>()
        map.put("id", id!!)
        map.put("movie_title", movie_title!!)

        val call = retrofitInterface.executeWatchResult(map)
        call!!.enqueue(object : Callback<WatchResult?> {
            override fun onResponse(call: Call<WatchResult?>, response: Response<WatchResult?>) {
                if (response.code() == 200) {
                    Toast.makeText(this@ResultActivity, "결과 출력 성공", Toast.LENGTH_SHORT).show()

                    val result =
                        response.body() // 받아온 객체 배열 : title, poster, genres, emotion_array, highlight_array

                    // 감상했던 영화 정보 불러오기
                    myTitle.setText(result?.title)
                    myGenres.setText(result?.genres)
                    myConPer.setText(result?.concentration)
                    // myHlTime.setText(result?.highlight) // highlight가 배열로 옴....

                    // 영화 포스터 출력 - 웹에서 url로 가져오기
                    var myUrl = result?.poster
                    var result_url = "https://image.tmdb.org/t/p/w500"
                    var image_task: URLtoBitmapTask = URLtoBitmapTask().apply {
                        url = URL(result_url + myUrl)
                    }
                    var bitmap: Bitmap = image_task.execute().get()
                    myPoster.setImageBitmap(bitmap)

                    // 입력했던 별점, 한줄평 값으로 초기화
                    myRating.rating = result?.rating!!
                    myComment.setText(result.comment)


                    // 감정 이모티콘 출력 - 서버에서 받아온 배열 객체 값 비교하여 감정 이미지 출력
                    val arr = intArrayOf(
                        result.emotion_array.HAPPY, result.emotion_array.SAD,
                        result.emotion_array.ANGRY, result.emotion_array.CONFUSED,
                        result.emotion_array.DISGUSTED, result.emotion_array.SURPRISED,
                        result.emotion_array.FEAR
                    )

                    val emoji = intArrayOf(
                        R.drawable.happy, R.drawable.sad, R.drawable.angry,
                        R.drawable.confused, R.drawable.disgusted, R.drawable.surprised,
                        R.drawable.fear, R.drawable.calm
                    )

                    var arrSorted = arr.sortedDescending() // 내림차순 정렬
                    for (i in 0..2) {
                        if (arrSorted[i] != 0) {
                            for (j in 0..6) {
                                if (i == 0) {
                                    if (arrSorted[i] == arr[j]) emotion1.setImageResource(emoji[j])
                                    else emotion1.setImageResource(emoji[7]) // calm 이미지
                                } else if (i == 1) {
                                    if (arrSorted[i] == arr[j]) emotion2.setImageResource(emoji[j])
                                    else emotion2.setImageResource(emoji[7]) // calm 이미지
                                } else if (i == 2) {
                                    if (arrSorted[i] == arr[j]) emotion3.setImageResource(emoji[j])
                                    else emotion3.setImageResource(emoji[7]) // calm 이미지
                                }
                            }
                        }
                    }

                    // 감상 결과 - 감정 그래프 출력
                    val chart1 = ArrayList<Entry>() // 감정 차트 배열 > 새 데이터 좌표값 추가 가능
                    val chart2 = ArrayList<Entry>()
                    val chart3 = ArrayList<Entry>()

                    // 서버에서 받아온 감정 배열 값 넣기 - 어떤 방식인지 더 알아봐야 함 - 수정 필요
                    val arrHT = result.highlight_array.time.toFloat()
                    val arrHE = result.highlight_array.emotion
                    val arrHD = result.highlight_array.emotion_diff
                    // for문으로 배열에 넣기 - 수정 필요
                    chart1.add(Entry(1F, 2F)) // (x, y) 값 Float형으로 입력! (x: 시간, y: 감정값)
                    chart2.add(Entry(1F, 6F))
                    chart3.add(Entry(2F, 8F))

                    // 좌표값들이 담긴 엔트리 차트 배열에 대한 데이터셋 생성
                    // 서버에서 받아온 배열에 따른 3가지 감정 그래프
                    val lineDataSet1 = LineDataSet(chart1, arrHE) // ex) "Happy"
                    val lineDataSet2 = LineDataSet(chart2, arrHE) // ex) "Sad"
                    val lineDataSet3 = LineDataSet(chart3, arrHE) // ex) "Surprised"

                    // 차트데이터 생성
                    val chartData = LineData()
                    chartData.addDataSet(lineDataSet1) // 데이터셋을 차트데이터 안에 넣기
                    chartData.addDataSet(lineDataSet2)
                    chartData.addDataSet(lineDataSet3)
                    myChart.data = chartData // 선 그래프에 차트데이터 표시하기
                    myChart.invalidate() // 차트 갱신

                    // 감상 결과 - 서버로부터 받은 하이라이트 이미지뷰 출력 - 수정 필요
                    myHighlight.setImageResource(R.drawable.highlight)

                    // 메인으로 돌아가는 버튼
                    btnMain.setOnClickListener {
                        var intent = Intent(
                            applicationContext,
                            MainActivity2::class.java
                        ) // 두번째 인자에 이동할 액티비티
                        intent.putExtra("user_id", id)
                        startActivityForResult(intent, 0)
                    }
                    // 리스트 목록으로 이동하는 버튼
                    btnList.setOnClickListener {
                        var intent = Intent(
                            applicationContext,
                            WatchListActivity::class.java
                        ) // 두번째 인자에 이동할 액티비티
                        intent.putExtra("user_id", id)
                        startActivityForResult(intent, 0)
                    }

                } else if (response.code() == 400) {
                    Toast.makeText(this@ResultActivity, "오류 발생", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<WatchResult?>, t: Throwable) {
                Toast.makeText(
                    this@ResultActivity, t.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

    }
}
