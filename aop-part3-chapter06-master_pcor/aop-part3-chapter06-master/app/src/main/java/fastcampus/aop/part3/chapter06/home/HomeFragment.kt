package fastcampus.aop.part3.chapter06.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fastcampus.aop.part3.chapter06.DBKey.Companion.CHILD_CHAT
import fastcampus.aop.part3.chapter06.DBKey.Companion.DB_ARTICLES
import fastcampus.aop.part3.chapter06.DBKey.Companion.DB_USERS
import fastcampus.aop.part3.chapter06.R
import fastcampus.aop.part3.chapter06.chatlist.ChatListItem
import fastcampus.aop.part3.chapter06.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference
    private lateinit var articleAdapter: ArticleAdapter


    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)  //ArticleModel 각각의 항목으로 매핑하여 저장
            articleModel ?: return      // null이면 return을 하여 예외처리 해주고

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}


    }

    private var binding: FragmentHomeBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view) //FragmentHomeBinding이 nullable이기에 onViewCreated내에서 절대 null이 될수 없는 fragmentHomeBinding로 사용
        binding = fragmentHomeBinding

        //더미값 넣어서 확인해보기
//        articleAdapter = ArticleAdapter(onItemClicked = {})
//        articleAdapter.submitList(mutableListOf<ArticleModel>().apply {
//            add(ArticleModel("0", "sell iphone 8+", 1000000, "5000원" , "" ))
//        })

        articleList.clear()     //초기화 안해주면 어떻게 될까?
        userDB = Firebase.database.reference.child(DB_USERS)
        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleAdapter = ArticleAdapter(onItemClicked = { articleModel ->       //항목 눌렀을 때 조건에 따른 처리
            // 로그인을 한 상태
            if (auth.currentUser != null) {
                //다른사람이 올린걸 눌렀으면
                if (auth.currentUser.uid != articleModel.sellerId) {

                    val chatRoom = ChatListItem(
                        buyerId = auth.currentUser.uid,     //내 id를 바이어로
                        sellerId = articleModel.sellerId,
                        itemTitle = articleModel.title,
                        key = System.currentTimeMillis()    //현재 시점
                    )

                    userDB.child(auth.currentUser.uid)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    userDB.child(articleModel.sellerId)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)


                    Snackbar.make(view, "채팅방이 생성되었습니다. 채팅탭에서 확인해주세요.", Snackbar.LENGTH_LONG).show()


                } else {
                    // 내가 올린 아이템
                    Snackbar.make(view, "내가 올린 아이템입니다", Snackbar.LENGTH_LONG).show()
                }
            } else {
                // 로그인을 안한 상태
                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }




        })

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            context?.let {
                if (auth.currentUser != null) {
                    val intent = Intent(it, AddArticleActivity::class.java)
                    startActivity(intent)
                } else {
                    Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
                }
            }


        }


        articleDB.addChildEventListener(listener)


    }

    override fun onResume() {
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //listener를 꼭 remove 해줘야하나?
        articleDB.removeEventListener(listener)
    }





}