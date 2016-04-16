package com.timia2109.kristwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.timia2109.kristwallet.util.JavaScriptAPI;

/**
 * Created by Tim on 25.02.2016.
 */
public class WebViewActivty extends AppCompatActivity {
    WebView webView;
    public Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();

        webView = new WebView(this);
        webView.getSettings().setUserAgentString(HTTP.USER_AGENT);
        webView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webView.getSettings().setJavaScriptEnabled(true);
        JavaScriptAPI jsAPI = new JavaScriptAPI(this, webView);
        webView.addJavascriptInterface(jsAPI, "KristAndroid");
        webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.loadUrl(intent.getStringExtra("url"));
        setContentView(webView);
    }
}
