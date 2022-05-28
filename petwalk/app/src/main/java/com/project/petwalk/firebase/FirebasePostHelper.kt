package com.project.petwalk.firebase

import android.util.Log
import com.google.firebase.database.*
import com.project.petwalk.Post
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.Pet

class FirebasePostHelper {
    private var mDatabase: FirebaseDatabase? = null
    private var mRefrencePost: DatabaseReference? = null
    private var mReferenceUser: DatabaseReference?=null
    private var mReferenceImage:DatabaseReference?=null
    private var bases // 조회 결과 Array List
            : ArrayList<Pet?>? = null
    private var keys:ArrayList<String?>?=null

    interface DataStatus {
        fun DataIsLoaded(post: Post)
        fun ImageIsLoaded(img:ImageModel)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
        fun NodeIsLoaded(comments: ArrayList<String?>?)
    }

    init {
        mDatabase = FirebaseDatabase.getInstance()
        mRefrencePost = mDatabase!!.getReference("Posts")
        mReferenceUser=mDatabase!!.getReference("Users")
        mReferenceImage = mDatabase!!.getReference("postImages")
    }

    fun readPosts(dataStatus: DataStatus) {
        mRefrencePost?.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value !== null) {
                    keys = arrayListOf()
                    bases = arrayListOf()
                    bases!!.clear()

                    val post = snapshot.getValue(Post::class.java) as Post
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun readCommentList(postUID:String, dataStatus:DataStatus){
        mRefrencePost?.child(postUID)?.child("commentList")?.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                keys= arrayListOf()

                for(data in snapshot.children){
                    keys!!.add(data.key)
                }

                dataStatus.NodeIsLoaded(keys)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun readImage(dataStatus: DataStatus, key:String){
        mReferenceImage!!.child(key).addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val image = snapshot.getValue(ImageModel::class.java) as ImageModel
                dataStatus.ImageIsLoaded(image)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun addPost(post: Post, userUID:String, dataStatus: DataStatus) {
        val key = mRefrencePost!!.push().key //note 키 생성
        post.uid = key.toString()
        Log.d("kej", "save post :::: $post, key::$key")
        mRefrencePost!!.child(key!!)
            .setValue(post)
            .addOnSuccessListener {
                mReferenceUser?.child(userUID)
                    ?.child("postList")
                    ?.child(key)?.setValue(true)
                    ?.addOnSuccessListener {
                        dataStatus.DataIsInserted()
                    }
            }

    }

    fun updatePost(key: String?, post: Post?, dataStatus: DataStatus) {
        mRefrencePost!!.child(key!!).setValue(post)
            .addOnSuccessListener { dataStatus.DataIsUpdated() }
    }

    fun deletePost(postUID: String?, dataStatus: DataStatus) {
        mRefrencePost!!.child(postUID!!).removeValue().addOnSuccessListener {
            dataStatus.DataIsDeleted()
        }
    }
}