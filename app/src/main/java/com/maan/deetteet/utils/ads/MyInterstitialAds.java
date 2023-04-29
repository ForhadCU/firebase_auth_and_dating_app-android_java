package com.maan.deetteet.utils.ads;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.maan.deetteet.SessionManager;

public class MyInterstitialAds {

    SessionManager sessionManager;
    private Context context;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd interstitialAdfb;

    public MyInterstitialAds(Context context) {
        this.context = context;
        initAds();
    }


    private void initAds() {
        sessionManager = new SessionManager(context);
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, sessionManager.getAdmobInt(), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                initFacebook();
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });


    }

    private void initFacebook() {
        interstitialAdfb = new com.facebook.ads.InterstitialAd(context, sessionManager.getFBInt());
        interstitialAdfb.loadAd(
                interstitialAdfb.buildLoadAdConfig()

                        .build());

    }

    public void showAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show((Activity) context);
        } else if (interstitialAdfb != null && interstitialAdfb.isAdLoaded()) {
            interstitialAdfb.show();
        }
    }
}
