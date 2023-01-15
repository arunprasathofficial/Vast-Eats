package com.mad.vasteats.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.models.Order;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MerchantHomeFragment extends Fragment {

    public MerchantHomeFragment() {
        // Required empty public constructor
    }

    private ImageView merchant_image_iv;
    private TextView greeting_tv, current_date_tv, average_ratings_tv, ratings_count_tv,
            total_earnings_tv, total_orders_tv, completed_orders_tv, pending_orders_tv, cancelled_orders_tv;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private String orderStatus;

    private ArrayList<Order> orderArrayList;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_home, container, false);

        merchant_image_iv = view.findViewById(R.id.merchant_image);
        greeting_tv = view.findViewById(R.id.greeting);
        current_date_tv = view.findViewById(R.id.current_date);
        average_ratings_tv = view.findViewById(R.id.average_ratings);
        ratings_count_tv = view.findViewById(R.id.ratings_count);
        total_earnings_tv = view.findViewById(R.id.total_earnings);
        total_orders_tv = view.findViewById(R.id.total_orders);
        completed_orders_tv = view.findViewById(R.id.completed_orders);
        pending_orders_tv = view.findViewById(R.id.pending_orders);
        cancelled_orders_tv = view.findViewById(R.id.cancelled_orders);

        firebaseAuth = FirebaseAuth.getInstance();

        if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
            noInternetConnectionBottomSheet();
        } else {
            loadMerchantInfo();
        }

        return view;
    }

    private void loadMerchantInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            //get data
                            String merchantName = "" + dataSnapshot.child("merchantName").getValue();
                            String merchantImage = "" + dataSnapshot.child("merchantImage").getValue();

                            final String timestamp = "" + System.currentTimeMillis();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
                            String formattedDate = simpleDateFormat.format(new Date(Long.parseLong(timestamp)));

                            //set data
                            greeting_tv.setText("Welcome Back, " + merchantName);

                            try {
                                Picasso.get().load(merchantImage).placeholder(R.drawable.placeholder_color).into(merchant_image_iv);
                            } catch (Exception e) {
                                merchant_image_iv.setImageResource(R.drawable.placeholder_color);
                            }

                            current_date_tv.setText(formattedDate);
                        }
                        loadRatings();
                        loadTotalEarnings();
                        loadTotalOrders();
                        loadCompletedOrders();
                        loadPendingOrders();
                        loadCancelledOrders();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadRatings() {
        final DecimalFormat decimalFormat = new DecimalFormat("#.00");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            float ratingSum = 0;
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                float rating = Float.parseFloat("" + dataSnapshot.child("rating").getValue());
                                ratingSum = ratingSum + rating;
                            }
                            long noOfRatings = snapshot.getChildrenCount();
                            float avgRating = ratingSum / noOfRatings;
                            average_ratings_tv.setText("" + decimalFormat.format(avgRating));
                            ratings_count_tv.setText("" + noOfRatings + " Rating(s)");
                        } else {
                            average_ratings_tv.setText("0.00");
                            ratings_count_tv.setText("-");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadTotalEarnings() {
        final DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
        //get orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double totalEarnings = 0.00;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String orderTotal = "" + dataSnapshot.child("orderTotal").getValue();
                            orderStatus = "" + dataSnapshot.child("orderStatus").getValue();

                            if (orderStatus.equals("Delivered")) {
                                totalEarnings = totalEarnings + Double.parseDouble(orderTotal);
                            }
                        }
                        if (totalEarnings != 0.00) {
                            total_earnings_tv.setText(decimalFormat.format(totalEarnings));
                        } else {
                            total_earnings_tv.setText("N/A");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadTotalOrders() {
        //get orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String totalOrders = "" + snapshot.getChildrenCount();

                        //set data
                        if (!totalOrders.equals("0")) {
                            total_orders_tv.setText(totalOrders);
                        } else {
                            total_orders_tv.setText("-");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCompletedOrders() {
        orderArrayList = new ArrayList<>();
        //get completed orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderArrayList.clear();
                        //get data
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();

                            if (orderStatus.equals("Delivered")) {
                                Order order = dataSnapshot.getValue(Order.class);
                                orderArrayList.add(order);
                            }
                        }
                        //set data
                        if (orderArrayList.size() != 0) {
                            completed_orders_tv.setText("" + orderArrayList.size());
                        } else {
                            completed_orders_tv.setText("-");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPendingOrders() {
        orderArrayList = new ArrayList<>();
        //get completed orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderArrayList.clear();
                        //get data
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();

                            if (orderStatus.equals("Order Placed")) {
                                Order order = dataSnapshot.getValue(Order.class);
                                orderArrayList.add(order);
                            }
                        }
                        //set data
                        if (orderArrayList.size() != 0) {
                            pending_orders_tv.setText("" + orderArrayList.size());
                        } else {
                            pending_orders_tv.setText("-");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCancelledOrders() {
        orderArrayList = new ArrayList<>();
        //get cancelled orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderArrayList.clear();
                        //get data
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();

                            if (orderStatus.equals("Cancelled")) {
                                Order order = dataSnapshot.getValue(Order.class);
                                orderArrayList.add(order);
                            }
                        }
                        //set data
                        if (orderArrayList.size() != 0) {
                            cancelled_orders_tv.setText("" + orderArrayList.size());
                        } else {
                            cancelled_orders_tv.setText("-");
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
                    loadMerchantInfo();
                }
            }
        });
    }
}