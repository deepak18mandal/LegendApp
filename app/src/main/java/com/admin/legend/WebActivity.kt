package com.admin.legend

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(DeviceInfoBridge(), "DeviceInfo")
        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }

    inner class DeviceInfoBridge {
        @JavascriptInterface
        fun getDeviceId(): String {
            val prefs = getSharedPreferences("legend", MODE_PRIVATE)
            return prefs.getString("device_id", "unknown") ?: "unknown"
        }
    }
}
