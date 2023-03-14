package com.example.myapplication.timeline_part_fragment

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.home_part_fragment.timeline

class timelineSectionAdapter(private val timelineSectionList: ArrayList<timelineSectionClass>,
                             private val listener : timelineSectionAdapter.onItemClickListener,
                             private val includeBundle : ArrayList<String>,
                             private val includeTask : ArrayList<String>,
                             private val includeDesc : ArrayList<String>
) : RecyclerView.Adapter<timelineSectionAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_timeline_section,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (includeTask.size > position){
            val currentitem : timelineSectionClass = timelineSectionList[position]
            val includeBundleitem : String = includeBundle[position]
            if (includeTask.size != 0){
                val includeDescitem : String = includeDesc[position]
                val includeTaskitem : String = includeTask[position]

                //-------------untuk ngisi include
                if (includeBundleitem != "null"){
                    Log.d(TAG,"==?"+ includeBundleitem + " " +includeTaskitem + " " +includeDescitem)
                    holder.inlcudeBar.text = includeBundleitem
                    holder.inlcudeTask.text = includeTaskitem
                    holder.inlcudeDesc.text = includeDescitem
                }else{
                    holder.inlcudeBar.text = "Ayok mulai buat..."
                    holder.inlcudeTask.text = "#antimagermagerclub"
                    holder.inlcudeDesc.text = "Semangat demi project SE!!!"
                }
            }

            holder.judulSection.text = currentitem.judulSection
            holder.statusSection.text = currentitem.workStatusSection
            if (currentitem.statusSection == "green"){
                holder.dueDate.setTextColor(Color.parseColor("#4FD34D"))
            }else if (currentitem.statusSection == "orange"){
                holder.dueDate.setTextColor(Color.parseColor("#CC8600"))
            }else {
                holder.dueDate.setTextColor(Color.parseColor("#B11E1E"))
            }
            holder.dueDate.text = currentitem.dueDate
        }
    }

    override fun getItemCount(): Int {
        return timelineSectionList.size
    }

    inner class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener, View.OnClickListener{
        val judulSection : TextView = itemView.findViewById(R.id.judulSection)
        val statusSection : TextView = itemView.findViewById(R.id.statusTL)
        val dueDate : TextView = itemView.findViewById(R.id.endDate)
        val inlcudeBar : AppCompatTextView = itemView.findViewById(R.id.includeSSjudul)
        val inlcudeTask : TextView = itemView.findViewById(R.id.includeSSTask)
        val inlcudeDesc : TextView = itemView.findViewById(R.id.includeSSDecs)


        init {
            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = timelineSectionList.get(position)
                listener.onLongItemClick(position, passData)
            }
            return true
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = timelineSectionList.get(position)
                listener.onItemClick(position, passData)
            }
        }
    }

    interface onItemClickListener {
        fun onLongItemClick(position: Int, passData: timelineSectionClass)
        fun onItemClick(position: Int, passData: timelineSectionClass)
    }
}