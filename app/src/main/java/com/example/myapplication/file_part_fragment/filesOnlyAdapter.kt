package com.example.myapplication.file_part_fragment

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.home_part_fragment.files
import com.squareup.picasso.Picasso

class filesOnlyAdapter (private val fileOnlyList: ArrayList<fileOnlyClass>,
                        private val listener : onItemClickListener
) : RecyclerView.Adapter<filesOnlyAdapter.MyViewHolder>(){
    lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_file_only,
            parent,false)
        context = parent.context
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem : fileOnlyClass =fileOnlyList[position]
        val strs = currentitem.fileType!!.split("/").toTypedArray()
        if (strs[1] == "link"){
            holder.fileLink.visibility = View.VISIBLE
            holder.fileLink.text = currentitem.fileURL.toString()
        }else if (strs[1] == "jpeg" || strs[1] == "jpg" || strs[1] == "png"){
            Picasso.get().load(currentitem.fileURL).into(holder.fileImage)
            holder.fileImage.visibility = View.VISIBLE
        }else {
            holder.fileLink.visibility = View.VISIBLE
            holder.fileLink.text = currentitem.fileURL.toString()
        }

    }

    override fun getItemCount(): Int {
        return fileOnlyList.size
    }
    fun restart(){
        this.fileOnlyList.clear()
    }
    inner class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener, View.OnClickListener{
        val fileImage : ImageView = itemView.findViewById(R.id.filesURL)
        val fileLink : TextView = itemView.findViewById(R.id.filesURL2)

        init {
            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = fileOnlyList.get(position)
                listener.onItemLongClick(position, passData)
            }
            return true
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = fileOnlyList.get(position)
                listener.onItemClick(position, passData)
            }
        }
    }
    interface onItemClickListener {
        fun onItemLongClick(position: Int, passData: fileOnlyClass)
        fun onItemClick(position: Int, passData: fileOnlyClass) {

        }
    }
}