package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.Constants;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.sdsmdg.tastytoast.TastyToast;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //make fullscreen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();

        //start login activity after 3 Sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (IsConnectedToInternet.isConnectedToInternet(getApplicationContext())) {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser == null) {
                        //if user not logged in, login activity will be show
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        //if user logged in, user type checking function will be run
                        checkUserType();
                    }
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        }, 3000);
    }

    private void checkUserType() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = "" + snapshot.child("accountType").getValue();
                        String online = "" + snapshot.child("online").getValue();

                        if (online.equals("true")) {
                            if (accountType.equals("Merchant")) {
                                //if account type is merchant, main merchant activity will be start
                                startActivity(new Intent(SplashActivity.this, MainMerchantActivity.class));
                                finish();
                            } else if (accountType.equals("Customer")){
                                //if account type is customer, main customer activity will be start
                                startActivity(new Intent(SplashActivity.this, MainCustomerActivity.class));
                                finish();
                            }
                        } else {
                            //if user not logged in, login activity will be show
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        TastyToast.makeText(SplashActivity.this, error.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
        Button tryAgainBtn = view.findViewById(R.id.try_again_btn);

        roundedBottomSheetDialog.show();

        tryAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roundedBottomSheetDialog.dismiss();
                recreate();
            }
        });
    }
}