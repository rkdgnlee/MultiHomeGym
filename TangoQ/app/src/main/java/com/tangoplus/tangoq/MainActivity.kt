package com.tangoplus.tangoq

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.tangoplus.tangoq.Fragment.FavoriteFragment
import com.tangoplus.tangoq.Fragment.MainFragment
import com.tangoplus.tangoq.Fragment.ProfileFragment
import com.tangoplus.tangoq.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.bnbMain.itemIconTintList = null
        binding.bnbMain.isItemActiveIndicatorEnabled = false
        binding.bnbMain.setOnItemSelectedListener {
            when(it.itemId) {
                // ---- fragment 경로 지정 시작 ----
                R.id.main -> setCurrentFragment(MainFragment())
//                R.id.exercise -> setCurrentFragment(ReportFragment())
//                R.id.measure -> setCurrentFragment(PickFragment())
                R.id.favorite -> setCurrentFragment(FavoriteFragment())
                R.id.profile -> setCurrentFragment(ProfileFragment())
            }
            true
        }
        binding.bnbMain.setOnItemReselectedListener {
            when(it.itemId) {
                // ---- fragment 경로 지정 시작 ----
                R.id.main -> {}
                R.id.exercise -> {}
                R.id.measure -> {}
                R.id.favorite -> {}
                R.id.profile -> {}
            }
        }
    }
    fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flMain, fragment)
//            addToBackStack(null)
            commit()
        }
    private val onBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {

        }
    }
}

