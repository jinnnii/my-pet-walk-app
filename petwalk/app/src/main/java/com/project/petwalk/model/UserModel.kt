package com.project.petwalk.model

data class UserModel (
    var email:String="",
    var name:String="",
    var uid:String="",
    var phone:String="",
    var walkList:Map<String,Boolean> = mapOf()
    )