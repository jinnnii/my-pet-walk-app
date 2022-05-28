package com.project.petwalk.model

import java.io.Serializable

data class TravelDetail (
        val contentSeq:Int,
        val areaName:String,
        val partName:String,
        val title:String,
        val keyword:String,
        val address:String,
        val tel:String,
        val usedTime:String,
        val homePage:String,
        val content:String,
        val provisionSupply:String,
        val petFacility:String,
        val restaurant:String,
        val parkingLog:String,
        val mainFacility:String,
        val usedCost:String,
        val policyCautions:String,
        val emergencyResponse:String,
        val memo:String,
        val bathFlag:String,
        val provisionFlag:String,
        val petFlag:String,
        val petWeight:String,
        val dogBreed:String,
        val emergencyFlag:String,
        val entranceFlag:String,
        val parkingFlag:String,
        val inOutFlag:String,
        val imageList:List<Map<String, String>>
        ):Serializable
