package com.example.mhg.VO

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mhg.MainActivity
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class UserViewModel: ViewModel() {
    val User = MutableLiveData(JSONObject())

    val UserHistory = MutableLiveData(JSONObject())


    // 즐겨찾기
    val UserBasket = MutableLiveData<ArrayList<HomeRVBeginnerDataClass>>()
    init {
        User.value = JSONObject()
        UserBasket.value = ArrayList()
    }

    fun addItem(item : HomeRVBeginnerDataClass) {
        UserBasket.value?.add(item)
        UserBasket.value = UserBasket.value
    }
    fun deleteItem(item: HomeRVBeginnerDataClass) {
        UserBasket.value?.remove(item)
        UserBasket.value = UserBasket.value
    }

    val idCondition = MutableLiveData(false)
    val nameCondition = MutableLiveData(false)
    val mobileCondition = MutableLiveData(false)
    val pwCondition = MutableLiveData(false)
    val pwCompare = MutableLiveData(false)
    val emailCondition = MutableLiveData(false)
    val mobileAuthCondition = MutableLiveData(false)
}
