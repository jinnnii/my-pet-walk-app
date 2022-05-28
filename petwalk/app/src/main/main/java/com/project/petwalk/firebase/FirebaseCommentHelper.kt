package com.project.petwalk.firebase

import android.support.annotation.NonNull
import android.util.Log
import com.google.firebase.database.*
import com.project.petwalk.model.Comment
import com.project.petwalk.model.User

class FirebaseCommentHelper {
    private var mDatabase: FirebaseDatabase? = null
    private var mRefrencePost: DatabaseReference? = null
    private var mRefrenceComment: DatabaseReference? = null
    private var mReferenceUser:DatabaseReference?=null

    private var bases:ArrayList<Comment?>?=null
    private var users:ArrayList<User?>?=null
    var keys:ArrayList<String?>?=null


    interface DataStatus {
        fun DataIsLoaded(comment:Comment, user:User)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    init {
        mDatabase = FirebaseDatabase.getInstance()
        mRefrencePost = mDatabase!!.getReference("Posts")
        mRefrenceComment = mDatabase!!.getReference("Comments")
        mReferenceUser= mDatabase!!.getReference("Users")
    }


    fun readComments(dataStatus: DataStatus, key:String) {
        mRefrenceComment?.child(key)?.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("kej", "getCommentItem>>>>>!!! ${snapshot.value}")
                val comment = snapshot.getValue(Comment::class.java)
                mReferenceUser?.child(comment!!.writerId)?.get()?.addOnSuccessListener {
                    val user = it.getValue(User::class.java) as User

                    dataStatus.DataIsLoaded(comment, user)
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }



    fun addComment(commment: Comment, postUID:String, dataStatus: DataStatus) {
        val key = mRefrenceComment!!.push().key //note 키 생성
        commment.commentId=key!!
        mRefrenceComment!!.child(key)
            .setValue(commment)
            .addOnSuccessListener {
                mRefrencePost?.child(postUID)
                    ?.child("commentList")
                    ?.child(key)?.setValue(true)
                    ?.addOnSuccessListener {
                        mRefrencePost?.child(postUID)
                            ?.child("commentCount")?.get()?.addOnSuccessListener {
                                val count = it.value.toString().toInt()+1
                                mRefrencePost?.child(postUID)
                                    ?.child("commentCount")
                                    ?.setValue(count)
                                    dataStatus.DataIsInserted()
                            }
                    }
            }

    }

    fun updateComment(key: String?, comment: Comment?, dataStatus: DataStatus) {
        mRefrenceComment!!.child(key!!).setValue(comment)
            .addOnSuccessListener {
                dataStatus.DataIsUpdated()
            }
    }

    fun deleteComment(commentUID: String?, dataStatus: DataStatus) {
        mRefrenceComment!!.child(commentUID!!).removeValue().addOnSuccessListener {
            dataStatus.DataIsDeleted()
        }
    }
}