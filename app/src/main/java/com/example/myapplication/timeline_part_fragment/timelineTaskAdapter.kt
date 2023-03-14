package com.example.myapplication.timeline_part_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class timelineTaskAdapter (private var tlTaskList: ArrayList<timelineTaskClass>) : RecyclerView.Adapter<timelineTaskAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_timeline_point,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem : timelineTaskClass = tlTaskList[position]
        holder.taskTitle.text = currentitem.context
        holder.taskDesc.text = currentitem.contextDecs
    }

    override fun getItemCount(): Int {
        return tlTaskList.size
    }

    inner class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val taskTitle : TextView = itemView.findViewById(R.id.taskSubSection)
        val taskDesc : TextView = itemView.findViewById(R.id.taskDescription)

    }


}