package com.project.petwalk.model

import java.io.Serializable

data class LocationModel (
    var latitude:Double=0.0,
    var longitude:Double=0.0,
    var time:Long=0L
    ): Serializable