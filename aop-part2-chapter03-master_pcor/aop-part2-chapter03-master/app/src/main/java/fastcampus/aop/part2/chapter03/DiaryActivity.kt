package fastcampus.aop.part2.chapter03

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener

class DiaryActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())//메인 스레드에 연결되 루퍼가 생성됨

    private val diaryEditText: EditText by lazy {
        findViewById<EditText>(R.id.diaryEditText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        initDetailEditText()
    }

    private fun initDetailEditText() {
        val detail = getSharedPreferences("diary", Context.MODE_PRIVATE).getString("detail", "")
        diaryEditText.setText(detail)

        val runnable = Runnable {
            getSharedPreferences("diary", Context.MODE_PRIVATE).edit(true) {
                putString("detail", diaryEditText.text.toString())
            }
            Log.d("DiaryActivity", "SAVE!!!!!!")
        }

        diaryEditText.addTextChangedListener {
            Log.d("DiaryActivity", "text Changed :: $it")
            handler.removeCallbacks(runnable)  //아직 실행되지 않고 pending되어있는 runnable이 있다면 삭제
            handler.postDelayed(runnable, 500)
        }
    }
}