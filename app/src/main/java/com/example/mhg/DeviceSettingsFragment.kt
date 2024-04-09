package com.example.mhg

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import com.example.mhg.databinding.FragmentDeviceSettingsBinding

class DeviceSettingsFragment : Fragment() {
    lateinit var binding: FragmentDeviceSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDeviceSettingsBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("CommitPrefEdits")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // -----! 다크모드 시작 !-----
        val sharedPref = context?.getSharedPreferences("deviceSettings", Context.MODE_PRIVATE)
        val ModeEditor = sharedPref?.edit()
        val darkmode = sharedPref?.getBoolean("darkMode", false)
        if (darkmode != null) {
            if (darkmode == true) {
                binding.schDeviceSettingDark.isChecked = true
            } else {
                binding.schDeviceSettingDark.isChecked = false
            }
        }
        binding.schDeviceSettingDark.setOnCheckedChangeListener{CompoundButton, onSwtich ->
            if (onSwtich) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                ModeEditor?.putBoolean("darkMode", true) ?: true
                ModeEditor?.apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                ModeEditor?.putBoolean("darkMode", false) ?: false
                ModeEditor?.apply()
            }
        } // -----! 다크모드 끝 !-----

        binding.ibtnDeviceSettingsBack.setOnClickListener {
            if (!it.isClickable) { return@setOnClickListener }
            it.isClickable = false
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flProfile, ProfileFragment())
                commit()
            }
            it.isClickable = true
        }

        // -----! 운동 정보 사용 !-----

    }
}