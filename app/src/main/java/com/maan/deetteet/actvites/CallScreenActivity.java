package com.maan.deetteet.actvites;

import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.BillingClient;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.maan.deetteet.JanuWork;
import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.adapters.BottomViewPagerAdapter;
import com.maan.deetteet.databinding.ActivityCallScreenBinding;
import com.maan.deetteet.databinding.ItemEmojiToastBinding;
import com.maan.deetteet.models.CoinPackageRoot;
import com.maan.deetteet.models.CountryRoot;
import com.maan.deetteet.models.GiftCategoryRoot;
import com.maan.deetteet.models.OfferCoinPackageRoot;
import com.maan.deetteet.models.RandomVideoRoot;
import com.maan.deetteet.models.User;
import com.maan.deetteet.popups.CallRejectPopup;
import com.maan.deetteet.popups.CommenPopup;
import com.maan.deetteet.popups.NoBalancePopup;
import com.maan.deetteet.retrofit.ApiCallWork;
import com.maan.deetteet.retrofit.Const;
import com.maan.deetteet.retrofit.RetrofitBuilder;
import com.maan.deetteet.utils.billing.MyPlayStoreBilling;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallScreenActivity extends BaseActivity implements Runnable, PaymentResultListener {
    private static final String TAG = "callscreenact";
    ActivityCallScreenBinding binding;
    SessionManager sessionManager;
    Handler handler = new Handler();
    Checkout checkout = new Checkout();
    private BottomSheetDialog bottomSheetDialog;
    private RandomVideoRoot.Data callGirl;
    private SimpleExoPlayer player;
    private List<GiftCategoryRoot.DataItem> giftCategories = new ArrayList<>();
    private MyPlayStoreBilling myPlayStoreBilling;
    private String purchasedCoin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_screen);
        sessionManager = new SessionManager(this);
        setMyWallet();


        getGiftCategory();
        Intent intent = getIntent();
        if (intent != null) {
            String girlobj = intent.getStringExtra("girl");
            if (girlobj != null && !girlobj.equals("")) {
                callGirl = new Gson().fromJson(girlobj, RandomVideoRoot.Data.class);
                setGirl();
                initCamera();
                initTimer();
                setVideo();

                initGooglePurchase();
                initRazorPay();
            }
        }

    }

    private void initRazorPay() {

        Checkout.preload(this);

        checkout.setKeyID(sessionManager.getSettings().getRazorpayKeyId());
    }

    private void initGooglePurchase() {

        myPlayStoreBilling = new MyPlayStoreBilling(this, new MyPlayStoreBilling.OnPurchaseComplete() {
            @Override
            public void onConnected(boolean isConnect) {

            }

            @Override
            public void onPurchaseResult(boolean isPurchaseSuccess) {
                if (isPurchaseSuccess) {
                    Toast.makeText(CallScreenActivity.this, " Success ", Toast.LENGTH_SHORT).show();
                    updateCoin(purchasedCoin);
                    purchasedCoin = "";
                    if (bottomSheetDialog != null) {
                        bottomSheetDialog.cancel();
                    }
                }
            }
        });
    }

    private void setMyWallet() {
        String wallet = sessionManager.getUser().getMyWallet();
        binding.tvWallet.setText(wallet);
    }

    private void initTimer() {
        handler.postDelayed(this, 20000);
    }

    private void reduceCoinWork() {

        String rate = callGirl.getRate();
        User user = sessionManager.getUser();
        if (Integer.parseInt(user.getMyWallet()) >= Integer.parseInt(rate)) {
            callApiForLessCoin(rate);
            JanuWork januWork = new JanuWork();
            januWork.lessCoinLocal(CallScreenActivity.this, Integer.parseInt(rate));
            handler.postDelayed(this, 20000);
        } else {
            handler.removeCallbacks(this);
            if (player != null) {
                player.setPlayWhenReady(false);
            }
            NoBalancePopup noBalancePopup = new NoBalancePopup(CallScreenActivity.this, sessionManager.getUser().getMyWallet());
            noBalancePopup.setPopupClickListnear(new NoBalancePopup.PopupClickListnear() {
                @Override
                public void onPlanItemClick(CoinPackageRoot.DataItem value) {

                    openPaymentSheet(value);
                }

                @Override
                public void onDismiss() {
                    onBackPressed();
                }
            });
        }
        setMyWallet();

    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(this);
    }

    private void callApiForLessCoin(String rate) {
        ApiCallWork apiCallWork = new ApiCallWork(this);
        apiCallWork.lessCoin(rate);
        apiCallWork.setOnResponseListnear(new ApiCallWork.OnResponseListnear() {
            @Override
            public void onSuccess() {
//
            }

            @Override
            public void onFailure() {
                //
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.lytSheet.lytSheet.getVisibility() == View.VISIBLE) {
            binding.lytSheet.lytSheet.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void getGiftCategory() {
        giftCategories.clear();
        Call<GiftCategoryRoot> call = RetrofitBuilder.create().getGiftCategory(Const.DEV_KEY);
        call.enqueue(new Callback<GiftCategoryRoot>() {


            @Override
            public void onResponse(Call<GiftCategoryRoot> call, Response<GiftCategoryRoot> response) {
                if (response.code() == 200 && response.body().isStatus() && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    giftCategories = response.body().getData();
                    settabLayout(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<GiftCategoryRoot> call, Throwable t) {
//
            }
        });
    }


    private void initCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    1);
        } else {
            binding.surfaceCamera.bindToLifecycle(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        binding.surfaceCamera.bindToLifecycle(this);
    }


    private void setGirl() {

        if (sessionManager.getCountry() != null) {
            CountryRoot.DataItem country = JanuWork.getCountry(callGirl.getCountryId(), this);
            if (country != null) {
                callGirl.setCountry(country);
                binding.tvCountry.setText(country.getCountry());
                Glide.with(this).load(Const.IMAGE_URL + callGirl.getCountry().getCountryMedia()).circleCrop().into(binding.imgcountry);
            }
        }
        Log.d(TAG, "setGirl: rate " + callGirl.getRate());
        Glide.with(this).load(Const.IMAGE_URL + callGirl.getThumbImg()).circleCrop().into(binding.imgprofile);

        binding.tvrate.setText(callGirl.getRate().concat("/min"));
        binding.tvName.setText(callGirl.getName());
        binding.tvBio.setText(callGirl.getBio());
        binding.video.setOnClickListener(v -> binding.lytSheet.lytSheet.setVisibility(View.GONE));
        binding.btnCallDecline.setOnClickListener(v -> showPopup());
        binding.lytbalance.setOnClickListener(v -> {

        });
        binding.btnCallDecline.setOnClickListener(v -> onBackPressed());
    }

    private void setVideo() {
        player = new SimpleExoPlayer.Builder(this).build();
        binding.playerview.setPlayer(player);
        binding.playerview.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        Uri uri = Uri.parse(Const.IMAGE_URL + callGirl.getVideo());
        com.google.android.exoplayer2.upstream.DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-retrytech");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);

        Log.d(TAG, "initializePlayer: " + uri);
        player.setPlayWhenReady(true);
        player.prepare(mediaSource, false, false);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case STATE_BUFFERING:
                        Log.d(TAG, "buffer: " + uri);
                        break;
                    case STATE_ENDED:
                        player.setRepeatMode(Player.REPEAT_MODE_ALL);
                        Log.d(TAG, "end: " + uri);
                        break;
                    case STATE_IDLE:
                        Log.d(TAG, "idle: " + uri);
                        break;

                    case STATE_READY:
                        Log.d(TAG, "ready: " + uri);

                        break;
                    default:
                        break;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.IS_CALLING_NOW.set(false);
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void openPaymentSheet(CoinPackageRoot.DataItem dataItem) {
        configGooglePurchase(dataItem);

//        bottomSheetDialog = new BottomSheetDialog(this);
//        bottomSheetDialog.setOnShowListener(dialog -> {
//            BottomSheetDialog d = (BottomSheetDialog) dialog;
//            FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
//            if (bottomSheet != null) {
//                BottomSheetBehavior.from(bottomSheet)
//                        .setState(BottomSheetBehavior.STATE_EXPANDED);
//            }
//        });
//
//        BottomsheetPaymentmethodBinding paymentmethodBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_paymentmethod, null, false);
//        bottomSheetDialog.setContentView(paymentmethodBinding.getRoot());
//        bottomSheetDialog.setContentView(paymentmethodBinding.getRoot());
//        paymentmethodBinding.tvamount.setText(dataItem.getPrice());
//        paymentmethodBinding.imgrazorpay.setOnClickListener(v -> {
//            configRazorpay(dataItem);
//            bottomSheetDialog.dismiss();
//
//        });
//        paymentmethodBinding.imggooglepay.setOnClickListener(v -> {
//            configGooglePurchase(dataItem);
//            bottomSheetDialog.dismiss();
//        });
//        bottomSheetDialog.show();
//        bottomSheetDialog.setCancelable(false);
//        paymentmethodBinding.tvcancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
    }

    private void showPopup() {
        CommenPopup commenPopup = new CommenPopup(this, "Popup Title Here", "You can Place your msg\nhere in this space\nmax 3 lines", "Yes, Please", "Cancel");
        commenPopup.setPopupClickListnear(new CommenPopup.PopupClickListnear() {
            @Override
            public void onPositiveClick() {
                commenPopup.closePopup();
            }

            @Override
            public void onNegativeClick() {
                commenPopup.closePopup();
            }

            @Override
            public void onCloseClick() {
                commenPopup.closePopup();
                onBackPressed();
            }
        });
    }

    public void onGiftClick(View view) {
        Log.d(TAG, "onGiftClick: " + view);
        binding.lytSheet.lytSheet.setVisibility(View.VISIBLE);


        BottomViewPagerAdapter bottomViewPagerAdapter = new BottomViewPagerAdapter(giftCategories);
        binding.lytSheet.viewpager.setAdapter(bottomViewPagerAdapter);

        binding.lytSheet.viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.lytSheet.tablayout));
        binding.lytSheet.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.lytSheet.viewpager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //ll
            }
        });
        bottomViewPagerAdapter.setEmojiListnerViewPager((bitmap, coin) -> {

            User user = sessionManager.getUser();
            if (Integer.parseInt(user.getMyWallet()) >= Integer.parseInt(coin)) {
                callApiForLessCoin(coin);
                JanuWork januWork = new JanuWork();
                januWork.lessCoinLocal(CallScreenActivity.this, Integer.parseInt(coin));
                Toast toast = new Toast(this);

                ItemEmojiToastBinding itemEmojiBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.item_emoji_toast, null, false);
                Glide.with(this)
                        .load(Const.IMAGE_URL + bitmap)
                        .circleCrop()
                        .into(itemEmojiBinding.ivThumb);
                toast.setView(itemEmojiBinding.getRoot());
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.setDuration(Toast.LENGTH_SHORT);

                toast.show();

            } else {
                handler.removeCallbacks(this);
                if (player != null) {
                    player.setPlayWhenReady(false);
                }
                NoBalancePopup noBalancePopup = new NoBalancePopup(CallScreenActivity.this, sessionManager.getUser().getMyWallet());
                noBalancePopup.setPopupClickListnear(new NoBalancePopup.PopupClickListnear() {
                    @Override
                    public void onPlanItemClick(CoinPackageRoot.DataItem value) {
                        openPaymentSheet(value);
                    }

                    @Override
                    public void onDismiss() {
                        onBackPressed();
                    }
                });
            }
            setMyWallet();


        });

        binding.lytSheet.btnClose.setOnClickListener(v -> onBackPressed());
    }

    private void settabLayout(List<GiftCategoryRoot.DataItem> data) {
        binding.lytSheet.tablayout.removeAllTabs();
        binding.lytSheet.tablayout.setTabGravity(TabLayout.GRAVITY_FILL);
        for (int i = 0; i < data.size(); i++) {

            binding.lytSheet.tablayout.addTab(binding.lytSheet.tablayout.newTab().setCustomView(createCustomView(data.get(i))));

        }
    }

    private View createCustomView(GiftCategoryRoot.DataItem dataItem) {

        View v = LayoutInflater.from(this).inflate(R.layout.custom_tabgift, null);
        TextView tv = (TextView) v.findViewById(R.id.tvTab);
        tv.setText(dataItem.getGiftCatName());
        ImageView img = (ImageView) v.findViewById(R.id.imagetab);

        Glide.with(getApplicationContext())
                .load(Const.IMAGE_URL + dataItem.getGiftCatMedia()).circleCrop()
                .into(img);
        return v;

    }


    @Override
    public void run() {
        reduceCoinWork();
    }


    private void configRazorpay(Object dataitem) {


        if (checkout != null) {
            checkout.setImage(R.drawable.icon_round);
            try {
                Activity activity = this;
                JSONObject options = new JSONObject();

                options.put("name", sessionManager.getUser().getFullName());
                options.put("description", "user id: " + sessionManager.getUser().getUserId());
                options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
                options.put("currency", "INR");

                if (dataitem instanceof CoinPackageRoot.DataItem) {
                    purchasedCoin = ((CoinPackageRoot.DataItem) dataitem).getCoinAmount();
                    Log.d(TAG, "razorpay : rate1= " + ((CoinPackageRoot.DataItem) dataitem).getPrice());
                    options.put("amount", 100 * Integer.parseInt(((CoinPackageRoot.DataItem) dataitem).getPrice()));
                } else {
                    purchasedCoin = ((OfferCoinPackageRoot.DataItem) dataitem).getCoinAmount();
                    Log.d(TAG, "razorpay : rate2= " + ((OfferCoinPackageRoot.DataItem) dataitem).getPrice());
                    options.put("amount", 100 * Integer.parseInt(((OfferCoinPackageRoot.DataItem) dataitem).getPrice()));

                }

                options.put("prefill.email", sessionManager.getUser().getUserEmail());
                options.put("prefill.contact", "0000000000");
                checkout.open(activity, options);


            } catch (Exception e) {
                Log.e(TAG, "Error in submitting payment details", e);
            }
        }
    }

    private void configGooglePurchase(Object dataitem) {
        if (myPlayStoreBilling != null) {
            if (dataitem instanceof CoinPackageRoot.DataItem) {
                purchasedCoin = ((CoinPackageRoot.DataItem) dataitem).getCoinAmount();
                Log.d(TAG, "configGooglePurchase: rate1= " + purchasedCoin);
                myPlayStoreBilling.startPurchase(((CoinPackageRoot.DataItem) dataitem).getPlaystoreProductId(), BillingClient.SkuType.INAPP, true);

            } else {

                purchasedCoin = ((OfferCoinPackageRoot.DataItem) dataitem).getCoinAmount();
                myPlayStoreBilling.startPurchase(((OfferCoinPackageRoot.DataItem) dataitem).getPlaystoreProductId(), BillingClient.SkuType.INAPP, true);
                Log.d(TAG, "configGooglePurchase: rate2= " + purchasedCoin);
            }
        } else {
            Log.d(TAG, "configGooglePurchase: bp not init ");
        }
    }

    private void updateCoin(String purchasedCoin) {
        Log.d(TAG, "updateCoin: " + purchasedCoin);

        ApiCallWork apiCallWork = new ApiCallWork(this);
        apiCallWork.addCoin(sessionManager.getUser().getToken(), Integer.parseInt(purchasedCoin));
        apiCallWork.setOnResponseListnear(new ApiCallWork.OnResponseListnear() {
            @Override
            public void onSuccess() {
                JanuWork januWork = new JanuWork();
                boolean added = januWork.addCoinLocal(CallScreenActivity.this, Integer.parseInt(purchasedCoin));
                setMyWallet();
                Log.d(TAG, "onSuccess: coin added " + added);
            }

            @Override
            public void onFailure() {
//
            }
        });
    }

    @Override
    public void onPaymentSuccess(String s) {
        Log.d(TAG, "onPaymentSuccess: ");
        updateCoin(purchasedCoin);
        purchasedCoin = "";
    }

    @Override
    public void onPaymentError(int i, String s) {
        purchasedCoin = "";
        Log.d(TAG, "onPaymentError: err razorpay " + s);
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void onClickReview(View view) {
        Log.d(TAG, "onClickReview: " + view);
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_report);
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            CallRejectPopup callRejectPopup = new CallRejectPopup(this, menuItem.getItemId() == R.id.menu_report ? "Do you really want to\nReport this account..?" : "Do you really want to\nBlock this account..?");
            callRejectPopup.setPopupClickListnear(new CallRejectPopup.PopupClickListnear() {
                @Override
                public void onContinueClick() {
                    callRejectPopup.closePopup();
                }

                @Override
                public void onPositiveClik() {
                    if (menuItem.getItemId() == R.id.menu_report) {
                        Toast.makeText(CallScreenActivity.this, "Thank You For Complain", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CallScreenActivity.this, "Block user successfully..!", Toast.LENGTH_SHORT).show();
                    }
                    callRejectPopup.closePopup();
                    onBackPressed();
                }
            });
            return true;
        });
        popupMenu.show();
    }
}