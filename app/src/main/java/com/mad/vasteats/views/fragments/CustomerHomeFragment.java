package com.mad.vasteats.views.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.models.Merchant;
import com.mad.vasteats.presenters.CustomerMerchantAdapter;
import com.mad.vasteats.views.activities.LoginActivity;
import com.mad.vasteats.views.activities.UpdateCustomerAccountActivity;

import java.util.ArrayList;
import java.util.Collections;

public class CustomerHomeFragment extends Fragment {

    public CustomerHomeFragment() {
        // Required empty public constructor
    }

    private ImageView merchant_search_iv;
    private TextView all_merchants_tv, near_you_merchants_tv, merchant_view_type_tv, customer_current_city_tv,
            customer_current_address_tv;
    private CardView customer_current_city_cv;
    private RecyclerView merchants_rv;
    private LinearLayout no_near_merchants_msg_lt;
    private ScrollView merchants_sv;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private ArrayList<Merchant> merchantArrayList;
    private CustomerMerchantAdapter customerMerchantAdapter;

    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);

        merchant_search_iv = view.findViewById(R.id.merchant_search);
        all_merchants_tv = view.findViewById(R.id.all_merchants);
        near_you_merchants_tv = view.findViewById(R.id.near_you_merchants);
        merchant_view_type_tv = view.findViewById(R.id.merchant_view_type);
        customer_current_city_tv = view.findViewById(R.id.customer_current_city);
        customer_current_address_tv = view.findViewById(R.id.customer_current_address);
        customer_current_city_cv = view.findViewById(R.id.customer_current_city_cv);
        merchants_rv = view.findViewById(R.id.merchants_rv);
        no_near_merchants_msg_lt = view.findViewById(R.id.no_near_merchants_msg);
        merchants_sv = view.findViewById(R.id.merchants_sv);

        firebaseAuth = FirebaseAuth.getInstance();

        //check is connected to internet
        if (IsConnectedToInternet.isConnectedToInternet(getContext())) {
            checkUser();
        } else {
            noInternetConnectionBottomSheet();
        }

        merchant_search_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomerMerchantSearchFragment customerMerchantSearchFragment = new CustomerMerchantSearchFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.customer_frame_layout, customerMerchantSearchFragment);
                fragmentTransaction.commit();
            }
        });

        all_merchants_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                all_merchants_tv.setTextColor(getResources().getColor(R.color.white));
                all_merchants_tv.setBackground(getResources().getDrawable(R.drawable.shape_rect02));

                near_you_merchants_tv.setTextColor(getResources().getColor(R.color.blue_grey));
                near_you_merchants_tv.setBackground(getResources().getDrawable(R.drawable.shape_rect03));

                merchants_sv.fullScroll(View.FOCUS_UP);

                merchant_view_type_tv.setText("Showing All");

                loadAllMerchants();
            }
        });

        near_you_merchants_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                near_you_merchants_tv.setTextColor(getResources().getColor(R.color.white));
                near_you_merchants_tv.setBackground(getResources().getDrawable(R.drawable.shape_rect02));

                all_merchants_tv.setTextColor(getResources().getColor(R.color.blue_grey));
                all_merchants_tv.setBackground(getResources().getDrawable(R.drawable.shape_rect03));

                merchants_sv.fullScroll(View.FOCUS_UP);

                merchant_view_type_tv.setText("Showing Near You");

                loadCustomerCurrentCity();
            }
        });

        customer_current_city_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), UpdateCustomerAccountActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            Intent intent = new Intent();
            intent.setClass(getActivity(), LoginActivity.class);
            getActivity().startActivity(intent);
        } else {
            loadCustomerCurrentCity();
        }
    }

    private void loadCustomerCurrentCity() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //get customer's current city
                            String currentCity = "" + dataSnapshot.child("city").getValue();
                            String currentAddress = "" + dataSnapshot.child("completeAddress").getValue();

                            //set customer's current city
                            customer_current_city_tv.setText(currentCity);
                            customer_current_address_tv.setText(currentAddress);

                            //load only those merchants that are in the city of customer
                            loadNearMerchants(currentCity);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllMerchants() {
        merchantArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        merchants_rv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("accountType").equalTo("Merchant")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        merchantArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            Merchant merchant = dataSnapshot.getValue(Merchant.class);
                            merchantArrayList.add(merchant);
                            Collections.shuffle(merchantArrayList);
                        }
                        if (merchantArrayList.size() != 0) {
                            no_near_merchants_msg_lt.setVisibility(View.GONE);
                            merchants_sv.setVisibility(View.VISIBLE);
                            //setup adapter
                            customerMerchantAdapter = new CustomerMerchantAdapter(getContext(), merchantArrayList);
                            //set adapter
                            merchants_rv.setAdapter(customerMerchantAdapter);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadNearMerchants(String currentCity) {
        merchantArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        merchants_rv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("accountType").equalTo("Merchant")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        merchantArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            Merchant merchant = dataSnapshot.getValue(Merchant.class);

                            String merchantCity = "" + dataSnapshot.child("merchantCity").getValue();

                            if (merchantCity.equals(currentCity)) {
                                merchantArrayList.add(merchant);
                            }

                            Collections.shuffle(merchantArrayList);
                        }

                        if (merchantArrayList.size() == 0) {
                            merchants_sv.setVisibility(View.GONE);
                            no_near_merchants_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            no_near_merchants_msg_lt.setVisibility(View.GONE);
                            merchants_sv.setVisibility(View.VISIBLE);
                            //setup adapter
                            customerMerchantAdapter = new CustomerMerchantAdapter(getContext(), merchantArrayList);
                            //set adapter
                            merchants_rv.setAdapter(customerMerchantAdapter);
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