package com.tangoplus.tangoq

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.tangoplus.tangoq.Object.Singleton_t_user
import com.tangoplus.tangoq.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    lateinit var binding : ActivitySplashBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private val PERMISSION_REQUEST_CODE = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val t_userData = Singleton_t_user.getInstance(this)
        val Handler = Handler(Looper.getMainLooper())
        Handler.postDelayed({
            IntroInit()
        }, 2000)


    }

    private fun IntroInit() {
        val intent = Intent(this@SplashActivity, IntroActivity::class.java)
        startActivity(intent)
        finish()
    }
}