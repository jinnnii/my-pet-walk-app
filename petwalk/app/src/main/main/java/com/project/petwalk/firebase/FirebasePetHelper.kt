package com.project.petwalk.firebase

import android.support.annotation.NonNull
import android.util.Log
import com.google.firebase.database.*
import com.project.petwalk.model.Pet
import com.project.petwalk.model.User


class FirebasePetHelper {
    private var mDatabase: FirebaseDatabase? = null
    private var mRefrencePet: DatabaseReference? = null
    private var mUserReference:DatabaseReference?=null
    private var bases // 조회 결과 Array List
            : ArrayList<Pet?>? = null

    interface DataStatus {
        fun DataIsLoaded(pet:Pet)
        fun DataIsInserted()
        fun DataIsUpdated()
        fun DataIsDeleted()
    }

    init {
        mDatabase = FirebaseDatabase.getInstance()
        mRefrencePet = mDatabase!!.getReference("pets")
        mUserReference=mDatabase!!.getReference("Users")
    }

    fun readPet(dataStatus: DataStatus, key:String) {
        mRefrencePet?.child(key)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange( snapshot: DataSnapshot) {
                if(snapshot.value!==null){
                    val pet = snapshot.getValue(Pet::class.java) as Pet
                    dataStatus.DataIsLoaded(pet)
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {}
        })
    }


    fun addPet(pet:Pet, userUID:String,  dataStatus: DataStatus) {
        val key = mRefrencePet!!.push().key //note 키 생성
        pet.uid=key.toString()

        mRefrencePet!!.child(key!!)
            .setValue(pet)
            .addOnSuccessListener {
                mUserReference?.child(userUID)
                    ?.child("petList")
                    ?.child(key)?.setValue(true)
                    ?.addOnSuccessListener {
                        dataStatus.DataIsInserted()
                    }
            }

    }

    fun updatePet(key: String?, recipe: Pet?, dataStatus: DataStatus) {
        mRefrencePet!!.child(key!!).setValue(recipe)
            .addOnSuccessListener { dataStatus.DataIsUpdated() }
    }

    fun deletePet(petUID: String?, userUID:String, dataStatus: DataStatus) {
        mRefrencePet!!.child(petUID!!).removeValue().addOnSuccessListener {
            dataStatus.DataIsDeleted()
        }
    }
}