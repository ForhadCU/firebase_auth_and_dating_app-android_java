package com.maan.deetteet.actvites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.databinding.ActivityLoginBinding;
import com.maan.deetteet.models.User;
import com.maan.deetteet.models.UserRoot;
import com.maan.deetteet.retrofit.Const;
import com.maan.deetteet.retrofit.RetrofitBuilder;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String MYTAG = "MyTag";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ActivityLoginBinding binding;
    private String userTkn;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        Log.d(MYTAG, "onCreate: Called.");

        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        mCheckCurrentUser();
        /*if (!sessionManager.getStringValue(Const.USER_TOKEN).isEmpty())
            Log.d(TAG, "onCreate: Token" + sessionManager.getStringValue(Const.USER_TOKEN));*/
    }

    private void mCheckCurrentUser() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                // Get signedIn user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //if user is signed in, we call a helper method to save the user details to Firebase
                if (user != null) {
                    // User is signed in
                    // you could place other firebase code
                    //logic to save the user details to Firebase
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Log.d(MYTAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(MYTAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public void onClickSignInWithEmailPassword(View view) {
        if (!binding.inputEmail.getText().toString().trim().isEmpty()
                && !binding.inputPassword.getText().toString().trim().isEmpty()) {

            binding.btnTxt.setVisibility(View.GONE);
            binding.progressBarSignIn.setVisibility(View.VISIBLE);

            Log.d(MYTAG, "onClickSignInWithEmailPassword: Pass: " + binding.inputPassword.getText().toString().trim());

//              Sign in with email and password
            firebaseAuth.signInWithEmailAndPassword(
                            binding.inputEmail.getText().toString().trim(),
                            binding.inputPassword.getText().toString().trim())
                    .addOnCompleteListener(this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(MYTAG, "onComplete: Success");

                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        assert user != null;
                                       /* Call<UserRoot> call = RetrofitBuilder.create().registerUser(Const.DEV_KEY, "notificationToken", account.getEmail(),
                                                account.getDisplayName(), "google", account.getEmail(), account.getEmail(), "android");
                                        registerUser(call);*/
                                        mGetTokenFromFirebase(user);


                                    } /*else {
                                        Log.e(TAG, "onComplete: Error");
                                        Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                                    }*/
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.btnTxt.setVisibility(View.VISIBLE);
                            binding.progressBarSignIn.setVisibility(View.GONE);
                            Log.e(MYTAG, "onComplete: Error", e);
                            Toast.makeText(LoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

                        }
                    });


        } else {
            Toast.makeText(getApplicationContext(), "Fill required field", Toast.LENGTH_LONG).show();
        }


    }

    private void mGetTokenFromFirebase(FirebaseUser user) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firebaseFirestore.
                collection("USER_TOKEN").document(Objects.requireNonNull(user.getEmail()));

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String userToken = documentSnapshot.getString("token");
                Log.d(MYTAG, "onSuccess: UserToken " + userToken);

                mGetUserDetailsFromServer(userToken);
            }
        });
    }

    private void mGetUserDetailsFromServer(String userToken) {
        //                    Get user data with token
        Call<UserRoot> userRootCall = RetrofitBuilder.create().getUserDetail(
                userToken,
                Const.DEV_KEY
        );

        userRootCall.enqueue(new Callback<UserRoot>() {
            @Override
            public void onResponse(@Nullable Call<UserRoot> call,@Nullable Response<UserRoot> response) {
                if (response.code() == 200 && response.body().isStatus()) {
                    Log.d(MYTAG,
                            "onResponse: User email from Server " +
                                    response.body().getData());

                    if (response.body() != null) {
                      User user = response.body().getData();
                      user.setToken(userToken);
                        sessionManager.saveUser(user);
                        String t =  sessionManager.getUser().getToken();
                        Log.d("TokenFromUser", "onResponse: Token" +t
                               );
                        sessionManager.saveBooleanValue(Const.IS_LOGIN, true);
                        sessionManager.saveStringValue(Const.USER_TOKEN, response.body().getData().getToken());
                        Log.d("TokenFromReg", "onResponse: " + response.body().getData().getToken());

                        // Navigate to Next page
                    }
                }
            }

            @Override
            public void onFailure(Call<UserRoot> call, Throwable t) {
                Log.e(MYTAG, "onFailure: "+ "Failed to get user data from server");
            }
        });
    }


    public void onClickCreateNewAccount(View view) {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onClickForgotPassword(View view) {
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent);

    }
}