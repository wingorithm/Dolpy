package com.example.myapplication.file_part_fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class filesSectionAdapter (private val fileSectionList: ArrayList<fileSectionClass>,
                           private val listener : onItemClickListener
) : RecyclerView.Adapter<filesSectionAdapter.MyViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_file_section,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem : fileSectionClass = fileSectionList[position]
        holder.fileSectionName.text = currentitem.Section_Name
        holder.fileSectionDate.text = currentitem.created_time.toString()
    }

    override fun getItemCount(): Int {
        return fileSectionList.size
    }
    inner class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener{
        val fileSectionName : TextView = itemView.findViewById(R.id.fileSectionName)
        val fileSectionDate : TextView = itemView.findViewById(R.id.fileSectionDate)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = fileSectionList.get(position)
                listener.onItemClick(position, passData)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = fileSectionList.get(position)
                listener.onItemLongClick(position, passData)
            }
            return true
        }
    }
    interface onItemClickListener {
        fun onItemClick(position: Int, passData: fileSectionClass)
        fun onItemLongClick(position: Int, passData: fileSectionClass)
    }
}