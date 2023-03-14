package com.example.myapplication.home_project_activity

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.home_part_fragment.peoples
import com.squareup.picasso.Picasso
import kotlin.contracts.Returns

class peoplesCardAdapter(
    private val peopleList: ArrayList<peoplesCardClass>,
    private val listenerPeople: onItemClickListener
    ) : RecyclerView.Adapter<peoplesCardAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.linear_peoplesview,
                parent,false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val currentitem : peoplesCardClass = peopleList[position]

            holder.mName.text = (currentitem.fName + " " + currentitem.lName)
            if (currentitem.profilePic != "null" && currentitem.profilePic!!.isNotEmpty()){
                Picasso.get().load(currentitem.profilePic).into(holder.pic)
            }
        }

        override fun getItemCount(): Int {
            return peopleList.size
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener{
            val mName : TextView = itemView.findViewById(R.id.memberName)
            val pic : ImageView = itemView.findViewById(R.id.memberProfile)

            init {
                itemView.setOnLongClickListener(this)
            }

            override fun onLongClick(p0: View?): Boolean {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    val passPeople = peopleList.get(position)
                    listenerPeople.onItemLongClick(position, passPeople)
                }
                return true
            }
        }

    interface onItemClickListener {
        fun onItemLongClick(position: Int, passPeople: peoplesCardClass)
    }
}