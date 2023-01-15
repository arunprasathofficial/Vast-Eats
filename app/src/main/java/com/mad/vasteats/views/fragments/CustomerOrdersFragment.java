package com.mad.vasteats.views.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.mad.vasteats.presenters.CustomerOrderAdapter;

import java.util.ArrayList;

public class CustomerOrdersFragment extends Fragment {

    public CustomerOrdersFragment() {
        // Required empty public constructor
    }

    private ImageView filter_btn_iv;
    private TextView filter_status_tv;
    private RecyclerView orders_rv;
    private LinearLayout no_orders_msg_lt;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private ArrayList<Order> orderArrayList;
    private CustomerOrderAdapter customerOrderAdapter;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_orders, container, false);

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
                        .setItems(Constants.filterCustomerOrderStatus, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //get selected status
                                String filteredOrderStatus = Constants.filterCustomerOrderStatus[i];

                                if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
                                    noInternetConnectionBottomSheet();
                                    return;
                                }

                                if (filteredOrderStatus.equals("All")) {
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
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = "" + dataSnapshot.getRef().getKey();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    databaseReference.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            Order order = dataSnapshot.getValue(Order.class);
                                            //add to orders list
                                            orderArrayList.add(order);
                                        }
                                        //setup adapter
                                        customerOrderAdapter = new CustomerOrderAdapter(getContext(), orderArrayList);
                                        //set to recyclerview
                                        orders_rv.setAdapter(customerOrderAdapter);

                                        no_orders_msg_lt.setVisibility(View.GONE);
                                        orders_rv.setVisibility(View.VISIBLE);
                                    }
                                    if (orderArrayList.size() == 0) {
                                        orders_rv.setVisibility(View.GONE);
                                        no_orders_msg_lt.setVisibility(View.VISIBLE);
                                    }
                                    filter_status_tv.setText("All (" + orderArrayList.size() + ")");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFilteredOrders(final String filteredOrderStatus) {
        orderArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        orders_rv.setLayoutManager(linearLayoutManager);

        //get filtered orders
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = "" + dataSnapshot.getRef().getKey();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    databaseReference.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            String orderStatus = "" + dataSnapshot.child("orderStatus").getValue();
                                            if (filteredOrderStatus.equals(orderStatus)) {
                                                Order order = dataSnapshot.getValue(Order.class);
                                                //add to orders list
                                                orderArrayList.add(order);
                                            }
                                        }
                                        //setup adapter
                                        customerOrderAdapter = new CustomerOrderAdapter(getContext(), orderArrayList);
                                        //set to recyclerview
                                        orders_rv.setAdapter(customerOrderAdapter);

                                        no_orders_msg_lt.setVisibility(View.GONE);
                                        orders_rv.setVisibility(View.VISIBLE);
                                    }
                                    if (orderArrayList.size() == 0) {
                                        orders_rv.setVisibility(View.GONE);
                                        no_orders_msg_lt.setVisibility(View.VISIBLE);
                                    }
                                    //set selected status
                                    filter_status_tv.setText(filteredOrderStatus + " (" + orderArrayList.size() + ")");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

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
                    loadAllOrders();
                }
            }
        });
    }
}