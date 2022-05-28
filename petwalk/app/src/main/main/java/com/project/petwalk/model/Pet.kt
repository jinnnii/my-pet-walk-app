package com.project.petwalk.model

import java.io.Serializable

data class Pet (
    var animal : String="",
    var kind : String ="",
    var name : String = "",
    var profile:String="",
    var uid:String="",
):Serializable