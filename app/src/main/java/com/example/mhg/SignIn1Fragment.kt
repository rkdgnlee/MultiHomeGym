package com.example.mhg

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import com.example.mhg.VO.UserViewModel
import com.example.mhg.databinding.FragmentSignIn1Binding
import org.json.JSONObject
import java.util.regex.Pattern


class SignIn1Fragment : Fragment() {
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction()
    }
    private var listener : OnFragmentInteractionListener? = null
    lateinit var binding : FragmentSignIn1Binding
    val viewModel : UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignIn1Binding.inflate(inflater)
        viewModel.User.observe(viewLifecycleOwner) {user ->
            if (user != null && user.has("user_name")) {
                binding.etName.setText(user.getString("user_name"))
                viewModel.nameCondition.value = true
            } else {
                binding.etName.text.clear()
                binding.etName.isEnabled = true
            }
        }
        binding.etName.setOnEditorActionListener{_, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                listener?.onFragmentInteraction()
                true
            } else {
                false
            }
        }

        view?.postDelayed({
            val fadeIn = ObjectAnimator.ofFloat(binding.tvNameSignIn, "alpha", 0f, 1f)
            fadeIn.duration = 900
            val moveUp = ObjectAnimator.ofFloat(binding.tvNameSignIn, "translationY", 100f, 0f)
            moveUp.duration = 900

            val animatorSet = AnimatorSet()
            animatorSet.apply {
                play(fadeIn)
                play(moveUp)
            }
            animatorSet.start()
        },500)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val namePatternKor =  "^[가-힣]{2,8}\$"
        val namePatternEng = "^[a-zA-Z\\s]{4,20}$"
        val NamePatternKor = Pattern.compile(namePatternKor)
        val NamePatternEng = Pattern.compile(namePatternEng)
        // ----- ! 이름 조건 코드 ! -----
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()
                viewModel.nameCondition.value = NamePatternKor.matcher(binding.etName.text.toString()).find() || NamePatternEng.matcher(binding.etName.text.toString()).find()
                viewModel.User.value?.put("user_name", newText)

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }
}