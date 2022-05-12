package com.project.petwalk.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.petwalk.databinding.FragmentFragWalkBinding


class FragWalk : Fragment() {
lateinit var binding: FragmentFragWalkBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFragWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

}