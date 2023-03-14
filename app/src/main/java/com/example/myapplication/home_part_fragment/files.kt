package com.example.myapplication.home_part_fragment

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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.databinding.FragmentFilesBinding
import com.example.myapplication.file_part_fragment.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class files : Fragment(), filesSectionAdapter.onItemClickListener{
//    private var param1: String? = null
//    private var param2: String? = null
    private lateinit var binding: FragmentFilesBinding
    private lateinit var fileSectionRecycler : RecyclerView
    private lateinit var fileSectionArrayList : ArrayList<fileSectionClass>
    private lateinit var fileSectionAdapter: filesSectionAdapter
    private var db = Firebase.firestore

    private lateinit var projectID : String
    private lateinit var currentSection : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFilesBinding.inflate(layoutInflater, container, false)
        //inflating recycle view -------------------------------------------------------------------
        fileSectionRecycler = binding.recyclerfiles
        fileSectionRecycler.layoutManager = LinearLayoutManager(context)
        fileSectionRecycler.setHasFixedSize(true)

        fileSectionArrayList = arrayListOf()
        fileSectionAdapter = filesSectionAdapter(fileSectionArrayList, this)
        fileSectionRecycler.adapter = fileSectionAdapter

        val data = arguments
        this.projectID = data!!.get("projectID").toString()

        binding.fileSectionadd.setOnClickListener(){
            createFileSection()
        }
        getSectionfile()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createFileSection() {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
//        formProject.setTitle("File Section")
        val dialogLayout = inflater.inflate(R.layout.activity_form_oneedit, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editMemberemail).text
        dialogLayout.findViewById<EditText>(R.id.editMemberemail).hint = "file section name..."
        dialogLayout.findViewById<TextView>(R.id.tvProjectTitle).text = "Section Title"
        dialogLayout.findViewById<TextView>(R.id.nameDialog).text = "create new file section"

        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Create New File Section") {
            DialogInterface, i -> pushSection(editText.toString())
        }
        formProject.setNegativeButton("Cancel") {
                DialogInterface, i -> Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pushSection(nameSection: String) {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID)

        var time = getdate().toString()
        val sectionFile = hashMapOf(
            "Section_Name" to nameSection,
            "created_time" to time
        )
        docRef2.collection("FileSection").add(sectionFile)
            .addOnSuccessListener{
                Log.d(TAG, "Section File and file added ${it.id}")
            }
    }

    private fun replaceFragment(fragment: Fragment, naem: String){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager

        db = FirebaseFirestore.getInstance()
        val docRef = db.collection("projects").document(projectID)
            .collection("FileSection")
        docRef.whereEqualTo("Section_Name", naem).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    val arguments = Bundle()
                    arguments.putString("projectID",projectID)
                    arguments.putString("sectionID",d.id)
                    fragment.arguments = arguments
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.addToBackStack("name");
                    fragmentTransaction.replace(R.id.frame_layout, fragment)
                    fragmentTransaction.commit()
                }
            }
        }

    }

//==================================================================================================
    private fun getSectionfile() {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID).collection("FileSection")
        docRef2.addSnapshotListener() { snapshot, e ->
            for (dc in snapshot!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        fileSectionArrayList.clear()
                        fileSectionRecycler?.adapter?.notifyDataSetChanged()
                    }
                    DocumentChange.Type.MODIFIED -> {
                        fileSectionArrayList.clear()
                        fileSectionRecycler?.adapter?.notifyDataSetChanged()
                    }
                    DocumentChange.Type.REMOVED ->{
                        fileSectionArrayList.clear()
                        fileSectionRecycler?.adapter?.notifyDataSetChanged()
                    }
                }
            }

            if (snapshot != null) {
                for (d in snapshot) {
                    val sectionFile : fileSectionClass? = d.toObject(fileSectionClass::class.java)
                    if (sectionFile != null){
                        fileSectionArrayList.add(sectionFile)
                        currentSection = d.id
                    }
                }
                fileSectionRecycler.adapter = filesSectionAdapter(fileSectionArrayList, this)
                if (fileSectionArrayList.size == 0){
                    binding.startchat.visibility = View.VISIBLE
                }else{
                    binding.startchat.visibility = View.GONE
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
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

    private fun removeSetion(SectionId: String) {
        val docref2 = db.collection("projects").document(projectID)
                    .collection("FileSection")
        docref2.whereEqualTo("Section_Name", SectionId).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot){
                    val temp: fileSectionClass? = d.toObject(fileSectionClass::class.java)
                    fileSectionArrayList.remove(temp)
                    docref2.document(d.id).delete()
                    break
                }
            }
            fileSectionRecycler.adapter = filesSectionAdapter(fileSectionArrayList, this)
        }
    }

    override fun onItemLongClick(position: Int, passData: fileSectionClass) {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        formProject.setTitle("Confirmation Status")

        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)

        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "Delete section file?"
        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Delete") { DialogInterface, i ->
            removeSetion(passData.Section_Name.toString())
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    override fun onItemClick(position: Int, passData: fileSectionClass) {
        replaceFragment(uploadScreen((passData.Section_Name.toString())), passData.Section_Name.toString())
    }
//    companion object {
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            files().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}