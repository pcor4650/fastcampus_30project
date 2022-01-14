package fastcampus.aop.part3.aop_part3_chapter05_final

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.*
import fastcampus.aop.part3.aop_part3_chapter05_final.DBKey.Companion.DIS_LIKE
import fastcampus.aop.part3.aop_part3_chapter05_final.DBKey.Companion.LIKE
import fastcampus.aop.part3.aop_part3_chapter05_final.DBKey.Companion.LIKED_BY
import fastcampus.aop.part3.aop_part3_chapter05_final.DBKey.Companion.MATCH
import fastcampus.aop.part3.aop_part3_chapter05_final.DBKey.Companion.NAME
import fastcampus.aop.part3.aop_part3_chapter05_final.DBKey.Companion.USERS
import fastcampus.aop.part3.aop_part3_chapter05_final.DBKey.Companion.USER_ID

//CatdStackListener 등록
class LikeActivity : AppCompatActivity(), CardStackListener {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference

    private val adapter = CardStackAdapter()
    private val cardItems = mutableListOf<CardItem>()

    private val manager by lazy {
        CardStackLayoutManager(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        userDB = Firebase.database.reference.child(USERS)

        val currentUserDB = userDB.child(getCurrentUserID())
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(NAME).value == null) {
                    showNameInputPopup()  //alert dialot 구현
                    return
                }

                getUnSelectedUsers()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

        initCardStackView()
        initSignOutButton()
        initMatchedListButton()
    }

    private fun initCardStackView() {
        val stackView = findViewById<CardStackView>(R.id.cardStackView)

        stackView.layoutManager = manager
        stackView.adapter = adapter

        manager.setStackFrom(StackFrom.Top)
        manager.setTranslationInterval(8.0f)
        manager.setSwipeThreshold(0.1f)
    }

    private fun initSignOutButton() {
        val signOutButton = findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener {
            auth.signOut()      //Firebase auth 로그아웃
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun initMatchedListButton() {
        val matchedListButton = findViewById<Button>(R.id.matchListButton)
        matchedListButton.setOnClickListener {
            startActivity(Intent(this, MatchListActivity::class.java))
        }
    }

    private fun getCurrentUserID(): String {
        //로그인이 되어있지 않다면
        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인이 되어있지않습니다.", Toast.LENGTH_SHORT).show()
            finish()  //LikeActivity 종료
        }

        return auth.currentUser.uid
    }

    //선택되지 않은 다른 유저 정보를 가져오는 메소드
    fun getUnSelectedUsers() {
        userDB.addChildEventListener(object : ChildEventListener {
            //유저가 add 될때 불린다
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.child(USER_ID).value != getCurrentUserID()     //유저 아이디가 나와 다르고
                    && snapshot.child(LIKED_BY).child(LIKE).hasChild(getCurrentUserID()).not()      //내가 like한 유저가 아니면서
                    && snapshot.child(LIKED_BY).child(DIS_LIKE).hasChild(getCurrentUserID()).not()      //내가 dislike한 유저가 아니면
                ) {

                    //조건에 맞는 유저 정보를 가져온다
                    val userId = snapshot.child(USER_ID).value.toString()
                    var name = "undecided"  //가입한 유저가 아직 이름을 작성하지 않았으면 undecided로 표시
                    if (snapshot.child(NAME).value != null) {
                        name = snapshot.child(NAME).value.toString()
                    }

                    cardItems.add(CardItem(userId, name))       //미리 만든 cardItems 리스트에 유저정보 추가
                    adapter.submitList(cardItems)
                    adapter.notifyDataSetChanged()      //리싸이클러 뷰 갱신 명령어
                }
            }
            //상대방의 데이터가 변경되었을 때
            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                cardItems.find { it.userId == dataSnapshot.key }?.let {
                    it.name = dataSnapshot.child("name").value.toString()
                }
                adapter.submitList(cardItems)
                adapter.notifyDataSetChanged()
            }
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun showNameInputPopup() {
        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.write_name))
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                if (editText.text.isEmpty()) {
                    showNameInputPopup()
                } else {
                    saveUserName(editText.text.toString())
                }

            }
            .setCancelable(false)  //alert dialog 떳을 때 취소하지 못하도록
            .show()

    }

    private fun saveUserName(name: String) {

        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()
        user[USER_ID] = userId
        user[NAME] = name
        currentUserDB.updateChildren(user)


        getUnSelectedUsers()
    }

    //내가 좋아요 한 내용은 상대방의 db의 liked by, disliked by에 저장한다
    private fun like() {
        val card = cardItems[manager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId)   //상대방의 userId
            .child(LIKED_BY)
            .child(LIKE)
            .child(getCurrentUserID())  //key가 된다
            .setValue(true)     //value다?

        saveMatchIfOtherLikeMe(card.userId)

        Toast.makeText(this, "${card.name}님을 Like 하셨습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun saveMatchIfOtherLikeMe(otherUserId: String) {
        val otherUserDB = userDB.child(getCurrentUserID()).child(LIKED_BY).child(LIKE).child(otherUserId)
        otherUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == true) {
                    userDB.child(getCurrentUserID())    //내 db에 match 되었다고 db 변경
                        .child(LIKED_BY)
                        .child(MATCH)
                        .child(otherUserId)
                        .setValue(true)

                    userDB.child(otherUserId)       //상대방 db에 match 되었다고 db 변경
                        .child(LIKED_BY)
                        .child(MATCH)
                        .child(getCurrentUserID())
                        .setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun disLike() {
        val card = cardItems[manager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId)
            .child(LIKED_BY)
            .child(DIS_LIKE)
            .child(getCurrentUserID())
            .setValue(true)

        Toast.makeText(this, "DisLike 하셨습니다.", Toast.LENGTH_SHORT).show()
    }

    //CardStackListener 추가에 따른 오버라이드 메소드
    override fun onCardDragging(direction: Direction?, ratio: Float) {}

    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Right -> like()
            Direction.Left -> disLike()
            else -> {}
        }
    }

    override fun onCardRewound() {}

    override fun onCardCanceled() {}

    override fun onCardAppeared(view: View?, position: Int) {}

    override fun onCardDisappeared(view: View?, position: Int) {}
}