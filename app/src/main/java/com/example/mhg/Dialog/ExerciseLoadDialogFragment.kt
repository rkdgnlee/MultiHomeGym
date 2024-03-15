package com.example.mhg.Dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.mhg.PersonalSetupActivity
import com.example.mhg.R
import com.example.mhg.Room.ExerciseDao
import com.example.mhg.Room.ExerciseDatabase
import com.example.mhg.Room.ExerciseRepository
import com.example.mhg.databinding.DialogfragmentExerciseLoadBinding
import com.example.mhg.`object`.NetworkService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ExerciseLoadDialogFragment : DialogFragment() {
    lateinit var binding: DialogfragmentExerciseLoadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true

    }
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogfragmentExerciseLoadBinding.inflate(inflater)
        // ----! local Room에 연결 코루틴 !-----
        val db = ExerciseDatabase.getInstance(requireContext())
        lifecycleScope.launch {
            val jsonArr = NetworkService.fetchExerciseJson(getString(R.string.IP_ADDRESS_t_Exercise_Description))
            Log.w(TAG, "jsonArr: $jsonArr")
//            if (jsonObj != null) {
//                ExerciseRepository(db.ExerciseDao(), NetworkService).StoreExercises(jsonObj)
//                Log.w(TAG + "db내용", "${ExerciseRepository(db.ExerciseDao(), NetworkService).getExercises()}")
//            }
            if (jsonArr != null) {
                try {
                    ExerciseRepository(db.ExerciseDao(), NetworkService).StoreExercises(jsonArr)
                    Log.w(TAG + "db내용", "${ExerciseRepository(db.ExerciseDao(), NetworkService).getExercises()}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error storing exercises", e)
                }
            }
            // -----! 통신완료된 상태에서 메인스레드 접근 !-----
            withContext(Dispatchers.Main) {
                binding.tvExerciseLoad.text = "완료됐습니다!\n페이지를 이동할게요!"
                binding.progressBar3.progress = binding.progressBar3.max
                delay(500)
                val intent = Intent(requireContext(), PersonalSetupActivity::class.java)
                startActivity(intent)
                dismiss()
            }
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        initializeDialogOptions()
    }

    private fun initializeDialogOptions() {
        val darkTransparentBlack = Color.argb((255 * 0.6).toInt(), 0, 0, 0)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(darkTransparentBlack))
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setDimAmount(0.4f)
        isCancelable = false
    }

}