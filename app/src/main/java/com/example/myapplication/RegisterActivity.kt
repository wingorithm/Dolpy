package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val TAG = "FIRESTORE"

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var user: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var current_userUID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = FirebaseAuth.getInstance()


        //--------------------Hide status bar
        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding.toLoginbtn2.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.Registerbtn.setOnClickListener{
            registerUser()
        }
    }

    private fun registerUser(){
        val fName= binding.firstNameRegisForm.text.toString()
        val lName = binding.lastNameRegisForm.text.toString()
        val email = binding.EmailRegisForm.text.toString()
        val password = binding.PasswordRegisForm.text.toString()
        val phoneNum = binding.PhoneRegisFrom.text.toString()
        val userUID = "nanti"

        if (email.isNotEmpty() && password.isNotEmpty() && fName.isNotEmpty() && lName.isNotEmpty() && phoneNum.isNotEmpty() && userUID.isNotEmpty() && password.length > 7 && phoneNum.length > 7){
            saveFireStore(fName, lName, email, password, phoneNum, userUID)

            user.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity()){
                if(it.isSuccessful){
                    Toast.makeText(
                    this,
                    "User Added Successfully",
                    Toast.LENGTH_SHORT).show()
                    user.signOut()

                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(
                    this,
                    it.exception!!.message,
                    Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            if (fName.isEmpty() || lName.isEmpty()){
                binding.errormsg.text = "your NAME is empty"
                binding.errormsg.visibility = View.VISIBLE
            }else if (phoneNum.isEmpty() || phoneNum.length < 8){
                binding.errormsg.text = "your PHONE NUMBER is empty"
                binding.errormsg.visibility = View.VISIBLE
            }else if (email.isEmpty()){
                binding.errormsg.text = "your EMAIL is empty"
                binding.errormsg.visibility = View.VISIBLE
            }else if (password.isEmpty() || password.length < 8){
                binding.errormsg.text = "your PASSWORD is empty"
                binding.errormsg.visibility = View.VISIBLE
            }
        }
    }


    private fun saveFireStore(firstname: String, lastname: String, email: String, pass: String, pNUm: String, userUID : String) {
        Log.d(TAG, "masuk sini gk om")
        val userr = hashMapOf(
            "fName" to firstname,
            "lName" to lastname,
            "email" to email,
            "password" to pass,
            "phoneNum" to pNUm,
            "userUID" to userUID,
            "profilePic" to "null",
            "birthday" to "null"
        )

        db.collection("users")
            .add(userr)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }
}