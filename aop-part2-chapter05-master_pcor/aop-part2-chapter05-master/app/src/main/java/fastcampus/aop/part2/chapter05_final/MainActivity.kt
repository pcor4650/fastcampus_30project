package fastcampus.aop.part2.chapter05_final

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val addPhotoButton: Button by lazy {
        findViewById<Button>(R.id.addPhotoButton)
    }

    private val startPhotoFrameModeButton: Button by lazy {
        findViewById<Button>(R.id.startPhotoFrameModeButton)
    }

    private val imageViewList: List<ImageView> by lazy {
        mutableListOf<ImageView>().apply {
            add(findViewById(R.id.imageView11))
            add(findViewById(R.id.imageView12))
            add(findViewById(R.id.imageView13))
            add(findViewById(R.id.imageView21))
            add(findViewById(R.id.imageView22))
            add(findViewById(R.id.imageView23))
        }
    }

    //uri????
    //uri를 저장을 해놨다가 다음 액티비티에 넘겨줘야하기 때문에 list를 만들어 uri를 저장
    private val imageUriList: MutableList<Uri> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAddPhotoButton()
        initStartPhotoFrameModeButton()
    }

    private fun initAddPhotoButton() {
        addPhotoButton.setOnClickListener {
            when {
                //1.권한이 부여가 되었을 때 갤러리에서 사진을 선택하는 기능
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    navigatePhotos()
                }
                //2.권한이 부여되지 않았을 때 교육용 팝업 확인 후 권한 팝업 띄우는 기능 구현
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    //3.걍 권한 요청 팝업 띄우기 requestPermissions를 통해서, 파라미터로 array를 받는 메소드
                    //requestCode가 뭐냐? 권한 선택에 대한 callback 확인 시 requestCode를 통해 어떤 권한에 대한 callback인지 확인
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
                }

            }
        }

    }


    private fun initStartPhotoFrameModeButton() {
        startPhotoFrameModeButton.setOnClickListener {
            val intent = Intent(this, PhotoFrameActivity::class.java)
            //uri를 자체로 넘길 수 없으니 string로 변경하여
            imageUriList.forEachIndexed { index, uri ->
                intent.putExtra("photo$index", uri.toString())
            }
            intent.putExtra("photoListSize", imageUriList.size)
            startActivity(intent)
        }

    }

    //callback함수, 권한 팝업 취소를 했는지 동의를 했는지 결과를 받아오는 callback함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1000 -> {
                //grantResults[0] == PackageManager.PERMISSION_GRANTED???권한이 하나라서?
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    navigatePhotos()

                } else {
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                //
            }
        }
    }

    //SAF를 사용해 사진을 가져오기 : 인텐트 사용
    private fun navigatePhotos() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)  //컨테츠를 가져오는 액션을 실행
        intent.type = "image/*"         //이미지만 가져올 거라 image/*
        startActivityForResult(intent, 2000)
        //인텐트 넘길 때 startActivityForResult를 사용하는 이유? SAF도 다른 액티비티를 실행한 다음 선택된 컨텐츠가 result를 통해서
    }
    //callback함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //왜 필요? Activity.RESULT_OK가 뭐임
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            2000 -> {
                //Nullable 이미지 데이터인 이유? 전 액티비티에서 데이터를 안내려 줬을 경우 오류가 발생할 수 있기때문에
                //Uri에 대해서는 설명 안하네
                val selectedImageUri: Uri? = data?.data

                if (selectedImageUri != null) {

                    if (imageUriList.size == 6) {
                        Toast.makeText(this, "이미 사진이 꽉 찼습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    //list에 add
                    imageUriList.add(selectedImageUri)
                    //size-1 인덱스 자리에 이미지 set
                    imageViewList[imageUriList.size - 1].setImageURI(selectedImageUri)

                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }


            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //권한이 필요한 이유를 적어준다. 권한팝업 실행 코드
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("전자액자에 앱에서 사진을 불러오기 위해 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->      //dialog onclicklistener구현
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()

    }

}