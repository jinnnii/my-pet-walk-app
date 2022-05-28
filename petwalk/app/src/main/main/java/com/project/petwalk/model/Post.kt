package com.project.petwalk

data class Post (
//    글의 ID
    var uid:String = "",
//    글 작성자의 ID
    var writerId:String = "",
//    글의 메세지
    var message:String = "",
    //    글이 쓰여진 시간
    var writeTime:Any=Any(),
    //    글의 배경이미지
    var imageList:Map<String,Boolean> = mapOf(),
    //    댓글의 개수
    var commentCount:Int = 0,
    var commentList:Map<String,Boolean> = mapOf(),
)