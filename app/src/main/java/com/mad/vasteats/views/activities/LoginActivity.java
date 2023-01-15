package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText email_et, password_et;
    private TextView forgot_password_tv, register_tv;
    private Button login_btn;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        email_et = findViewById(R.id.email);
        password_et = findViewById(R.id.password);
        forgot_password_tv = findViewById(R.id.forgot_password);
        register_tv = findViewById(R.id.register);
        login_btn = findViewById(R.id.login_btn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Logging In");

        firebaseAuth = FirebaseAuth.getInstance();

        forgot_password_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open forgot password activity
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });

        register_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open customer registration activity
                startActivity(new Intent(LoginActivity.this, CustomerRegisterActivity.class));
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check is connected to internet
                if (IsConnectedToInternet.isConnectedToInternet(LoginActivity.this)) {
                    //login customer or merchant
                    validateData();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });
    }

    String email, password;

    private void validateData() {
        email = email_et.getText().toString().trim();
        password = password_et.getText().toString().trim();

        //set error icon
        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_red);
        errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        if (!TextUtils.isEmpty(email)) {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (!TextUtils.isEmpty(password)) {
                    loginUser();
                } else {
                    password_et.setError("Please enter password!", errorIcon);
                }
            } else {
                email_et.setError("invalid email format!", errorIcon);
            }
        } else {
            email_et.setError("Please enter email!", errorIcon);
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged in successfully
                        makeOnline();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        TastyToast.makeText(LoginActivity.this, e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                    }
                });
    }

    private void makeOnline() {
        progressDialog.setMessage("Checking user");

        //setup data to update
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        //update value to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        //update successfully
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        TastyToast.makeText(LoginActivity.this, e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                    }
                });
    }

    private void checkUserType() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            String accountType = "" + dataSnapshot.child("accountType").getValue();

                            if (accountType.equals("Merchant")) {
                                //setup data to update
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("merchantOpen", "true");

                                //update value to db
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                //update successfully
                                                //if account type is merchant, main merchant activity will be start
                                                startActivity(new Intent(LoginActivity.this, MainMerchantActivity.class));
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                TastyToast.makeText(LoginActivity.this, e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                                            }
                                        });
                            } else {
                                //if account type is customer, main customer activity will be start
                                startActivity(new Intent(LoginActivity.this, MainCustomerActivity.class));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        TastyToast.makeText(LoginActivity.this, error.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
                if (!IsConnectedToInternet.isConnectedToInternet(LoginActivity.this)) {
                    roundedBottomSheetDialog.show();
                }
            }
        });
    }
}