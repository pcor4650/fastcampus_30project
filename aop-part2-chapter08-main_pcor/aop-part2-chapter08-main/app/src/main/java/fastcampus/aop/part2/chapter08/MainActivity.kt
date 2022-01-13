package fastcampus.aop.part2.chapter08

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

  private val goHomeButton: ImageButton by lazy {
    findViewById(R.id.goHomeButton)
  }
  private val addressBar: EditText by lazy {
    findViewById(R.id.addressBar)
  }
  private val goBackButton: ImageButton by lazy {
    findViewById(R.id.goBackButton)
  }
  private val goForwardButton: ImageButton by lazy {
    findViewById(R.id.goForwardButton)
  }
  private val refreshLayout: SwipeRefreshLayout by lazy {
    findViewById(R.id.refreshLayout)
  }
  private val webView: WebView by lazy {
    findViewById(R.id.webView)
  }
  private val progressBar: ContentLoadingProgressBar by lazy {
    findViewById(R.id.progressBar)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    initViews()
    bindViews()
  }

  //back 버튼 눌렀을때 callback 함수
  override fun onBackPressed() {
    if (webView.canGoBack()) {
      webView.goBack()    //뒤로가기 or
    } else {
      super.onBackPressed()   //앱 종료
    }
  }

  //앱 시작시 명시된 url에 접근하도록 구현
  @SuppressLint("SetJavaScriptEnabled")
  private fun initViews() {
    webView.apply {
      webViewClient = WebViewClient()   //외부 웹 브라우저에서 실행하지 않기 위해
      webChromeClient = WebChromeClient()
      //webViewClient와 webChromeClient의 차이?
      settings.javaScriptEnabled = true  //안드로이드에서는 디폴트로 자바스크립트 관련된 것들을 허용하지 않고 있다
      loadUrl(DEFAULT_URL)
    }
  }

  //이벤트를 바인딩 할거니까 initview가 아닌
  private fun bindViews() {
    goHomeButton.setOnClickListener {
      webView.loadUrl(DEFAULT_URL)
    }

    //action 버튼 눌렀을때 수행되는 리스너
    addressBar.setOnEditorActionListener { v, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        val loadingUrl = v.text.toString()
        if (URLUtil.isNetworkUrl(loadingUrl)) {
          webView.loadUrl(loadingUrl)
        } else {
          webView.loadUrl("http://$loadingUrl")  //isNetworkUrl을 통해 검사하고 http:// 자동적으로 붙여주도록
        }
      }

      return@setOnEditorActionListener false   //키보드 내려야 해서 false를 반환
    }

    goBackButton.setOnClickListener {
      webView.goBack()
    }

    goForwardButton.setOnClickListener {
      webView.goForward()
    }

    //refreshLayout 스와이프 이벤트 처리
    refreshLayout.setOnRefreshListener {
      webView.reload()
    }
  }

  inner class WebViewClient : android.webkit.WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
      super.onPageStarted(view, url, favicon)

      //페이지 시작할 때 progressBar 보여주고
      progressBar.show()
    }

    //페이지가 다 로딩되었을때 처리
    override fun onPageFinished(view: WebView?, url: String?) {
      super.onPageFinished(view, url)

      refreshLayout.isRefreshing = false  //리프레시 애니메이션 사라지게
      progressBar.hide()
      goBackButton.isEnabled = webView.canGoBack()  //웹뷰에서 뒤로갈 수 있을때는 버튼 보이도록
      goForwardButton.isEnabled = webView.canGoForward()
      addressBar.setText(url)  //최종적으로 로딩된 주소를 표시해주기 위해
    }
  }

  //inner를 붙여주는 이유? 상위에 정의된 property에 접근하기 위해
  inner class WebChromeClient : android.webkit.WebChromeClient() {

    //로딩된 정도를 알려주는 메소드
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
      super.onProgressChanged(view, newProgress)

      progressBar.progress = newProgress
    }
  }

  companion object {
    private const val DEFAULT_URL = "http://www.google.com"
  }
}
