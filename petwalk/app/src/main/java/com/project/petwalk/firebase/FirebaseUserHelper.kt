package com.project.petwalk.firebase

import android.support.annotation.NonNull
import com.google.firebase.database.*
import com.project.petwalk.model.Pet
import com.project.petwalk.model.User

class FirebaseUserHelper {
    private var mDatabase: FirebaseDatabase? = null
    private var mRefrenceUser: DatabaseReference? = null
    private var bases // 조회 결과 Array List
            : ArrayList<Pet?>? = null
    private var nBases:ArrayList<Boolean>?=null
    var keys: ArrayList<String?>? = null

    interface DataStatus {
        fun DataIsLoaded(user:User?){}
        fun DataIsInserted(){}
        fun DataIsUpdated(){}
        fun DataIsDeleted(){}
        fun NodeIsLoaded(b:ArrayList<Boolean>?, keys: ArrayList<String?>?){}
    }

    init {
        mDatabase = FirebaseDatabase.getInstance()
        mRefrenceUser = mDatabase!!.getReference("Users")
    }

    fun readUser(dataStatus: DataStatus,key:String) {
        mRefrenceUser?.child(key)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange( snapshot: DataSnapshot) {
                val user:User ?= snapshot.getValue(User::class.java)
                dataStatus.DataIsLoaded(user)
            }

            override fun onCancelled(@NonNull error: DatabaseError) {}
        })
    }

    fun readUserPetList(dataStatus: DataStatus,key:String){
        mRefrenceUser!!.child(key).child("petList").addValueEventListener(object : ValueEventListener {
            override fun onDataChange( snapshot: DataSnapshot) {
                nBases = arrayListOf()
                nBases!!.clear()
                keys = ArrayList()
                keys!!.clear()

                for (node in snapshot.children) {
                    keys!!.add(node.key)
                    val bool:Boolean? = node.getValue(Boolean::class.java)
                    nBases!!.add(bool!!)
                }
                dataStatus.NodeIsLoaded(nBases, keys)
            }

            override fun onCancelled(@NonNull error: DatabaseError) {}
        })
    }

    fun readUserWalkList(dataStatus: DataStatus,key:String){
        mRefrenceUser?.child(key)?.child("walkList")?.get()?.addOnSuccessListener {
            nBases = arrayListOf()
            nBases!!.clear()
            keys = ArrayList()
            keys!!.clear()

            for (node in it.children) {
                keys!!.add(node.key)
                val bool:Boolean? = node.getValue(Boolean::class.java)
                nBases!!.add(bool!!)
            }
            dataStatus.NodeIsLoaded(nBases, keys)
        }
    }



    fun addUser(user: User?, dataStatus: DataStatus) {
        val key = mRefrenceUser!!.push().key //note 키 생성
        mRefrenceUser!!.child(key!!).setValue(user)
            .addOnSuccessListener { dataStatus.DataIsInserted() }
    }


    fun updateUser(key: String?, user: User?, dataStatus: DataStatus) {
        mRefrenceUser!!.child(key!!).setValue(user)
            .addOnSuccessListener { dataStatus.DataIsUpdated() }
    }

    fun deleteUser(key: String?, dataStatus: DataStatus) {
        mRefrenceUser!!.child(key!!).setValue(null)
            .addOnSuccessListener { dataStatus.DataIsDeleted() }
    }

    fun deleteUserPet(userUID:String, petUID:String,dataStatus: DataStatus){
        mRefrenceUser!!.child(userUID).child("petList").child(petUID).removeValue()
            .addOnSuccessListener { dataStatus.DataIsDeleted() }
    }
    fun deleteUserPost(userUID:String, postUID:String,dataStatus: DataStatus){
        mRefrenceUser!!.child(userUID).child("postList").child(postUID).removeValue()
            .addOnSuccessListener { dataStatus.DataIsDeleted() }
    }

}