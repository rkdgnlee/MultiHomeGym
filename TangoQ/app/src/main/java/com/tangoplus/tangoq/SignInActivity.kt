package com.tangoplus.tangoq

import android.R
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.shuhart.stepview.StepView
import com.tangoplus.tangoq.Adapter.SignInSpinnerAdapter
import com.tangoplus.tangoq.Dialog.AgreementBottomSheetDialogFragment
import com.tangoplus.tangoq.Dialog.SignInBottomSheetDialogFragment
import com.tangoplus.tangoq.Listener.OnSingleClickListener
import com.tangoplus.tangoq.Object.NetworkUserService.fetchUserSELECTJson
import com.tangoplus.tangoq.Object.Singleton_t_user
import com.tangoplus.tangoq.ViewModel.SignInViewModel
import com.tangoplus.tangoq.ViewModel.UserViewModel
import com.tangoplus.tangoq.databinding.ActivitySignInBinding
import com.tangoplus.tangoq.transition.SignInTransition
import java.util.regex.Pattern


class SignInActivity : AppCompatActivity() {
    lateinit var binding : ActivitySignInBinding
    val viewModel : SignInViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        // -----! 초기 버튼 숨기기 및 세팅 시작 !-----
        binding.llPwCondition.visibility = View.GONE
        binding.etPw.visibility = View.GONE
        binding.llPwRepeat.visibility = View.GONE
        binding.etPwRepeat.visibility = View.GONE
        binding.btnSI.visibility = View.GONE
        binding.llId.visibility = View.GONE
        binding.etId.visibility = View.GONE
        binding.tvNameGuide.visibility = View.GONE
        binding.etName.visibility = View.GONE
        binding.llEmail.visibility = View.GONE

        binding.btnAuthSend.isEnabled = false
        binding.etAuthNumber.isEnabled = false
        binding.btnAuthConfirm.isEnabled = false
        binding.btnSI.isEnabled = false


        binding.svSI.getState()
            .animationType(StepView.ANIMATION_CIRCLE)
            .steps(object : ArrayList<String?>() {
                init {
                    add("휴대폰 인증")
                    add("이름, 이메일")
                    add("아이디")
                    add("비밀번호")
                }
            })
            .stepsNumber(4)
            .animationDuration(getResources().getInteger(R.integer.config_shortAnimTime))
            // other state methods are equal to the corresponding xml attributes
            .commit()
        // -----! 초기 버튼 숨기기 및 세팅 끝 !-----

        // -----! progress bar 시작 !-----
        binding.pvSI.progress = 25
        // -----! progress bar 끝 !-----

        // -----! 통신사 선택 시작 !-----
        binding.tvTelecom.setOnSingleClickListener {
            showTelecomBottomSheetDialog(this@SignInActivity)
        } // -----! 통신사 선택 끝 !-----

        // -----! 회원가입 입력 창 anime 시작  !-----
        TransitionManager.beginDelayedTransition(binding.llSI, SignInTransition())

        // -----! 휴대폰 인증 시작 !-----

        binding.btnAuthSend.setOnSingleClickListener {
            val transformMobile = phoneNumber82(binding.etMobile.text.toString())
            val dialog = AlertDialog.Builder(this)
                .setTitle("📩 문자 인증 ")
                .setMessage("$transformMobile 로 인증 하시겠습니까?")
                .setPositiveButton("예") { _, _ ->

                    // TODO FIREBASE연동
                    transformMobile.replace("-", "")
                    transformMobile.replace("-", "")
                    Log.w("전화번호", transformMobile)
                    val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
                    alphaAnimation.duration = 600
                    binding.etAuthNumber.isEnabled = true

                    val objectAnimator = ObjectAnimator.ofFloat(binding.clMobile, "translationY", 1f)
                    objectAnimator.duration = 1000
                    objectAnimator.start()
                    binding.etAuthNumber.requestFocus()
                }
                .setNegativeButton("아니오", null)
                .show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)

            // TODO 일단 열어놓기
            binding.btnAuthConfirm.isEnabled = true
        }
        // -----! 휴대폰 인증 끝 !-----

        // -----! 인증 문자 확인 시작 !-----
        //        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            override fun onVerificationCompleted(p0: PhoneAuthCredential) {}
//            override fun onVerificationFailed(p0: FirebaseException) {}
//            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                super.onCodeSent(verificationId, token)
//                this@SignIn4Fragment.verificationId = verificationId
//
//                // -----! 메시지 발송에 성공하면 스낵바 호출 !------
//                Snackbar.make(requireView(), "메시지 발송에 성공했습니다. 잠시만 기다려주세요", Snackbar.LENGTH_LONG).show()
//                binding.btnAuthConfirm.isEnabled = true
//            }
//        }
//        binding.btnAuthSend.setOnClickListener {
//            val transformMobile = phoneNumber82(binding.etMobile.text.toString())
//            val dialog = AlertDialog.Builder(requireContext())
//                .setTitle("전화번호 확인")
//                .setMessage("${binding.etMobile.text} 번호로 인증번호를 보내시겠습니까?")
//                .setPositiveButton("예") { _, _ ->
//                    val optionsCompat = PhoneAuthOptions.newBuilder(auth)
//                        .setPhoneNumber(transformMobile)
//                        .setTimeout(60L, TimeUnit.SECONDS)
//                        .setActivity(requireActivity())
//                        .setCallbacks(callbacks)
//                        .build()
//                    Log.w(TAG+"문자", "${PhoneAuthOptions.newBuilder(auth)}")
//                    PhoneAuthProvider.verifyPhoneNumber(optionsCompat)
//                    auth.setLanguageCode("kr")
//                }
//                .setNegativeButton("아니오", null)
//                .show()
//
//            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
//            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
//        }
//        binding.btnAuthConfirm.setOnClickListener {
//            val credential = PhoneAuthProvider.getCredential(verificationId, binding.etAuthNumber.text.toString())
//            signInWithPhoneAuthCredential(credential)
//        }

        binding.btnAuthConfirm.setOnSingleClickListener {
            binding.etAuthNumber.isEnabled = false
            val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
            alphaAnimation.duration = 600
            binding.tvNameGuide.startAnimation(alphaAnimation)
            binding.etName.startAnimation(alphaAnimation)
            binding.llEmail.startAnimation(alphaAnimation)

            binding.tvNameGuide.visibility = View.VISIBLE
            binding.etName.visibility = View.VISIBLE
            binding.llEmail.visibility= View.VISIBLE
            binding.tvSIIntro.text = "이름과 이메일을 입력해주세요"
            val objectAnimator = ObjectAnimator.ofFloat(binding.clMobile, "translationY", 1f)
            objectAnimator.duration = 1000
            objectAnimator.start()
            binding.pvSI.progress = 50
            binding.etName.requestFocus()
            binding.svSI.go(1, true)
        }  // -----! 인증 문자 확인 끝 !-----

        binding.etName.setOnEditorActionListener {v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.etEmail.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }
        binding.etEmailId.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
                alphaAnimation.duration = 600
                binding.etId.startAnimation(alphaAnimation)
                binding.llId.startAnimation(alphaAnimation)
                binding.etId.visibility = View.VISIBLE
                binding.llId.visibility = View.VISIBLE

                val objectAnimator = ObjectAnimator.ofFloat(binding.clMobile, "translationY", 1f)
                objectAnimator.duration = 1000
                objectAnimator.start()
                return@setOnEditorActionListener true
            }
            false
        }
        val domain_list = listOf("gmail.com", "naver.com", "kakao.com", "직접입력")
        val actAdapter = ArrayAdapter(
            this,
            R.layout.simple_dropdown_item_1line,
            domain_list
        )

        binding.spinner.adapter = SignInSpinnerAdapter(this, com.tangoplus.tangoq.R.layout.item_spinner_sign_in, domain_list)
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.spinner.getItemAtPosition(position).toString()
                if (position == 3) {
                    binding.etEmail.visibility = View.VISIBLE
                    binding.spinner.visibility = View.GONE
                    binding.ivSpinner.setOnClickListener{
                        binding.spinner.performClick()
                        binding.spinner.visibility = View.VISIBLE
                    }

                } else {
                    binding.etEmail.visibility = View.GONE
                    binding.etEmail.setText("")
                    binding.spinner.visibility = View.VISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
//        binding.psvSIEmail.setSpinnerAdapter(actAdapter)
//        binding.psvSIEmail.setText(domain_list.firstOrNull(), false)

        binding.etId.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
                alphaAnimation.duration = 600

                binding.llPwCondition.startAnimation(alphaAnimation)
                binding.etPw.startAnimation(alphaAnimation)
                binding.llPwRepeat.startAnimation(alphaAnimation)
                binding.etPwRepeat.startAnimation(alphaAnimation)
                binding.btnSI.startAnimation(alphaAnimation)
                binding.llPwCondition.visibility = View.VISIBLE
                binding.etPw.visibility = View.VISIBLE
                binding.llPwRepeat.visibility = View.VISIBLE
                binding.etPwRepeat.visibility = View.VISIBLE
                binding.btnSI.visibility = View.VISIBLE


                val objectAnimator = ObjectAnimator.ofFloat(binding.clMobile, "translationY", 1f)
                objectAnimator.duration = 1000
                objectAnimator.start()
                binding.pvSI.progress = 75
                binding.etPw.requestFocus()
                binding.svSI.go(2, true)
                return@setOnEditorActionListener true
            }
            false
        }

        binding.btnSI.setOnSingleClickListener {
            showAgreementBottomSheetDialog(this)

        }
        val mobilePattern = "^010-\\d{4}-\\d{4}\$"
        val MobilePattern = Pattern.compile(mobilePattern)
        val pwPattern = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[$@$!%*#?&.^])[A-Za-z[0-9]$@$!%*#?&.^]{8,20}$" // 영문, 특수문자, 숫자 8 ~ 20자 패턴
        val idPattern = "^[a-zA-Z0-9]{4,16}$" // 영문, 숫자 4 ~ 16자 패턴
        val IdPattern = Pattern.compile(idPattern)
        val Pwpattern = Pattern.compile(pwPattern)

        // ------! 핸드폰 번호 조건 코드 !-----
        binding.etMobile.addTextChangedListener(object: TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val cleaned =s.toString().replace("-", "")
                when {
                    cleaned.length <= 3 -> s?.replace(0, s.length, cleaned)
                    cleaned.length <= 7 -> s?.replace(0, s.length, "${cleaned.substring(0, 3)}-${cleaned.substring(3)}")
                    else -> s?.replace(0, s.length, "${cleaned.substring(0, 3)}-${cleaned.substring(3, 7)}-${cleaned.substring(7)}")
                }
                isFormatting = false
                Log.w("전화번호형식", "${MobilePattern.matcher(binding.etMobile.text.toString()).find()}")
                viewModel.mobileCondition.value = MobilePattern.matcher(binding.etMobile.text.toString()).find()
                if (viewModel.mobileCondition.value == true) {
                    viewModel.User.value?.put("user_mobile", s.toString() )
                    binding.btnAuthSend.isEnabled = true
                }

            }
        })


        // ----- ! ID 조건 코드 ! -----
        binding.etId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.idCondition.value = IdPattern.matcher(binding.etId.text.toString()).find()
                if (viewModel.idCondition.value == true) {
                    binding.tvIdCondition.setTextColor(binding.tvIdCondition.resources.getColor(com.tangoplus.tangoq.R.color.successColor))
                    binding.tvIdCondition.text = "사용 가능합니다"
                    viewModel.User.value?.put("user_id", s.toString())
                } else {
                    binding.tvIdCondition.setTextColor(binding.tvIdCondition.resources.getColor(com.tangoplus.tangoq.R.color.mainColor))
                    binding.tvIdCondition.text = "다시 입력해주세요"
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        // ----- ! 비밀번호 조건 코드 ! -----
        binding.etPw.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                viewModel.pwCondition.value = Pwpattern.matcher(binding.etPw.text.toString()).find()
                if (viewModel.pwCondition.value == true) {
                    binding.tvPwCondition.setTextColor(binding.tvPwCondition.resources.getColor(com.tangoplus.tangoq.R.color.successColor))
                    binding.tvPwCondition.text = "사용 가능합니다"

                } else {
                    binding.tvPwCondition.setTextColor(binding.tvPwCondition.resources.getColor(com.tangoplus.tangoq.R.color.mainColor))
//                    binding.tvPwCondition.text = "영문, 숫자, 특수문자( ! @ # $ % ^ & * ? .)를 모두 포함해서 8~20자리를 입력해주세요"
                    binding.tvPwCondition.text = ""
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        // ----- ! 비밀번호 확인 코드 ! -----
        binding.etPwRepeat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.pwCompare.value = (binding.etPw.text.toString() == binding.etPwRepeat.text.toString())
                if (viewModel.pwCompare.value == true) {
                    binding.tvPwRepeat.setTextColor(binding.tvPwRepeat.resources.getColor(com.tangoplus.tangoq.R.color.successColor))
                    binding.tvPwRepeat.text = "일치합니다"
                    binding.pvSI.progress = 100
                    binding.svSI.go(3, true)
                    binding.tvSIIntro.text = "하단의 완료 버튼을 눌러주세요"
                    binding.btnSI.isEnabled = true
                } else {
                    binding.tvPwRepeat.setTextColor(binding.tvPwRepeat.resources.getColor(com.tangoplus.tangoq.R.color.mainColor))
                    binding.tvPwRepeat.text = "다시 입력해주세요"
                }
                // -----! 뷰모델에 보낼 값들 넣기 !-----

                viewModel.User.value?.put("user_password", s.toString())

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }) //-----! 입력 문자 조건 끝 !-----

        // -----! 뒤로 가기 버튼 시작 !-----
        binding.btnBckSI.setOnSingleClickListener {
            finish()
        }
    } // -----! 회원가입 입력 창 anime 끝 !-----

//    override fun onFragmentInteraction() {
//        binding.tvSignInNext.performClick()
//    }

    fun phoneNumber82(msg: String) : String {
        val firstNumber: String = msg.substring(0,3)
        var phoneEdit = msg.substring(3)
        when (firstNumber) {
            "010" -> phoneEdit = "+8210$phoneEdit"
            "011" -> phoneEdit = "+8211$phoneEdit"
            "016" -> phoneEdit = "+8216$phoneEdit"
            "017" -> phoneEdit = "+8217$phoneEdit"
            "018" -> phoneEdit = "+8218$phoneEdit"
            "019" -> phoneEdit = "+8219$phoneEdit"
            "106" -> phoneEdit = "+82106$phoneEdit"
        }
        return phoneEdit
    }
    private fun showTelecomBottomSheetDialog(context: FragmentActivity) {
        val bottomsheetfragment = SignInBottomSheetDialogFragment()
        bottomsheetfragment.setOnCarrierSelectedListener(object : SignInBottomSheetDialogFragment.onTelecomSelectedListener {
            override fun onTelecomSelected(telecom: String) {
                binding.tvTelecom.text = telecom
            }
        })
        val fragmentManager = context.supportFragmentManager
        bottomsheetfragment.show(fragmentManager, bottomsheetfragment.tag)
    }
    override fun onResume() {
        super.onResume()
        binding.nsvSI.isNestedScrollingEnabled = false
    }
    private fun showAgreementBottomSheetDialog(context: FragmentActivity) {
        val bottomSheetFragment = AgreementBottomSheetDialogFragment()
        bottomSheetFragment.setOnFinishListener(object : AgreementBottomSheetDialogFragment.onAgreeListener {
            override fun onFinish() {
                val JsonObj = viewModel.User.value
                // TODO 동의 간주 후, 연락처 보내기
                viewModel.User.value?.put("user_name", binding.etName.text)
                when (binding.spinner.selectedItemPosition) {
                    0, 1, 2 -> {
                        viewModel.User.value?.put("user_email", "${binding.etEmailId.text}@${binding.spinner.selectedItem as String}")
                        Log.w("현재VM값", "${viewModel.User.value}")
                    }
                    else -> {
                        viewModel.User.value?.put("user_email", "${binding.etEmailId.text}@${binding.etEmail.text}")
                        Log.w("현재VM값", "${viewModel.User.value}")
                    }
                }

                fetchUserSELECTJson(getString(com.tangoplus.tangoq.R.string.IP_ADDRESS_t_user), viewModel.User.value?.optString("user_mobile").toString()) {
                    runOnUiThread{
                        val t_userInstance = context?.let { Singleton_t_user.getInstance(this@SignInActivity) }
                        t_userInstance?.jsonObject = JsonObj
                        Log.e("OKHTTP3>싱글톤", "${t_userInstance?.jsonObject}")

//                        val intent = Intent(this ,PersonalSetupActivity::class.java)
//                        startActivity(intent)
//                        ActivityCompat.finishAffinity(requireActivity())
                    }
                }
            }
        })
//        bottomSheetFragment.isCancelable = false
        val fragmentManager = context.supportFragmentManager
        bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)


    }

    private fun View.setOnSingleClickListener(action: (v: View) -> Unit) {
        val listener = View.OnClickListener { action(it) }
        setOnClickListener(OnSingleClickListener(listener))
    }

}