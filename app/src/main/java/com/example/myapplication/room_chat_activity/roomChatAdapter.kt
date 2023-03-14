package com.example.myapplication.room_chat_activity

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class roomChatAdapter(
    private val chatList: ArrayList<roomChatClass>,
    private val totalComment : ArrayList<Int>,
    private val bundles : ArrayList<chatBubbleClass>,
    private val listener : onItemClickListener,
) :
    RecyclerView.Adapter<roomChatAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_topic_chat,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem : roomChatClass = chatList[position]
        val bundlesItem : chatBubbleClass = bundles[position]
        val totalCommentItem : Int = totalComment[position]

        holder.roomChatName.text = currentitem.topic_Name
        holder.roomChatdate.text = currentitem.created_time.toString()
        holder.roomChatcomments.text = "no comments yet~"

        Log.d(TAG, "|||-> " + bundlesItem.chat_content)
        if (bundlesItem.chat_content == "null"){
            holder.includeChat.visibility = View.GONE
        }else {
            holder.roomChatcomments.text = (totalCommentItem.toString() + " comments")
            holder.userName.text = bundlesItem.chat_user
            holder.userChat.text = bundlesItem.chat_content
            holder.chatDate.text = bundlesItem.chat_time
            Picasso.get().load(bundlesItem.userPP).into(holder.userPP)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener{
        val roomChatName : TextView = itemView.findViewById(R.id.roomChatName)
        val roomChatdate : TextView = itemView.findViewById(R.id.roomchatDate)
        val roomChatcomments : TextView = itemView.findViewById(R.id.totalComment)
        val userName : TextView = itemView.findViewById(R.id.textView)
        val chatDate : TextView = itemView.findViewById(R.id.chatdate)
        val userChat : TextView = itemView.findViewById(R.id.textView2)
        val includeChat : ConstraintLayout = itemView.findViewById(R.id.includeChat)
        val userPP : ShapeableImageView = itemView.findViewById(R.id.imageView)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = chatList.get(position)
                listener.onItemClick(position, passData)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val passData = chatList.get(position)
                listener.onItemLongClick(position, passData)
            }
            return true
        }
    }

    interface onItemClickListener{
        fun onItemClick(position: Int, passData: roomChatClass)
        fun onItemLongClick(position: Int, passData: roomChatClass)
    }

}