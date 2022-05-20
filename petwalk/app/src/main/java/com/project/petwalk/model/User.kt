package com.project.petwalk.model

data class User (
    var uid: String="",
    var email: String="",
    var name: String?= null,
    var phone: String? =null,
    var walkList:List<Map<String,Walk>> = arrayListOf(),
    var petList:List<Map<String,Pet>> = arrayListOf(),
    val profile:Map<String,Boolean> = mapOf()
)

