package com.project.petwalk.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.petwalk.R
import com.project.petwalk.databinding.ItemCalendarBinding
import com.project.petwalk.model.Walk
import java.text.SimpleDateFormat

class CalViewHolder(val binding:ItemCalendarBinding):RecyclerView.ViewHolder(binding.root)

class CalAdapter(val context: Context, val days:List<String>?, val walkList:List<Walk>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    = CalViewHolder(ItemCalendarBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CalViewHolder).binding
        val day=days?.get(position)
        Log.d("pet", "day :::::: "+day)
        val fmt = SimpleDateFormat("dd")
        for(walk in walkList){
            val endDay = fmt.format(walk.endTime)
            Log.d("pet", "end day :::::: "+endDay)
            if(endDay==day){
                binding.calMark.visibility= View.VISIBLE
            }
        }


        binding.calDayTxt.text=day

        binding.calDayTxt.setOnClickListener{
//            itemClickListener.onClick(it,position)
            Toast.makeText(this.context, "click ${day} ${position} ",Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount(): Int {
        return days?.size ?:0
    }

}





//class CalViewHolder(@NonNull itemView: View, val onItemListener: CalAdapter.OnItemListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
//    val dayOfMonth: TextView
//    init {
//        this.dayOfMonth = itemView.findViewById(R.id.cal_day_txt)
//        itemView.setOnClickListener(this)
//    }
//
//    override fun onClick(v: View?) {
//        onItemListener.onItemClick(adapterPosition, dayOfMonth.text.toString())
//    }
//
//}

//class CalAdapter(var daysOfMonth: ArrayList<String>, var onItemListener: OnItemListener) : RecyclerView.Adapter<CalViewHolder>(){
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val view:View = inflater.inflate(R.layout.item_calendar, parent, false)
//        val layoutParams:ViewGroup.LayoutParams = view.layoutParams
//        layoutParams.height=parent.height* (0.166666666).toInt()
//        return CalViewHolder(view, onItemListener)
//
//    }
//
//    override fun onBindViewHolder(holder:CalViewHolder, position: Int) {
//        Log.d("pet", daysOfMonth[position].toString())
//        holder.dayOfMonth.text= daysOfMonth[position]
//    }
//
//    override fun getItemCount(): Int {
//        return daysOfMonth.size
//    }
//
//    interface OnItemListener{
//        fun onItemClick(position:Int, dayText:String)
//    }
//
//}