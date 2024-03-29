package com.example.mhg.`object`

import android.content.ContentValues.TAG
import android.util.Log
import com.example.mhg.VO.ExerciseVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object NetworkService{
    // TODO 매니저님이 짜준 로직 대로, JSON, METHOD 바꿔야함
    fun fetchUserINSERTJson(myUrl : String, json: String, callback: () -> Unit){
        val client = OkHttpClient()
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url("${myUrl}create.php")
            .post(body) // post방식으로 insert 들어감
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("$TAG, 응답실패", "Failed to execute request!")
            }
            override fun onResponse(call: Call, response: Response)  {
                val responseBody = response.body?.string()
                Log.e("$TAG, 응답성공", "$responseBody")
                callback()
            }
        })
    }
    fun fetchUserUPDATEJson(myUrl : String, json: String, user_mobile:String, callback: () -> Unit) {
        val client = OkHttpClient()
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url("${myUrl}update.php?user_mobile=$user_mobile")
            .patch(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("$TAG, 응답실패", "Failed to execute request!")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.e("$TAG, 응답성공", "$responseBody")
                callback()
            }
        })
    }

    fun fetchUserDeleteJson(myUrl : String, user_mobile:String, callback: () -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${myUrl}delete.php?user_mobile=$user_mobile")
            .delete()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("$TAG, 응답실패", "Failed to execute request!")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.e("$TAG, 응답성공", "$responseBody")
                callback()
            }
        })
    }
    // 전체 조회
    suspend fun fetchExerciseJson(myUrl: String): List<ExerciseVO> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${myUrl}read.php")
            .get()
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.e("OKHTTP3/ExerciseFetch", "Success to execute request!: $responseBody")
                val jsonObj__ = responseBody?.let { JSONObject(it) }

                jsonObj__?.optJSONArray("data")
                val exerciseDataList = mutableListOf<ExerciseVO>()
                val jsonArr = jsonObj__?.optJSONArray("data")
                if (jsonArr != null) {
                    for (i in 0 until jsonArr.length()) {
                        val jsonObject = jsonArr.getJSONObject(i)
                        val exerciseData = ExerciseVO(
                            exerciseName = jsonObject.optString("exercise_name"),
                            exerciseDescription = jsonObject.getString("exercise_description"),
                            relatedJoint = jsonObject.getString("related_joint"),
                            relatedMuscle = jsonObject.getString("related_muscle"),
                            relatedSymptom = jsonObject.getString("related_symptom"),
                            exerciseStage = jsonObject.getString("exercise_stage"),
                            exerciseFequency = jsonObject.getString("exercise_frequency"),
                            exerciseIntensity = jsonObject.getString("exercise_intensity"),
                            exerciseInitialPosture = jsonObject.getString("exercise_initial_posture"),
                            exerciseMethod = jsonObject.getString("exercise_method"),
                            exerciseCaution = jsonObject.getString("exercise_caution"),
                            videoAlternativeName = jsonObject.getString("video_alternative_name"),
                            videoFilepath = jsonObject.getString("video_filepath"),
                            videoTime = jsonObject.getString("video_time"),
                            exerciseTypeId = jsonObject.getString("exercise_type_id"),
                            exerciseTypeName = jsonObject.getString("exercise_type_name")
                        )
                        exerciseDataList.add(exerciseData)
                    }
                }
                exerciseDataList
            }
        }
    }
    /// type 별 조회
    suspend fun fetchExerciseJsonByType(myUrl: String, id: String) : MutableList<ExerciseVO> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${myUrl}read.php?exercise_type_id=$id")
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.e("OKHTTP3/ExerciseFetch", "Success to execute request!: $responseBody")
                val jsonObj__ = responseBody?.let {JSONObject(it)}
                val exerciseDataList = mutableListOf<ExerciseVO>()
                val jsonArr = jsonObj__?.optJSONArray("data")
                if (jsonArr != null) {
                    for (i in 0 until jsonArr.length()) {
                        val jsonObject = jsonArr.getJSONObject(i)
                        val exerciseData = ExerciseVO(
                            exerciseName = jsonObject.optString("exercise_name"),
                            exerciseDescription = jsonObject.getString("exercise_description"),
                            relatedJoint = jsonObject.getString("related_joint"),
                            relatedMuscle = jsonObject.getString("related_muscle"),
                            relatedSymptom = jsonObject.getString("related_symptom"),
                            exerciseStage = jsonObject.getString("exercise_stage"),
                            exerciseFequency = jsonObject.getString("exercise_frequency"),
                            exerciseIntensity = jsonObject.getString("exercise_intensity"),
                            exerciseInitialPosture = jsonObject.getString("exercise_initial_posture"),
                            exerciseMethod = jsonObject.getString("exercise_method"),
                            exerciseCaution = jsonObject.getString("exercise_caution"),
                            videoAlternativeName = jsonObject.getString("video_alternative_name"),
                            videoFilepath = jsonObject.getString("video_filepath"),
                            videoTime = jsonObject.getString("video_time"),
                            exerciseTypeId = jsonObject.getString("exercise_type_id"),
                            exerciseTypeName = jsonObject.getString("exercise_type_name")
                        )
                        exerciseDataList.add(exerciseData)
                    }
                }
                exerciseDataList
                }
        }
    }
    // 즐겨찾기 넣기
    fun fetchPickItemInsertJson(myUrl : String, json: String, callback: () -> Unit) {
        val client = OkHttpClient()
        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
        val request = Request.Builder()
            .url("${myUrl}create.php")
            .post(body) // post방식으로 insert 들어감
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("$TAG, 응답실패", "Failed to execute request!")
            }
            override fun onResponse(call: Call, response: Response)  {
                val responseBody = response.body?.string()
                Log.e("$TAG, 응답성공", "$responseBody")
                callback()
            }
        })
    }

    // 즐겨찾기 목록 조회
    suspend fun fetchPickListJsonById(myUrl: String, id: String): MutableList<String> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${myUrl}read.php?user_id=$id")
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use {response ->
                val responseBody = response.body?.string()
                Log.e("OKHTTP3/picklistfetch", "Success to execute request!: $responseBody")
                val jsonObj__ = responseBody?.let { JSONObject(it) }
                val jsonArray = jsonObj__?.getJSONArray("data")
                val pickData = mutableListOf<String>()
                if (jsonArray != null) {
                    for (i in 0 until jsonArray.length()) {
                        pickData.add(jsonArray.getString(i))
                    }
                }
                pickData
            }
        }
    }
    // TODO: json 으로 받아올 수 있게 변환해야 함 responsebody를
    suspend fun fetchPickItemJsonById(myUrl: String, PickName: String, id: String) : JSONObject? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("${myUrl}read.php?")
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                Log.e("OKHTTP3/pickitemfetch", "Success to execute request!: $responseBody")
                val jsonObj__ = responseBody?.let { JSONObject(it) }
                val jsonObj = jsonObj__?.getJSONObject("data")
                jsonObj
            }
        }
    }
}