package com.wenming.weiswift.app.unlogin.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.wenming.weiswift.R;
import com.wenming.weiswift.app.common.oauth.constant.WeicoAuthConstants;
import com.wenming.weiswift.app.main.activity.MainActivity;
import com.wenming.weiswift.app.mvp.presenter.WebViewActivityPresent;
import com.wenming.weiswift.app.mvp.presenter.imp.WebViewActivityPresentImp;
import com.wenming.weiswift.app.mvp.view.WebViewActivityView;

/**
 * Created by wenmingvs on 16/5/12.
 */

public class WebViewActivity extends Activity implements WebViewActivityView {
    public static final String EXTRA_URL = "extra_url";
    private Context mContext;
    private String sRedirectUri;
    private WebView mWeb;
    private String mLoginURL;
    private WebViewActivityPresent mWebViewActivityPresent;
    private boolean mComeFromAccoutActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.webview_layout);
        mLoginURL = getIntent().getStringExtra(EXTRA_URL);
        mComeFromAccoutActivity = getIntent().getBooleanExtra("comeFromAccoutActivity", false);
        sRedirectUri = WeicoAuthConstants.REDIRECT_URL;
        mWeb = (WebView) findViewById(R.id.webview);
        mWebViewActivityPresent = new WebViewActivityPresentImp(this);
        initWebView();
    }

    private void initWebView() {
        WebSettings settings = mWeb.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWeb.setWebViewClient(new MyWebViewClient());
        mWeb.loadUrl(mLoginURL);
        mWeb.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                        if (mWeb.canGoBack()) {
                            mWeb.goBack();
                        } else {
                            finish();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void startMainActivity() {
        Intent intent = new Intent(WebViewActivity.this, MainActivity.class);
        intent.putExtra("fisrtstart", true);
        if (mComeFromAccoutActivity) {
            intent.putExtra("comeFromAccoutActivity", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(intent);
        finish();
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (isUrlRedirected(url)) {
                view.stopLoading();
                mWebViewActivityPresent.handleRedirectedUrl(mContext, url);
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!url.equals("about:blank") && isUrlRedirected(url)) {
                view.stopLoading();
                mWebViewActivityPresent.handleRedirectedUrl(mContext, url);
                return;
            }
            super.onPageStarted(view, url, favicon);
        }
    }


    public boolean isUrlRedirected(String url) {
        return url.startsWith(sRedirectUri);
    }


}
