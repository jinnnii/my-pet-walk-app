package com.project.petwalk.firebase

import android.support.annotation.NonNull
import com.google.firebase.database.*
import com.project.petwalk.model.Pet
import com.project.petwalk.model.Walk

class FirebaseWalkHelper {
    private var mDatabase: FirebaseDatabase? = null
    private var mRefrenceWalk: DatabaseReference? = null
    private var mRefrenceUser: DatabaseReference? = null

    interface DataStatus {
        fun DataIsLoaded(walk: Walk)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    init {
        mDatabase = FirebaseDatabase.getInstance()
        mRefrenceWalk = mDatabase!!.getReference("walk")
        mRefrenceUser = mDatabase!!.getReference("Users")
    }


    fun readWalk(dataStatus: DataStatus, key:String) {
        mRefrenceWalk?.child(key)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange( snapshot: DataSnapshot) {
                if(snapshot.value!==null){
                    val walk = snapshot.getValue(Walk::class.java) as Walk
                    dataStatus.DataIsLoaded(walk)
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {}
        })
    }


    fun addWalk(walk: Walk, userUID:String, dataStatus: DataStatus) {
        val key = mRefrenceWalk!!.push().key //note 키 생성
        walk.uid=key.toString()

        mRefrenceWalk!!.child(key!!)
            .setValue(walk)
            .addOnSuccessListener {
                mRefrenceUser?.child(userUID)
                    ?.child("walkList")
                    ?.child(key)?.setValue(true)
                    ?.addOnSuccessListener {
                        dataStatus.DataIsInserted()
                    }
            }

    }

    fun updateWalk(key: String?, walk: Walk?, dataStatus: DataStatus) {
        mRefrenceWalk!!.child(key!!).setValue(walk)
            .addOnSuccessListener { dataStatus.DataIsUpdated() }
    }

    fun deletePet(petUID: String?, dataStatus: DataStatus) {
        mRefrenceWalk!!.child(petUID!!).removeValue().addOnSuccessListener {
            dataStatus.DataIsDeleted()
        }
    }
}