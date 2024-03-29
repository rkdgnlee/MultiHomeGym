package com.example.mhg

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mhg.Adapter.HomeVerticalRecyclerViewAdapter
import com.example.mhg.VO.ExerciseViewModel
import com.example.mhg.VO.PickItemVO
import com.example.mhg.databinding.FragmentPickAddBinding
import com.example.mhg.`object`.Singleton_t_user
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject


class PickAddFragment : Fragment() {
   lateinit var binding : FragmentPickAddBinding
    val viewModel: ExerciseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPickAddBinding.inflate(inflater)
        binding.clPickAddUnlisted.visibility = View.GONE
        binding.clPickAddPrivate.visibility = View.GONE

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val t_userData = Singleton_t_user.getInstance(requireContext())
        binding.nsvPickAdd.isNestedScrollingEnabled = true
        binding.rvPickadd.isNestedScrollingEnabled = false
        binding.rvPickadd.overScrollMode = View.OVER_SCROLL_NEVER
        binding.btnPickAddGoBasket.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right)
                replace(R.id.flPick, PickBasketFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
        // -----! viewmodel의 추가되는 값 관찰하기 !-----
        viewModel.exerciseUnits.observe(viewLifecycleOwner) { basketUnits ->
            val adapter = HomeVerticalRecyclerViewAdapter(basketUnits, "add")
            adapter.verticalList = basketUnits

            val linearLayoutManager2 =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.rvPickadd.layoutManager = linearLayoutManager2

            // -----! item에 swipe 및 dragNdrop 연결 !-----
            val callback = ItemTouchCallback(adapter).apply {
//                setClamp(260f)
//                removePreviousClamp(binding.rvPickadd)
            }

            val touchHelper = ItemTouchHelper(callback)
            touchHelper.attachToRecyclerView(binding.rvPickadd)
            binding.rvPickadd.adapter = adapter
            adapter.startDrag(object: HomeVerticalRecyclerViewAdapter.OnStartDragListener {
                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                    touchHelper.startDrag(viewHolder)
                }
            })
            adapter.notifyDataSetChanged()
            // -----! swipe, drag 연동 !-----
        }

        binding.btnPickAddExercise.setOnClickListener {
            viewModel.pickItem.value
            // -----! 즐겨찾기 하나 만들기 시작 !-----
            val pickItemVO = PickItemVO(
                pickName = binding.etPickAddName.text.toString(),
                pickExplainTitle = binding.etPickAddExplainTitle.text.toString(),
                pickExplain = binding.etPickAddExplain.text.toString(),
                pickDisclosure = when {
                    binding.clPickAddPublic.visibility == View.VISIBLE -> "public"
                    binding.clPickAddUnlisted.visibility == View.VISIBLE -> "unlisted"
                    else -> "private"
                },
                exercises = viewModel.exerciseUnits.value
            )
            // 나중에Detail에서 꺼내볼 vm 만들기
            viewModel.pickItems.value?.add(pickItemVO)
            // -----! json으로 형식을 변환 !-----
            val jsonObj = JSONObject(Gson().toJson(pickItemVO))
            Log.w("즐겨찾기 하나 만들기", "${jsonObj.optString("pickName")}, ${jsonObj.optString("pickExplain")}, ${jsonObj.optString("exercises")}")
            viewModel.pickItem.value = jsonObj


            // TODO 기능 구현을 위한 하드 코딩 (지워야 함)
            viewModel.addPick(jsonObj.optString("pickName"), "4")
            Log.w("뷰모델picklist", "${viewModel.pickList.value}")
            // -----! 즐겨찾기 하나 넣을 때, key값 = basket_name으로 !-----
//            lifecycleScope.launch {
//                 fetchPickItemInsertJson(getString(R.string.IP_ADDRESS_t_Exercise_Description), jsonObj_.toString()) {
//                // -----! 즐겨찾기 리스트에 업데이트 시작 !-----
//                // TODO 즐겨찾기가 추가되면서 CALLBACK으로 해당 내용다시 VIEWMODEL에 담기. PICKLIST로
//            fetchPickListJsonById(getString(R.string.IP_ADDRESS_t_Exercise_Description), t_userData.jsonObject?.getString("user_mobile").toString())


                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flPick, PickDetailFragment.newInstance(viewModel.pickItem.value!!.optString("pickName")))
                    commit()
                }
//            }
//                // -----! 즐겨찾기 하나 만들기 끝 !-----
//            }












            // -----! 운동 만들기 버튼 클릭 끝 !-----
        }

        // -----! 공개 설정 코드 시작 !-----
        var rangeExpanded = false
        binding.clPickAddPublic.setOnClickListener{
            if (!rangeExpanded) {
                binding.clPickAddUnlisted.visibility = View.VISIBLE
                binding.clPickAddPrivate.visibility = View.VISIBLE
                rangeExpanded = true
            } else {
                binding.clPickAddUnlisted.visibility = View.GONE
                binding.clPickAddPrivate.visibility = View.GONE
                rangeExpanded = false
            }
        }

        binding.clPickAddUnlisted.setOnClickListener{
            if (!rangeExpanded) {
                binding.clPickAddPublic.visibility = View.VISIBLE
                binding.clPickAddPrivate.visibility = View.VISIBLE
                rangeExpanded = true
            } else {
                binding.clPickAddPublic.visibility = View.GONE
                binding.clPickAddPrivate.visibility = View.GONE
                rangeExpanded = false
            }
        }
        binding.clPickAddPrivate.setOnClickListener{
            if (!rangeExpanded) {
                binding.clPickAddPublic.visibility = View.VISIBLE
                binding.clPickAddUnlisted.visibility = View.VISIBLE
                rangeExpanded = true
            } else {
                binding.clPickAddPublic.visibility = View.GONE
                binding.clPickAddUnlisted.visibility = View.GONE
                rangeExpanded = false
            }
        }
        // -----! 공개 설정 코드 끝 !-----

    }
}