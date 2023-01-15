package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.Constants;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.models.OrderedItem;
import com.mad.vasteats.presenters.MerchantOrderedItemAdapter;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MerchantOrderDetailsActivity extends AppCompatActivity {

    private ImageView back_btn_iv, phone_btn_iv, order_received_icon_iv, order_confirmed_icon_iv,
            order_processed_icon_iv, order_delivered_icon_iv, order_cancelled_icon_iv, navigation_btn_iv;
    private TextView order_id_tv, order_by_tv, order_status_tv, order_info_tab_tv, item_list_tab_tv,
            order_received_date_tv, delivered_heading_tv, order_delivered_date_tv, delivery_address_tv,
            order_total_tv, details_btn_tv, sub_total_tv, delivery_fee_tv, grand_total_tv;
    private ProgressBar order_confirmed_progress_bar, order_processed_progress_bar, order_delivered_progress_bar;
    private Button change_order_status_btn;
    private LinearLayout order_info_layout, item_list_layout;
    private RecyclerView item_list_rv;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private String orderId, orderBy, merchantAddress, customerName, customerPhone, deliveryAddress, orderStatus,
            orderPlacedDateTime, orderDeliveredDateTime, orderCancelledDateTime, orderTotal, deliveryFee,
            merchantName;
    private Double subTotal = 0.00;

    private DecimalFormat decimalFormat;
    private SimpleDateFormat simpleDateFormat;

    private ArrayList<OrderedItem> orderedItems;
    private MerchantOrderedItemAdapter merchantOrderedItemAdapter;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_order_details);

        back_btn_iv = findViewById(R.id.back_btn);
        phone_btn_iv = findViewById(R.id.phone_btn);
        order_received_icon_iv = findViewById(R.id.order_received_icon);
        order_confirmed_icon_iv = findViewById(R.id.order_confirmed_icon);
        order_processed_icon_iv = findViewById(R.id.order_processed_icon);
        order_delivered_icon_iv = findViewById(R.id.order_delivered_icon);
        order_cancelled_icon_iv = findViewById(R.id.order_cancelled_icon);
        navigation_btn_iv = findViewById(R.id.navigation_btn);
        order_id_tv = findViewById(R.id.order_id);
        order_by_tv = findViewById(R.id.order_by);
        order_status_tv = findViewById(R.id.order_status);
        order_info_tab_tv = findViewById(R.id.order_info_tab);
        item_list_tab_tv = findViewById(R.id.item_list_tab);
        order_received_date_tv = findViewById(R.id.order_received_date);
        delivered_heading_tv = findViewById(R.id.delivered_heading);
        order_delivered_date_tv = findViewById(R.id.order_delivered_date);
        delivery_address_tv = findViewById(R.id.delivery_address);
        order_total_tv = findViewById(R.id.order_total);
        details_btn_tv = findViewById(R.id.details_btn);
        sub_total_tv = findViewById(R.id.sub_total);
        delivery_fee_tv = findViewById(R.id.delivery_fee);
        grand_total_tv = findViewById(R.id.grand_total);
        order_confirmed_progress_bar = findViewById(R.id.order_confirmed_progress_bar);
        order_processed_progress_bar = findViewById(R.id.order_processed_progress_bar);
        order_delivered_progress_bar = findViewById(R.id.order_delivered_progress_bar);
        change_order_status_btn = findViewById(R.id.change_order_status_btn);
        order_info_layout = findViewById(R.id.order_info_layout);
        item_list_layout = findViewById(R.id.item_list_layout);
        item_list_rv = findViewById(R.id.item_list_rv);

        //get order id
        orderId = getIntent().getStringExtra("orderId");
        //get customer id
        orderBy = getIntent().getStringExtra("orderBy");

        decimalFormat = new DecimalFormat("#,###.00");
        simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Please wait");

        firebaseAuth = FirebaseAuth.getInstance();

        if (!IsConnectedToInternet.isConnectedToInternet(MerchantOrderDetailsActivity.this)) {
            noInternetConnectionBottomSheet();
        } else {
            loadMerchantDetails();
            loadCustomerDetails();
            loadOrderDetails();
        }

        order_info_tab_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item_list_layout.setVisibility(View.GONE);
                item_list_tab_tv.setBackgroundColor(getResources().getColor(R.color.dim_grey));

                order_info_layout.setVisibility(View.VISIBLE);
                order_info_tab_tv.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });

        item_list_tab_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order_info_layout.setVisibility(View.GONE);
                order_info_tab_tv.setBackgroundColor(getResources().getColor(R.color.dim_grey));

                item_list_layout.setVisibility(View.VISIBLE);
                item_list_tab_tv.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });

        phone_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialPhone();
            }
        });

        navigation_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        details_btn_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order_info_layout.setVisibility(View.GONE);
                order_info_tab_tv.setBackgroundColor(getResources().getColor(R.color.dim_grey));

                item_list_layout.setVisibility(View.VISIBLE);
                item_list_tab_tv.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });

        change_order_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MerchantOrderDetailsActivity.this);
                builder.setTitle("Change Order Status")
                        .setItems(Constants.changeMerchantOrderStatus, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //get selected status
                                String changedOrderStatus = Constants.changeMerchantOrderStatus[i];

                                if (!IsConnectedToInternet.isConnectedToInternet(MerchantOrderDetailsActivity.this)) {
                                    noInternetConnectionBottomSheet();
                                    return;
                                }

                                updateOrderStatus(changedOrderStatus);

                            }
                        }).show();
            }
        });

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goes back to previous activity or fragment
                onBackPressed();
            }
        });
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(customerPhone))));
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String addressAPI = "https://maps.google.com/maps?saddr=" + merchantAddress + "&daddr=" + deliveryAddress;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(addressAPI));
        startActivity(intent);
    }

    private void loadCustomerDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        customerName = "" + snapshot.child("fullName").getValue();
                        customerPhone = "" + snapshot.child("phoneNo").getValue();

                        //set data
                        order_by_tv.setText(customerName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMerchantDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        merchantName = "" + snapshot.child("merchantName").getValue();
                        merchantAddress = "" + snapshot.child("merchantAddress").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        deliveryAddress = "" + snapshot.child("deliveryAddress").getValue();
                        orderStatus = "" + snapshot.child("orderStatus").getValue();
                        orderPlacedDateTime = "" + snapshot.child("orderPlacedDateTime").getValue();
                        orderDeliveredDateTime = "" + snapshot.child("orderDeliveredDateTime").getValue();
                        orderCancelledDateTime = "" + snapshot.child("orderCancelledDateTime").getValue();
                        deliveryAddress = "" + snapshot.child("deliveryAddress").getValue();
                        orderTotal = "" + snapshot.child("orderTotal").getValue();
                        deliveryFee = "" + snapshot.child("deliveryFee").getValue();

                        //set data
                        order_id_tv.setText(orderId);
                        order_received_date_tv.setText(simpleDateFormat.format(new Date(Long.parseLong(orderPlacedDateTime))));
                        delivery_address_tv.setText(deliveryAddress);
                        order_total_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(orderTotal)));
                        delivery_fee_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(deliveryFee)));

                        if (orderStatus.equals("Order Placed")) {
                            order_status_tv.setText("Order received, confirmation pending.");
                            order_status_tv.setTextColor(getResources().getColor(R.color.blue_grey));
                            delivered_heading_tv.setText("Delivered");
                            order_delivered_date_tv.setText("-");

                        } else if (orderStatus.equals("Order Confirmed")) {
                            order_status_tv.setText("Confirmed and processing.");
                            order_status_tv.setTextColor(getResources().getColor(R.color.orange));
                            delivered_heading_tv.setText("Delivered");
                            order_delivered_date_tv.setText("-");

                        } else if (orderStatus.equals("Order Processed")) {
                            order_status_tv.setText("Processed and ready to delivery.");
                            order_status_tv.setTextColor(getResources().getColor(R.color.dark_yellow));
                            delivered_heading_tv.setText("Delivered");
                            order_delivered_date_tv.setText("-");

                        } else if (orderStatus.equals("Delivered")) {
                            order_status_tv.setText("Delivered successfully!");
                            order_status_tv.setTextColor(getResources().getColor(R.color.google_green));
                            delivered_heading_tv.setText("Delivered");

                            if (orderDeliveredDateTime.equals("") || orderDeliveredDateTime.equals("null")) {
                                order_delivered_date_tv.setText("-");
                            } else {
                                order_delivered_date_tv.setText(simpleDateFormat.format(new Date(Long.parseLong(orderDeliveredDateTime))));
                            }

                        } else if (orderStatus.equals("Cancelled")) {
                            order_status_tv.setText("Cancelled");
                            order_status_tv.setTextColor(getResources().getColor(R.color.red));
                            delivered_heading_tv.setText("Cancelled");

                            if (orderCancelledDateTime.equals("") || orderCancelledDateTime.equals("null")) {
                                order_delivered_date_tv.setText("-");
                            } else {
                                order_delivered_date_tv.setText(simpleDateFormat.format(new Date(Long.parseLong(orderCancelledDateTime))));
                            }
                        }

                        if (orderStatus.equals("Delivered") || orderStatus.equals("Cancelled")) {
                            change_order_status_btn.setVisibility(View.GONE);
                        } else {
                            change_order_status_btn.setVisibility(View.VISIBLE);
                        }

                        loadOrderedItems();
                        loadOrderStatusTimeline(orderStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems() {
        orderedItems = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        item_list_rv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItems.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            OrderedItem orderedItem = dataSnapshot.getValue(OrderedItem.class);
                            String foodTotal = "" + dataSnapshot.child("foodTotal").getValue();
                            subTotal = subTotal + Double.parseDouble(foodTotal);

                            //add to list
                            orderedItems.add(orderedItem);
                        }
                        //calculate grand total
                        Double grandTotal = subTotal + Double.parseDouble(deliveryFee);
                        //setup adapter
                        merchantOrderedItemAdapter = new MerchantOrderedItemAdapter(MerchantOrderDetailsActivity.this, orderedItems);
                        //set adapter
                        item_list_rv.setAdapter(merchantOrderedItemAdapter);
                        //set subtotal
                        sub_total_tv.setText("LKR " + decimalFormat.format(subTotal));
                        //set grand total
                        grand_total_tv.setText("LKR " + decimalFormat.format(grandTotal));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderStatusTimeline(String orderStatus) {
        if (orderStatus.equals("Order Placed")) {
            //first step
            order_received_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            //other steps
            order_confirmed_icon_iv.setVisibility(View.VISIBLE);
            order_confirmed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.dim_grey), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_confirmed_progress_bar.setVisibility(View.VISIBLE);
            order_confirmed_progress_bar.setProgress(0);

            order_processed_icon_iv.setVisibility(View.VISIBLE);
            order_processed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.dim_grey), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_processed_progress_bar.setVisibility(View.VISIBLE);
            order_processed_progress_bar.setProgress(0);

            order_delivered_icon_iv.setVisibility(View.VISIBLE);
            order_delivered_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_delivered_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.dim_grey));
            order_delivered_progress_bar.setVisibility(View.VISIBLE);
            order_delivered_progress_bar.setProgress(0);

            order_cancelled_icon_iv.setVisibility(View.GONE);

        } else if (orderStatus.equals("Order Confirmed")) {
            //first step
            order_received_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            //second step
            order_confirmed_icon_iv.setVisibility(View.VISIBLE);
            order_confirmed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_confirmed_progress_bar.setVisibility(View.VISIBLE);
            order_confirmed_progress_bar.setProgress(100);
            //other steps
            order_processed_icon_iv.setVisibility(View.VISIBLE);
            order_processed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.dim_grey), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_processed_progress_bar.setVisibility(View.VISIBLE);
            order_processed_progress_bar.setProgress(0);

            order_delivered_icon_iv.setVisibility(View.VISIBLE);
            order_delivered_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_delivered_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.dim_grey));
            order_delivered_progress_bar.setVisibility(View.VISIBLE);
            order_delivered_progress_bar.setProgress(0);

            order_cancelled_icon_iv.setVisibility(View.GONE);

        } else if (orderStatus.equals("Order Processed")) {
            //first step
            order_received_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            //second step
            order_confirmed_icon_iv.setVisibility(View.VISIBLE);
            order_confirmed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_confirmed_progress_bar.setVisibility(View.VISIBLE);
            order_confirmed_progress_bar.setProgress(100);
            //third step
            order_processed_icon_iv.setVisibility(View.VISIBLE);
            order_processed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_processed_progress_bar.setVisibility(View.VISIBLE);
            order_processed_progress_bar.setProgress(100);
            //other steps
            order_delivered_icon_iv.setVisibility(View.VISIBLE);
            order_delivered_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_delivered_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.dim_grey));
            order_delivered_progress_bar.setVisibility(View.VISIBLE);
            order_delivered_progress_bar.setProgress(0);
            order_delivered_progress_bar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.google_green)));

            order_cancelled_icon_iv.setVisibility(View.GONE);

        } else if (orderStatus.equals("Delivered")) {
            //first step
            order_received_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            //second step
            order_confirmed_icon_iv.setVisibility(View.VISIBLE);
            order_confirmed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_confirmed_progress_bar.setVisibility(View.VISIBLE);
            order_confirmed_progress_bar.setProgress(100);
            //third step
            order_processed_icon_iv.setVisibility(View.VISIBLE);
            order_processed_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            order_processed_progress_bar.setVisibility(View.VISIBLE);
            order_processed_progress_bar.setProgress(100);
            //fourth step
            order_delivered_icon_iv.setVisibility(View.VISIBLE);
            order_delivered_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_delivered_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green));
            order_delivered_progress_bar.setVisibility(View.VISIBLE);
            order_delivered_progress_bar.setProgress(100);
            order_delivered_progress_bar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.google_green)));
            //other steps
            order_cancelled_icon_iv.setVisibility(View.GONE);

        } else if (orderStatus.equals("Cancelled")) {
            //first step
            order_received_icon_iv.setColorFilter(ContextCompat.getColor(this, R.color.google_green), android.graphics.PorterDuff.Mode.MULTIPLY);
            //fourth step
            order_cancelled_icon_iv.setVisibility(View.VISIBLE);
            order_delivered_progress_bar.setProgress(100);
            order_delivered_progress_bar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
            //other steps
            order_confirmed_icon_iv.setVisibility(View.GONE);
            order_confirmed_progress_bar.setVisibility(View.GONE);
            order_processed_icon_iv.setVisibility(View.GONE);
            order_processed_progress_bar.setVisibility(View.GONE);
            order_delivered_icon_iv.setVisibility(View.GONE);
        }
    }

    private void updateOrderStatus(final String changedOrderStatus) {
        //cancel order
        progressDialog.setMessage("Updating order status");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        if (changedOrderStatus.equals("Pending")) {
            //setup data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("orderStatus", "Order Placed");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Orders").child(orderId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            //if order status updated
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "Order status updated to 'Pending'", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //if any error happened
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });

        } else if (changedOrderStatus.equals("Confirmed")) {
            //setup data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("orderStatus", "Order Confirmed");
            hashMap.put("orderConfirmedDateTime", "" + timestamp);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Orders").child(orderId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            //if order status updated
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "Order status updated to 'Confirmed'", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //if any error happened
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });
        } else if (changedOrderStatus.equals("Processed")) {
            //setup data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("orderStatus", "Order Processed");
            hashMap.put("orderProcessedDateTime", "" + timestamp);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Orders").child(orderId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            //if order status updated
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "Order status updated to 'Processed'", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //if any error happened
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });
        } else if (changedOrderStatus.equals("Delivered")) {
            //setup data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("orderStatus", "Delivered");
            hashMap.put("orderDeliveredDateTime", "" + timestamp);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Orders").child(orderId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            //if order status updated
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "Order status updated to 'Delivered'", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //if any error happened
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });
        } else if (changedOrderStatus.equals("Cancelled")) {
            //setup data
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("orderStatus", "Cancelled");
            hashMap.put("orderCancelledDateTime", "" + timestamp);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Orders").child(orderId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            //if order status updated
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "Order status updated to 'Cancelled'", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //if any error happened
                            TastyToast.makeText(MerchantOrderDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });
        }
        prepareNotificationMessage(orderId, changedOrderStatus, merchantName);
    }

    private void prepareNotificationMessage(String orderId, String orderStatus, String merchantName) {
        //when the merchant change the order status, notification will be send to customer

        //prepare data for notification
        String NOTIFICATION_TOPIC = null;
        String NOTIFICATION_TITLE = null;
        String NOTIFICATION_MESSAGE = null;
        String NOTIFICATION_TYPE = null;

        //prepare json (what to send and where to send)
        JSONObject notificationObj = new JSONObject();
        JSONObject notificationBodyObj = new JSONObject();

        if (orderStatus.equals("Confirmed")) {
            NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
            NOTIFICATION_TITLE = "Thank you for your order with #" + orderId;
            NOTIFICATION_MESSAGE = "Your order has been accepted by " + merchantName + " and is being processed";
            NOTIFICATION_TYPE = "OrderStatusChanged";

        } else if (orderStatus.equals("Processed")) {
            NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
            NOTIFICATION_TITLE = "Your order (#" + orderId + ") status changed";
            NOTIFICATION_MESSAGE = "Your order is now ready to delivery";
            NOTIFICATION_TYPE = "OrderStatusChanged";

        } else if (orderStatus.equals("Delivered")) {
            NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
            NOTIFICATION_TITLE = "Your order completed";
            NOTIFICATION_MESSAGE = "Your order has been completed with Order ID: #" + orderId;
            NOTIFICATION_TYPE = "OrderStatusChanged";

        } else if (orderStatus.equals("Cancelled")) {
            NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
            NOTIFICATION_TITLE = "Your order cancelled";
            NOTIFICATION_MESSAGE = "Your order has been cancelled by " + merchantName;
            NOTIFICATION_TYPE = "OrderStatusChanged";

        }

        try {
            //what to send
            notificationBodyObj.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyObj.put("customerUid", orderBy);
            notificationBodyObj.put("merchantUid", firebaseAuth.getUid());
            notificationBodyObj.put("orderId", orderId);
            notificationBodyObj.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyObj.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationObj.put("to", NOTIFICATION_TOPIC);
            notificationObj.put("data", notificationBodyObj);
        } catch (Exception e) {
            TastyToast.makeText(this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }

        sendFCMNotification(notificationObj);
    }

    private void sendFCMNotification(JSONObject notificationObj) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.FCM_API, notificationObj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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
                if (!IsConnectedToInternet.isConnectedToInternet(MerchantOrderDetailsActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadMerchantDetails();
                    loadCustomerDetails();
                    loadOrderDetails();
                }
            }
        });
    }
}