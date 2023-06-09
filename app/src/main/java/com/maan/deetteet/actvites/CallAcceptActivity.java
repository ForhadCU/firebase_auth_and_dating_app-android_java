package com.maan.deetteet.actvites;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.BillingClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.maan.deetteet.JanuWork;
import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.databinding.ActivityCallBinding;
import com.maan.deetteet.models.CoinPackageRoot;
import com.maan.deetteet.models.OfferCoinPackageRoot;
import com.maan.deetteet.models.RandomVideoRoot;
import com.maan.deetteet.models.User;
import com.maan.deetteet.popups.CallRejectPopup;
import com.maan.deetteet.popups.NoBalancePopup;
import com.maan.deetteet.retrofit.ApiCallWork;
import com.maan.deetteet.retrofit.Const;
import com.maan.deetteet.utils.billing.MyPlayStoreBilling;

import org.json.JSONObject;

public class CallAcceptActivity extends BaseActivity implements PaymentResultListener {
    private static final int CALL_ACCEPT = 1;
    private static final int CALL_INCOME = 0;
    private static final String TAG = "callAcceptact";
    int state = CALL_INCOME;
    ActivityCallBinding binding;
    SessionManager sessionManager;
    Checkout checkout = new Checkout();
    Vibrator vibrator;
    private BottomSheetDialog bottomSheetDialog;
    private RandomVideoRoot.Data callGirl;
    private boolean noBalancePopupShowed = false;
    private String purchasedCoin = "";

    private MyPlayStoreBilling myPlayStoreBilling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call);
        sessionManager = new SessionManager(this);
        Intent intent = getIntent();
        if (intent != null) {
            String girlobj = intent.getStringExtra("girl");
            if (girlobj != null && !girlobj.equals("")) {
                callGirl = new Gson().fromJson(girlobj, RandomVideoRoot.Data.class);
                makeCall();
                MainActivity.IS_CALLING_NOW.set(true);
            }
        }
        initGooglePurchase();
        initRazorPay();

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
                    Toast.makeText(CallAcceptActivity.this, " Success ", Toast.LENGTH_SHORT).show();
                    updateCoin(purchasedCoin);
                    purchasedCoin = "";
                    if (bottomSheetDialog != null) {
                        bottomSheetDialog.cancel();
                    }
                }
            }
        });
    }

    private void makeCall() {

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long milliseconds = 1000;
        vibrator.vibrate(milliseconds);
        long[] pattern = {0, 1000, 1000, 1000, 1500, 1000, 2000, 1000, 2500, 1000, 3000};
        vibrator.vibrate(pattern, -1);


        binding.tvdes1.setText(callGirl.getName().concat(" is ready for a call...!"));
        binding.tvName.setText(callGirl.getName());
        binding.tvdes2.setText(callGirl.getName().concat(" invites you for video call"));
        binding.tvMin.setText(callGirl.getRate().concat("/min"));
        Glide.with(this).load(Const.IMAGE_URL + callGirl.getThumbImg()).circleCrop().into(binding.imggirl);
        Glide.with(this).load(Const.IMAGE_URL + callGirl.getThumbImg()).circleCrop().into(binding.imggirlconnected);

        Glide.with(this).load(sessionManager.getStringValue(Const.IMAGE_URL))
                .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(15)))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.img2.setImageDrawable(resource);
                        return true;
                    }
                }).into(binding.img2);
        Glide.with(this).load(Const.IMAGE_URL + callGirl.getThumbImg())
                .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(15)))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        binding.img1.setImageDrawable(resource);
                        return true;
                    }
                }).into(binding.img1);
        initLinstear();
    }

    private void initLinstear() {
        binding.btnCallDecline.setOnClickListener(v -> {
            if (vibrator != null) {
                vibrator.cancel();
            }
            CallRejectPopup callRejectPopup = new CallRejectPopup(this, "Do you really want to\nloose this girl..?");
            callRejectPopup.setPopupClickListnear(new CallRejectPopup.PopupClickListnear() {
                @Override
                public void onContinueClick() {
                    callRejectPopup.closePopup();
                }

                @Override
                public void onPositiveClik() {
                    MainActivity.IS_CALLING_NOW.set(false);
                    callRejectPopup.closePopup();
                    onBackPressed();
                }
            });
        });
    }

    private void openPaymentSheet(CoinPackageRoot.DataItem dataItem) {
        configGooglePurchase(dataItem);
//        bottomSheetDialog = new BottomSheetDialog(this);
//        bottomSheetDialog.setOnShowListener(dialog -> {
//            BottomSheetDialog d = (BottomSheetDialog) dialog;
//            FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
//            BottomSheetBehavior.from(bottomSheet)
//                    .setState(BottomSheetBehavior.STATE_EXPANDED);
//        });
//
//        BottomsheetPaymentmethodBinding paymentmethodBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.bottomsheet_paymentmethod, null, false);
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

    public void onClickAccept(View view) {
        Log.d(TAG, "onClickAccept: " + view);
        if (vibrator != null) {
            vibrator.cancel();
        }
        state = CALL_ACCEPT;
        setUI();
    }

    private void setUI() {

        if (state == CALL_ACCEPT) {
            binding.tvstatus.setText("Connecting..");
            binding.lytbuttons.setVisibility(View.GONE);
            binding.lytPhotos.setVisibility(View.GONE);
            binding.tvdes2.setVisibility(View.GONE);
            binding.tvdes1.setVisibility(View.GONE);
            binding.lytconnected.setVisibility(View.VISIBLE);

            String rate = callGirl.getRate();
            Log.d(TAG, "setUI: Rate"+ rate);
            User user = sessionManager.getUser();
            Log.d(TAG, "setUI: User"+ user.toString());

            if (Integer.parseInt(user.getMyWallet()) >= Integer.parseInt(rate)) {
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(CallAcceptActivity.this, CallScreenActivity.class);
                    intent.putExtra("girl", new Gson().toJson(callGirl));
                    Pair[] pairs = new Pair[2];
                    pairs[0] = new Pair<View, String>(binding.imggirlconnected, "logo");
                    pairs[1] = new Pair<View, String>(binding.tvName, "name");
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(CallAcceptActivity.this, pairs);
                    startActivityForResult(intent, 10, activityOptions.toBundle());

                }, 10000);
            } else {

                NoBalancePopup noBalancePopup = new NoBalancePopup(CallAcceptActivity.this, sessionManager.getUser().getMyWallet());
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
                noBalancePopupShowed = true;
            }


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            onBackPressed();
        }

    }

    @Override
    public void onBackPressed() {
        if (!noBalancePopupShowed) {
            if (vibrator != null) {
                vibrator.cancel();
            }
        }
        MainActivity.IS_CALLING_NOW.set(false);
        super.onBackPressed();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vibrator != null) {
            vibrator.cancel();
        }
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
                boolean added = januWork.addCoinLocal(CallAcceptActivity.this, Integer.parseInt(purchasedCoin));
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
}