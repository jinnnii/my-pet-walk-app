package com.project.petwalk.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.petwalk.R
import com.project.petwalk.databinding.ItemCalendarBinding
import com.project.petwalk.model.Walk
import java.text.SimpleDateFormat

class CalViewHolder(val binding:ItemCalendarBinding):RecyclerView.ViewHolder(binding.root)


class CalAdapter(val context: Context, val days:List<String>?, val walkList:List<Walk>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    View.OnClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    = CalViewHolder(ItemCalendarBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    @SuppressLint("SimpleDateFormat", "ResourceAsColor")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int ) {
        val binding = (holder as CalViewHolder).binding
        val day=days?.get(position)

        val walk = checkWalk(day!!)
        binding.calDayTxt.text=day

        if(walk!=null){
            binding.calMark.visibility= View.VISIBLE
            binding.calDayTxt.setTextColor(R.color.white)
            binding.calDayTxt.tag=walk
            binding.calDayTxt.setOnClickListener(this)
        }


//        binding.calDayTxt.setOnClickListener{
////            itemClickListener.onClick(it,position)
//            Toast.makeText(this.context, "click ${day} ${position} ",Toast.LENGTH_SHORT).show()
//        }

    }

    override fun getItemCount(): Int {
        return days?.size ?:0
    }

    @SuppressLint("SimpleDateFormat")
    fun checkWalk(day:String): Walk? {
        val fmt = SimpleDateFormat("dd")
        for(walk in walkList){
            if(fmt.format(walk.endTime)==day){
                Log.d("pet", walk.toString())
                return walk
            }
        }
        return null
    }

    override fun onClick(v: View?) {
        val walk:Walk = v?.tag as Walk
        val intent = Intent(context, CalendarDetail::class.java)
        intent.putExtra("walk",walk)
        context.startActivity(intent)
    }

}
