package fastcampus.aop.part3.chapter06

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import fastcampus.aop.part3.chapter06.chatlist.ChatListFragment
import fastcampus.aop.part3.chapter06.home.HomeFragment
import fastcampus.aop.part3.chapter06.mypage.MyPageFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //각각의 프래그먼트 생성
        val homeFragment = HomeFragment()
        val chatListFragment = ChatListFragment()
        val myPageFragment = MyPageFragment()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        replaceFragment(homeFragment)       //초기값 homeFragment

        //하단 메뉴 선택에 따른 프래그먼트 선택하여 FrameLayout에 붙일지
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.chatList -> replaceFragment(chatListFragment)
                R.id.myPage -> replaceFragment(myPageFragment)
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        //액티비티에는 supportFragmentManager이 있다
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }


    }

}