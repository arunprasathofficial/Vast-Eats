package com.mad.vasteats.views.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.Constants;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.models.Order;
import com.mad.vasteats.presenters.MerchantOrderAdapter;

import java.util.ArrayList;

public class MerchantOrdersFragment extends Fragment {

    public MerchantOrdersFragment() {
        // Required empty public constructor
    }

    private ImageView filter_btn_iv;
    private TextView filter_status_tv;
    private RecyclerView orders_rv;
    private LinearLayout no_orders_msg_lt;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private ArrayList<Order> orderArrayList;
    private MerchantOrderAdapter merchantOrderAdapter;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_orders, container, false);

        filter_btn_iv = view.findViewById(R.id.filter_btn);
        filter_status_tv = view.findViewById(R.id.filter_status);
        orders_rv = view.findViewById(R.id.orders_rv);
        no_orders_msg_lt = view.findViewById(R.id.no_orders_msg);

        firebaseAuth = FirebaseAuth.getInstance();

        if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
            noInternetConnectionBottomSheet();
        } else {
            loadAllOrders();
        }

        filter_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Filter By Order Status")
                        .setItems(Constants.filterMerchantOrderStatus, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //get selected status
                                String filteredOrderStatus = Constants.filterMerchantOrderStatus[i];

                                if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
                                    noInternetConnectionBottomSheet();
                                    return;
                                }

                                if (filteredOrderStatus.equals("All")) {
                                    //load all orders
                                    loadAllOrders();
                                } else {
                                    //load filtered orders
                                    loadFilteredOrders(filteredOrderStatus);
                                }
                            }
                        }).show();
            }
        });

        return view;
    }

    private void loadAllOrders() {
        //init order list
        orderArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        orders_rv.setLayoutManager(linearLayoutManager);

        //get orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Order order = dataSnapshot.getValue(Order.class);
                            //add to orders list
                            orderArrayList.add(order);
                        }
                        //setup adapter
                        merchantOrderAdapter = new MerchantOrderAdapter(getContext(), orderArrayList);
                        //set to recyclerview
                        orders_rv.setAdapter(merchantOrderAdapter);

                        if (orderArrayList.size() == 0) {
                            orders_rv.setVisibility(View.GONE);
                            no_orders_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            no_orders_msg_lt.setVisibility(View.GONE);
                            orders_rv.setVisibility(View.VISIBLE);
                        }
                        filter_status_tv.setText("All (" + orderArrayList.size() + ")");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredOrders(final String filteredOrderStatus) {
        //init order list
        orderArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        orders_rv.setLayoutManager(linearLayoutManager);

        //get orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String status;
                            String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();
                            if (filteredOrderStatus.equals("Pending")) {
                                status = "Order Placed";
                                if (status.equals(orderStatus)) {
                                    Order order = dataSnapshot.getValue(Order.class);
                                    //add to orders list
                                    orderArrayList.add(order);
                                }
                            } else if (filteredOrderStatus.equals("Confirmed")) {
                                status = "Order Confirmed";
                                if (status.equals(orderStatus)) {
                                    Order order = dataSnapshot.getValue(Order.class);
                                    //add to orders list
                                    orderArrayList.add(order);
                                }
                            } else if (filteredOrderStatus.equals("Processed")) {
                                status = "Order Processed";
                                if (status.equals(orderStatus)) {
                                    Order order = dataSnapshot.getValue(Order.class);
                                    //add to orders list
                                    orderArrayList.add(order);
                                }
                            } else if (filteredOrderStatus.equals("Delivered")) {
                                status = "Delivered";
                                if (status.equals(orderStatus)) {
                                    Order order = dataSnapshot.getValue(Order.class);
                                    //add to orders list
                                    orderArrayList.add(order);
                                }
                            } else if (filteredOrderStatus.equals("Cancelled")) {
                                status = "Cancelled";
                                if (status.equals(orderStatus)) {
                                    Order order = dataSnapshot.getValue(Order.class);
                                    //add to orders list
                                    orderArrayList.add(order);
                                }
                            }
                        }
                        //setup adapter
                        merchantOrderAdapter = new MerchantOrderAdapter(getContext(), orderArrayList);
                        //set to recyclerview
                        orders_rv.setAdapter(merchantOrderAdapter);

                        if (orderArrayList.size() == 0) {
                            orders_rv.setVisibility(View.GONE);
                            no_orders_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            no_orders_msg_lt.setVisibility(View.GONE);
                            orders_rv.setVisibility(View.VISIBLE);
                        }
                        filter_status_tv.setText(filteredOrderStatus + " (" + orderArrayList.size() + ")");
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
                    loadAllOrders();
                }
            }
        });
    }
}