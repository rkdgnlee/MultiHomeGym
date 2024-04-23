package com.tangoplus.tangoq.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.tangoplus.tangoq.databinding.ItemSpinnerSignInBinding

@Suppress("UNREACHABLE_CODE")
class SignInSpinnerAdapter(context:Context, private val resId: Int, private val domainList: List<String>) : ArrayAdapter<String>(context, resId, domainList) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemSpinnerSignInBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.tvSpinner.text = domainList[position]
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemSpinnerSignInBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.tvSpinner.text = domainList[position]
        return binding.root
    }

    override fun getCount(): Int {
        return super.getCount()
        return domainList.size
    }
}