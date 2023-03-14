package com.example.myapplication.timeline_part_fragment

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
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.widget.AppCompatButton
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.FragmentTransaction
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.myapplication.R
    import com.example.myapplication.TAG
    import com.example.myapplication.databinding.FragmentTimelineDetailBinding
    import com.google.firebase.firestore.DocumentChange
    import com.google.firebase.firestore.FieldValue
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.firestore.ktx.snapshots
    import com.google.firebase.ktx.Firebase


class timelineDetail() : Fragment(), timelineProgressAdapter.onItemClickListener{
    private lateinit var binding : FragmentTimelineDetailBinding
    private lateinit var tlDetailRecycler : RecyclerView
    lateinit var tlDetailArrayList : ArrayList<timelineProgressClass>
    private lateinit var tlDetailAdapter: timelineProgressAdapter
    private var db = Firebase.firestore

    private lateinit var section_name : String
    private lateinit var projectID : String
    private lateinit var SectionId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTimelineDetailBinding.inflate(layoutInflater, container, false)

        val data = arguments
        this.projectID = data!!.get("projectID").toString()
        this.section_name = data.get("SectionName").toString()
        this.SectionId = data.get("SectionID").toString()

        //inflating recycle view -------------------------------------------------------------------
        tlDetailRecycler = binding.recyclerProgress
        tlDetailRecycler.layoutManager = LinearLayoutManager(context)
        tlDetailRecycler.setHasFixedSize(true)

        tlDetailArrayList = arrayListOf()
        tlDetailAdapter = timelineProgressAdapter(tlDetailArrayList, this, projectID, SectionId)
        tlDetailRecycler.adapter = tlDetailAdapter


        //basic replacement
        replaceImportant()
        binding.addSubSectionTimeline.setOnClickListener(){

            addSubSection()
        }
        binding.descText.setOnClickListener(){
            binding.DescOk.visibility = View.VISIBLE
            binding.DescOk.setOnClickListener(){
                updateDesc(binding.descText.text.toString())
                binding.DescOk.visibility = View.GONE
            }
        }
        return binding.root
    }

    private fun updateDesc(text: String) {
        val dbref = db.collection("projects").document(projectID)
            .collection("Timeline").document(SectionId)

        dbref.update("SectionDecs", text)
    }

    private fun getSubData() {
        db = FirebaseFirestore.getInstance()
        val dbref = db.collection("projects").document(projectID)
            .collection("Timeline").document(SectionId)
            .collection("SubSection")

        dbref.get().addOnSuccessListener{ snapshot ->

//            for (dc in snapshot!!.documentChanges) {
//                when (dc.type) {
//                    DocumentChange.Type.ADDED -> {
//                        tlDetailArrayList.clear()
//                        tlDetailRecycler?.adapter?.notifyDataSetChanged()
//                    }
//                    DocumentChange.Type.MODIFIED -> {
//                        tlDetailArrayList.clear()
//                        tlDetailRecycler?.adapter?.notifyDataSetChanged()
//                    }
//                    DocumentChange.Type.REMOVED ->{
//                        tlDetailArrayList.clear()
//                        tlDetailRecycler?.adapter?.notifyDataSetChanged()
//                    }
//                }
//            }

            //-------------------------------STEP 1 DATA GETTER
                if (snapshot != null) {
                    var flag : Int = 0
                    var tempCuriga : String = "nggakada"
                    for (d in snapshot) {

                        val subs : timelineProgressClass? = d.toObject(timelineProgressClass::class.java)
                        if (subs != null){
                        //-------------------------------STEP 2 DATA GETTER
                            if(subs.colorCode == "null"){
                                flag = 1
                                tempCuriga = d.id
                            }else if(subs.context!!.contains("null")){
                                flag = 2
                                tempCuriga = d.id
                            }
                            tlDetailArrayList.add(subs)
                        }
                    }
                    if(flag == 1 && tlDetailArrayList.size > 1){
                        dbref.document(tempCuriga).delete()
                        flag = 0
                    }else if(flag == 2){
                        Log.d(TAG, "--> " + tlDetailArrayList)
                        dbref.document(tempCuriga).update("context", FieldValue.arrayRemove("null"))
                        dbref.document(tempCuriga).update("contextDecs", FieldValue.arrayRemove("null"))
                        flag = 0
                    }
                    tlDetailRecycler.adapter = timelineProgressAdapter(tlDetailArrayList, this, projectID, SectionId)
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }

    private fun replaceImportant() {
        //initial data collecting
        db = FirebaseFirestore.getInstance()
        var temp: timelineSectionClass?
        val dbref = db.collection("projects").document(projectID).collection("Timeline")
        dbref.whereEqualTo("judulSection", section_name).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot){
                    temp = d.toObject(timelineSectionClass::class.java)
                    Log.d(TAG, "KETEMU ID TIMELINE: " + d.data)
                    this.SectionId = d.id
                    getSubData()

                    //kasih value
                    if (temp!!.dueDate == "done"){
                        binding.statusTL.text = "done"
                    }
                    binding.selectDueDate.text = temp!!.dueDate

                    if (temp!!.SectionDecs != "null"){
                        binding.descText.setText(temp!!.SectionDecs)
                    }
                }
                binding.judulTimeline.text = section_name
            }
        }
    }
//==================================================================================================
    private fun addSubSection() {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
//        formProject.setTitle("Add Timeline Sub Section")
        val dialogLayout = inflater.inflate(R.layout.activity_formsubsectoin2_tl, null)
        dialogLayout.findViewById<EditText>(R.id.editSectionName).hint = "task name here..."

        val tempname = dialogLayout.findViewById<EditText>(R.id.editSectionName).text
        var colorPick : Int = 0

        // color picker [1 = green, 2 = orange, 3 = brown, 4 = black]
        dialogLayout.findViewById<AppCompatButton>(R.id.greenBack).setOnClickListener(){ colorPick = 1 }
        dialogLayout.findViewById<AppCompatButton>(R.id.orangeBack).setOnClickListener(){ colorPick = 2 }
        dialogLayout.findViewById<AppCompatButton>(R.id.brownBack).setOnClickListener(){ colorPick = 3 }
        dialogLayout.findViewById<AppCompatButton>(R.id.blackBack).setOnClickListener(){ colorPick = 4 }

        // Form Shower
        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Confirm") { DialogInterface, i ->
            if (colorPick != 0){
                pushSubSection(tempname.toString(), colorPick)
            }else{
                Toast.makeText(activity, "Please chosee label color", Toast.LENGTH_SHORT).show()
            }
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    private fun pushSubSection(tempname: String, color : Int) {
        val subs = hashMapOf(
            "progressTitle" to tempname,
            "colorCode" to color.toString(),
            "context" to arrayListOf("null"),
            "contextDecs" to arrayListOf("null")
        )
        db.collection("projects").document(projectID)
            .collection("Timeline").document(SectionId)
            .collection("SubSection")
            .add(subs)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "subs baru ${documentReference.id}")
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error adding roomChat", e)
            }

    }

    private fun pushSubTask(TaskName: String, TaskDesc: String, SubTitle: String, COM_projectID: String, COM_SectionId: String) {
        Log.d(TAG, "hello FORM" + TaskName.toString()  +" "+ TaskDesc.toString()  +" "+
                SubTitle +" "+ COM_projectID +" "+ COM_SectionId)
        var tempUid : String
        db = FirebaseFirestore.getInstance()
        val dbref =  db.collection("projects").document(COM_projectID)
            .collection("Timeline").document(COM_SectionId)
            .collection("SubSection")
        dbref.whereEqualTo("progressTitle", SubTitle).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot){
                    // UPDATE ARRAY
                    dbref.document(d.id).update("context", FieldValue.arrayUnion(TaskName))
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!")}
                        .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

                    dbref.document(d.id).update("contextDecs", FieldValue.arrayUnion(TaskDesc))
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully updated!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
                }
            }
        }
        //ulang disini
//        val ft: FragmentTransaction = requireFragmentManager().beginTransaction()
//        if (Build.VERSION.SDK_INT >= 26) {
//            ft.setReorderingAllowed(false)
//        }
//        ft.detach(this).attach(this).commit()
        lifecycleScope.launchWhenResumed {
            findNavController().navigate(R.id.timelineDetail)
        }
//        projectID = COM_projectID
//        SectionId = COM_SectionId
//        tlDetailRecycler = binding.recyclerProgress
//        tlDetailRecycler.layoutManager = LinearLayoutManager(context)
//        tlDetailRecycler.setHasFixedSize(true)
//
//        tlDetailArrayList = arrayListOf()
//        tlDetailAdapter = timelineProgressAdapter(tlDetailArrayList, this, projectID, SectionId)
//        tlDetailRecycler.adapter = tlDetailAdapter
//        getSubData()
    }

    private fun findProgressRemove(progressTitle: String) {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID)
                        .collection("Timeline").document(SectionId).collection("SubSection")
        docRef2.whereEqualTo("progressTitle", progressTitle).addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                for (d in snapshot) {
                    docRef2.document(d.id).delete()
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }

    override fun onItemLongClick(position: Int, passData: timelineProgressClass) {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        formProject.setTitle("Confirmation Status")

        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)
        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "Delete Task: " + passData.progressTitle

        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Delete") { DialogInterface, i ->
            findProgressRemove(passData.progressTitle.toString())
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    companion object {
        val a : timelineDetail = timelineDetail()
        lateinit var tlDetailArrayList2 : ArrayList<timelineProgressClass>
//        var COM_projectID = a.projectID
//        var COM_SectionId = a.SectionId

        fun continuePush(
            TaskName: String,
            TaskDesc: String,
            SubTitle: String,
            COM_SectionId: String,
            COM_projectID: String){
            a.pushSubTask(TaskName, TaskDesc, SubTitle, COM_projectID, COM_SectionId)
        }

//            tlDetailArrayList.clear()
//            tlDetailRecycler?.adapter?.notifyDataSetChanged()
//            tlDetailRecycler.adapter = timelineProgressAdapter(tlDetailArrayList, this, projectID, SectionId)
//
//            dbref.addSnapshotListener(){snapshot, e ->
//                if (snapshot != null) {
//                    for (d in snapshot) {
//                        val subs : timelineProgressClass? = d.toObject(timelineProgressClass::class.java)
//                        if (subs != null){
//                            tlDetailArrayList.add(subs)
//                        }
//                    }
//                    tlDetailRecycler.adapter = timelineProgressAdapter(tlDetailArrayList, this, projectID, SectionId)
//                } else {
//                    Log.d(TAG, "Current data: null" + e)
//                }
//            }

    }

}