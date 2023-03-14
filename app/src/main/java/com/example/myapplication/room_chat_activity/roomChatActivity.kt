package com.example.myapplication.room_chat_activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.databinding.ActivityRoomChatBinding
import com.example.myapplication.home_project_activity.peoplesCardClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.rpc.context.AttributeContext.Auth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

class roomChatActivity : AppCompatActivity(), chatBubbleAdapter.onItemClickListener {
    private lateinit var binding: com.example.myapplication.databinding.ActivityRoomChatBinding
    private lateinit var user: FirebaseAuth
    private var db = Firebase.firestore
    private lateinit var bubbleChatRecycler : RecyclerView
    private lateinit var bubbleChatArrayList : ArrayList<chatBubbleClass>
    private lateinit var bubbleChatAdapter: chatBubbleAdapter

    private lateinit var currTopicID : String
    private lateinit var projectID : String
    private lateinit var currName : String
    private lateinit var DateTopic : String
    private lateinit var NamaTopic : String
    private lateinit var userPP : String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()!!.setCustomView(R.layout.activity_actionbar);

        //inflating recycle view -------------------------------------------------------------------
        bubbleChatRecycler = binding.chatRecycler
        bubbleChatRecycler.layoutManager = LinearLayoutManager(this)
        bubbleChatRecycler.setHasFixedSize(true)

        bubbleChatArrayList = arrayListOf()
        bubbleChatAdapter = chatBubbleAdapter(bubbleChatArrayList, this)
        bubbleChatRecycler.adapter = bubbleChatAdapter

        this.currTopicID = intent.getStringExtra("currTopic").toString()
        this.projectID = intent.getStringExtra("projectID").toString()
        this.DateTopic = intent.getStringExtra("DateTopic").toString()
        this.NamaTopic = intent.getStringExtra("NamaTopic").toString()

        binding.roomChatName.text = "DISCUSSION TOPIC: " + NamaTopic
        binding.roomchatDate.text = DateTopic
        getChatData()

        user = FirebaseAuth.getInstance()
        getUserData(user.currentUser!!.uid)

        binding.sendButton.setOnClickListener(){
            var temp = binding.messageInput.text
            pushChat(currName, getdate()!!, temp.toString())
            binding.messageInput.text.clear()
        }
    }
//==================================================================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getdate(): String? {
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter)
        return formatted
    }

    private fun getUserData(uid : String){
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("users").whereEqualTo("userUID", uid)
        docRef2.addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    val membersss: peoplesCardClass? = d.toObject(peoplesCardClass::class.java)
                    if (membersss != null) {
                        currName = (membersss.fName + membersss.lName)
                        userPP = membersss.profilePic.toString()
                        Log.d(TAG, currName)
                        break
                    }
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }
//==================================================================================================
    private fun pushChat(userName : String, time : String, content : String) {
        val chat = hashMapOf(
            "chat_topic" to this.currTopicID,
            "chat_time" to time,
            "chat_content" to content,
            "chat_user" to userName,
            "userPP" to userPP
        )
        val docRef = db.collection("projects").document(projectID)
                    .collection("RoomChat").document(currTopicID)
                    .collection("chatCache")
                    .add(chat)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "chat baru ${documentReference.id}")
                    }.addOnFailureListener { e ->
                        Log.w(TAG, "Error adding roomChat", e)
                    }
    }

    private fun getChatData() {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID)
            .collection("RoomChat").document(currTopicID)
            .collection("chatCache")

        docRef2.orderBy("chat_time", Query.Direction.ASCENDING).addSnapshotListener(){snapshot, e ->
                for (dc in snapshot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            bubbleChatArrayList.clear()
                            bubbleChatRecycler?.adapter?.notifyDataSetChanged()
                        }
                        DocumentChange.Type.MODIFIED -> {
                            bubbleChatArrayList.clear()
                            bubbleChatRecycler?.adapter?.notifyDataSetChanged()
                        }
                        DocumentChange.Type.REMOVED ->{
                            bubbleChatArrayList.clear()
                            bubbleChatRecycler?.adapter?.notifyDataSetChanged()
                        }
                    }
                }

                if (snapshot != null) {
                    var flux = 0
                    var tempid = "asdsad"
                    for (d in snapshot) {
                        val newchat : chatBubbleClass? = d.toObject(chatBubbleClass::class.java)
                        if (newchat != null){
                            bubbleChatArrayList.add(newchat)
                            if(newchat.chat_content == "null"){
                                flux = 1
                                tempid = d.id
                            }
                        }
                    }
                    if(bubbleChatArrayList.size > 1 && flux == 1){
                        Log.d(TAG, "PUSH -> ")
                        docRef2.document(tempid).delete()
                    }
                    bubbleChatRecycler.adapter = chatBubbleAdapter(bubbleChatArrayList, this)
                } else {
                    Log.d(TAG, "Current data: null" + e)
                }
            }
    }
    //==================================================================================================
    private fun removeBubbleChat(chatContent: String?) {
        db = FirebaseFirestore.getInstance()
        val dbRef = db.collection("projects").document(projectID)
                .collection("RoomChat").document(currTopicID)
                .collection("chatCache")
        dbRef.whereEqualTo("chat_content", chatContent).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    dbRef.document(d.id).delete()
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }

    override fun onItemLongClick(position: Int, passData: chatBubbleClass) {
        val formProject = AlertDialog.Builder(this)
        val inflater = layoutInflater
        formProject.setTitle("Confirmation Status")

        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)
        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "Delete this chat?"

        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Delete") { DialogInterface, i ->
            removeBubbleChat(passData.chat_content)
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(this, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }


}

//        var temp = getdate()
//
//        if (topic_Name.isNotEmpty()){
//            val roomChat = hashMapOf(
//                "content" to "nanti",
//                "time" to temp,
//                "userName" to "0",
//            )