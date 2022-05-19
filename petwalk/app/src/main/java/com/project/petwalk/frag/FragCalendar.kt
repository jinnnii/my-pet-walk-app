package com.project.petwalk.frag

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.project.petwalk.calendar.CalAdapter
import com.project.petwalk.databinding.FragmentFragCalendarBinding
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.Walk
import kotlinx.android.synthetic.main.fragment_frag_calendar.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class FragCalendar : Fragment(){
    lateinit var binding: FragmentFragCalendarBinding
    lateinit var monthYearText:TextView
    lateinit var recyclerView: RecyclerView
    lateinit var selectDate:LocalDate

    //저장 및 표시할 walk 데이터
    var walkDataList = arrayListOf<Walk>()


    //파이어베이스
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val userReference:DatabaseReference=database.getReference("Users")
    val walkReference: DatabaseReference = database.getReference("walk")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentFragCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //todo init
        initWidgets()
        selectDate = LocalDate.now()

        getWalkData()



        binding.prevMonth.setOnClickListener{
            previousMonthAction(binding.root)
        }
        binding.nextMonth.setOnClickListener{
            nextMonthAction(binding.root)
        }

    }
    fun initWidgets(){
        recyclerView=binding.calendarRecycler
        monthYearText=binding.tvMonthYear
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setMonthView(walkList:ArrayList<Walk>){
        monthYearText.text= monthYeartFromDate(selectDate)
        val dayInMonth:ArrayList<String> = daysInMonthArray(selectDate)

        val calAdapter=CalAdapter(context as Activity, dayInMonth, walkList)
        val layoutManager:RecyclerView.LayoutManager = GridLayoutManager(activity?.applicationContext, 7)

        recyclerView.layoutManager=layoutManager
        recyclerView.adapter=calAdapter

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysInMonthArray(date:LocalDate):ArrayList<String>{
        val daysInMonthArray = arrayListOf<String>()
        val yearMonth:YearMonth = YearMonth.from(date)

        val dayInMonth=yearMonth.lengthOfMonth()

        val firstOfMonth:LocalDate = selectDate.withDayOfMonth(1)
        val dayOfWeek:Int = firstOfMonth.dayOfWeek.value

        for(i in 1..42){
            if(i<=dayOfWeek || i>dayInMonth+dayOfWeek){
                daysInMonthArray.add("")
            }else{
                daysInMonthArray.add((i-dayOfWeek).toString())
            }
        }
        return daysInMonthArray
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun monthYeartFromDate(date:LocalDate):String{
        val  fomatter:DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(fomatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun previousMonthAction(view:View){
        selectDate=selectDate.minusMonths(1)
        getThisMonthData(walkDataList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextMonthAction(view:View){
        selectDate= selectDate.plusMonths(1)
        getThisMonthData(walkDataList)
    }
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onItemClick(position: Int, dayText: String) {
//        if(dayText == ""){
//            val message = "Selected Date $dayText ${monthYeartFromDate(selectDate)}"
//            Toast.makeText(this.context,message, Toast.LENGTH_SHORT).show()
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    fun getThisMonthData(walkList: ArrayList<Walk>){
        val satisWalkList = arrayListOf<Walk>()
        for (walk in walkList) {
            val fmt = SimpleDateFormat("MMMM yyyy")
            val walkDay = fmt.format(walk.endTime)

            if (walkDay == monthYeartFromDate(selectDate)) {
                Log.d("pet","이번달 기록된 날짜 ::::: $walk")
                satisWalkList.add(walk)
            }
        }

        setMonthView(satisWalkList)
    }

    /**
     * 파이어베이스 정보 가져오기
     */
    fun getWalkData() {
        //todo 테스트 유저 사용하기
        val testUserUid = "NQkNJArulWd4vAMN8wO9JOb9VUw2"
        userReference.child(testUserUid).child("walk")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val walkArr = dataSnapshot.value as Map<*, *>


                    for (walkData in walkArr) {
                        val key = walkData.key.toString()
                        walkReference.child(key).get().addOnSuccessListener {
                            val walk = it.getValue(Walk::class.java)
                            walkDataList.add(walk!!)
                        }
                    }

                    getThisMonthData(walkDataList)
                }

                override fun onCancelled(databaseError: DatabaseError) {}

            })
    }
}

