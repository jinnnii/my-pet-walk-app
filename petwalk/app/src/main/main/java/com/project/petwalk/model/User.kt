package com.project.petwalk.model

import java.io.Serializable

data class User (
    var uid: String="",
    var email: String="",
    var name: String?= null,
    var phone: String? =null,
    var walkList:Map<String,Boolean> = mapOf(),
    var petList:Map<String,Boolean> = mapOf(),
    val profile:Map<String,Boolean> = mapOf(),
):Serializable

