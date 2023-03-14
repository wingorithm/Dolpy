package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import java.util.Timer
import kotlin.concurrent.timerTask

class LandingActivity : AppCompatActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.activity_landing)
         supportActionBar?.hide()

//         Hide status bar
         window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
         val isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
             .getBoolean("isFirstRun", true)

         if (!isFirstRun) {
             startActivity(Intent(this, LoginActivity::class.java))
             finish()
         }

         getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
             .putBoolean("isFirstRun", false).commit()

         goToLogin()
     }

    @SuppressLint("MissingInflatedId")
    private fun goToLogin(){
        if (!isDestroyed){
            val pindah = Intent(this, getStartedActivity::class.java)
            val tmtask = timerTask {
                if (!isDestroyed) {
                    startActivity(pindah)
                    finish()
                }
            }

            val timer = Timer()
            timer.schedule(tmtask, 5000)
        }
    }

}