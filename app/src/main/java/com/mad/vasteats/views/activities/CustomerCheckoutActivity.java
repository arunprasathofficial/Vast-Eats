package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.Constants;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.helpers.easyDB;
import com.mad.vasteats.models.CartItem;
import com.mad.vasteats.presenters.CustomerCheckoutItemAdapter;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomerCheckoutActivity extends AppCompatActivity {

    private ImageView back_btn_iv;
    private TextView customer_name_tv, customer_address_tv, merchant_name_tv, sub_total_tv, delivery_fee_tv,
            checkout_total_tv, ok_btn_tv;
    private RecyclerView item_list_rv;
    private Button place_order_btn;
    private LinearLayout unavailable_msg_lt;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private FirebaseAuth firebaseAuth;

    private DecimalFormat decimalFormat;

    private ArrayList<CartItem> cartItems;
    private CustomerCheckoutItemAdapter customerCheckoutItemAdapter;

    private String merchantUid, customerUid, customerName, customerCity, customerAddress, customerEmail,
            customerPhoneNo, merchantPhoneNo, merchantName, merchantAddress, merchantImage, merchantCity,
            merchantEmail, merchantOpen, deliveryFee, online;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_checkout);

        back_btn_iv = findViewById(R.id.back_btn);
        customer_name_tv = findViewById(R.id.customer_name);
        customer_address_tv = findViewById(R.id.customer_address);
        merchant_name_tv = findViewById(R.id.merchant_name);
        sub_total_tv = findViewById(R.id.sub_total);
        delivery_fee_tv = findViewById(R.id.delivery_fee);
        checkout_total_tv = findViewById(R.id.checkout_total);
        ok_btn_tv = findViewById(R.id.ok_btn);
        item_list_rv = findViewById(R.id.item_list_rv);
        place_order_btn = findViewById(R.id.place_order_btn);
        unavailable_msg_lt = findViewById(R.id.unavailable_msg);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Placing Order");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        decimalFormat = new DecimalFormat("#,###.00");

        //get id of merchant
        SharedPreferences merchantSharedPreferences = this.getSharedPreferences("merchantUid_Pref", Context.MODE_PRIVATE);
        merchantUid = merchantSharedPreferences.getString("merchantUid", "");

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
            loadCustomerDetails();
            loadMerchantDetails();
        } else {
            noInternetConnectionBottomSheet();
        }

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goes back to previous activity or fragment
                onBackPressed();
            }
        });

        place_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToInternet.isConnectedToInternet(CustomerCheckoutActivity.this)) {
                    placeOrder();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });

        ok_btn_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goes to home activity
                Intent intent = new Intent(CustomerCheckoutActivity.this, MainCustomerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadMerchantDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //get user data
                            customerUid = "" + dataSnapshot.child("uid").getValue();
                            customerName = "" + dataSnapshot.child("fullName").getValue();
                            customerCity = "" + dataSnapshot.child("city").getValue();
                            customerAddress = "" + dataSnapshot.child("completeAddress").getValue();
                            customerEmail = "" + dataSnapshot.child("email").getValue();
                            customerPhoneNo = "" + dataSnapshot.child("phoneNo").getValue();

                            //set data
                            customer_name_tv.setText(customerName);
                            customer_address_tv.setText(customerAddress);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCustomerDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                merchantName = "" + snapshot.child("merchantName").getValue();
                merchantAddress = "" + snapshot.child("merchantAddress").getValue();
                merchantImage = "" + snapshot.child("merchantImage").getValue();
                merchantCity = "" + snapshot.child("merchantCity").getValue();
                merchantPhoneNo = "" + snapshot.child("phoneNo").getValue();
                merchantEmail = "" + snapshot.child("email").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                merchantOpen = "" + snapshot.child("merchantOpen").getValue();
                online = "" + snapshot.child("online").getValue();

                //set data
                merchant_name_tv.setText(merchantName);
                delivery_fee_tv.setText("LKR " + String.valueOf(decimalFormat.format(Double.parseDouble(deliveryFee))));

                if (merchantOpen.equals("true") && online.equals("true")) {
                    place_order_btn.setVisibility(View.VISIBLE);
                    unavailable_msg_lt.setVisibility(View.GONE);
                } else {
                    place_order_btn.setVisibility(View.GONE);
                    unavailable_msg_lt.setVisibility(View.VISIBLE);
                }

                loadCartItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCartItems() {
        //init list
        cartItems = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        item_list_rv.setLayoutManager(linearLayoutManager);

        Double subTotal = 0.00;
        //get all records from AndroidEasySQL DB
        Cursor res = easyDB.easyDB.getAllData();
        while (res.moveToNext()) {
            String rowId = res.getString(0);
            String fId = res.getString(1);
            String fName = res.getString(2);
            String fImage = res.getString(3);
            String fQty = res.getString(4);
            String fRate = res.getString(5);
            String fTotal = res.getString(6);

            subTotal = subTotal + Double.parseDouble(fTotal);

            CartItem cartItem = new CartItem(
                    "" + rowId,
                    "" + fId,
                    "" + fName,
                    "" + fImage,
                    "" + fQty,
                    "" + fRate,
                    "" + fTotal
            );

            cartItems.add(cartItem);
        }

        //setup adapter
        customerCheckoutItemAdapter = new CustomerCheckoutItemAdapter(this, cartItems);
        //set to recyclerview
        item_list_rv.setAdapter(customerCheckoutItemAdapter);

        sub_total_tv.setText("LKR " + decimalFormat.format(subTotal));
        checkout_total_tv.setText("LKR " + decimalFormat.format((Double.parseDouble(deliveryFee) + subTotal)));
    }

    private void placeOrder() {
        decimalFormat = new DecimalFormat("#,###.00");

        //show progress dialog
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        //for order id and order time
        final String timestamp = "" + System.currentTimeMillis();

        //get data
        final String orderAmount = checkout_total_tv.getText().toString().trim().replaceAll("([a-zA-Z \\\\,])", "");

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderPlacedDateTime", "" + timestamp);
        hashMap.put("orderConfirmedDateTime", "" + "");
        hashMap.put("orderProcessedDateTime", "" + "");
        hashMap.put("orderDeliveredDateTime", "" + "");
        hashMap.put("orderCancelledDateTime", "" + "");
        hashMap.put("orderStatus", "Order Placed");
        hashMap.put("orderTotal", "" + orderAmount);
        hashMap.put("deliveryFee", "" + deliveryFee);
        hashMap.put("deliveryAddress", "" + customerAddress);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + merchantUid);
        hashMap.put("orderRating", "");

        if (merchantOpen.equals("true") && online.equals("true")) {
            //add to db
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(merchantUid).child("Orders");
            databaseReference.child(timestamp).setValue(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            for (int i = 0; i < cartItems.size(); i++) {
                                //get data
                                String fId = cartItems.get(i).getCartFoodId();
                                String fName = cartItems.get(i).getCartFoodName();
                                String fImage = cartItems.get(i).getCartFoodImage();
                                String fQty = cartItems.get(i).getCartFoodQty();
                                String fRate = cartItems.get(i).getCartFoodRate();
                                String fTotal = cartItems.get(i).getCartFoodTotal();

                                //setup order items data
                                HashMap<String, String> hashMap1 = new HashMap<>();
                                hashMap1.put("foodId", "" + fId);
                                hashMap1.put("foodName", "" + fName);
                                hashMap1.put("foodImage", "" + fImage);
                                hashMap1.put("foodQty", "" + fQty);
                                hashMap1.put("foodRate", "" + fRate);
                                hashMap1.put("foodTotal", "" + fTotal);

                                databaseReference.child(timestamp).child("Items").child(fId).setValue(hashMap1);
                            }
                            progressDialog.dismiss();

                            //delete cart items
                            deleteCartData();

                            //send notification
                            prepareNotificationMessage(timestamp, orderAmount);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            TastyToast.makeText(CustomerCheckoutActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                        }
                    });
        } else {
            place_order_btn.setVisibility(View.GONE);
            unavailable_msg_lt.setVisibility(View.VISIBLE);
        }
    }

    private void deleteCartData() {
        easyDB.init(this);
        easyDB.easyDB.deleteAllDataFromTable();
    }

    private void prepareNotificationMessage(String orderId, String orderAmount) {
        //when customer place order, notification will be send to merchant

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order #" + orderId;
        String NOTIFICATION_MESSAGE = "Congratulation...! You have received an order";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationObj = new JSONObject();
        JSONObject notificationBodyObj = new JSONObject();

        try {
            //what to send
            notificationBodyObj.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyObj.put("customerUid", firebaseAuth.getUid());
            notificationBodyObj.put("merchantUid", merchantUid);
            notificationBodyObj.put("orderId", orderId);
            notificationBodyObj.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyObj.put("notificationMessage", NOTIFICATION_MESSAGE);
            //where to send
            notificationObj.put("to", NOTIFICATION_TOPIC);
            notificationObj.put("data", notificationBodyObj);
        } catch (Exception e) {
            TastyToast.makeText(this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
        }

        sendFCMNotification(notificationObj, orderId, orderAmount);
    }

    private void sendFCMNotification(JSONObject notificationObj, final String orderId, final String orderAmount) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.FCM_API, notificationObj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //goes to order confirmation activity
                Intent intent = new Intent(CustomerCheckoutActivity.this, CustomerOrderConfirmationActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderAmount", orderAmount);
                startActivity(intent);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //goes to order confirmation activity
                Intent intent = new Intent(CustomerCheckoutActivity.this, CustomerOrderConfirmationActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderAmount", orderAmount);
                startActivity(intent);
                finish();
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
                if (!IsConnectedToInternet.isConnectedToInternet(CustomerCheckoutActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadCustomerDetails();
                    loadMerchantDetails();
                }
            }
        });
    }
}