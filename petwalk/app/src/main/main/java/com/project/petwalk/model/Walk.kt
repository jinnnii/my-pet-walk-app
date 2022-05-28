package com.project.petwalk.model

import java.io.Serializable


data class Walk (
    var distance:Double=0.0,
    var startTime:Long=0L,
    var endTime:Long=0L,
    var usedTime:Long=0L,
    var locations:Map<String,Boolean> = mapOf(),
    var memo:String="",
    var uid:String="",
): Serializable