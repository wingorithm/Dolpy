package com.example.myapplication.timeline_part_fragment

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints.LayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.Boolean
import kotlin.Int


class timelineProgressAdapter (private var tlDetailList: ArrayList<timelineProgressClass>,
                               private val listener : timelineProgressAdapter.onItemClickListener,
                               private val COM_projectID : kotlin.String,
                               private val COM_SectionId : kotlin.String
) : RecyclerView.Adapter<timelineProgressAdapter.MyViewHolder>() {

    private lateinit var tlTaskAdapter: timelineTaskAdapter
    private lateinit var tlTaskList: ArrayList<timelineTaskClass>
    private lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_timeline_subsection,
            parent,false)

        context = parent.context
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem: timelineProgressClass = tlDetailList[position]
        //--------------------------------------------------------- Check dulu ya
        if (currentitem.progressTitle == "null") {
            holder.progressTitle.text = "Mau buat timeline? teken tombol biru..."
        } else {
            if (currentitem.colorCode == "1") {
                holder.progressTitle.setBackgroundColor(Color.parseColor("#286A27"))
            } else if (currentitem.colorCode == "2") {
                holder.progressTitle.setBackgroundColor(Color.parseColor("#CC8600"))
            } else if (currentitem.colorCode == "3") {
                holder.progressTitle.setBackgroundColor(Color.parseColor("#6D5145"))
            } else {
                holder.progressTitle.setBackgroundColor(Color.parseColor("#1D1D1D"))
            }
            holder.progressTitle.text = currentitem.progressTitle
            //------------------------------------------------ini untuk nested Recycler
            tlTaskList = arrayListOf()
            var count = 0
            for (d in currentitem.context!!) {
                    val childItem = timelineTaskClass(
                        d,
                        currentitem.contextDecs?.get(count).toString(),
                    )
                    Log.d(TAG, "=" + childItem)
                    tlTaskList.add(childItem)
                    count++
                }
            val adapterMember = timelineTaskAdapter(tlTaskList)
            val linearLayoutManager = LinearLayoutManager(context)
            holder.nested_rv.setLayoutManager(linearLayoutManager)
            holder.nested_rv.setAdapter(adapterMember)

            //--------------------------------------------------------- Cari cara ini biar yang lain close

            holder.checker.setOnClickListener() {
                holder.form.visibility = View.VISIBLE
                holder.checker.layoutParams.height = 700
                //                timelineDetail.hide()
                holder.exception.setOnClickListener() {
                    holder.form.visibility = View.GONE
                    holder.checker.layoutParams.height = LayoutParams.WRAP_CONTENT
                }
                holder.push.setOnClickListener() {
                    val job = GlobalScope.launch(Dispatchers.Default) {
                        timelineDetail.continuePush(
                            holder.text1.text.toString(), holder.text2.text.toString(),
                            currentitem.progressTitle.toString(), COM_SectionId, COM_projectID
                        )
                    }

                    runBlocking{
                        job.join()
                        holder.form.visibility = View.GONE
                        holder.checker.layoutParams.height = LayoutParams.WRAP_CONTENT
                        notifyDataSetChanged()
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return tlDetailList.size
    }

    inner class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnLongClickListener{
        val progressTitle : AppCompatTextView = itemView.findViewById(R.id.SubSectionjudul)
        val checker : ConstraintLayout = itemView.findViewById(R.id.addTask)
        val form : ConstraintLayout = itemView.findViewById(R.id.task_form)
        val exception : ConstraintLayout = itemView.findViewById(R.id.timelineLayout)
        val push :ImageButton = itemView.findViewById(R.id.okAddTask)

        val text1 : EditText = itemView.findViewById(R.id.taskSubSectionform)
        val text2 : EditText = itemView.findViewById(R.id.taskDecsform)

        val nested_rv : RecyclerView = itemView.findViewById(R.id.pointsRecycler)
        init {
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = tlDetailList.get(position)
                listener.onItemLongClick(position, passData)
            }
            return true
        }

    }

    interface onItemClickListener {
        fun onItemLongClick(position: Int, passData: timelineProgressClass)
    }
}