package com.tangoplus.tangoq.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject

class UserViewModel: ViewModel() {
    val User = MutableLiveData(JSONObject())

    val UserHistory = MutableLiveData(JSONObject())

    init {
        User.value = JSONObject()
    }


    val idCondition = MutableLiveData(false)
    val nameCondition = MutableLiveData(false)
    val mobileCondition = MutableLiveData(false)
    val pwCondition = MutableLiveData(false)
    val pwCompare = MutableLiveData(false)
    val emailCondition = MutableLiveData(false)
    val mobileAuthCondition = MutableLiveData(false)
}