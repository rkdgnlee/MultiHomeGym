package com.tangoplus.tangoq.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tangoplus.tangoq.adapter.FavoriteRVAdapter
import com.tangoplus.tangoq.listener.OnFavoriteDetailClickListener
import com.tangoplus.tangoq.`object`.NetworkExerciseService
import com.tangoplus.tangoq.`object`.NetworkExerciseService.fetchFavoriteItemsJsonByMobile
import com.tangoplus.tangoq.`object`.NetworkExerciseService.jsonToExerciseVO
import com.tangoplus.tangoq.`object`.Singleton_t_user
import com.tangoplus.tangoq.R
import com.tangoplus.tangoq.data.ExerciseVO
import com.tangoplus.tangoq.data.ExerciseViewModel
import com.tangoplus.tangoq.data.FavoriteItemVO
import com.tangoplus.tangoq.databinding.FragmentFavoriteBinding
import com.tangoplus.tangoq.dialog.FavoriteAddDialogFragment
import com.tangoplus.tangoq.dialog.FeedbackDialogFragment
import kotlinx.coroutines.launch
import org.json.JSONObject


class FavoriteFragment : Fragment(), OnFavoriteDetailClickListener {
    lateinit var binding : FragmentFavoriteBinding
    val viewModel : ExerciseViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater)
        binding.sflFV.startShimmer()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ------! singleton에서 전화번호 가져오기 시작 !------
        val t_userData = Singleton_t_user.getInstance(requireContext()).jsonObject?.optJSONObject("data")
        val user_mobile = t_userData?.optString("user_mobile")

        binding.tvFrTitle.text = "${t_userData?.optString("user_name")} 님의\n플레이리스트 목록"

        // ------! 즐겨 찾기 1개 추가 시작 !------
        binding.btnFrAdd.setOnClickListener {
            val dialog = FavoriteAddDialogFragment()
            dialog.show(requireActivity().supportFragmentManager, "FavoriteAddDialogFragment")
        }
        // ------! 즐겨 찾기 1개 추가 끝 !------

//        val encodedUserMobile = URLEncoder.encode(user_mobile, "UTF-8")
        lifecycleScope.launch {

            // ------! 핸드폰 번호로 PickItems 가져오기 시작 !------
            val pickList = fetchFavoriteItemsJsonByMobile(getString(R.string.IP_ADDRESS_t_favorite), user_mobile.toString()) // user_mobile 넣기

            // ------! list관리 시작 !------
            if (pickList != null) {
                val imgList = mutableListOf<String>()
                viewModel.favoriteList.value?.clear()
                viewModel.exerciseUnits.value?.clear()
                for (i in 0 until pickList.length()) {
                    // 일단 favorite 1
                    val favoriteItem = FavoriteItemVO(
                        imgThumbnailList = imgList,
                        favoriteSn = pickList.getJSONObject(i).optInt("favorite_sn"),
                        favoriteName = pickList.getJSONObject(i).optString("favorite_name"),
                        favoriteRegDate = pickList.getJSONObject(i).optString("reg_date"),
                        favoriteExplain = pickList.getJSONObject(i).optString("favorite_description"),
                        favoriteTotalCount = (if ((pickList.getJSONObject(i).optString("exercise_description_ids")) == "null") 0 else (pickList.getJSONObject(i).optString("exercise_description_ids").split(",").size)).toString(),
                        favoriteDisclosure = "",
                        exercises = mutableListOf()
                    ) // 각각의 FavoriteItemVO 만들고,  그후 추가적으로 조회해서 썸네일 넣기.
                    val exerciseItemBySn = NetworkExerciseService.fetchFavoriteItemJsonBySn(getString(R.string.IP_ADDRESS_t_favorite),
                        favoriteItem.favoriteSn.toString()
                    )
                    val exerciseUnits = mutableListOf<ExerciseVO>()
                    if (exerciseItemBySn != null) {
                        val exercises = exerciseItemBySn.optJSONArray("exercise_detail_data")
                        if (exercises != null) {
                            for (i in 0 until exercises.length()) {
                                exerciseUnits.add(jsonToExerciseVO(exercises.get(i) as JSONObject))
                            }
                        }
                        favoriteItem.exercises = exerciseUnits
                    }
                    var time = 0
                    for (i in 0 until  favoriteItem.exercises?.size!!) {
                        time += ( favoriteItem.exercises!![i].videoTime!!.toInt())
                    }
                    favoriteItem.favoriteTotalTime = (time/60).toString()
                    viewModel.favoriteList.value?.add(favoriteItem) // 썸네일, 시리얼넘버, 이름까지 포함한 dataclass로 만든 favoriteVO형식의 리스트
//                    if ( favoriteItem.exercises?.size!! >= 4) {
//
//                        for (j in 0 until 4) {
//                            imgList.add( favoriteItem.exercises!![j].imgUrl.toString())
//                        }
//                    }


                    // 일단 운동은 비워놓고, detail에서 넣음
                }
            }


            viewModel.favoriteList.observe(viewLifecycleOwner) { jsonArray ->
//                 아무것도 없을 때 나오는 캐릭터
                if (jsonArray.isEmpty()) {
//                    binding.sflFV.stopShimmer()
//                    binding.sflFV.visibility = View.GONE
//                    binding.ivPickNull.visibility = View.VISIBLE
                } else {
                    binding.sflFV.stopShimmer()
                    binding.sflFV.visibility = View.GONE

                }
            } // -----! appClass list관리 끝 !-----
            val FavoriteRVAdapter = FavoriteRVAdapter(viewModel.favoriteList.value!!, this@FavoriteFragment, this@FavoriteFragment)
            binding.rvFv.adapter = FavoriteRVAdapter
            val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.rvFv.layoutManager = linearLayoutManager
            FavoriteRVAdapter.notifyDataSetChanged()
            Log.v("리스트", "${viewModel.favoriteList.value!!}")


//            binding.btnFavoriteadd.setOnClickListener {
//                viewModel.exerciseUnits.value?.clear()
//                requireActivity().supportFragmentManager.beginTransaction().apply {
//                    setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right)
//                    replace(R.id.flPick, PickAddFragment())
//                    addToBackStack(null)
//                    commit()
//                }
            }
            // ------! 핸드폰 번호로 PickItems 가져오기 끝 !------
        }

    override fun onFavoriteClick(title: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flMain, FavoriteDetailFragment.newInstance(title))
            addToBackStack(null)
            commit()

        }
    }
}
