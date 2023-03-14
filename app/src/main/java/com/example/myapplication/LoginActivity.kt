package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.project_activity.ProjectsHomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var user: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance()

        //--------------------Hide status bar
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        checkuserSedangLogIn()

        binding.Loginbtn.setOnClickListener {
            loginUser()
        }

        binding.toRegisterbtn.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun checkuserSedangLogIn(){
        if(user.currentUser != null){
            startActivity(Intent(this, ProjectsHomeActivity::class.java))
            finish()
        }
    }

    private fun loginUser(){
        val email = binding.EmailForm.text.toString()
        val password = binding.PasswordForm.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()){
            user.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity()) { it2 ->
                if (it2.isSuccessful) {
                    if (user.currentUser != null){

                        db.collection("users")
                            .whereEqualTo("userUID", "nanti")
                            .get()
                            .addOnSuccessListener { documents ->
                                lateinit var tempDoc : String
                                for (document in documents) {
                                    tempDoc = document.id
                                    Log.d(TAG, "${document.id} => ${document.data}")
                                    db.collection("users").document(tempDoc).update("userUID", user.currentUser!!.uid)
                                    Log.d(TAG, "${tempDoc} udah diganti sama : ${user.currentUser!!.uid}")
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting documents: ", exception)
                            }
                    }
                    startActivity(Intent(this, ProjectsHomeActivity::class.java))
                    finish()

                }else{
                    binding.errormsglog.text = "email & password not registered"
                    binding.errormsglog.visibility = View.VISIBLE
                }
            }
        }else{
            if(email.isEmpty()){
                binding.errormsglog.text = "your EMAIL is empty"
                binding.errormsglog.visibility = View.VISIBLE
            }else if (password.isEmpty()){
                binding.errormsglog.text = "your password is empty"
                binding.errormsglog.visibility = View.VISIBLE
            }
        }
    }
}