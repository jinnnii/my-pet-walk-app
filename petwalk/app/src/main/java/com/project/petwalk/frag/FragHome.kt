package com.project.petwalk.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.petwalk.databinding.FragmentFragHomeBinding


class FragHome : Fragment() {
    lateinit var binding: FragmentFragHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFragHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

}