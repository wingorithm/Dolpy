package com.example.myapplication.file_part_fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.TAG
import com.example.myapplication.databinding.FragmentUploadScreenBinding
import com.example.myapplication.project_activity.ProjectsCardClass
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_formconfirmation.view.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class uploadScreen(sectionName :String) : Fragment(), filesOnlyAdapter.onItemClickListener {
    private var section_name : String = sectionName
    private val storage = Firebase.storage
    private lateinit var binding: FragmentUploadScreenBinding
    private lateinit var fileOnlyRecycler : RecyclerView
    private lateinit var fileOnlyArrayList : ArrayList<fileOnlyClass>
    private lateinit var fileOnlyAdapter: filesOnlyAdapter
    private var db = Firebase.firestore

    private lateinit var projectID : String
    private lateinit var SectionId : String
    private lateinit var ImageURI : Uri

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUploadScreenBinding.inflate(layoutInflater,container, false)
        //inflating recycle view -------------------------------------------------------------------
        fileOnlyRecycler = binding.recyclerfiles
        fileOnlyRecycler.layoutManager = LinearLayoutManager(context)
        fileOnlyRecycler.setHasFixedSize(true)

        fileOnlyArrayList = arrayListOf()
        fileOnlyAdapter = filesOnlyAdapter(fileOnlyArrayList, this)
        fileOnlyRecycler.adapter = fileOnlyAdapter

        val data = arguments
        this.projectID = data!!.get("projectID").toString()
        this.SectionId = data!!.get("sectionID").toString()
        binding.textView.text = section_name
        Log.d(TAG, "{{{{{" +section_name +" "+ SectionId)

        binding.imagebtn.setOnClickListener(){
            searchLocalImage()
        }
        binding.attachbtn.setOnClickListener(){
            searchLocalfile()
        }
        binding.linkbtn.setOnClickListener(){
            putlink()
        }
        getAllFiles()
        return binding.root
    }


    private lateinit var pickedBitMap : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    //==================================================================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun putlink() {
        val formProject = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.activity_form_oneedit, null)

        dialogLayout.findViewById<EditText>(R.id.editMemberemail).hint = "your link..."
        dialogLayout.findViewById<TextView>(R.id.nameDialog).text = "Upload Link"
        dialogLayout.findViewById<TextView>(R.id.tvProjectTitle).text = "Link Reference"
        var link  = dialogLayout.findViewById<EditText>(R.id.editMemberemail).text.toString()


        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Upload") { DialogInterface, i ->
            Log.d(TAG, "-->" + link)
            pushFile_firestore(link, "string/link")
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    private fun searchLocalfile() {
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 111 && resultCode == RESULT_OK) {
//            val selectedFile = data?.data // The URI with the location of the file
//        }
//    }

    //==================================================================================================
    private fun searchLocalImage() {
        if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) { // izin alınmadıysa
            ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
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
                val galeriIntext = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
                    val source = ImageDecoder.createSource(requireActivity().contentResolver,ImageURI!!)
                    pickedBitMap = ImageDecoder.decodeBitmap(source)
                    confimationDialog(pickedBitMap)
                }
                else {
                    pickedBitMap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,ImageURI)
                    confimationDialog(pickedBitMap)
                }
            }
        }else if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            val selectedFile = data?.data // The URI with the location of the file
            if (selectedFile != null) {
                if (Build.VERSION.SDK_INT >= 28) {
//                    val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedFile)
//                    pickedBitMap = ImageDecoder.decodeBitmap(source)
                    confimationDialog2(selectedFile)
                }
                else {
//                    pickedBitMap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedFile)
                    confimationDialog2(selectedFile)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //==================================================================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun confimationDialog2(pickedfile: Uri?) {
        val formProject = AlertDialog.Builder(requireContext())
        formProject.setTitle("Confirmation Status")
        val dialogLayout = layoutInflater.inflate(R.layout.activity_formconfirmation, null)

        var file = Uri.fromFile(File(pickedfile.toString()))
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = ("Kirim " + file + " ?")

        formProject.setView(dialogLayout)
        dialogLayout.tempImage.visibility = View.GONE
        formProject.setPositiveButton("Upload") { DialogInterface, i ->
            pushFileSection(pickedfile!!)
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confimationDialog(pickedBitMap: Bitmap?) {
        val formProject = AlertDialog.Builder(requireContext())
        formProject.setTitle("Confirmation Status")
        val dialogLayout = layoutInflater.inflate(R.layout.activity_formconfirmation, null)

        formProject.setView(dialogLayout)
        dialogLayout.tempImage.setImageBitmap(pickedBitMap)
        formProject.setPositiveButton("Upload") { DialogInterface, i ->
            pushFileSection(ImageURI)
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pushFileSection(ImageURI: Uri) {
    lateinit var tempName : String
    lateinit var tempPhotoUrl : String
    lateinit var tempType : String

    //=============================== cari nama dulu
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID)
        docRef2.addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                val tempname: ProjectsCardClass? = snapshot.toObject(ProjectsCardClass::class.java)
                tempName = tempname!!.pName.toString()

                //=============================== push ke storge
                var file = Uri.fromFile(File(ImageURI.toString()))
                val storageReference = storage.reference.child(tempName).child("${file.lastPathSegment}")
                storageReference.putFile(ImageURI)
                    .addOnSuccessListener{

                        Log.d(TAG, "KEUPLOAD MASUK" + it)
                        //=============================== dapetin data untuk firestore
//                        tempPhotoUrl = storageReference.downloadUrl
                        storage.reference.child(tempName).listAll().addOnSuccessListener{
                            it.items.forEach(){
                                if (it == storageReference){
                                    it.downloadUrl.addOnSuccessListener {
                                        tempPhotoUrl = it.toString()

                                        storageReference.metadata
                                            .addOnSuccessListener{
                                                tempType = it.contentType.toString()
                                                //=============================== push datanya ke firestore
                                                pushFile_firestore(tempPhotoUrl, tempType)
                                        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun pushFile_firestore(tempPhotoUrl: String, tempType: String) {
        db = FirebaseFirestore.getInstance()
        val docRef2 = db.collection("projects").document(projectID)

        var time = getdate().toString()
        val Filess = hashMapOf(
            "fileURL" to tempPhotoUrl,
            "fileType" to tempType,
            "fileCreated" to time
        )
        docRef2.collection("FileSection").document(SectionId)
            .collection("Logfiles").add(Filess)
            .addOnSuccessListener{
                Log.d(TAG, "Section File and file added ${it.id}")
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

    private fun getAllFiles() {
        db = FirebaseFirestore.getInstance()
        val docRef = db.collection("projects").document(projectID)
            .collection("FileSection").document(SectionId)
            .collection("Logfiles")
        docRef.addSnapshotListener() { snapshot, e ->
            for (dc in snapshot!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> { fileOnlyAdapter.restart()
                        fileOnlyRecycler?.adapter?.notifyDataSetChanged() }
                    DocumentChange.Type.MODIFIED -> { fileOnlyRecycler?.adapter?.notifyDataSetChanged() }
                    DocumentChange.Type.REMOVED ->{
                        fileOnlyAdapter.restart()
                        fileOnlyRecycler?.adapter?.notifyDataSetChanged() }
                }
            }
            if (snapshot != null) {
                for (d in snapshot) {
                    val Filess : fileOnlyClass? = d.toObject(fileOnlyClass::class.java)
                    if (Filess != null){
                        fileOnlyArrayList.add(Filess)
                    }
                }
                fileOnlyRecycler.adapter = filesOnlyAdapter(fileOnlyArrayList, this)
            } else {
                Log.d(TAG, "Current data: null" + e)
            }
        }
    }
    //==================================================================================================

    private fun findFile(fileURL: String?) {
        db = FirebaseFirestore.getInstance()
        val docRef = db.collection("projects").document(projectID)
        docRef.addSnapshotListener() { snapshot, e ->
            if (snapshot != null) {
                val tempname: ProjectsCardClass? = snapshot.toObject(ProjectsCardClass::class.java)
                // Delete di storage dulu
                val storageRef = storage.reference.child(tempname!!.pName.toString())
                storageRef.listAll().addOnSuccessListener{
                    it.items.forEach(){
                        var tempChild = it.name
                        Log.d(TAG, tempChild)
                        it.downloadUrl.addOnSuccessListener {
                            Log.d(TAG, it.toString() + "$\n" + fileURL)
                            if (it.toString() == fileURL){
                                Log.d("Namanya", "KETEMU URL SAMA")
                                storageRef.child(tempChild).delete()
                            }
                        }
                   }
                    // Delete di firestore
                    val docref2 = docRef.collection("FileSection").document(SectionId).collection("Logfiles")
                    docref2.whereEqualTo("fileURL", fileURL).addSnapshotListener() { snapshot, e ->
                        if (snapshot != null) {
                            for (d in snapshot){
                                Log.d("Namanya", "KETEMU URL SAMA")
                                val temp: fileOnlyClass? = d.toObject(fileOnlyClass::class.java)
                                fileOnlyArrayList.remove(temp)
                                docref2.document(d.id).delete()
                                break
                            }
                        }
                        fileOnlyRecycler.adapter = filesOnlyAdapter(fileOnlyArrayList, this)
                    }
                }
            }
        }
    }

    override fun onItemClick(position: Int, passData: fileOnlyClass) {
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.linear_file_only, null)
        val t2 = dialogLayout.findViewById<TextView>(R.id.filesURL2)

        t2.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onItemLongClick(position: Int, passData: fileOnlyClass) {
        val formProject = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        formProject.setTitle("Confirmation Status")

        val dialogLayout = inflater.inflate(R.layout.activity_formconfirmation, null)

        dialogLayout.findViewById<ImageView>(R.id.tempImage).visibility = View.GONE
        dialogLayout.findViewById<TextView>(R.id.acc_action).text = "Delete attachment?"
        formProject.setView(dialogLayout)
        formProject.setPositiveButton("Delete") { DialogInterface, i ->
            findFile(passData.fileURL)
        }
        formProject.setNegativeButton("Cancel") { DialogInterface, i ->
            Toast.makeText(activity, " ok bos cancel ", Toast.LENGTH_SHORT).show()
        }
        formProject.show()
    }

    companion object{
//        fun downloadFile(
//            context: Context,
//            fileName: String,
//            fileExtension: String,
//            destinationDirectory:
//            url: String?
//        ): Long {
//            val downloadmanager =
//                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            val uri = Uri.parse(url)
//            val request = DownloadManager.Request(uri)
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            request.setDestinationInExternalFilesDir(
//                context,
//                destinationDirectory,
//                fileName + fileExtension
//            )
//            return downloadmanager.enqueue(request)
//        }
    }
}