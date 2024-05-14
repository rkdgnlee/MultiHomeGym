package com.tangoplus.tangoq.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.tangoplus.tangoq.adapter.ProfileRVAdapter
import com.tangoplus.tangoq.listener.BooleanClickListener
import com.tangoplus.tangoq.`object`.Singleton_t_user
import com.tangoplus.tangoq.databinding.FragmentProfileBinding
import java.util.Calendar
import java.util.TimeZone


class ProfileFragment : Fragment(), BooleanClickListener {
    lateinit var binding : FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------! profile의 나이, 몸무게, 키  설정 코드 시작 !------
        val t_userdata = Singleton_t_user.getInstance(requireContext())
        val userJson= t_userdata.jsonObject?.getJSONObject("data")
        Log.v("Singleton>Profile", "${userJson}")
        binding.tvPfName.text = userJson?.optString("user_name")
        binding.tvPfHeight.text = userJson?.optString("user_height")
        binding.tvPfWeight.text = userJson?.optString("user_weight")
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        binding.tvPfAge.text = (c.get(Calendar.YEAR) - userJson?.optString("user_birthday")?.substring(0, 4)!!.toInt()).toString()
        // ----- 이미지 로드 시작 -----
        val sharedPreferences = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val imageUri = sharedPreferences.getString("imageUri", null)
        if (imageUri != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(16))))
                    .into(binding.civPf)
            }
        }
        val profilemenulist = mutableListOf(
            "내정보",
            "다크 모드",
            "연동 관리",
            "자주 묻는 질문",
            "문의하기",
            "공지사항",
            "앱 버전",
            "개인정보 처리방침",
            "서비스 이용약관",
            "로그아웃",
            "회원탈퇴",
        )
        setAdpater(profilemenulist.subList(0,3), binding.rvPfNormal,0)
        setAdpater(profilemenulist.subList(3,6), binding.rvPfHelp, 1)
        setAdpater(profilemenulist.subList(6, profilemenulist.size), binding.rvPfDetail, 2)
//        binding.ibtnPfEdit.setOnClickListener {
//            when {
//                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
//                    navigateGallery()
//                }
//                else -> requestPermissions(
//                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
//                    1000
//                )
//            }
//        }
    }

    fun setAdpater(list: MutableList<String>, rv: RecyclerView, index: Int) {
        if (index != 0 ) {
            val adapter = ProfileRVAdapter(this@ProfileFragment, this@ProfileFragment, false)
            adapter.profilemenulist = list
            rv.adapter = adapter
            rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        } else {
            val adapter = ProfileRVAdapter(this@ProfileFragment, this@ProfileFragment, true)
            adapter.profilemenulist = list
            rv.adapter = adapter
            rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    navigateGallery()
                else
                    Toast.makeText(requireContext(), "권한 설정을 허용해 주십시오", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun navigateGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)

    }
    @SuppressLint("CheckResult")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK)
            return
        when (requestCode) {
            2000 -> {
                val selectedImageUri : Uri? = data?.data
                if (selectedImageUri != null) {
                    val sharedPreferences = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("imageUri", selectedImageUri.toString())
                    editor.apply()
                    Glide.with(this)
                        .load(selectedImageUri)
                        .apply(RequestOptions.bitmapTransform(MultiTransformation(CenterCrop(), RoundedCorners(16))))
                        .into(binding.civPf)
                } else {
                    Toast.makeText(requireContext(), "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(requireContext(), "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(requireContext())
            .setTitle("권한이 필요합니다.")
            .setMessage("프로필 이미지를 바꾸기 위해서는 갤러리 접근 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    override fun onSwitchChanged(isChecked: Boolean) {
        val sharedPref = requireActivity().getSharedPreferences("deviceSettings", Context.MODE_PRIVATE)
        val modeEditor = sharedPref?.edit()
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            modeEditor?.putBoolean("darkMode", true) ?: true
            modeEditor?.apply()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            modeEditor?.putBoolean("darkMode", false) ?: false
            modeEditor?.apply()
        }
    }

}