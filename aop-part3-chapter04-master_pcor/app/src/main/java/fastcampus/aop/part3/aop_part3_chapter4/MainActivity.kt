package fastcampus.aop.part3.aop_part3_chapter4

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import fastcampus.aop.part3.aop_part3_chapter4.adapter.BookAdapter
import fastcampus.aop.part3.aop_part3_chapter4.adapter.HistoryAdapter
import fastcampus.aop.part3.aop_part3_chapter4.api.BookAPI
import fastcampus.aop.part3.aop_part3_chapter4.databinding.ActivityMainBinding
import fastcampus.aop.part3.aop_part3_chapter4.model.BestSellerDto
import fastcampus.aop.part3.aop_part3_chapter4.model.History
import fastcampus.aop.part3.aop_part3_chapter4.model.SearchBooksDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var service: BookAPI

    private lateinit var db: AppDatabase

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) //setContentView 전에 , activity_main이어 -> ActivityMainBinding
        setContentView(binding.root)  //activity_main layout 말고 

        //Room을 사용해 database 생성하
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"   //BookSearchDB로 바꾸
        ).build()

        adapter = BookAdapter(clickListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("bookModel", it)    //"bookModel로 한번에 데이터 클래스로 데이터 넘기기
            startActivity(intent)
        })

        //initHistoryRecyclerView method로
       historyAdapter = HistoryAdapter(historyDeleteClickListener = {
            deleteSearchKeyword(it)
        })

        //BookAPI 그 자체로는 인터페이스이기에 구현체로 만들어 사용해야 한
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)     //사용할 open api의 url 작
            .addConverterFactory(GsonConverterFactory.create())  //컨버터도 생성
            .build()

        service = retrofit.create(BookAPI::class.java)
        service.getBestSeller(getString(R.string.interpark_apikey))  //내 open api key 입
            .enqueue(object: Callback<BestSellerDto> {
                //api 요청 실패했을 때 호출, 실패처리 관련 코드 구
                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    Log.e(TAG, t.toString())
                }
                //api 요청 성공했을 때 호출
                override fun onResponse(call: Call<BestSellerDto>, response: Response<BestSellerDto>) {
                    if (response.isSuccessful.not()) {
                        return
                    }

                    //body에는 bestSellerDto가 있을 거다. 왜? 어디서 설정해줬는
                    response.body()?.let {
                        Log.d(TAG, it.toString() )
                        it.books.forEach { book ->
                            Log.d(TAG, book.toString())

                        }
                        adapter.submitList(it.books)
                    }
                }

            })


        //initBookRecyclerView() 메소드라 생각하면
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        //searchEditText에서 엔터키를 눌렀을 때 줄바꿈이 아닌 search를 수행하도록 구
        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true    //true는 이벤트를 처리했음을 의미한
            }
            return@setOnKeyListener false

        }

        binding.searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showHistoryView()
            }

            return@setOnTouchListener false
        }


        binding.historyRecyclerView.adapter = historyAdapter
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)


    }

    private fun search(text: String) {


        service.getBooksByName(getString(R.string.interpark_apikey), text)
            .enqueue(object: Callback<SearchBooksDto> {
                override fun onFailure(call: Call<SearchBooksDto>, t: Throwable) {
                    hideHistoryView()
                }

                override fun onResponse(call: Call<SearchBooksDto>, response: Response<SearchBooksDto>) {

                    hideHistoryView()
                    saveSearchKeyword(text)  //키워드를 db에 저장하는 메소

                    if (response.isSuccessful.not()) {
                        return
                    }

                    adapter.submitList(response.body()?.books.orEmpty())  //아래코드를 이렇게 간결하게(?) 변경 가능
//                    response.body()?.let {
//                        adapter.submitList(it.books)
//                    }
                }

            })
    }

    private fun showHistoryView() {
        Thread(Runnable {
            //val keywords = db.~.reversed()로 바꾸고
            db.historyDao().getAll().reversed().run {
                runOnUiThread {
                    binding.historyRecyclerView.isVisible = true
                    historyAdapter.submitList(this)  //this를 keywords.orEmpty()로 바꾸도 됨
                }
            }

        }).start()

    }

    private fun hideHistoryView() {
        binding.historyRecyclerView.isVisible = false
    }

    private fun saveSearchKeyword(keyword: String) {
        //왜 thread를 통해 저장해줌?
        Thread(Runnable {
            db.historyDao().insertHistory(History(null, keyword))
        }).start()
    }

    //기록된 검색어 db에서 지우기
    private fun deleteSearchKeyword(keyword: String) {
        Thread(Runnable {
            db.historyDao().delete(keyword)
            showHistoryView()
        }).start()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val BASE_URL = "https://book.interpark.com/"
    }
}