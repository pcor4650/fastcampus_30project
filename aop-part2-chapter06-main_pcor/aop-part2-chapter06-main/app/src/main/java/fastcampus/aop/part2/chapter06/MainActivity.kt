package fastcampus.aop.part2.chapter06

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }
    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }
    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }

    //soundpool은 빌더 패턴으로 구현되어 있다
    private val soundPool = SoundPool.Builder().build()

    private var currentCountDownTimer: CountDownTimer? = null  //이 타이머는 앱이 시작하자 마자 생기는 타이머가 아니라 seekBar 설정 시 시작하는 타이머라 null 넣어줌
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSounds()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()  //sound는 특정앱에서 리소스를 가지는 것이 아니고 디바이스에서 가지고 있는데에 요청?하는 것이기에 앱 라이프사이클에 따라 시작/정지 필요
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release() //sound 같은 리소스는 많은 메모리를 차지하기 때문에 사용하지 않을때 메모리에서 해제 필요
    }

    //각각의 뷰에 있는 리스너와 실제 로직을 연결하는 코드 구현
    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                //유저가 seekbar를 컨트롤해서 발생한 이벤트인지, 코드상에서 값을 주어 발생한 것인지에 대한 여부를 알려주는 flag
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    //코드상이 아닌 사용자가 건드렸을 때만
                    if (fromUser) {
                        updateRemainTime(progress * 60 * 1000L)
                    }
                }

                //seekBar를 제어할 때때
               override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopCountDown()
                }

                //seekBar에서 손을 떼는 순간 시작됨
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar ?: return  //seekBar가 Null일 경우 return을 해서 더이상 진행하지 않도록

                    if (seekBar.progress == 0) {
                        stopCountDown()
                    } else {
                        startCountDown()
                    }
                }
            }
        )
    }

    //사운드를 재생하기 전에 로드하는 과정
    private fun initSounds() {
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)
    }

    //CountDownTimer생성, java는 어떻게?
    private fun createCountDownTimer(initialMillis: Long) =
        object : CountDownTimer(initialMillis, 1000L) {
            //매초마다 UI 갱신 코드 구현, onTick은 얼마마다 한번씩 불림?
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }

    private fun startCountDown() {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.start()

        tickingSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()  //현재의 카운트다운을 멈추게 한다
        currentCountDownTimer = null
        soundPool.autoPause()
    }

    private fun completeCountDown() {
        updateRemainTime(0)
        updateSeekBar(0)

        soundPool.autoPause()
        //인자로 전달해야할 property가 Nullable할 경우 Null이 아닐때만 let을 통해 soundId로 해당값을 전달하고 그 값을 바로 인자로 전달
        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateRemainTime(remainMillis: Long) {
        val remainSeconds = remainMillis / 1000

        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60) //자리수 표현위해
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60)
    }

    private fun updateSeekBar(remainMillis: Long) {
        //seekBar는 하나하나의 값이 분을 나타내고 있다. 뭔말?
        seekBar.progress = (remainMillis / 1000 / 60).toInt()
    }
}
