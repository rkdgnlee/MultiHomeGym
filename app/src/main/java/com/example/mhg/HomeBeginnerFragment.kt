package com.example.mhg

import android.R
import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mhg.Adapter.HomeHorizontalRecyclerViewAdapter
import com.example.mhg.Adapter.HomeVerticalRecyclerViewAdapter

import com.example.mhg.VO.ExerciseVO
import com.example.mhg.VO.ExerciseViewModel
import com.example.mhg.VO.UserViewModel
import com.example.mhg.databinding.FragmentHomeBeginnerBinding
import com.example.mhg.`object`.NetworkExerciseService.fetchExerciseJson
import com.example.mhg.`object`.NetworkUserService
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch


class HomeBeginnerFragment : Fragment() {
    lateinit var binding: FragmentHomeBeginnerBinding

    lateinit var ExerciseList : MutableList<ExerciseVO>
    private val exerciseTypeList = listOf("목관절", "어깨", "팔꿉", "손목", "몸통전면(복부)", "몸통 후면(척추)", "몸통 코어", "엉덩", "무릎", "발목", "유산소")
    val viewModel : UserViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBeginnerBinding.inflate(layoutInflater)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ----- 로그인 시 변경 할 부분 시작 -----
        binding.tvHomeWeight.text
        binding.tvHomeAchieve.text
        binding.tvHomeGoal.text
        // ----- 로그인 시 변경 할 부분 끝 -----


        lifecycleScope.launch {

            // -----! db에서 받아서 뿌려주기 시작 !-----
            val responseArrayList = fetchExerciseJson(getString(com.example.mhg.R.string.IP_ADDRESS_t_Exercise_Description))
            try {
                // -----! horizontal 어댑터 시작 !-----
                val adapter = HomeHorizontalRecyclerViewAdapter(
                    this@HomeBeginnerFragment,
                    exerciseTypeList
                )
                adapter.routineList = exerciseTypeList.slice(0..3)
                binding.rvHomeBeginnerHorizontal.adapter = adapter
                val linearlayoutmanager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.rvHomeBeginnerHorizontal.layoutManager = linearlayoutmanager
                // -----! horizontal 어댑터 끝 !-----

                // -----! vertical 어댑터 시작 !-----
                val verticalDataList = responseArrayList.toMutableList()


                val adapter2 = HomeVerticalRecyclerViewAdapter(verticalDataList,"home" )
                adapter2.verticalList = verticalDataList
                binding.rvHomeBeginnerVertical.adapter = adapter2
                val linearLayoutManager2 =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                binding.rvHomeBeginnerVertical.layoutManager = linearLayoutManager2
                // -----! vertical 어댑터 끝 !-----


                // -----! db에서 받아서 뿌려주기 끝 !-----
                binding.tvExerciseCount.text = verticalDataList.size.toString()
                binding.nsv.isNestedScrollingEnabled = false
                binding.rvHomeBeginnerVertical.isNestedScrollingEnabled = false
                binding.rvHomeBeginnerVertical.overScrollMode = 0

                // -----! 메인 유저 체중/달성률/업적 control 시작 !-----
                binding.tvHomeWeight.text = viewModel.User.value?.optString("user_weight")

                binding.tvHomeGoal


                // -----! 메인 유저 별 체중 달성률 업적 control 끝 !-----

                // ----- autoCompleteTextView를 통해 sort 하는 코드 시작 -----
                val sort_list = listOf("인기순", "조회순", "최신순", "오래된순")
                val adapter3 = ArrayAdapter(
                    requireContext(),
                    R.layout.simple_dropdown_item_1line,
                    sort_list
                )
                binding.actHomeBeginner.setAdapter(adapter3)
                binding.actHomeBeginner.setText(sort_list.firstOrNull(), false)

                binding.actHomeBeginner.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        when (s.toString()) {
                            "인기순" -> {
                                // 추후에 이런 필터링을 거치려면, DATA를 받아올 때 운동이 있을 때, 수정날짜? 갱신날짜같은 걸 넣어서 받아올 때, 그것만 일주일마다 갱신되게? 유지보수 하면 될 듯?
                                verticalDataList.sortByDescending { it.exerciseName }
                            }
                            "조회순" -> {
                                verticalDataList.sortByDescending { it.videoTime }
                            }
                            "최신순" -> {
                                verticalDataList.sortBy { it.relatedMuscle }
                            }
                            "오래된순" -> {

                            }
                        }
                        adapter2.notifyDataSetChanged()
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error storing exercises", e)
            }
            // ---- vertical RV에 들어갈 데이터 담는 공간 시작 ----

            // ----- autoCompleteTextView를 통해 sort 하는 코드 끝 -----

            binding.tabRoutine.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> {
                            val adapter = HomeHorizontalRecyclerViewAdapter(
                                this@HomeBeginnerFragment,
                                exerciseTypeList.slice(0..3)
                            )
                            binding.rvHomeBeginnerHorizontal.adapter = adapter
                        }

                        1 -> {
                            val adapter = HomeHorizontalRecyclerViewAdapter(
                                this@HomeBeginnerFragment,
                                exerciseTypeList.slice(4..6)
                            )
                            binding.rvHomeBeginnerHorizontal.adapter = adapter
                        }

                        2 -> {
                            val adapter = HomeHorizontalRecyclerViewAdapter(
                                this@HomeBeginnerFragment,
                                exerciseTypeList.slice(7..9)
                            )
                            binding.rvHomeBeginnerHorizontal.adapter = adapter
                        }

                        3 -> {
                            val adapter = HomeHorizontalRecyclerViewAdapter(
                                this@HomeBeginnerFragment,
                                exerciseTypeList.slice(10..10)
                            )
                            binding.rvHomeBeginnerHorizontal.adapter = adapter
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
        }
}

