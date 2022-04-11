package com.example.harumub_front

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_search.view.*
import kotlinx.android.synthetic.main.activity_search.view.imageView
import kotlinx.android.synthetic.main.recyclerview_row.view.*

/**
 * Created by 규열 on 2018-02-13.
 */
class SearchAdapter(var context: Context, var unFilteredlist: ArrayList<String>) :
    RecyclerView.Adapter<SearchAdapter.MyViewHolder>(), Filterable {

    var filteredList: ArrayList<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_row, parent, false) // RecyclerView에 들어갈 아이템의 레이아웃 설정
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = filteredList[position]
    }

    override fun getItemCount(): Int {
        return filteredList.size // 검색된 영화 개수
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView

        init {
            textView = itemView.findViewById<View>(R.id.textview) as TextView
        }
        fun setItem(item: String) {
            if(item.equals("About Times")){
                itemView.imageView.setImageResource(R.drawable.about)
                itemView.textview.text = "About Times"
            }
            else if(item.equals("Gucci")) {
                itemView.imageView.setImageResource(R.drawable.gucci)
                itemView.textview.text ="Gucci"
            }
            else if(item.equals("Spider Man3")) {
                itemView.imageView.setImageResource(R.drawable.spider)
                itemView.textview.text ="Spider Man3"
            }

            itemView.setOnClickListener { // 영화 클릭 시 토스트 메세지
                Toast.makeText(itemView.context,itemView.textview.text,Toast.LENGTH_LONG).show()
                // movie id 전달!!
            }
        }
    }

    override fun getFilter(): Filter { // 검색
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val charString = constraint.toString()
                filteredList = if (charString.isEmpty()) {
                    unFilteredlist
                } else {
                    val filteringList = ArrayList<String>()
                    for (name in unFilteredlist) {
                        if (name.toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(name)
                        }
                    }
                    filteringList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) { // 검색 결과
                filteredList = results.values as ArrayList<String>
                notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = filteredList[position]
        holder.setItem(item)
    }

    init {
        filteredList = unFilteredlist
    }
}