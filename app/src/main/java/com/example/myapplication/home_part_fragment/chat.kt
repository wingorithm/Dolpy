package com.example.myapplication.home_part_fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.databinding.FragmentChatBinding
import com.example.myapplication.room_chat_activity.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class chat : Fragment(), roomChatAdapter.onItemClickListener {
    private lateinit var binding: FragmentChatBinding
    private lateinit var chatRecycler : RecyclerView
    private lateinit var chatArrayList : ArrayList<roomChatClass>
    private lateinit var chatAdapter: roomChatAdapter
    private var db = Firebase.firestore

    private lateinit var projectID : String
    private lateinit var topicId : String
    private lateinit var subsList : ArrayList<chatBubbleClass>
    private lateinit var count : ArrayList<Int>
    var flag : Int = 0
    var tempCount = 0
    var subs = chatBubbleClass(
        "null",
        "null",
        "null",
        "null",
        "null"
    )
    lateinit var temp: chatBubbleClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         binding = FragmentChatBinding.inflate(layoutInflater, container, false)

        //inflating recycle view -------------------------------------------------------------------
        chatRecycler = binding.recyclerChat
        chatRecycler.layoutManager = LinearLayoutManager(context)
        chatRecycler.setHasFixedSize(true)

        chatArrayList = arrayListOf()
        count = arrayListOf()
        subsList = arrayListOf()
        chatAdapter = roomChatAdapter(chatArrayList,  count, subsList, this)
        chatRecycler.adapter = chatAdapter

        val data = arguments
        this.projectID = data!!.get("projectID").toString()
        getroomChat()

        binding.roomChatadd.setOnClickListener(){
            addTopicRoom()
        }
        return binding.root
    }

    // tombol tambah dan form
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTopicRoom() {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
//        formProject.setTitle("Topic Title")

        val dialogLayout = inflater.inflate(R.layout.activity_form_oneedit, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editMemberemail).text
        dialogLayout.findViewById<EditText>(R.id.editMemberemail).hint = "chat section name..."
        dialogLayout.findViewById<TextView>(R.id.tvProjectTitle).text = "Section Title"
        dialogLayout.findViewById<TextView>(R.id.nameDialog).text = "create new chat section"


        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Create New Topic") {
                DialogInterface, i -> pushTopicData(editText.toString())
        }
        formProject.setNegativeButton("Cancel") {
                DialogInterface, i -> Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pushTopicData(topic_Name : String){
        var time = getdate().toString()

        val roomChat = hashMapOf(
            "topic_Name" to topic_Name,
            "created_time" to time
        )
        val dbref = db.collection("projects").document(projectID).collection("RoomChat")
        dbref.add(roomChat).addOnSuccessListener { documentReference ->
            Log.d(TAG, "roomChat added ${documentReference.id}")
            dbref.document(documentReference.id).collection("chatCache").add(subs)
            Log.d(TAG, "--> " + documentReference.id)
            getroomChat()

        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding roomChat", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getdate(): String? {
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter)
        return formatted
    }

//==================================================================================================
    private fun getroomChat() {
    db = FirebaseFirestore.getInstance()
    val docRef2 = db.collection("projects").document(projectID).collection("RoomChat")
    docRef2.orderBy("created_time", Query.Direction.DESCENDING).addSnapshotListener(){ snapshot, e ->
        for (dc in snapshot!!.documentChanges) {
            when (dc.type) {
                DocumentChange.Type.ADDED -> {
                    chatArrayList.clear()
                    chatRecycler?.adapter?.notifyDataSetChanged()
                }
                DocumentChange.Type.MODIFIED -> {
                    chatArrayList.clear()
                    chatRecycler?.adapter?.notifyDataSetChanged()
                }
                DocumentChange.Type.REMOVED ->{
                    chatArrayList.clear()
                    chatRecycler?.adapter?.notifyDataSetChanged()
                }
            }
        }
        //---------------- Document Project
        if (snapshot != null) {
            for (d in snapshot) {
                val roomChat: roomChatClass? = d.toObject(roomChatClass::class.java)
                if (roomChat != null) {
                    //---------------- Document Project-> setiap chatcache
                    docRef2.document(d.id).collection("chatCache").addSnapshotListener { snapshot, e ->
                        if (snapshot != null) {
                            flag = 0
                            var tempid = "sadsadsa"
                            for (A in snapshot) {
                                temp = A.toObject(chatBubbleClass::class.java)
                                subs.chat_user = temp.chat_user
                                subs.chat_content = temp.chat_content
                                subs.chat_time = temp.chat_time
                                if (temp.chat_content == "null"){
                                }
                                tempCount++
                                flag = 1
                            }
                            count.add(tempCount)
                            if (tempCount == 0){
                                subs.chat_user = "null"
                                subs.chat_content = "null"
                                subs.chat_time = "null"
                                subsList.add(subs)
                            }else{
                                subsList.add(temp)
                            }
                            chatArrayList.add(roomChat)
                            Log.d(TAG, "{ " + tempCount + " - " + subs.chat_content + " - " + subsList + "{ ")
                            if (flag == 1){
                                chatRecycler.adapter = roomChatAdapter(chatArrayList, count, subsList, this)
                                Log.d(TAG, "PUSH -> " + count.size.toString() + " " + subsList.size.toString() + " " + chatArrayList.size.toString())
                                if (chatArrayList.size == 0){
                                    binding.startchat.visibility = View.VISIBLE
                                }else{
                                    binding.startchat.visibility = View.GONE
                                }
                            }
                            tempCount = 0
                        }
                    }
                    Log.d(TAG, "WOI WOIN " + d.id)
                }
            }
        }
        else {
            Log.d(TAG, "Current data: null")
        }
        }
    }
//==================================================================================================
    private fun findRoomRemove(topicName: String?) {
        var flag = false
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID)
                        .collection("RoomChat")
        docRef2.whereEqualTo("topic_Name", topicName).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    docRef2.document(d.id).delete()
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }

    override fun onItemClick(position: Int, passData: roomChatClass) {
        val intent = Intent(activity, roomChatActivity::class.java)

        //Cari topic ID dulu
        db = FirebaseFirestore.getInstance()
        db.collection("projects").document(projectID)
            .collection("RoomChat").whereEqualTo("topic_Name", passData.topic_Name.toString())
            .addSnapshotListener(){snapshot, e ->
                if (snapshot != null) {
                    for (d in snapshot) {
                        val roomChat : roomChatClass? = d.toObject(roomChatClass::class.java)
                        this.topicId = d.id
                    }
                    intent.putExtra("projectID", this.projectID)
                    intent.putExtra("currTopic", this.topicId)
                    intent.putExtra("NamaTopic", passData.topic_Name)
                    intent.putExtra("DateTopic", passData.created_time)
                    Log.d(TAG, "TOPIC YANG DIPOER ${this.topicId}")
                    startActivity(intent)
                } else {
                    Log.d(TAG, "Current data: null" + e)
                }
            }
    }

    override fun onItemLongClick(position: Int, passData: roomChatClass) {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        formProject.setTitle("Confirmation Status")

        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)
        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "Delete Chat Room " + passData.topic_Name

        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Delete") { DialogInterface, i ->
            findRoomRemove(passData.topic_Name)
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }
}

