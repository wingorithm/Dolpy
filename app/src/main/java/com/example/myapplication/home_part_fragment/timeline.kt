package com.example.myapplication.home_part_fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.databinding.FragmentTimelineBinding
import com.example.myapplication.timeline_part_fragment.timelineDetail
import com.example.myapplication.timeline_part_fragment.timelineProgressClass
import com.example.myapplication.timeline_part_fragment.timelineSectionAdapter
import com.example.myapplication.timeline_part_fragment.timelineSectionClass
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class timeline : Fragment(), timelineSectionAdapter.onItemClickListener {
    private lateinit var binding : FragmentTimelineBinding
    private lateinit var timelineRecycler : RecyclerView
    private lateinit var timelineArrayList : ArrayList<timelineSectionClass>
    private lateinit var timelineAdapter: timelineSectionAdapter
    private var db = Firebase.firestore

    private lateinit var projectID : String
    private lateinit var tlSectionID : String
    private lateinit var includeDesc : ArrayList<String>
    private lateinit var includeTask : ArrayList<String>
    private lateinit var includeBundle : ArrayList<String>


    val TimelineSubs = timelineProgressClass(
        "null",
        "null",
        arrayListOf("null"),
        arrayListOf("null")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTimelineBinding.inflate(layoutInflater, container, false)

        //inflating recycle view -------------------------------------------------------------------
        timelineRecycler = binding.recyclerTimeline
        timelineRecycler.layoutManager = LinearLayoutManager(context)
        timelineRecycler.setHasFixedSize(true)

        timelineArrayList = arrayListOf()
        includeBundle = arrayListOf()
        includeTask = arrayListOf()
        includeDesc = arrayListOf()

        timelineAdapter = timelineSectionAdapter(timelineArrayList, this, includeBundle, includeTask, includeDesc)
        timelineRecycler.adapter = timelineAdapter

        val data = arguments
        this.projectID = data!!.get("projectID").toString()
        getTimelineSection()

        binding.TimeLineadd.setOnClickListener(){
            addTimelineSection()
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimelineSection() {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID).collection("Timeline")
        docRef2.get().addOnSuccessListener { snapshot ->
//            for (dc in snapshot!!.documentChanges) {
//                when (dc.type) {
//                    DocumentChange.Type.ADDED -> {
//                        timelineArrayList.clear()
//                        timelineRecycler?.adapter?.notifyDataSetChanged()
//                    }
//                    DocumentChange.Type.MODIFIED -> {
//                        timelineArrayList.clear()
//                        timelineRecycler?.adapter?.notifyDataSetChanged()
//                    }
//                    DocumentChange.Type.REMOVED ->{
//                        timelineArrayList.clear()
//                        timelineRecycler?.adapter?.notifyDataSetChanged()
//                    }
//                }
//            }

            if (snapshot != null) {
                for (d in snapshot) {
                Log.d(TAG, "--> " + d.id)
                    tlSectionID = d.id.toString()
                    val timelines : timelineSectionClass? = d.toObject(timelineSectionClass::class.java)
                    if (timelines != null){
                        Log.d(TAG, "-=: " + timelines)
                        val Cdtnew : LocalDateTime = LocalDateTime.ofEpochSecond(timelines.dueSection!!.toLong(), 0, ZoneOffset.UTC)
                        //compare and update
                        val now: LocalDateTime = LocalDateTime.now()
                        val hours: Long = ChronoUnit.HOURS.between(now, Cdtnew)
                        docRef2.document(d.id).update("durationSection", hours.toString())

                        if (hours < 24){
                            docRef2.document(d.id).update("statusSection", "red")
                        }else if (hours > 24 && hours < 168) {
                            docRef2.document(d.id).update("statusSection", "orange")
                        }

                        docRef2.document(tlSectionID).collection("SubSection").limit(1).get().addOnSuccessListener{ snapshot ->
                            if (snapshot != null) {
                                var subs : timelineProgressClass = TimelineSubs
                                var flag = 0
                                for (i in snapshot) {
                                    Log.d(TAG, "--> " + i.id)
                                    subs = i.toObject(timelineProgressClass::class.java)
                                    for (i in subs.context!!){
                                        if(subs.context == null){
                                            subs.context!!.add("null")
                                            subs.contextDecs!!.add("null")
                                            break
                                        }else{
                                            this.includeDesc.add(i.toString())
                                            this.includeTask.add(i.toString())
                                            break
                                        }
                                    }
                                    this.includeBundle.add(subs.progressTitle.toString())
                                    this.timelineArrayList.add(timelines)

                                    Log.d(TAG, "++: " + timelines + includeBundle + includeTask + includeDesc)
                                    timelineRecycler.adapter = timelineSectionAdapter(timelineArrayList, this, includeBundle, includeTask, includeDesc)
                                }
                            }else{
                                timelineRecycler.adapter = timelineSectionAdapter(timelineArrayList, this, includeBundle, includeTask, includeDesc)
                            }
                            if (timelineArrayList.size == 0){
                                binding.startchat.visibility = View.VISIBLE
                            }else{
                                binding.startchat.visibility = View.GONE
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }
    //==================================================================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun addTimelineSection() {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
//        formProject.setTitle("Section Title")
        val dialogLayout = inflater.inflate(R.layout.activity_formproject, null)
        val tempname  = dialogLayout.findViewById<EditText>(R.id.editText).text
        dialogLayout.findViewById<EditText>(R.id.editText).hint = "Enter your timeline Section"
        dialogLayout.findViewById<TextView>(R.id.tvProjectTitle).text = "Timeline Section"
        dialogLayout.findViewById<TextView>(R.id.createProject).text = "Create new Timeline"


        dialogLayout.visibility = View.VISIBLE
        dialogLayout.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //Set Calendar Picker
        val datePicked = dialogLayout.findViewById<DatePicker>(R.id.datePicker2)
        var today = Calendar.getInstance()
        var strDate : String
        datePicked.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)) { view, year, month, day ->
            today.set(year, month, day)
        }

        //LocalTIme Comparison Later
        val now: LocalDateTime = LocalDateTime.now()
//        val nowInSeconds: Long = now.toEpochSecond(ZoneOffset.UTC) //LocalTIme TO LONG
//            val hours2 : Long = Cdt2 - nowInSeconds

        // Showing the Form
        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Confirm") { DialogInterface, i ->
            //formating the date
            val format = SimpleDateFormat("dd-MMM-yyyy")
            strDate = format.format(today.time)

            //Comparing Value
            var Cdt = convertToLocalDateTimeViaInstant(today.time)
            var Cdt2 : Long = Cdt!!.toEpochSecond(ZoneOffset.UTC)
            val hours: Long = ChronoUnit.HOURS.between(now, Cdt)

            if (hours > 1 && tempname.isNotEmpty()){
                pushSectionData(tempname.toString(), Cdt2, hours, strDate)
                Log.d(TAG, "message: " + strDate)
                Log.d(TAG, "converted Long : " + Cdt2)
            }else{
                Toast.makeText(activity, "Please fill all the blank", Toast.LENGTH_SHORT).show()
            }
        }
        formProject.setNegativeButton("Cancel") {
                DialogInterface, i -> Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToLocalDateTimeViaInstant(dateToConvert: Date): LocalDateTime? {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }

    private fun pushSectionData(namaSection: String, dueSection: Long, durr : Long, dueDate : String) {
        var status : String
        if (durr < 24){
            status = "red"
        }else if (durr > 24 && durr < 168){
            status = "orange"
        }else{
            status = "green"
        }

        if (status.isNotEmpty()){
            val timelines = hashMapOf(
                "judulSection" to namaSection,
                "statusSection" to status,
                "dueDate" to dueDate,
                "dueSection" to dueSection.toString(),
                "durationSection" to durr.toString(),
                "SectionDecs" to "null",
                "workStatusSection" to "on going"
            )

            val dbref = db.collection("projects").document(projectID)
                .collection("Timeline")

            dbref.add(timelines).addOnSuccessListener { documentReference ->
                Log.d(TAG, "roomChat added ${documentReference.id}")
                dbref.document(documentReference.id).collection("SubSection").add(TimelineSubs)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding roomChat", e)
            }
        }
    }
    //==================================================================================================
    private fun replaceFragment(fragment: Fragment, SectionID : String, namaSection : String){
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val arguments = Bundle()
        arguments.putString("projectID",projectID)
        arguments.putString("SectionName", namaSection)
        arguments.putString("SectionID", SectionID)

        fragment.arguments = arguments
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack("name");
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun findSectionRemove(judulSection: String?) {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID).collection("Timeline")
        docRef2.whereEqualTo("judulSection", judulSection).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    docRef2.document(d.id).delete()
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }

    override fun onLongItemClick(position: Int, passData: timelineSectionClass) {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        formProject.setTitle("Confirmation Status")

        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)
        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "Delete Section: " + passData.judulSection

        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Delete") { DialogInterface, i ->
            findSectionRemove(passData.judulSection)
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    override fun onItemClick(position: Int, passData: timelineSectionClass) {
        Toast.makeText(activity, " Kepencet: " + position, Toast.LENGTH_SHORT).show()
        replaceFragment(timelineDetail(), tlSectionID, passData.judulSection.toString())
    }

}