package com.maan.deetteet.actvites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.databinding.ActivityLoginBinding;
import com.maan.deetteet.models.UserRoot;
import com.maan.deetteet.retrofit.Const;
import com.maan.deetteet.retrofit.RetrofitBuilder;

import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LG";

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
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
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

            Log.d(TAG, "onClickSignInWithEmailPassword: Pass: " + binding.inputPassword.getText().toString().trim());

//              Sign in with email and password
            firebaseAuth.signInWithEmailAndPassword(
                            binding.inputEmail.getText().toString().trim(),
                            binding.inputPassword.getText().toString().trim())
                    .addOnCompleteListener(this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: Success");

                                      FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        assert user != null;
                                        Log.d(TAG, "onComplete: UserData"+ user.getDisplayName());
                                       /* Call<UserRoot> call = RetrofitBuilder.create().registerUser(Const.DEV_KEY, "notificationToken", account.getEmail(),
                                                account.getDisplayName(), "google", account.getEmail(), account.getEmail(), "android");
                                        registerUser(call);*/

                                        Toast.makeText(getApplicationContext(), "Signed in", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
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
                            Log.e(TAG, "onComplete: Error", e);
                            Toast.makeText(LoginActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();

                        }
                    });


        } else {
            Toast.makeText(getApplicationContext(), "Fill required field", Toast.LENGTH_LONG).show();
        }


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