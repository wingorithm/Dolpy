package com.example.myapplication.home_project_activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import com.example.myapplication.*
import com.example.myapplication.databinding.ActivityActionbarBinding
import com.example.myapplication.databinding.ActivityHomepageBinding
import com.example.myapplication.home_part_fragment.chat
import com.example.myapplication.home_part_fragment.files
import com.example.myapplication.home_part_fragment.peoples
import com.example.myapplication.home_part_fragment.timeline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomePage : AppCompatActivity(){
    private lateinit var binding: ActivityHomepageBinding
    private lateinit var user: FirebaseAuth
    private var db = Firebase.firestore
    private lateinit var currentProject : String
    private lateinit var current_userUID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val projectID = intent.getStringExtra("projectID")
        this.currentProject = intent.getStringExtra("projectName").toString()
        Log.d(TAG, "Masuk ke project ID: $currentProject")

        user = FirebaseAuth.getInstance()
        this.current_userUID = user.currentUser!!.uid.toString()

        // buat ganti layout ke fragment
        replaceFragment(chat())

        //buat action bar
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        getSupportActionBar()!!.setCustomView(R.layout.activity_actionbar)
        val inflater = layoutInflater
        val CurrProject = inflater.inflate(R.layout.activity_actionbar, null)
        CurrProject.findViewById<TextView>(R.id.judul).text = currentProject.toString()
        Toast.makeText(applicationContext, "currentProject", Toast.LENGTH_SHORT).show()
//        getSupportActionBar()!!.setTitle(currentProject)
//        binding_acb.judul.text = currentProject

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.chat -> replaceFragment(chat())
                R.id.timeline -> replaceFragment(timeline())
                R.id.files -> replaceFragment(files())
                R.id.people -> replaceFragment(peoples())

                else -> {

                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val bundle = Bundle()
        val projectID = intent.getStringExtra("projectID")
        bundle.putString("projectID", projectID)
        fragment.arguments = bundle

        val fragmentManager = supportFragmentManager
        fragmentManager.popBackStack();
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    //    Menu selalu ada
    override public fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.fragment_menu, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    //    Menu atas 3 titik
    override public fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

//        if (item.itemId == R.id.signout_opt){
//            //buat signout
//            user.signOut()
//            startActivity(Intent(this, LandingActivity::class.java))
//            finish();
//        }
        if (item.itemId == R.id.refresh){
            finish();
            startActivity(getIntent());
        }
        return true
    }
}