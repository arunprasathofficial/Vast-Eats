package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.sdsmdg.tastytoast.TastyToast;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText email_address_et;
    private Button reset_password_btn;
    private ImageView back_btn_iv;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        email_address_et = findViewById(R.id.email_address);
        reset_password_btn = findViewById(R.id.reset_password_btn);
        back_btn_iv = findViewById(R.id.back_btn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Please wait");

        firebaseAuth = FirebaseAuth.getInstance();

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        reset_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check is connected to internet
                if (IsConnectedToInternet.isConnectedToInternet(ForgotPasswordActivity.this)) {
                    resetPassword();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });
    }

    String email;

    private void resetPassword() {
        email = email_address_et.getText().toString().trim();

        //set error icon
        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_red);
        errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        if (TextUtils.isEmpty(email)) {
            email_address_et.setError("Please enter email!", errorIcon);
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_address_et.setError("invalid email format!", errorIcon);
        }

        progressDialog.setMessage("Sending Password Resetting Link");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        TastyToast.makeText(ForgotPasswordActivity.this, "Recovery mail sent successfully! Check your inbox.", TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        TastyToast.makeText(ForgotPasswordActivity.this, e.getMessage(), TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
                    }
                });
    }

    private void noInternetConnectionBottomSheet() {
        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(this);

        //inflate view for bottom sheet
        View view = LayoutInflater.from(this).inflate(R.layout.connectivity_lost_layout, null);

        //set view to bottom sheet
        roundedBottomSheetDialog.setContentView(view);
        roundedBottomSheetDialog.setCanceledOnTouchOutside(false);

        //init ui views
        Button try_again_btn = view.findViewById(R.id.try_again_btn);

        roundedBottomSheetDialog.show();

        try_again_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roundedBottomSheetDialog.dismiss();
                if (!IsConnectedToInternet.isConnectedToInternet(ForgotPasswordActivity.this)) {
                    roundedBottomSheetDialog.show();
                }
            }
        });
    }
}