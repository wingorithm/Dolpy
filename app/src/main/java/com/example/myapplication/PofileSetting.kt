package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.myapplication.databinding.ActivityPofileSettingBinding
import com.example.myapplication.home_project_activity.peoplesCardClass
import com.example.myapplication.project_activity.ProjectsCardClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_formconfirmation.view.*
import org.checkerframework.common.returnsreceiver.qual.This
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class PofileSetting : AppCompatActivity() {
    private lateinit var binding: ActivityPofileSettingBinding
    private lateinit var user: FirebaseAuth
    private var db = Firebase.firestore
    private val storageRef = Firebase.storage("gs://myapplogin-b7a5d.appspot.com/")
    private lateinit var current_userUID : String
    private lateinit var ImageURI : Uri
    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPofileSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = FirebaseAuth.getInstance()

        // update data in profile image
        if (user.currentUser != null){
            user.currentUser?.let {
                this.current_userUID = it.uid.toString()
                Log.d(TAG, "user login with: " + it.uid)
                getUserData()
            }
        }

        //buat action bar
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()!!.setCustomView(R.layout.activity_actionbar)

        //         buat ilangin hamburger
        val actbarLayout = layoutInflater.inflate(R.layout.activity_actionbar, null)

        binding.changeprofile.setOnClickListener(){
            searchLocalImage()
        }

        binding.changeBirth.setOnClickListener(){
            binding.changeBirth.rotation = (-90).toFloat()
            changeDialog("Birthday")
        }

        binding.Logout.setOnClickListener(){
            confirmationDialog()
        }
    }

    private fun confirmationDialog() {
        val formProject = AlertDialog.Builder(this)
        val inflater = layoutInflater
        formProject.setTitle("Logout Confirmation")
        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)

        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "are you sure to logout?"

        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Log Out") { DialogInterface, i ->
            Toast.makeText(this, "SEE YOU", Toast.LENGTH_SHORT).show()
            user.signOut()
            startActivity(Intent(this, LandingActivity::class.java))
            finish();
        }
        formProject.setNegativeButton("Cancel") {
                DialogInterface, i -> Toast.makeText(this, "ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    private fun changeDialog(change : String) {
        val formProject = AlertDialog.Builder(this)
        val inflater = layoutInflater
        formProject.setTitle("Change " + change)
        val dialogLayout = inflater.inflate(R.layout.activity_formtimeline, null)
        dialogLayout.findViewById<EditText>(R.id.editText).visibility = View.GONE

        //Set Calendar Picker
        val datePicked = dialogLayout.findViewById<DatePicker>(R.id.datePicker)
        var today = Calendar.getInstance()
        var strDate : String
        datePicked.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)) { view, year, month, day ->
            today.set(year, month, day)
        }

        // Showing the Form
        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Confirm") { DialogInterface, i ->
            //formating the date
            val format = SimpleDateFormat("dd-MMM-yyyy")
            strDate = format.format(today.time)

            //push the data
            db = FirebaseFirestore.getInstance()
            val dbref = db.collection("users")
            dbref.whereEqualTo("userUID", current_userUID).addSnapshotListener() { snapshot, e ->
                if (snapshot != null) {
                    for (d in snapshot) {
                        dbref.document(d.id).update("birthday", strDate)
                    }
                }
            }
        }
        formProject.setNegativeButton("Cancel") {
                DialogInterface, i -> Toast.makeText(this, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        val show = formProject.show()

        binding.changeBirth.rotation = (0).toFloat()
    }

    private fun searchLocalImage() {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) { // izin alınmadıysa
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1)
        } else {
            val galeriIntext = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galeriIntext,2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galeriIntext = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntext,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            ImageURI = data.data!!
            if (ImageURI != null) {
                if (Build.VERSION.SDK_INT >= 28) {
                    val source = ImageDecoder.createSource(this.contentResolver,ImageURI!!)
                    val pickedBitMap = ImageDecoder.decodeBitmap(source)
                    confimationDialog(pickedBitMap)
                }
                else {
                    val pickedBitMap = MediaStore.Images.Media.getBitmap(this.contentResolver,ImageURI)
                    confimationDialog(pickedBitMap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun confimationDialog(pickedBitMap: Bitmap?) {
        val formProject = AlertDialog.Builder(this)
        formProject.setTitle("Confirmation Status")
        val dialogLayout = layoutInflater.inflate(R.layout.activity_formconfirmation, null)

        formProject.setView(dialogLayout)
        dialogLayout.tempImage.setImageBitmap(pickedBitMap)
        formProject.setPositiveButton("Kirim") { DialogInterface, i ->
            pushFileSection(ImageURI)
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(this, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    private fun pushFileSection(ImageURI: Uri) {
        lateinit var tempPhotoUrl : String

        //=============================== cari nama dulu
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("users").whereEqualTo("userUID", current_userUID)
        docRef2.addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
//                val tempname: ProjectsCardClass? = snapshot.toObject(ProjectsCardClass::class.java)
//                tempName = tempname!!.pName.toString()

                //=============================== push ke storge
                var file = Uri.fromFile(File(ImageURI.toString()))
                val storageReference = storage.reference.child("userProfile").child("${file.lastPathSegment}")
                storageReference.putFile(ImageURI)
                    .addOnSuccessListener{

                        Log.d(TAG, "KEUPLOAD MASUK" + it)
                        //=============================== dapetin data untuk firestore
//                        tempPhotoUrl = storageReference.downloadUrl
                        storage.reference.child("userProfile").listAll().addOnSuccessListener{
                            it.items.forEach(){
                                if (it == storageReference){
                                    it.downloadUrl.addOnSuccessListener {
                                        tempPhotoUrl = it.toString()
                                        //=============================== push datanya ke firestore
                                        for (d in snapshot) {
                                            db.collection("users").document(d.id).update("profilePic", tempPhotoUrl)
                                        }
//                                        pushFile_firestore(tempPhotoUrl, tempType)
                                    }
                                }
                            }
                        }

                    }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }

    private fun getUserData(){
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("users").whereEqualTo("userUID", current_userUID)
        docRef2.addSnapshotListener() { snapshot, e ->
//            for (dc in snapshot!!.documentChanges) {
//                getUserData()
//            }
            if (snapshot != null) {
                for (d in snapshot) {
                    Log.d(TAG, "PROFILE PICTURE NI BOSS ${d.id} => ${d.data}")
                    val membersss: peoplesCardClass? = d.toObject(peoplesCardClass::class.java)
                    if (membersss != null) {
                        binding.userfName.text = membersss.fName + " " + membersss.lName
                        binding.userEmail.text = membersss.email
                        binding.userPhone.text = membersss.phoneNum
                        if (membersss.profilePic != "null"){
//                            binding.CircleProfile.setImageURI(membersss.profilePic!!.toUri())
                            Picasso.get().load(membersss.profilePic).into(binding.CircleProfile)
                        }
                        if (membersss.birthday != "null"){
                            binding.userBirtday.text = membersss.birthday
                        }

                        break
                    }
                }
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }
}