package com.maan.deetteet.actvites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.databinding.ActivityRegisterBinding;
import com.maan.deetteet.models.UserRoot;
import com.maan.deetteet.retrofit.Const;
import com.maan.deetteet.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotifyActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    ActivityRegisterBinding binding;
    FirebaseUser currentUser;
    private SessionManager sessionManager;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mCallbackManager = CallbackManager.Factory.create();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onClickGoogle(View view) {

        Log.d("TAG", "onClickGoogle: " + view);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.pd.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            binding.pd.setVisibility(View.GONE);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    String notificationToken = sessionManager.getStringValue(Const.NOTIFICATION_TOKEN);
                    if (account.getPhotoUrl() != null && !account.getPhotoUrl().toString().isEmpty()) {
                        sessionManager.saveStringValue(Const.PROFILE_IMAGE, account.getPhotoUrl().toString());
                    } else {
                        sessionManager.saveStringValue(Const.PROFILE_IMAGE, "");
                    }
                    Call<UserRoot> call = RetrofitBuilder.create().registerUser(Const.DEV_KEY, "notificationToken", account.getEmail(),
                            account.getDisplayName(), "google", account.getEmail(), account.getEmail(), "android");
                    registerUser(call);
                    Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                    Log.d("TAG", "firebaseAuthWithGoogle:" + account.getIdToken());


                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void registerUser(Call<UserRoot> call) {

        call.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(@Nullable Call<UserRoot> call, @Nullable Response<UserRoot> response) {
                if (response.code() == 200 && response.body().isStatus() && response.body().getData() != null) {
                    sessionManager.saveUser(response.body().getData());
                    sessionManager.saveBooleanValue(Const.IS_LOGIN, true);
                    if (response.body() != null) {
                        sessionManager.saveStringValue(Const.USER_TOKEN, response.body().getData().getToken());

                    }

                    Log.d("TAG", "onResponse: usercreted success");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }


                binding.pd.setVisibility(View.GONE);


            }

            @Override
            public void onFailure(@Nullable Call<UserRoot> call, @Nullable Throwable t) {
                Log.d("TAG", "faill " + t);
            }
        });
    }


    public void onClickFacebok(View view) {}

    public void onclickAggrement(View view) {
        Log.d("TAG", "onclickAggrement: " + view);
        startActivity(new Intent(this, WebActivity.class).putExtra("URL", Const.ABOUTUS).putExtra("TITLE", "Terms of Use"));

    }

    public void onclickPrivacy(View view) {
        Log.d("TAG", "onclickPrivacy: " + view);
        startActivity(new Intent(this, WebActivity.class).putExtra("URL", Const.ABOUTUS).putExtra("TITLE", "Terms of Use"));

    }
}