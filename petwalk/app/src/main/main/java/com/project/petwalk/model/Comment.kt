package com.project.petwalk.model

data class Comment (
    var commentId:String= "",
    //    댓글의 대상이되는 글의 ID
    var postId:String = "",
    //    댓글작성자의 아이디
    var writerId:String = "",
    //    댓글 내용,
    var message:String = "",
    //    작성시간
    var writeTime:Any = Any(),
)
