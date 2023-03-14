package com.example.myapplication.project_activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings.Global
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.databinding.ActivityProjectsHomeBinding
import com.example.myapplication.home_project_activity.HomePage
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProjectsHomeActivity : AppCompatActivity(), ProjectsCardsAdapter.onItemClickListener {
    private lateinit var binding: ActivityProjectsHomeBinding
    private lateinit var user: FirebaseAuth
    private var db = Firebase.firestore
    private lateinit var projectRecycle : RecyclerView
    private lateinit var projectArraylist : ArrayList<ProjectsCardClass>
    private lateinit var projectAdapter : ProjectsCardsAdapter
    private lateinit var current_userUID : String

    private lateinit var toogle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectsHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = FirebaseAuth.getInstance()

        //hamburger -------------------------------------------------------------------
        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_Sliding)

        toogle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_Calendar -> Toast.makeText(applicationContext, "Calendar", Toast.LENGTH_SHORT).show()
                R.id.nav_Profile -> {
                    Toast.makeText(applicationContext, "PROFILE KUY", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PofileSetting::class.java ))
                }
                R.id.nav_Project -> Toast.makeText(applicationContext, "Calendar", Toast.LENGTH_SHORT).show()
                R.id.nav_Help -> Toast.makeText(applicationContext, "Calendar", Toast.LENGTH_SHORT).show()
                R.id.nav_TodoList -> Toast.makeText(applicationContext, "Calendar", Toast.LENGTH_SHORT).show()
            }
        true
        }
//        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()!!.setCustomView(R.layout.activity_actionbar);


        //inflating recycle view -------------------------------------------------------------------
        projectRecycle = findViewById(R.id.projectCardList)
        projectRecycle.layoutManager = LinearLayoutManager(this)
        projectRecycle.setHasFixedSize(true)

        projectArraylist = arrayListOf()
        projectAdapter = ProjectsCardsAdapter(projectArraylist,this)
        projectRecycle.adapter = projectAdapter

        // nampilin email & id   -------------------------------------------------------------------

        if (user.currentUser != null){
            user.currentUser?.let {
                this.current_userUID = it.uid.toString()
                getprojectdata()
            }
        }

        binding.addbtn.setOnClickListener(){
            addProjectTask()
        }
    }

//==================================================================================================
    // tombol tambah dan form
    @SuppressLint("MissingInflatedId")
    private fun addProjectTask() {
        val formProject = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.activity_formproject, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        val editText2 = dialogLayout.findViewById<EditText>(R.id.editText2)
        dialogLayout.visibility = View.VISIBLE
        dialogLayout.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogLayout.findViewById<Button>(R.id.createProjectbtn).visibility = View.GONE
        formProject.setView(dialogLayout)

        //Set Calendar Picker
        val datePicked = dialogLayout.findViewById<DatePicker>(R.id.datePicker2)
        var today = Calendar.getInstance()
        var strDate : String
        datePicked.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)) { view, year, month, day ->
            today.set(year, month, day)
        }

        formProject.setPositiveButton("OK") { DialogInterface, i ->
            val format = SimpleDateFormat("dd-MMM-yyyy")
            strDate = format.format(today.time)
            pushProjectData(editText.text.toString(), editText2.text.toString(), current_userUID, strDate)
        }
        formProject.setNegativeButton("Cancel") {
            DialogInterface, i -> Toast.makeText(applicationContext, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    private fun pushProjectData(Projectname: String, Creatorname: String, userID: String, strDate: String) {
        if (Projectname.isNotEmpty() && strDate.isNotEmpty()){
            val project = hashMapOf(
                "projectUid" to "nanti",
                "pName" to Projectname,
                "pCreator" to user.currentUser!!.displayName,
                "userID" to arrayListOf(userID),
                "pdueDate" to strDate
            )

        // ======= collection adding data
            db.collection("projects")
                .add(project)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Project added with ID: ${documentReference.id}")

                    // Setelah dibuat langsung update id
                    val tempDoc = db.collection("projects").document(documentReference.id)

                    tempDoc.update("projectUid", documentReference.id)

                    db.collection("projects").document(documentReference.id)
                        .collection("Roomchat")
                    db.collection("projects").document(documentReference.id)
                        .collection("TimeLine")
                    db.collection("projects").document(documentReference.id)
                        .collection("File")
                    db.collection("projects").document(documentReference.id)
                        .collection("People")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding Project", e)
                }
            Toast.makeText(applicationContext, Projectname + " is created ", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "ISI dulu semua data yang diminta Cuy", Toast.LENGTH_SHORT).show()
        }
    }


//==================================================================================================
    //  untuk bikin data recycle view
    private fun getprojectdata() {
        db = FirebaseFirestore.getInstance()
        //------------------ Project (snapshot ini realtime punya)
        val docRef = db.collection("projects").whereArrayContains("userID", current_userUID)
        docRef.addSnapshotListener() { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            for (dc in snapshot!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        projectArraylist.clear()
                        Log.d(TAG, "YANG DITAMBAHIN: ${dc.document.data}")
                        projectRecycle?.adapter?.notifyDataSetChanged()
                    }
                    DocumentChange.Type.MODIFIED -> {
                        projectArraylist.clear()
                        Log.d(TAG, "Modified city: ${dc.document.data}")
                        projectRecycle?.adapter?.notifyDataSetChanged()
                    }
                    DocumentChange.Type.REMOVED ->{
                        projectArraylist.clear()
                        Log.d(TAG, "Removed city: ${dc.document.data}")
                        projectRecycle?.adapter?.notifyDataSetChanged()
                    }
                }
            }

            if (snapshot != null) {
                for (document in snapshot) {
                    Log.d(TAG, " TEST BRO ${document.id} => ${document.data}")
                    val project : ProjectsCardClass?  = document.toObject(ProjectsCardClass::class.java)
                    if (project != null){
                        projectArraylist.add(project)
                    }
                }
                projectRecycle.adapter = ProjectsCardsAdapter(projectArraylist, this)
                if (projectArraylist.size == 0){
                    binding.startchat.visibility = View.VISIBLE
                }else{
                    binding.startchat.visibility = View.GONE
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }
    }
//==================================================================================================
    //    Menu selalu ada
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.profile_menu, menu)
        return true
    }

    //    Menu atas 3 titik
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        if (item.itemId == R.id.settingProfile){
            startActivity(Intent(this, PofileSetting::class.java ));
        }

        if (toogle.onOptionsItemSelected(item)){
            return true
        }
        return true
    }

    override fun onItemClick(position: Int, passData: ProjectsCardClass) {
//        Toast.makeText(applicationContext, " kepencet ke- $position" + passData.projectUid, Toast.LENGTH_SHORT).show()
        val intent = Intent(this@ProjectsHomeActivity,  HomePage::class.java)
        val extras = Bundle()
        extras.putString("projectID",passData.projectUid)
        extras.putString("projectName",passData.pName)
        intent.putExtras(extras)
        startActivity(intent)
    }

    companion object{
        val a : ProjectsHomeActivity = ProjectsHomeActivity()
        lateinit var currUser :String
        lateinit var pName : String

        fun removeuser(pID: String){
            a.db = FirebaseFirestore.getInstance()
            val docRef = a.db.collection("projects").document(pID)
            docRef.update("userID", FieldValue.arrayRemove(currUser))
        }

//        fun getDbref(pID: String): DocumentReference {
//            a.db = FirebaseFirestore.getInstance()
//            return a.db.collection("projects").document(pID)
//        }

        fun leaveProject(pID: String) : Int{
            a.user = FirebaseAuth.getInstance()
            a.user.currentUser?.let {
                this.currUser = it.uid.toString()
            }
            var returns = 1
            val job = GlobalScope.launch(Dispatchers.Default) {
            a.db = FirebaseFirestore.getInstance()
            val docRef = a.db.collection("projects").document(pID)
            docRef.addSnapshotListener(){snapshot, e ->
                    val temp : ProjectsCardClass? = snapshot?.toObject(ProjectsCardClass::class.java)
                    var count = 0
                    if (temp != null) {
                        for (i in temp.userID!!){
                            if (i == currUser && count == 0){
                                returns = 1
                            }else{
                                returns = 0
                            }
                            count++
                            Log.d(TAG, "?= " + i + " " +currUser +" "+  returns)
                        }
                    }
                }
            }
            return runBlocking {
                job.join()
                Log.d(TAG, "=> " + returns)
                return@runBlocking returns
            }
        }

        fun markAsDone(pID2: String): Int {
            var flag = 0
            var returns : Int = 0
            lateinit var currUser : String
            lateinit var the_boss : String
            a.user = FirebaseAuth.getInstance()
            a.user.currentUser?.let {
                currUser = it.uid.toString()
            }

            runBlocking {
                launch {
                    a.db = FirebaseFirestore.getInstance()
                        val docRef = a.db.collection("projects").document(pID2).addSnapshotListener() { snap, e ->
                            if (snap != null) {
                                var project : ProjectsCardClass? = snap.toObject(ProjectsCardClass::class.java)
                                Log.d(TAG, "boss: " + project?.userID?.get(0).toString() + " " + pID2)
                                if (project != null) {
                                    the_boss = project.userID?.get(0).toString()
                                    pName = project.pName.toString()

                                    //Check Siapa Bossnya
                                    if (currUser == the_boss){
                                        returns = 1
                                    }
                                    else{
                                        returns = 2
                                    }
                                    Log.d(TAG, "?= " + returns)
                                }
                            }

                        }
                }
            }
        Log.d(TAG, "-> return" + returns)
        return returns
        }
    }

}