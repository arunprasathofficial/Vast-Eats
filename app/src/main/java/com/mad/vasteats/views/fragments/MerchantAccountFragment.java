package com.mad.vasteats.views.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.views.activities.LoginActivity;
import com.mad.vasteats.views.activities.UpdateMerchantAccountActivity;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class MerchantAccountFragment extends Fragment {

    public MerchantAccountFragment() {
        // Required empty public constructor
    }

    private ImageView update_account_btn, logout_btn;
    private CircularImageView merchant_image_iv;
    private TextView full_name_tv, merchant_name_tv, merchant_address_tv, email_address_tv, phone_number_tv, delivery_fee_tv, open_status_tv;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_account, container, false);

        update_account_btn = view.findViewById(R.id.update_account_btn);
        logout_btn = view.findViewById(R.id.logout_btn);
        merchant_image_iv = view.findViewById(R.id.merchant_image);
        full_name_tv = view.findViewById(R.id.full_name);
        merchant_name_tv = view.findViewById(R.id.merchant_name);
        merchant_address_tv = view.findViewById(R.id.merchant_address);
        email_address_tv = view.findViewById(R.id.email_address);
        phone_number_tv = view.findViewById(R.id.phone_number);
        delivery_fee_tv = view.findViewById(R.id.delivery_fee);
        open_status_tv = view.findViewById(R.id.open_status);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Logging out");

        firebaseAuth = FirebaseAuth.getInstance();

        if (IsConnectedToInternet.isConnectedToInternet(getContext())) {
            checkUser();
        } else {
            noInternetConnectionBottomSheet();
        }

        update_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), UpdateMerchantAccountActivity.class);
                getActivity().startActivity(intent);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToInternet.isConnectedToInternet(getContext())) {
                    //make user offline, logout and go to login activity
                    makeOffline();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });

        return view;
    }

    private void makeOffline() {
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        //setup data to update
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");
        hashMap.put("merchantOpen", "false");

        //update value to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        TastyToast.makeText(getActivity(), "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                    }
                });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), LoginActivity.class);
            progressDialog.dismiss();
            getActivity().startActivity(intent);
        } else {
            loadMerchantDetails();
        }
    }

    private void loadMerchantDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            String fullName = "" + dataSnapshot.child("fullName").getValue();
                            String merchantName = "" + dataSnapshot.child("merchantName").getValue();
                            String email = "" + dataSnapshot.child("email").getValue();
                            String phoneNo = "" + dataSnapshot.child("phoneNo").getValue();
                            String merchantAddress = "" + dataSnapshot.child("merchantAddress").getValue();
                            String deliveryFee = "" + dataSnapshot.child("deliveryFee").getValue();
                            String merchantOpen = "" + dataSnapshot.child("merchantOpen").getValue();
                            String merchantImage = "" + dataSnapshot.child("merchantImage").getValue();

                            full_name_tv.setText(fullName);
                            merchant_name_tv.setText(merchantName);
                            email_address_tv.setText(email);
                            phone_number_tv.setText(phoneNo);
                            merchant_address_tv.setText(merchantAddress);
                            delivery_fee_tv.setText("LKR " + deliveryFee);

                            if (merchantOpen.equals("true")) {
                                open_status_tv.setText("Open");
                            } else {
                                open_status_tv.setText("Closed");
                            }

                            try {
                                Picasso.get().load(merchantImage).placeholder(R.drawable.placeholder_color).into(merchant_image_iv);
                            } catch (Exception e) {
                                merchant_image_iv.setImageResource(R.drawable.placeholder_color);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void noInternetConnectionBottomSheet() {
        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(getContext());

        //inflate view for bottom sheet
        View view = LayoutInflater.from(getContext()).inflate(R.layout.connectivity_lost_layout, null);

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
                if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
                    roundedBottomSheetDialog.show();
                } else {
                    checkUser();
                }
            }
        });
    }
}