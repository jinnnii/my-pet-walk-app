package com.project.petwalk.firebase

import android.support.annotation.NonNull
import com.google.firebase.database.*
import com.project.petwalk.model.ImageModel
import com.project.petwalk.model.Walk

class FirebaseImageHelper {
    private var mDatabase: FirebaseDatabase? = null
    private var mRefrenceImage: DatabaseReference? = null
    private var mRefrenceUser: DatabaseReference? = null

    interface DataStatus {
        fun DataIsLoaded(image: ImageModel)
//        fun DataIsInserted()
//        fun DataIsUpdated()
//        fun DataIsDeleted()
    }

    init {
        mDatabase = FirebaseDatabase.getInstance()
        mRefrenceImage = mDatabase!!.getReference("profileImages")
        mRefrenceUser = mDatabase!!.getReference("Users")
    }


    fun readImage(dataStatus: DataStatus, key:String) {
        mRefrenceImage?.child(key)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange( snapshot: DataSnapshot) {
                if(snapshot.value!==null){
                    val image = snapshot.getValue(ImageModel::class.java) as ImageModel
                    dataStatus.DataIsLoaded(image)
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {}
        })
    }



    fun addImage(image: ImageModel, userUID:String, dataStatus: DataStatus) {
        val key = mRefrenceImage!!.push().key //note 키 생성
        mRefrenceImage!!.child(key!!)
            .setValue(image)
            .addOnSuccessListener {
                mRefrenceUser?.child(userUID)
                    ?.child("profile")
                    ?.child(key)?.setValue(true)
                    ?.addOnSuccessListener {
//                        dataStatus.DataIsInserted()
                    }
            }

    }

    fun updateImage(key: String?, walk: Walk?, dataStatus: DataStatus) {
        mRefrenceImage!!.child(key!!).setValue(walk)
            .addOnSuccessListener {
//                dataStatus.DataIsUpdated()
            }
    }

    fun deleteImage(petUID: String?, dataStatus: DataStatus) {
        mRefrenceImage!!.child(petUID!!).removeValue().addOnSuccessListener {
//            dataStatus.DataIsDeleted()
        }
    }
}