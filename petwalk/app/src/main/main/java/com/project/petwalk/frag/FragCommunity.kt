package com.project.petwalk.frag

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.project.petwalk.Post
import com.project.petwalk.R
import com.project.petwalk.community.CommentActivity
import com.project.petwalk.community.CommunityWriteActivity
import com.project.petwalk.databinding.FragmentFragCommunityBinding
import com.project.petwalk.firebase.FirebasePostHelper
import com.project.petwalk.firebase.FirebaseUserHelper
import com.project.petwalk.model.ImageModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_background.view.*
import kotlinx.android.synthetic.main.card_background.view.imageView
import kotlinx.android.synthetic.main.card_post.*
import kotlinx.android.synthetic.main.card_post.view.*
import kotlinx.android.synthetic.main.fragment_frag_community.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FragCommunity : Fragment() {
    lateinit var binding: FragmentFragCommunityBinding

    val posts: MutableList<Post> = mutableListOf()

    // 로그에 TAG로 사용할 문자열
    val TAG = "MainActivity"

    // 파이어베이스의 test 키를 가진 데이터의 참조 객체를 가져온다.
    var ref = FirebaseDatabase.getInstance().getReference("test")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentFragCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // actionbar 의 타이틀을 "글목록" 으로 변경
        (activity as AppCompatActivity).supportActionBar?.title ="글목록"

        // 하단의 floatingActionButton 이 클릭될때의 리스너를 설정한다.
//        binding.floatingActionButton.setOnClickListener {
//            //Intent 생성
//            val intent = Intent(this.context, FragCommunityWrite::class.java)
//            //val intent = Intent()
//            //Intent 로 WriteActivity 실행
//            startActivity(intent)
//
//        }

        // RecyclerView 에 LayoutManager 설정
        val layoutManager = LinearLayoutManager(this.context)

        // 리싸이클러뷰의 아이템을 역순으로 정렬하게 함
        layoutManager.reverseLayout = true

        // 리싸이클러뷰의 아이템을 쌓는 순서를 끝부터 쌓게 함
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
       recyclerView.adapter = MyAdapter()

//       ----------------------------------------------------------------------


        // 글 목록을 가져오는 부분 -----------------------------------------------

        // Firebase 에서 Post 데이터를 가져온 후 posts 변수에 저장



        FirebaseDatabase.getInstance().getReference("/Posts")
            .orderByChild("writeTime").addChildEventListener(object : ChildEventListener {
                // 글이 추가된 경우
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let { snapshot ->
                        // snapshot의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let {
                            // 새 글이 마지막 부분에 추가된 경우
                            if (previousChildName == null){
                                // 글 목록을 저장하는 변수에 post 객체 추가
                                posts.add(it)
                                // RecyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                            } else {
                                // 글이 중간에 삽입된 경우 prevChildKey 로 한단계 앞의 데이터의 위치를 찾은 뒤 데이터를 추가한다.
                                val prevIndex = posts.map { it.uid }.indexOf(previousChildName)
                                posts.add(prevIndex + 1, post)
                                // RecyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                            }
                        }
                    }
                }
                // 글이 변경된 경우
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let { snapshot ->
                        //snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let { post ->
                            // 글이 변경된 경우 글의 앞의 데이터 인덱스에 데이터를 변경한다.
                            val prevIndex = posts.map { it.uid }.indexOf(previousChildName)
                            posts[prevIndex + 1] = post
                            recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                        }
                    }
                }

                //글이 삭제된 경우
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot?.let {
                        // snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)

                        //
                        post?.let { post ->
                            // 기존에 저장된 인덱스를 찾아서 해당 인덱스의 데이터를 삭제한다.
                            val existIndex = posts.map {it.uid}.indexOf(post.uid)
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                        }
                    }

                }
                // 글의 순서가 이동한 경우
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    //snapshot
                    snapshot?.let {
                        // snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)

                        post?.let { post ->
                            // 기존 인덱스를 구한다
                            val existIndex = posts.map { it.uid }.indexOf(post.uid)
                            // 기존 데이터를 지운다
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                            //previousChildName 가 없는 경우 맨 마지막으로 이동 된 것
                            if(previousChildName== null){
                                posts.add(post)
                                recyclerView.adapter?.notifyItemChanged(posts.size - 1)
                            } else {
                                //previousChildName 다음 글로 추가
                                val prevIndex = posts.map {it.uid}.indexOf(previousChildName)
                                posts.add(prevIndex + 1, post)
                                recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                            }
                        }
                    }
                }

                // 취소된 경우
                override fun onCancelled(error: DatabaseError) {
                    // 취소가 된 경우 에러를 로그로 보여준다
                    error?.toException()?.printStackTrace()
                }
            })
        //       --------------------------------------------------------------

        // 값의 변경이 있는 경우의 이벤트 리스너를 추가한다.
        ref.addValueEventListener(object: ValueEventListener {
            // 데이터 읽기가 취소된 경우 호출된다.
            // ex) 데이터의 권한이 없는 경우
            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
            // 데이터 변경이 감지 되면 호출된다.
            override fun onDataChange(snapshot: DataSnapshot) {
                // test 키를 가진 데이터 스냅샷에서 값을 읽고 문자열로 변경한다.
                val message = snapshot.value.toString()
                // 읽은 문자열을 로깅
                Log.d(TAG, message)
                // Firebase 에서 전달받은 메세지로 제목을 변경한다.
                //supportActionBar?.title = message
            }
        })

//       플로팅버튼으로 WriteActivity로 이동-----------------------------------------
        binding.floatingActionButton.setOnClickListener {
            //Intent 생성
            val intent = Intent(this.context, CommunityWriteActivity::class.java)
            //val intent = Intent()
            //Intent 로 WriteActivity 실행
            startActivity(intent)

        }
    }

    // RecyclerView 에서 사용하는 View 홀더 클래스
    inner class  MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        // 글의 배경 이미지뷰
        val imageView : ImageView = itemView.imageView
        // 글의 내용 텍스트뷰
        val contentsText : TextView = itemView.contentsText
        // 글쓴 시간 텍스트뷰
        val timeTextView : TextView = itemView.timeTextView
        // 댓글 개수 텍스트뷰
        val commentCountText : TextView = itemView.commentCountText

        val commentImageView : ImageButton = itemView.commentImage
    }

    // RecyclerView 의 어댑터 클래스
    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>(){
        // RecyclerView 에서 각 Row(행)에서 그릴 ViewHolder 를 생성할때 불리는 메소드
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.card_post,parent,false))
        }

        // 각 행의 포지션에서 그려야할 ViewHoler UI 에 데이터를 적용하는 메소드
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val post = posts[position]
            val imageURLList = post.imageList.keys

            for(imgUid in imageURLList){
                val imageUID=imgUid

                FirebasePostHelper().readImage(object:FirebasePostHelper.DataStatus{
                    override fun DataIsLoaded(post: Post) {}
                    override fun ImageIsLoaded(img: ImageModel) {
                        val imageURL=img.imageUrl

                        Log.d("kej", "get post uri::: !!! $imageURL")
                        // 배경 이미지 설정
                        Glide.with(context!!)
                            .load(imageURL)
                            .fitCenter()
                            .into(holder.imageView)
                    }
                    override fun DataIsInserted() {}
                    override fun DataIsUpdated() {}
                    override fun DataIsDeleted() {}
                    override fun NodeIsLoaded(comments: ArrayList<String?>?) {
                    }

                },imageUID)
            }
//            Picasso.get().load(Uri.parse(imageURL)).fit().centerCrop().into(holder.imageView)
            // 카드에 글을 세팅
            holder.contentsText.text = post.message
            // 글이 쓰여진 시간
//            holder.timeTextView.text = getDiffTimeText(post.writeTime as Long)
            val text:String = getDiffTimeText(post.writeTime as Long)
            holder.timeTextView.text = text
//            holder.timeTextView.setText().toString() = getDiffTimeText(post.writeTime as Long)
//            holder.timeTextView.text = getString(getDiffTimeText(post.writeTime as Long))

            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
            // 피드형 게시판 각 카드에 댓글 개수 표기
            holder.commentCountText.text = post.commentCount.toString()
            //■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■

            holder.commentImageView.setOnClickListener {
                val intent = Intent(context, CommentActivity::class.java)
                intent.putExtra("postId", post.uid)
                startActivity(intent)
            }

        }

        // RecyclerView 에서 몇개의 행을 그릴지 기준이 되는 메소드
        override fun getItemCount(): Int {
            return posts.size
        }

    }

    // 글이 쓰여진 시간을 "방금전", "시간전", "yyyy년 MM월 dd일 HH:mm" 포맷으로 변환해주는 메소드
    fun getDiffTimeText(targetTime: Long): String{
        val curDateTime = DateTime()
        val targetDateTime = DateTime().withMillis(targetTime)

        val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
        val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
        val diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).minutes
        if(diffDay == 0) {
            if(diffHours == 0 && diffMinutes == 0){
                return "방금 전"
            }
            return if(diffHours > 0){
                ""+ diffHours + "시간 전"
            } else "" + diffMinutes + "분 전"
        } else {
            val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
            return format.format(Date(targetTime)).toString()
        }
    }


}