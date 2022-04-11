package com.example.harumub_front

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RecommendAdapter2: RecyclerView.Adapter<RecommendAdapter2.ViewHolder>() {

    // 데이터
    private var movie_images = intArrayOf(R.drawable.spider, R.drawable.gucci, R.drawable.about, R.drawable.ic_launcher_foreground, R.drawable.spider, R.drawable.gucci, R.drawable.about)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendAdapter2.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.movie_collection_layout, parent, false) // RecyclerView에 들어갈 아이템의 레이아웃 설정
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecommendAdapter2.ViewHolder, position: Int) { // 데이터 설정
        holder.movieImage.setImageResource(movie_images[position])

        // 해당 아이템 클릭시 유사 사용자 추천 리스트로 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, UserMovieListActivity::class.java)
            // intent.putExtra("user_id", id)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return movie_images.size // 영화 개수
    }

    inner class ViewHolder(movieCollectionView: View): RecyclerView.ViewHolder(movieCollectionView) {
        var movieImage: ImageButton

        init {
            movieImage = movieCollectionView.findViewById(R.id.movie_collection_image) // 영화 이미지 버튼

            movieCollectionView.setOnClickListener { // 영화 이미지 버튼 클릭 시
//                val position: Int = adapterPosition
//                Toast.makeText(movieCollectionView.context, "영화 선택", Toast.LENGTH_LONG).show()
                Log.d("영화 : ", "선택")
            }
        }
    }
}