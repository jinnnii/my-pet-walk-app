package com.project.petwalk.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.petwalk.databinding.FragmentFragCommunityBinding


class FragCommunity : Fragment() {
    lateinit var binding: FragmentFragCommunityBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentFragCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }


}