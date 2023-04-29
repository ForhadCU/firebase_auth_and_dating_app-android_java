package com.maan.deetteet.actvites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.messaging.FirebaseMessaging;
import com.maan.deetteet.JanuWork;
import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.databinding.ActivityStartBinding;
import com.maan.deetteet.models.CountryRoot;
import com.maan.deetteet.models.SettingRoot;
import com.maan.deetteet.retrofit.Const;
import com.maan.deetteet.retrofit.RetrofitBuilder;
import com.maan.deetteet.utils.ads.MyInterstitialAds;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartActivity extends AppCompatActivity {
    SessionManager sessionManager;
    ActivityStartBinding binding;
    private boolean isSettingLoded;
    private boolean isCountryLoded = false;
    MyInterstitialAds myInterstitialAds;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_start);
        sessionManager = new SessionManager(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
//                        String msg = getString(R.string.msg_token_fmt, token);
                    Log.d("TAG", token);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                });

        getCountry();

        FirebaseMessaging.getInstance().subscribeToTopic("livestream");
        JanuWork.setNames();
        binding.lytterms.setOnClickListener(v -> startActivity(new Intent(this, WebActivity.class).putExtra("URL", Const.ABOUTUS).putExtra("TITLE", "Terms of Use")));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getCountry() {
        Call<CountryRoot> call = RetrofitBuilder.create().getCountryList(Const.DEV_KEY);
        call.enqueue(new Callback<CountryRoot>() {
            @Override
            public void onResponse(Call<CountryRoot> call, Response<CountryRoot> response) {
                if (response.code() == 200 && response.body().isStatus() && !response.body().getData().isEmpty()) {
                    List<CountryRoot.DataItem> list = response.body().getData();
                    sessionManager.saveCountry(list);
                    isCountryLoded = true;
                    getSettings();


                }
            }

            @Override
            public void onFailure(Call<CountryRoot> call, Throwable t) {
                getSettings();
            }
        });
    }

    private void getSettings() {
        Call<SettingRoot> call = RetrofitBuilder.create().getSettings(Const.DEV_KEY);
        call.enqueue(new Callback<SettingRoot>() {


            @Override
            public void onResponse(Call<SettingRoot> call, Response<SettingRoot> response) {
                if (response.code() == 200 && response.body() != null && response.body().isStatus() && response.body().getData() != null) {
                    sessionManager.saveSettings(response.body().getData());
                    isSettingLoded = true;
                    onClickLetsFind();
                }
            }

            @Override
            public void onFailure(Call<SettingRoot> call, Throwable t) {
//
            }
        });
    }


    public void onClickLetsFind() {
        if (isSettingLoded && isCountryLoded) {
            binding.pb.setVisibility(View.GONE);
            binding.tvLetsFind.setVisibility(View.VISIBLE);
            myInterstitialAds = new MyInterstitialAds(this);
            binding.lnrLetsFind.setOnClickListener(v -> {
                if (sessionManager.getBooleanValue(Const.IS_LOGIN)) {
//                    JanuWork januWork = new JanuWork();
//                    boolean added = januWork.addCoinLocal(StartActivity.this, 100000);
                    startActivity(new Intent(this, MainActivity.class));
                    myInterstitialAds.showAds();
                } else {
//                    startActivity(new Intent(this, RegisterActivity.class));
                    startActivity(new Intent(this, LoginActivity.class));
                }
                finishAffinity();
            });

        } else {
            getCountry();
            getSettings();
        }

    }
}