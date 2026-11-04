package id.man19.guru

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import id.man19.guru.databinding.FragmentWebBinding

class WebFragment : Fragment() {

    private var _b: FragmentWebBinding? = null
    private val b get() = _b!!
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        fileCallback?.onReceiveValue(uris.toTypedArray())
        fileCallback = null
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentWebBinding.inflate(i, c, false)
        return b.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(b.webView, true)

        b.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest) = false
        }
        b.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(v: WebView, p: Int) {
                _b?.progress?.progress = p
                _b?.progress?.visibility = if (p < 100) View.VISIBLE else View.GONE
            }
            override fun onShowFileChooser(
                v: WebView, cb: ValueCallback<Array<Uri>>, params: FileChooserParams
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = cb
                pickFile.launch("*/*")
                return true
            }
        }

        if (savedInstanceState != null) b.webView.restoreState(savedInstanceState)
        else b.webView.loadUrl(BuildConfig.START_URL)
    }

    fun goBack(): Boolean {
        val w = _b?.webView ?: return false
        return if (w.canGoBack()) { w.goBack(); true } else false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _b?.webView?.saveState(outState)
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
