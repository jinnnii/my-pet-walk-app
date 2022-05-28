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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.petwalk.calendar.CalAdapter
import com.project.petwalk.databinding.FragmentFragCalendarBinding
import com.project.petwalk.firebase.FirebaseUserHelper
import com.project.petwalk.firebase.FirebaseWalkHelper
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.Pet
import com.project.petwalk.model.User
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
    var thisWalkList = arrayListOf<Walk>()
    var allWalkList = arrayListOf<Walk>()


    //파이어베이스
    val firebaseAuth = FirebaseAuth.getInstance()

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
    fun setMonthView(){
        monthYearText.text= monthYeartFromDate(selectDate)
        val dayInMonth:ArrayList<String> = daysInMonthArray(selectDate)

        val calAdapter=CalAdapter(context as Activity, dayInMonth, thisWalkList)
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

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateMonth(){
        thisWalkList.clear()
        for(walk in allWalkList){
            if(getThisMonthData(walk)){
                thisWalkList.add(walk)
            }
        }
        setMonthView()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun monthYeartFromDate(date:LocalDate):String{
        val  fomatter:DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(fomatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun previousMonthAction(view:View){
        selectDate=selectDate.minusMonths(1)
        updateMonth()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextMonthAction(view:View){
        selectDate= selectDate.plusMonths(1)
        updateMonth()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    fun getThisMonthData(walk: Walk):Boolean{
        val fmt = SimpleDateFormat("MMMM yyyy")
        val walkDay = fmt.format(walk.endTime)

        if (walkDay == monthYeartFromDate(selectDate)) {
            Log.d("pet","이번달 기록된 날짜 ::::: $walk")
            return true
        }
        return false
    }

    /**
     * 파이어베이스 정보 가져오기
     */
    fun getWalkData() {
        //todo 테스트 유저 사용하기
        val userUID = firebaseAuth.currentUser?.uid

        //펫 리스트 담기
        FirebaseUserHelper().readUserWalkList(object : FirebaseUserHelper.DataStatus {
            override fun DataIsLoaded(user: User) {}
            override fun DataIsInserted() {}
            override fun DataIsUpdated() {}
            override fun DataIsDeleted() {}
            @SuppressLint("NotifyDataSetChanged")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun NodeIsLoaded(b: ArrayList<Boolean>?, keys: ArrayList<String?>?) {
                thisWalkList.clear()
                allWalkList.clear()
                for (key in keys!!) {
                    FirebaseWalkHelper().readWalk(object : FirebaseWalkHelper.DataStatus {
                        @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
                        override fun DataIsLoaded(walk: Walk) {
                            allWalkList.add(walk)

                            if(getThisMonthData(walk)){
                                thisWalkList.add(walk)
                            }
                            recyclerView.adapter?.notifyDataSetChanged()

                        }
                        override fun DataIsInserted() {}
                        override fun DataIsUpdated() {}
                        override fun DataIsDeleted() {}

                    }, key.toString())
                }
                setMonthView()

                Log.d("kej", "walk recode :::: $thisWalkList")
            }

        }, userUID.toString())
    }
}

