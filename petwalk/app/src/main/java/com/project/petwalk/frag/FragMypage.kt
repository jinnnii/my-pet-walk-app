package com.project.petwalk.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.petwalk.databinding.FragmentFragMypageBinding


class FragMypage : Fragment() {
    lateinit var binding: FragmentFragMypageBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFragMypageBinding.inflate(inflater, container, false)
        return binding.root
    }
}