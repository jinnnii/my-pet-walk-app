package com.project.petwalk.walk

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityWalkResultBinding
import com.project.petwalk.model.LocationModel
import com.project.petwalk.model.TravelDetail
import com.project.petwalk.model.Walk
import java.text.SimpleDateFormat

class WalkResultActivity : AppCompatActivity() {
    lateinit var binding: ActivityWalkResultBinding
    lateinit var walk: Walk
    lateinit var locations:List<LocationModel>

    //파이어베이스
    val firebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val reference: DatabaseReference =database.getReference("locations")
    val walkReference: DatabaseReference = database.getReference("walk")
    val userReference:DatabaseReference=database.getReference("Users")


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWalkResultBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        getExtra()
        // 산책 기록하기
        val sdf = SimpleDateFormat("mm분 ss초")
        val time = sdf.format(walk.usedTime)
        binding.result.text = "$time 초 동안\n${walk.distance}km 산책을 했어요"

        binding.saveBtn.setOnClickListener {
            //todo 파이어베이스에 위치 정보 저장하기
            val locKey = reference.push().key!!
            for( i in locations.indices){
                sendLocation(
                    i,
                    locKey,
                    locations[i].latitude,
                    locations[i].longitude,
                    locations[i].time)
            }

            //todo 파이어베이스에 walk 정보 저장하기
            sendWalk(locKey)


            val intent = Intent()
            setResult(RESULT_OK,intent)
            finish()

        }

        binding.cancleBtn.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK,intent)
            finish()
        }
    }

    /**
     * 받아온 walk 객체 정보 출력
     */
    private fun getExtra() {
        if (intent.hasExtra("walk")) {
            walk = intent.getSerializableExtra("walk") as Walk
        }
        if(intent.hasExtra("locations")){
            locations = intent.getSerializableExtra("locations") as List<LocationModel>
        }
    }

    /**
     * 파이어베이스에 위치정보 저장
     */
    private fun sendLocation(idx:Int, key:String, lat:Double, lon:Double, time:Long){
        //todo walk 객체 저장
        val location= LocationModel( lat, lon, time)
        reference.child(key).child(idx.toString()).setValue(location)

    }

    /**
     * 파이어베이스에 walk 정보 저장
     */
    private fun sendWalk(locKey:String){
        val walkKey = walkReference.push().key!!
        walk.locations = mapOf(locKey to true)
        walk.uid=walkKey
        walk.memo=binding.edMemo.text.toString()
        walkReference.child(walkKey).setValue(walk)

        sendUser(walkKey)
    }

    private fun sendUser(walkKey:String){
        val userUid= firebaseAuth.currentUser?.uid.toString()
        userReference.child(userUid).child("walkList").child(walkKey).setValue(true)
    }

}