package com.project.petwalk.model

class Weather {
    var rainType :String?= ""       // 강수 형태
    var sky:String? = ""            // 하능 상태
    var temp:String? = ""           // 기온
    var fcstTime:String?=""
    override fun toString(): String {
        return "Weather(rainType='$rainType', sky='$sky', temp='$temp', fcstTime='$fcstTime')"
    }

}