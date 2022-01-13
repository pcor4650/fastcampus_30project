package fastcampus.aop.part3.chapter3

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.content.edit
import java.util.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initOnOffButton()
        initChangeAlarmTimeButton()

        //sharedPreference에 저장된 데이터를 가져오는 함수
        val model = fetchDataFromSharedPreferences()
        //저장된 데이터를 기반으로 뷰 그려주기 구현
        renderView(model)

    }

    private fun initOnOffButton() {
        val onOffButton = findViewById<Button>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            //onOff버튼 클릭시 필요한 작업
            //1.데이터 확인 필요
            //2.온오프에 따라 작업 처리해준다
            //3.데이터를 저장한다

            //as?통해 형변환
            val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener
            val newModel = saveAlarmModel(model.hour, model.minute, model.onOff.not())
            renderView(newModel)

            if (newModel.onOff) { //온 일때 -> 알람을 등록
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, newModel.hour)
                    set(Calendar.MINUTE, newModel.minute)

                    //지금 시간보다 이전이라면
                    if (before(Calendar.getInstance())) {
                        //DATE에 1을 더해서 다음날 시간으로 변경
                        add(Calendar.DATE, 1)
                    }
                }

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                //PendingIntent.FLAG_UPDATE_CURRENT 기존 인텐트를 현재 인텐트로 업데이트하겠다

                //알람이 꼭 실행되어야 한다면, 현재 앱은 잠자기모드에서 실행 안될 수 있음
               //alarmManager.setAndAllowWhileIdle()

                //반복 알람으로 설정
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,        //RTC_WAKEUP과 ELAPSED_REALTIME_WAKEUP 차이?
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )

            } else {
                cancelAlarm()     //오프일 때 알람을 제거
            }

        }
    }

    private fun initChangeAlarmTimeButton() {
        val changeAlarmButton = findViewById<Button>(R.id.changeAlarmTimeButton)
        changeAlarmButton.setOnClickListener {





            val calendar = Calendar.getInstance()
            //2.TimePickDialog를 띄워서 시간을 설정하고, 그 시간을 가져와서
            TimePickerDialog(this, { picker, hour, minute ->





                //3.데이터를 저장한다
                val model = saveAlarmModel(hour, minute, false)
                //4.뷰를 업데이트 한다.
                renderView(model)
                //5.기존에 있던 알람을 삭제한다.
                cancelAlarm()

               //1.현재시간을 가져온다.
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()

        }

    }

    private fun saveAlarmModel(
        hour: Int,
        minute: Int,
        onOff: Boolean
    ): AlarmDisplayModel {
        val model = AlarmDisplayModel(
            hour = hour,
            minute = minute,
            onOff = onOff
        )

        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        //sharedPreference 에 데이터 저장
        with(sharedPreferences.edit()) {
            putString(ALARM_KEY, model.makeDataForDB())
            putBoolean(ONOFF_KEY, model.onOff)
            commit()    //commit 또는 apply를 해주어야 데이터가 저장된다
        }

        return model
    }

    private fun fetchDataFromSharedPreferences():AlarmDisplayModel {
        val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        //기본값 설정, timeDBValue는 Nullable이므로 ?: 으로 Null 조건일 경우에 대해 return값 작성해준다
        val timeDBValue = sharedPreferences.getString(ALARM_KEY, "9:30") ?: "9:30"
        val onOffDBValue = sharedPreferences.getBoolean(ONOFF_KEY, false)
        val alarmData = timeDBValue.split(":")

        val alarmModel = AlarmDisplayModel(
            hour = alarmData[0].toInt(),
            minute = alarmData[1].toInt(),
            onOff = onOffDBValue
        )

        // 보정 보정 예외처리

        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)

        // 알람은 등록 안되어 있는데, 알람 on으로 되있는 경우
        if ((pendingIntent == null) and alarmModel.onOff) {
            //알람 off로 바꿔준다
            alarmModel.onOff = false
            // 알람은 등록 되어 있는데, 알람 off로 되어있는 경우
        } else if ((pendingIntent != null) and alarmModel.onOff.not()){

            // 알람을 취소함, pendingIntent를 취소해줌
            pendingIntent.cancel()
        }

        return alarmModel

    }

    private fun renderView(model: AlarmDisplayModel) {
        //am인지 pm인지
        findViewById<TextView>(R.id.ampmTextView).apply {
            text = model.ampmText
        }
        //설정된 알람 time 보여주기
        findViewById<TextView>(R.id.timeTextView).apply {
            text = model.timeText
        }
        //알람 on/off 보여주기
        findViewById<Button>(R.id.onOffButton).apply {
            text = model.onOffText
            tag = model   //model을 전역변수로 저장을 안해두었다?-> on off 버튼을 누를 때 데이터를 가져올 곳이 없다. -> tag에 model값을 저장해준다 -> tag에는 object 이기에 아무거나 들어갈 수 있다

        }

    }

    private fun cancelAlarm() {
        val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.cancel()
    }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "time"
        private const val ALARM_KEY = "alarm"
        private const val ONOFF_KEY = "onOff"
        private const val ALARM_REQUEST_CODE = 1000

    }
}