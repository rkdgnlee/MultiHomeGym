package com.example.mhg

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.example.mhg.Dialog.ExerciseLoadDialogFragment
import com.example.mhg.VO.UserViewModel
import com.example.mhg.`object`.NetworkService.fetchINSERTJson
import com.example.mhg.databinding.FragmentIntro3Binding
import com.example.mhg.`object`.Singleton_t_user
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception

@Suppress("NAME_SHADOWING")
class Intro3Fragment : Fragment() {
    lateinit var binding: FragmentIntro3Binding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val TAG = this.javaClass.simpleName
//    val viewModel : UserViewModel by viewModels()

//    fun fetchSELECTJson(myUrl : String, user_mobile:String, callback: () -> Unit){
//        val client = OkHttpClient()
////        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull())
//        val request = Request.Builder()
//            .url("${myUrl}users/read.php?user_mobile=$user_mobile")
//            .get()
//            .build()
//
//        client.newCall(request).enqueue(object : Callback  {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("OKHTTP3", "Failed to execute request!")
//            }
//            override fun onResponse(call: Call, response: Response)  {
//                val responseBody = response.body?.string()
//                Log.e("OKHTTP3", "Success to execute request!: $responseBody")
//                val jsonObj__ = responseBody?.let { JSONObject(it) }
////              TODO  if () 어떤 response가 오는지에 맞게 일단 조건을 걸어야 함 해당 값들이 반환되면 거기다가 UPDATE만 하기, 없으면 INSERT
//                val jsonObj = jsonObj__?.optJSONObject("data")
//                val t_userInstance = context?.let { Singleton_t_user.getInstance(requireContext()) }
//                t_userInstance?.jsonObject = jsonObj
//                Log.e("OKHTTP3>싱글톤", "${t_userInstance?.jsonObject}")
//                callback()
//            }
//        })
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntro3Binding.inflate(inflater)
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            firebaseAuth = FirebaseAuth.getInstance()
            launcher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(), ActivityResultCallback { result ->
                    Log.d(TAG, "resultCode: ${result.resultCode}입니다.")
                    Log.d(TAG, "$result")
                    if (result.resultCode == RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            task.getResult(ApiException::class.java)?.let { account ->
                                val tokenId = account.idToken
                                Log.d("토큰 있나요?", "예. $tokenId")
                                if (tokenId != null && tokenId != "") {
                                    val credential: AuthCredential =
                                        GoogleAuthProvider.getCredential(account.idToken, null)
                                    firebaseAuth.signInWithCredential(credential)
                                        .addOnCompleteListener {
                                            if (firebaseAuth.currentUser != null) {
                                                Log.d("로그인", "${firebaseAuth.currentUser}")
                                                // ---- Google 토큰에서 가져오기 시작 ----
                                                val user: FirebaseUser = firebaseAuth.currentUser!!

                                                // ----- GOOGLE API: 전화번호 담으러 가기(signin) 시작 -----
                                                val JsonObj = JSONObject()
                                                JsonObj.put("user_name", user.displayName.toString())
                                                JsonObj.put("user_email", user.email.toString())
                                                JsonObj.put("google_login_id", user.uid)

                                                Log.e("구글JsonObj", JsonObj.getString("google_login_id"))
                                                val intent = Intent(requireContext(), SignInActivity::class.java)
                                                intent.putExtra("user", JsonObj.toString())
                                                startActivity(intent)

                                                // ----- GOOGLE API에서 DB에 넣는 공간 끝 -----

                                                val googleSignInToken = account.idToken ?: ""
                                                if (googleSignInToken != "") {
                                                    Log.e(TAG, "googleSignInToken : $googleSignInToken")
                                                } else {
                                                    Log.e(TAG, "googleSignInToken이 null")
                                                }
                                                // ---- Google 토큰에서 가져오기 끝 ----
                                            }
                                        }
                                }
                            } ?: throw Exception()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        } else {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)
            ActivityCompat.finishAffinity(requireActivity())
        }
        // ---- firebase 초기화 및 Google Login API 연동 끝 ----
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---- firebase 초기화 및 Google Login API 연동 시작 ----

        // ---- 구글 로그인 시작 ----
        binding.ibtnGoogleLogin.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.firebase_client_id))
                    .requestEmail()
                .build()
                val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
                val signInIntent: Intent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }
        }
        // ---- 구글 로그인 끝 ----

        // ---- 앱 내 자체 회원가입/로그인 시작 ----
        binding.btnSignin.setOnClickListener {
            val intent = Intent(requireContext(), SignInActivity::class.java)
            startActivity(intent)

        }
        // ---- 네이버 로그인 연동 시작 ----
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(
                    requireContext(),
                    "errorCode: $errorCode, errorDesc: $errorDescription",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onSuccess() {
                // ---- 네이버 로그인 성공 동작 시작 ----
                NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
                    override fun onError(errorCode: Int, message: String) {}
                    override fun onFailure(httpStatus: Int, message: String) {}

                    override fun onSuccess(result: NidProfileResponse) {
                        val JsonObj = JSONObject()
                        val naver_mobile = result.profile?.mobile.toString().replaceFirst("010", "+8210")
                        val naver_gender : String
                        naver_gender = if (result.profile?.gender.toString() == "M") {
                            "MALE"
                        } else {
                            "FEMALE"
                        }

                        JsonObj.put("user_name", result.profile?.name.toString())
                        JsonObj.put("user_gender", naver_gender)
                        JsonObj.put("user_mobile", naver_mobile)
                        JsonObj.put("user_email", result.profile?.email.toString())
                        JsonObj.put("user_birthday", result.profile?.birthYear.toString() + "-" + result.profile?.birthday.toString())
                        JsonObj.put("naver_login_id" , result.profile?.id.toString())

                        Log.i("$TAG, 네이버", JsonObj.getString("user_mobile"))
                        fetchINSERTJson(getString(R.string.IP_ADDRESS_t_user), JsonObj.toString()) {
                            val t_userInstance = context?.let { Singleton_t_user.getInstance(requireContext()) }
                            t_userInstance?.jsonObject = JsonObj
                            Log.e("OKHTTP3>싱글톤", "${t_userInstance?.jsonObject}")
                            ExerciseDialogInit()

                        }
                    }
                })
                // ---- 네이버 로그인 성공 동작 끝 ----
            }
        }
        binding.buttonOAuthLoginImg.setOAuthLogin(oauthLoginCallback = oauthLoginCallback)
        // ---- 네이버 로그인 연동 끝  ----

        // ---- 카카오톡 OAuth 불러오기 ----
        val callback: (OAuthToken?, Throwable?) -> Unit = {token, error ->
            if (error != null) {
                Log.e("카카오톡", "카카오톡 로그인 실패 $error")
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Log.e("카카오톡", "접근이 거부 됨(동의 취소) $error")
                    }
                }
            } else if (token != null) {
                Log.e("카카오톡", "로그인에 성공하였습니다.")
                UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                    if (error != null) {
                        Log.e(TAG, "토큰 정보 보기 실패", error)
                    }
                    else if (tokenInfo != null) {
                        UserApiClient.instance.me { user, error ->
                            if (error != null) {
                                Log.e(TAG, "사용자 정보 요청 실패", error)
                            }
                            else if (user != null) {
                                val JsonObj = JSONObject()
                                val kakao_mobile = user.kakaoAccount?.phoneNumber.toString().replaceFirst("010", "+8210")
                                JsonObj.put("user_name" , user.kakaoAccount?.name.toString())
                                JsonObj.put("user_gender", user.kakaoAccount?.gender.toString())
                                JsonObj.put("user_mobile", kakao_mobile)
                                JsonObj.put("user_email", user.kakaoAccount?.email.toString())
                                JsonObj.put("user_birthday", user.kakaoAccount?.birthyear.toString() + user.kakaoAccount?.birthday.toString())
                                JsonObj.put("kakao_login_id" , user.id.toString())

                                Log.w("$TAG, 카카오회원가입", JsonObj.getString("user_mobile"))
                                fetchINSERTJson(getString(R.string.IP_ADDRESS_t_user), JsonObj.toString()) {
                                    val t_userInstance = context?.let { Singleton_t_user.getInstance(requireContext()) }
                                    t_userInstance?.jsonObject = JsonObj
                                    Log.e("OKHTTP3>싱글톤", "${t_userInstance?.jsonObject}")
                                    ExerciseDialogInit()
                                }
                            }
                        }
                    }
                }
            }
        }


        binding.ibtnKakaoLogin.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
                UserApiClient.instance.loginWithKakaoTalk(requireContext(), callback = callback)
            }  else {
                UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
            }
        }
        // ---- 카카오 로그인 연동 끝 ----
    }

    private fun ExerciseDialogInit() {
        val dialogFragment = ExerciseLoadDialogFragment()
        dialogFragment.show(requireActivity().supportFragmentManager, "DialogFragment")
    }
//    private fun setToken(context: Context, key: String, value: String) {
//        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
//        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
//        val sharedPreferences = EncryptedSharedPreferences.create(
//            "encryptedShared", masterKeyAlias, context,
//            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//        )
//
//        sharedPreferences.edit().putString(key, value).apply()
//    }

}