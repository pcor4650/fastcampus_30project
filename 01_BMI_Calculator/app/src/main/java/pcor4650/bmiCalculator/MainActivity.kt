package pcor4650.bmiCalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val heightEditText: EditText = findViewById(R.id.heightEditText)
        val weightEditText: EditText = findViewById(R.id.weightEditText)

        val resultButton: Button = findViewById<Button>(R.id.resultButton)
        resultButton.setOnClickListener {
            if(heightEditText.text.isEmpty() || weightEditText.text.isEmpty()) {
                Toast.makeText(this, "빈 값이 있어 계산할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val height: Int = heightEditText.text.toString().toInt()
            val weight:Int = weightEditText.text.toString().toInt()

        }
    }
}