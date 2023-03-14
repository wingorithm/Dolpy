package com.example.myapplication.room_chat_activity

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class chatBubbleAdapter (
    private val chatList: ArrayList<chatBubbleClass>,
    private val listener : onItemClickListener
) :
    RecyclerView.Adapter<chatBubbleAdapter.MyViewHolder>() {

    var count = chatList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_chat_bubble,
            parent,false)
        return MyViewHolder(itemView)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem : chatBubbleClass = chatList[position]
        if (currentitem.chat_time != "null"){
            holder.chatUserName.text = currentitem.chat_user
            holder.chatContent.text = currentitem.chat_content
            holder.chatDate.text = currentitem.chat_time.toString()
            if (currentitem.userPP != "null"){
                Picasso.get().load(currentitem.userPP).into(holder.userPP)
            }
        }
        else{
            holder.emptyBar.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener{
        val chatUserName : TextView = itemView.findViewById(R.id.userName)
        val chatDate : TextView = itemView.findViewById(R.id.chatDate)
        val chatContent : TextView = itemView.findViewById(R.id.chatContent)
        val userPP : ShapeableImageView = itemView.findViewById(R.id.userProfile)
        val emptyBar : ConstraintLayout = itemView.findViewById(R.id.chat3)

        init {
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = chatList.get(position)
                listener.onItemLongClick(position, passData)
            }
            return true
        }
    }

    interface onItemClickListener{
        fun onItemLongClick(position: Int, passData: chatBubbleClass)
    }

}