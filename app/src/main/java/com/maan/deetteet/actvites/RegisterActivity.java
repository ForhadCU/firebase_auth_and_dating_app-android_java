package com.maan.deetteet.actvites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.maan.deetteet.R;
import com.maan.deetteet.SessionManager;
import com.maan.deetteet.databinding.ActivityRegisterBinding;
import com.maan.deetteet.models.UserRoot;
import com.maan.deetteet.retrofit.Const;
import com.maan.deetteet.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegisterActivity extends AppCompatActivity {

    //    private static final int RC_SIGN_IN = 100;
    ActivityRegisterBinding binding;
    FirebaseUser currentUser;
    private SessionManager sessionManager;
    private CallbackManager mCallbackManager;
//    private FirebaseAuth mAuth;

    //Updated.........................
    private static final String TAG = "MainActivity";
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1;
    String name, email;
    String idToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private EditText editTextUserName, editTextEmail, editTextPassword, editTextConfirmPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        sessionManager = new SessionManager(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        mCallbackManager = CallbackManager.Factory.create();

    }

    //    Click on Google Sign in Button
    public void onClickGoogle(View view) {

        Log.d("TAG", "onClickGoogle: " + view);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.pd.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    // Handle the result of the Google sign-in flow
    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            binding.pd.setVisibility(View.GONE);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
//                Log.d(TAG, "onActivityResult: LoggedIn");
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in is failed", e);
                // ...
            }
        }
    }

    // Authenticate with Firebase using the Google token
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();

                            Call<UserRoot> call = RetrofitBuilder.create().registerUser(Const.DEV_KEY, "notificationToken", account.getEmail(),
                                    account.getDisplayName(), "google", account.getEmail(), account.getEmail(), "android");
                            registerUser(call);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();

                            // ...
                        } /*else {
                            // Sign in failed, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                            // ...
                        }*/
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Error: ", e);
                        Toast.makeText(RegisterActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


/*    @Override
    protected void onStart() {
        super.onStart();
       *//* if (authStateListener != null){
            FirebaseAuth.getInstance().signOut();
        }*//*
        firebaseAuth.addAuthStateListener(authStateListener);
    }*/

  /*  @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }*/


    public void onClickAlreadyHaveAccount(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


/*
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
    }*/

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
                   /* startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();*/
                }


                binding.pd.setVisibility(View.GONE);


            }

            @Override
            public void onFailure(@Nullable Call<UserRoot> call, @Nullable Throwable t) {
                Log.d("TAG", "faill " + t);
            }
        });
    }


    public void onClickSignUp(View view) {

        if (!binding.inputFullName.getText().toString().trim().isEmpty() && !binding.inputEmail.getText().toString().trim().isEmpty()
                && !binding.inputPassword.getText().toString().trim().isEmpty() && !binding.inputCorrectPassword.getText().toString().trim().isEmpty()) {
            if (binding.inputPassword.getText().toString().trim().equals(binding.inputCorrectPassword.getText().toString().trim())) {
                binding.btnTxt.setVisibility(View.GONE);
                binding.progressBarSignUp.setVisibility(View.VISIBLE);
//              Sign up with email and password
                firebaseAuth.createUserWithEmailAndPassword(
                                binding.inputEmail.getText().toString().trim(),
                                binding.inputPassword.getText().toString().trim())
                        .addOnCompleteListener(this,
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: Success");
                                            Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();

//                                            save data to server
                                            Call<UserRoot> call = RetrofitBuilder.create().registerUser(
                                                    Const.DEV_KEY, "notificationToken",
                                                    binding.inputEmail.getText().toString().trim(), binding.inputFullName.getText().toString().trim()
                                                    , "google", binding.inputEmail.getText().toString().trim(),
                                                    binding.inputEmail.getText().toString().trim(), "android");
                                            registerUser(call);


                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } /*else {
                                            Log.e(TAG, "onComplete: Error");
                                            Toast.makeText(RegisterActivity.this, "Connection error!", Toast.LENGTH_SHORT).show();
                                        }*/
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.btnTxt.setVisibility(View.VISIBLE);
                                binding.progressBarSignUp.setVisibility(View.GONE);
                                Log.e(TAG, "onComplete: Error: "+ e.toString());
                                Toast.makeText(RegisterActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getApplicationContext(), "Password doesn't match!", Toast.LENGTH_LONG).show();
                binding.inputCorrectPassword.setText("");
            }

        } else {
            Toast.makeText(getApplicationContext(), "Fill required field", Toast.LENGTH_LONG).show();
        }


    }

    public void onclickAggrement(View view) {
        Log.d("TAG", "onclickAggrement: " + view);
        startActivity(new Intent(this, WebActivity.class).putExtra("URL", Const.ABOUTUS).putExtra("TITLE", "Terms of Use"));

    }

    public void onclickPrivacy(View view) {
        Log.d("TAG", "onclickPrivacy: " + view);
        startActivity(new Intent(this, WebActivity.class).putExtra("URL", Const.ABOUTUS).putExtra("TITLE", "Terms of Use"));

    }
}
