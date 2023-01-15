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
import com.mad.vasteats.views.activities.UpdateCustomerAccountActivity;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CustomerAccountFragment extends Fragment {

    public CustomerAccountFragment() {
        // Required empty public constructor
    }

    private ImageView update_account_btn_iv, logout_btn_iv;
    private CircularImageView customer_image_iv;
    private TextView full_name_tv, customer_address_tv, email_address_tv, phone_number_tv;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_account, container, false);

        update_account_btn_iv = view.findViewById(R.id.update_account_btn);
        logout_btn_iv = view.findViewById(R.id.logout_btn);
        customer_image_iv = view.findViewById(R.id.customer_image);
        full_name_tv = view.findViewById(R.id.full_name);
        customer_address_tv = view.findViewById(R.id.customer_address);
        email_address_tv = view.findViewById(R.id.email_address);
        phone_number_tv = view.findViewById(R.id.phone_number);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Logging Out");

        firebaseAuth = FirebaseAuth.getInstance();

        //check is connected to internet
        if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
            noInternetConnectionBottomSheet();
        } else {
            checkUser();
        }

        update_account_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), UpdateCustomerAccountActivity.class);
                getActivity().startActivity(intent);
            }
        });

        logout_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check is connected to internet
                if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
                    noInternetConnectionBottomSheet();
                } else {
                    //make user offline, logout and go to login activity
                    makeOffline();
                }
            }
        });

        return view;
    }

    private void makeOffline() {
        progressDialog.setMessage("Logging Out");
        progressDialog.show();

        //setup data to update
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

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
                        TastyToast.makeText(getContext(), "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
            loadCustomerDetails();
        }
    }

    String userImage, fullName, emailAddress, phoneNo, address;

    private void loadCustomerDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            fullName = "" + dataSnapshot.child("fullName").getValue();
                            emailAddress = "" + dataSnapshot.child("email").getValue();
                            phoneNo = "" + dataSnapshot.child("phoneNo").getValue();
                            address = "" + dataSnapshot.child("completeAddress").getValue();
                            userImage = "" + dataSnapshot.child("userImage").getValue();

                            full_name_tv.setText(fullName);
                            email_address_tv.setText(emailAddress);
                            phone_number_tv.setText(phoneNo);
                            customer_address_tv.setText(address);

                            try {
                                Picasso.get().load(userImage).placeholder(R.drawable.ic_acc_placeholder_grey).into(customer_image_iv);
                            } catch (Exception e) {
                                customer_image_iv.setImageResource(R.drawable.ic_acc_placeholder_grey);
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
                }
            }
        });
    }
}