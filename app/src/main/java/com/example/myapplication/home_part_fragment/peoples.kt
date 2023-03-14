package com.example.myapplication.home_part_fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.databinding.FragmentPeoplesBinding
import com.example.myapplication.home_project_activity.peoplesCardAdapter
import com.example.myapplication.home_project_activity.peoplesCardClass
import com.example.myapplication.project_activity.ProjectsCardClass
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class peoples(): Fragment(), peoplesCardAdapter.onItemClickListener{
    private lateinit var binding: FragmentPeoplesBinding
    private lateinit var peopleRecycle: RecyclerView
    private lateinit var peopleArraylist: ArrayList<peoplesCardClass>
    private lateinit var temp: ProjectsCardClass
    private lateinit var peopleAdapter: peoplesCardAdapter
    private var db = Firebase.firestore
    private lateinit var projectID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPeoplesBinding.inflate(layoutInflater, container, false)

        //inflating recycle view -------------------------------------------------------------------
        peopleRecycle = binding.peopleCardList
        peopleRecycle.layoutManager = LinearLayoutManager(context)
        peopleRecycle.setHasFixedSize(true)

        peopleArraylist = arrayListOf()
        peopleAdapter = peoplesCardAdapter(peopleArraylist, this)
        peopleRecycle.adapter = peopleAdapter

        val data = arguments
        this.projectID = data!!.get("projectID").toString()
        getItemPeople(projectID)

        binding.memberaddbtn.setOnClickListener() {
            addMemberProject()
        }

        setHasOptionsMenu(true)
        return binding.root
    }

    // ini penting buat menu ============================================================================
//     override fun setHasOptionsMenu(
//        hasMenu:Boolean
//    ):Unit {}
//
//    @Deprecated("Deprecated in Java")
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) : Unit {
//        menu.clear()
//        activity?.menuInflater?.inflate(R.menu.information_people_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    //    Menu atas 3 titik
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        super.onOptionsItemSelected(item)
//
//        if (item.itemId == R.id.removeMember){
//            Log.d(tag, "ini masuk gk")
//            removeMemberProject()
//        }
//        return true
//    }
    //==================================================================================================
    private fun addMemberProject() {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
//        formProject.setTitle("Add New Member")

        val dialogLayout = inflater.inflate(R.layout.activity_form_oneedit, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editMemberemail).text
        dialogLayout.findViewById<EditText>(R.id.editMemberemail).hint = "your friend email..."
        dialogLayout.findViewById<TextView>(R.id.tvProjectTitle).text = "New Member Email"
        dialogLayout.findViewById<TextView>(R.id.nameDialog).text = "add new member"


        var flag : Boolean = true
        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Add Member") { DialogInterface, i ->
            for (i in peopleArraylist){
                if (i.email == editText.toString() || binding.creatoremail.text.toString() == editText.toString()){
                    flag = false
                }
                Log.d(TAG, "ni email: ${i.email} + ${binding.creatoremail.text.toString()}")
            }
            if (flag){
                findMemberAdded(editText.toString())
            }else{
                Toast.makeText(activity, "EMAILNYA UDAH ADA", Toast.LENGTH_SHORT).show()
            }
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    private fun findMemberRemove(memberUID: String) {
        Log.d(TAG, " MEMBER Hapus ni Boss ${memberUID}")
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("users").whereEqualTo("userUID", memberUID)
        docRef2.addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    val membersss: peoplesCardClass? = d.toObject(peoplesCardClass::class.java)
                    if (membersss != null) {
                        peopleArraylist.remove(membersss)
                        removeProjectmember(membersss.userUID.toString())
                        break
                    }
                }
                peopleRecycle.adapter = peoplesCardAdapter(peopleArraylist, this)
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }

    private fun findMemberAdded(membergmail: String) {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("users").whereEqualTo("email", membergmail)
        docRef2.addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    Log.d(TAG, " MEMBER BARU ni Boss ${d.id} => ${d.data}")
                    val membersss: peoplesCardClass? = d.toObject(peoplesCardClass::class.java)
                    if (membersss != null) {
                        peopleArraylist.add(membersss)
                        updateProjectmember(membersss.userUID.toString())
                        peopleRecycle.adapter = peoplesCardAdapter(peopleArraylist, this)
                        break
                    }
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
                Toast.makeText(activity, "Email must be wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProjectmember(uid : String) {
        db.collection("projects").document(projectID).update("userID", FieldValue.arrayUnion(uid))
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    private fun removeProjectmember(uid : String) {
        db.collection("projects").document(projectID).update("userID", FieldValue.arrayRemove(uid))
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    fun getItemPeople(projectID: String) {
        db = FirebaseFirestore.getInstance()
        val docRef = db.collection("projects").document(projectID)
        docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    // cariin nama dan data usernya
                    val temp: ProjectsCardClass? = document.toObject(ProjectsCardClass::class.java)
                    var count: Int = 0
                    for (id in temp!!.userID!!) {
                        val docRef2 = db.collection("users").whereEqualTo("userUID", id)
                        docRef2.addSnapshotListener() { snapshot, e ->
                            if (snapshot != null) {
                                for (d in snapshot) {

                                    val membersss: peoplesCardClass? = d.toObject(peoplesCardClass::class.java)

                                    if (count == 0) {
                                        binding.creatorName.text = (membersss!!.fName + membersss!!.lName)
                                        binding.creatoremail.text = (membersss!!.email)
                                        if (membersss.profilePic != "null"){
                                            Picasso.get().load(membersss.profilePic).into(binding.creatorProfile)
                                        }
                                    } else if (membersss != null) {
                                        peopleArraylist.add(membersss)
                                    }
                                }
                                peopleRecycle.adapter = peoplesCardAdapter(peopleArraylist, this)
                                count++
                                if (peopleArraylist.size == 0){
                                    binding.startchat.visibility = View.VISIBLE
                                }else{
                                    binding.startchat.visibility = View.GONE
                                }
                            } else {
                                Log.d(TAG, "Current data: null" + e)
                            }
                        }
                    }

                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    override fun onItemLongClick(position: Int, passPeople: peoplesCardClass) {
        Log.d(TAG, "Mau Dihapus => ${passPeople.userUID}")
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        formProject.setTitle("Confirmation Status")

        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)
        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "are you sure kick ${passPeople.fName} ${passPeople.lName}?"

        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Kick") { DialogInterface, i ->
            findMemberRemove(passPeople.userUID.toString())
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }
}
