package com.project.petwalk.model

import java.io.Serializable


data class Walk (
    var id:String,
    var distance:Double,
    var startTime:Long,
    var endTime:Long,
    var usedTime:Long,
    var locations:Map<String,Boolean>,
    var memo:String
): Serializable