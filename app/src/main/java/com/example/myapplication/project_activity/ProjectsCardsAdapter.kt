package com.example.myapplication.project_activity

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*


class ProjectsCardsAdapter(
    private val projectList: ArrayList<ProjectsCardClass>,
    private val listener : onItemClickListener
) :
    RecyclerView.Adapter<ProjectsCardsAdapter.MyViewHolder>(){

    private lateinit var context : android.content.Context
    private lateinit var pID: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.linear_listview,
            parent,false)
        this.context = parent.context
        return MyViewHolder(itemView)

    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem : ProjectsCardClass = projectList[position]

        holder.projectCard.text = currentitem.pName
        holder.projectMembers.text = (currentitem.userID!!.size.toString() + " members")
        holder.projectDeadline.text = currentitem.pdueDate
       holder.flag.setOnClickListener { v: View ->
           pID = currentitem.projectUid.toString()
           showMenu(
               v,
               R.menu.project_setting,
               currentitem.projectUid.toString(),
               currentitem.pName.toString(),
               holder
           )
       }
    }

    @SuppressLint("ResourceAsColor")
    fun showMenu(v: View, menu: Int, pID: String, pName: String, holder: MyViewHolder){
        val popup = PopupMenu(context, v)
        popup.menuInflater.inflate(menu, popup.menu)
        popup.show()

        //Set on click listener for the menu
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            if (item.itemId == R.id.option_1){
                var flag2 :Int = 0
                val job = GlobalScope.launch(Dispatchers.Default) {
                    flag2 = ProjectsHomeActivity.leaveProject(pID)
                    Log.d(TAG, "=> " + flag2)
                }

                runBlocking {
                    job.join()
                    if (flag2 == 1){
                        Log.d(TAG, "=> " + flag2)
                        confirmationBox(flag2, pName)
                    }else {

                        Log.d(TAG, "=> " + flag2)
                        confirmationBox(flag2, pName)
                    }
                }
            }

            else if (item.itemId == R.id.option_2){
                var flag :Int = 2
                val job = GlobalScope.launch(Dispatchers.Default) {
                    flag = ProjectsHomeActivity.markAsDone(pID)
                    flag  = 2
                    Log.d(TAG, "job : " + flag)
                }

                runBlocking {
                    job.join()
                    if (flag == 1){
                        Toast.makeText(context, "sorry kamu bukan bossnya" , Toast.LENGTH_SHORT).show()
                    }else if(flag == 2){
                        Toast.makeText(context, "Horee " + pName + " udah KELARR!" , Toast.LENGTH_SHORT).show()
                        holder.itemin.setBackgroundResource(R.drawable.projectcard_done)
                        holder.projectDeadline.text = "KELAR"
                    }
                }
            }
            true
        })
    }

    fun confirmationBox(code: Int, pName: String) {
        val formProject = AlertDialog.Builder(context)
        formProject.setTitle("Confirmation Status")
//        val inflater = LayoutInflater.from(context).inflate()
        val dialogLayout = LayoutInflater.from(context).inflate(R.layout.activity_formconfirmation, null)
        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE

        if(code == 1){
            dialogLayout.findViewById<TextView>(R.id.acc_action).text = ("You are the Leader, leave means *DELETE* the project delete "+ pName +"?")
            formProject.setView(dialogLayout)
            formProject.setPositiveButton("Delete") { DialogInterface, i ->
                ProjectsHomeActivity.removeuser(pID)
                Toast.makeText(context, " Delete Succeed ", Toast.LENGTH_SHORT).show()
            }
            formProject.setNegativeButton("Cancel") { DialogInterface, i ->
                Toast.makeText(context, " ok bos cancel ", Toast.LENGTH_SHORT).show()
            }
            formProject.show()
        }else{
            dialogLayout.findViewById<TextView>(R.id.acc_action).text = "are you sure want to leave " + pName +"?"
            formProject.setView(dialogLayout)
            formProject.setPositiveButton("Delete") { DialogInterface, i ->
                ProjectsHomeActivity.removeuser(pID)
                Toast.makeText(context, " Delete Succeed ", Toast.LENGTH_SHORT).show()
            }
            formProject.setNegativeButton("Cancel") { DialogInterface, i ->
                Toast.makeText(context, " ok bos cancel ", Toast.LENGTH_SHORT).show()
            }
            formProject.show()
        }

    }

    override fun getItemCount(): Int {
        return projectList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
    View.OnClickListener{
        val projectCard : TextView = itemView.findViewById(R.id.tvProjectName)
        val projectMembers : TextView = itemView.findViewById(R.id.tvProjectMembers)
        val projectDeadline : TextView = itemView.findViewById(R.id.tvProjectDate)
        val flag : ImageButton = itemView.findViewById(R.id.ProjectSetting)
        val menu : LinearLayout = itemView.findViewById(R.id.menuOptionDown)

        val opt1 : AppCompatButton = itemView.findViewById(R.id.option1)
        val opt2 : AppCompatButton = itemView.findViewById(R.id.option2)

        val itemin : ConstraintLayout = itemView.findViewById(R.id.group1)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                val passData = projectList.get(position)
                listener.onItemClick(position, passData)
            }
        }
    }

    interface onItemClickListener{
        fun onItemClick(position: Int, passData: ProjectsCardClass)
    }

}



