package com.example.harumub_front

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ResultFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface

    private lateinit var adapter : LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        // ?
        Log.d("태그", "inflater")

        //adapter = LogAdapter()

        // ?
        Log.d("태그", "어댑터")

        // 현재 로그인하고 있는 사용자 아이디 / 보려는 영화 아이디 (수정 필요) --수민 작성
        var userid = ""
        var movieid = ""
        var result : WatchResult?

        var title:String ? = null
        var poster : String ? = null
        var genres: String ? = null
        var concentration: String ? = null
        var emotion : String ? = null
        var highlight: String ? = null
        var rating : Int = 0
        var comment : String ? = null

        var map = HashMap<String, String>()
        map.put("id", userid)
        map.put("movieId", movieid)

        var call = retrofitInterface.executeWatchResult(map)

        call!!.enqueue(object : Callback<WatchResult?>{
            override fun onResponse( call: Call<WatchResult?>, response: Response<WatchResult?>) {
                if(response.code() == 200){
                    result = response.body()

                    title = result?.title
                    poster = result?.poster
                    genres = result?.genres
                    concentration = result?.concentration
                    emotion = result?.emotion
                    highlight = result?.highlight
                    rating = result?.rating!!
                    comment = result?.comment

                    Toast.makeText(activity, "get watch result successfully", Toast.LENGTH_SHORT).show()
                }
                else if (response.code() == 410){
                    Toast.makeText(activity, "get watch result error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WatchResult?>, t: Throwable) {
                Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
            }
        })

//        val layoutManager = LinearLayoutManager(context)
//        view.findViewById<RecyclerView>(R.id.resultpage_recycler).layoutManager = layoutManager
//        view.findViewById<RecyclerView>(R.id.resultpage_recycler).adapter = adapter


        // 감상 로그 - 이모티콘(이미지뷰)으로 출력하기
//        adapter.items.add(LogData("00:01:23", "happy"))
//        adapter.items.add(LogData("00:04:56", "scary"))
//        adapter.items.add(LogData("00:07:00", "angry"))
//        adapter.items.add(LogData("11:11:11", "anger"))

        return view
    }


    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}