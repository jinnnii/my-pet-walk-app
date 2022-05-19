package com.project.petwalk.community

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.core.RepoManager.clear
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.project.petwalk.Comment
import com.project.petwalk.R
import com.project.petwalk.databinding.ActivityCommentBinding
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.card_comment.*
import kotlinx.android.synthetic.main.card_comment.view.*

class CommentActivity : AppCompatActivity() {

    lateinit var binding: ActivityCommentBinding
    val comment = Comment()
    val fireDatabase = FirebaseDatabase.getInstance() //f -> 파이어베이스데이터베이스의 자료를 담음
    var database = Firebase.database.reference // 데이터베이스레퍼런스를 위한 인스턴트 생성

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.commentRecyclerView.adapter = CommentRecyclerviewAdapter()

        // commentRecyclerView에서 LinearLayout을 안써서 필요한건지 잘 모르겠음
        //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)
        //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■

        // 댓글 개수 세기위해 은진님이 작성중이던 코드
        //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
        (binding.commentRecyclerView.adapter as CommentRecyclerviewAdapter).itemCount
        //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■


        binding.commentSubmit.setOnClickListener {
            comment.message = CommentText.text.toString()
            comment.writeTime = System.currentTimeMillis()
            comment.commentId =
                fireDatabase.getReference("Comments/${comment.postId}/").push().toString()// 이 코멘트의 고유값

            // 계정받아오고나서는 if문 없어도 되는 부분
            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
            if(comment.writerId==null){
                comment.writerId="test"
            } else{
            comment.writerId = FirebaseAuth.getInstance().currentUser?.email.toString()}
            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■

            // 지금 들어와있는 글의 id - 게시글파트랑 통합후 if문이 필요한지 확인?
            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
            var postid = intent.getStringExtra("postId")
            if (postid != null) {
                comment.postId = postid
            } else {
                comment.postId = "test"
            }
            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
            fireDatabase.getReference("Comments").child(comment.postId).child(comment.commentId).setValue(comment)
        }
    }
    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        //댓글들을 담을 어레이리스트
        var comments : ArrayList<Comment> = arrayListOf()
        //데이터 담기
        init {
            fireDatabase.reference.child("Comments")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("ayr","for문 이전................................")
                        comments.clear()
                        for (data in snapshot.children) {
                            Log.d("ayr","for문 내부................................")
                            Log.d("ayr","${data.value}")
                            comments.add(data.getValue<Comment>()!!)
                            Log.d("ayr","${comments}")
                            println(data)
                        }
                        notifyDataSetChanged()
                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.card_comment,parent,false)
            return CustomViewHolder(view)
        }

        private  inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view) // 리싸이클러 뷰홀더를 상속받고 뷰 넘겨주기

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.commentNick.text = comments[position].writerId //닉네임 매핑
            holder.itemView.CommentTextView.text=comments[position].message //내용 매핑
            Log.d("ayr", "<AFDAFADSFDSAFDAGDSAGDSGDTDSA")

            // 테스트를 위해 특정값 지정해서 넣은것. 수정필요.
            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
            fireDatabase.getReference("Users")
                .child("wee45387")
                .child("profile")
                .child("test")
                .child("imageUrl")
                .addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageUri = snapshot.getValue(Map::class.java)
                        val uriKey = imageUri?.keys.toString()
                        Log.d("ayr",uriKey+"><<<<<<<")
//                        fireDatabase.getReference("profileImages").child(uriKey).addListenerForSingleValueEvent(object : ValueEventListener{
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                Log.d("ayr",snapshot.value.toString())
//                            }
//                            override fun onCancelled(error: DatabaseError) {
//                                Log.d("ayr","데이터를 불러오지 못했습니다.")
//                            }
//                        })
                    }
            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })


        }

        override fun getItemCount(): Int {
            return comments.size
        }
//        init {
//            fireDatabase.getReference().orderByChild(w)
//            FirebaseFirestore.getInstance() // 파이어베이스 데이터를 읽어오는 부분
//
//                    //////////////////////////////////////////////////////
//
//
//                .collection("posts")
//                .document(writerId)
//                .collection("comments")
//                .orderBy(writeTime)  // 시간순으로 읽어오기(timestemp?)
//
//                    //////////////////////////////////////////////////////
//
//                .addSnapshotListener { quarySnapshot, FirebaseFirestoreException ->
//                    comments.clear() //값이 중복으로 쌓일수있기때문에 clear() 넣어주기
//                    if(quarySnapshot == null)return@addSnapshotListener //코드 안정성을 위해 쿼리스냅샷이 널일때는 리턴값널어주기
//
//                    for(snapshot in quarySnapshot.documents!!){ //for문으로 스냅샷 하나씩 읽어오기
//                        comments.add(snapshot.toObjext(Comment::class.java)!!) //느낌표두개로 널세이프티 해제해주기
//                    }
//                    notifyDataSetChanged() // 리싸이클러뷰를 새로고침해주기
//                }
//        }
    }
}