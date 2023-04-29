package com.maan.deetteet.actvites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebViewClient;

import androidx.databinding.DataBindingUtil;

import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.databinding.ActivityWebBinding;


public class WebActivity extends BaseActivity {
    ActivityWebBinding binding;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web);
        sessionManager = new SessionManager(this);

        Intent intent = getIntent();
        if (intent != null) {
            String url = sessionManager.getSettings().getPrivacyPolicy();
            String title = intent.getStringExtra("TITLE");
            if (title != null) {
                binding.tvtitle.setText(title);
            }
            if (url != null) {
                binding.webView.loadUrl(url);
                binding.webView.setWebViewClient(new WebViewClient());

            }
        }
    }

    public void onClickBack(View view) {
        Log.d("TAG", "onclickAggrement: " + view);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}