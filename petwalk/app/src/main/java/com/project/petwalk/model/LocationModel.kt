package com.project.petwalk.model

import java.io.Serializable

data class LocationModel (
    var latitude:Double,
    var longitude:Double,
    var time:Long
    ): Serializable