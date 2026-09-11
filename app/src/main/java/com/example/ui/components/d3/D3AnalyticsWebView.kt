package com.example.ui.components.d3

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ArtisanAnalyticsDashboardData
import com.example.ui.theme.TerracottaPrimary

/**
 * JavaScript interface exposed to the WebView for bidirectional interaction
 * between D3 charts and native Android Jetpack Compose code.
 */
class D3ChartsJsInterface(
    private val onItemClick: (type: String, id: String, details: String) -> Unit
) {
    @JavascriptInterface
    fun onItemClicked(type: String, id: String, details: String) {
        onItemClick(type, id, details)
    }
}

/**
 * Embedded Jetpack Compose Composable hosting D3.js visualization charts.
 * Seamlessly loads local offline assets and renders reactive charts from Room data.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3AnalyticsWebView(
    data: ArtisanAnalyticsDashboardData,
    modifier: Modifier = Modifier,
    onChartItemClicked: (type: String, id: String, details: String) -> Unit = { _, _, _ -> }
) {
    val isDark = isSystemInDarkTheme()
    var isLoading by remember { mutableStateOf(true) }

    val htmlContent = remember(data, isDark) {
        D3ChartsHtmlGenerator.generateHtml(data = data, isDarkMode = isDark)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("d3_analytics_webview_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("d3_analytics_webview"),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setBackgroundColor(Color.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    addJavascriptInterface(
                        D3ChartsJsInterface(onChartItemClicked),
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }

                    loadDataWithBaseURL(
                        "file:///android_asset/",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(
                    "file:///android_asset/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = TerracottaPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
