package pcor4650.bmiCalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlin.math.pow

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val height = intent.getIntExtra("height", 0)
        val weight = intent.getIntExtra("weight", 0)

        Log.d("ResultActivity", "height: $height , weight : $weight ")

        val bmi = weight / (height / 100.0).pow(2)
        val bmiResultText = when{
            bmi >=35 -> "고도 비만"
            bmi >=30 -> "중정도 비만"
            bmi >=25 -> "경도 비만"
            bmi >=23 -> "과체중"
            bmi >=18.5 -> "정상체중"
            else -> "저체중"
        }

        val bmiValueTextView: TextView = findViewById(R.id.bmiValueTextView)
        val bmiResultTextView = findViewById<TextView>(R.id.bmiResultTextView)

        bmiValueTextView.text = bmi.toString()
        bmiResultTextView.text = bmiResultText

    }
}