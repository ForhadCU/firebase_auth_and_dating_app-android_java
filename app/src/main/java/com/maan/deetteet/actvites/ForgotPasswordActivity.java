package com.maan.deetteet.actvites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.maan.deetteet.R;
import com.maan.deetteet.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";
    private ActivityForgotPasswordBinding binding;
   private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);

        firebaseAuth = FirebaseAuth.getInstance();


    }

    public void onClickResetPassword(View view) {

        if (!binding.inputEmail.getText().toString().trim().isEmpty()) {
            binding.btnTxt.setVisibility(View.GONE);
            binding.progressBarResetPass.setVisibility(View.VISIBLE);
//              Send a link into email to reset password
            firebaseAuth.sendPasswordResetEmail(binding.inputEmail.getText().toString().trim())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                binding.txtResetPassGuide.setVisibility(View.GONE);
                                binding.inputEmail.setVisibility(View.GONE);
                                binding.btnResetPass.setVisibility(View.GONE);
                                binding.txtEmailSentNotice.setVisibility(View.VISIBLE);
                                binding.txtEmailSentNotice.setText("Link has been sent to "+ binding.inputEmail.getText().toString().trim()+"\n"+"Please Check.");

                            } /*else {
                                Toast.makeText(getApplicationContext(), "Connection error!", Toast.LENGTH_LONG).show();

                            }*/
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            binding.btnTxt.setVisibility(View.VISIBLE);
                            binding.progressBarResetPass.setVisibility(View.GONE);
                            Log.e(TAG, "onFailure: ", e);
                            Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                        }
                    });



        } else {
            Toast.makeText(getApplicationContext(), "Fill required field", Toast.LENGTH_LONG).show();
        }



    }
}