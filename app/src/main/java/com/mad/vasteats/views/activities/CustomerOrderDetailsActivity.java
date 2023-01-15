package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
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
import com.mad.vasteats.presenters.CustomerOrderedItemAdapter;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomerOrderDetailsActivity extends AppCompatActivity {

    private ImageView back_btn_iv, up_down_btn_iv;
    private TextView order_id_tv, merchant_name_tv, merchant_address_tv, customer_name_tv, customer_address_tv,
            sub_total_iv, delivery_fee_tv, order_total_tv, cancel_order_tv;
    private RatingBar order_rating_bar;
    private Button rate_order_btn;
    private CircularImageView merchant_image_iv;
    private RecyclerView ordered_items_rv;
    private View payment_method_divider;
    private LinearLayout payment_method_lt;
    private CardView timeline_cv;

    private ImageView order_step_one_icon_iv, order_step_two_icon_iv, order_step_three_icon_iv,
            order_final_step_icon_iv;
    private LinearLayout order_step_one_layout, order_step_two_layout, order_step_three_layout,
            order_final_step_layout;
    private TextView order_step_one_description_tv, order_step_one_date_tv, order_step_two_description_tv,
            order_step_two_date_tv, order_step_three_description_tv, order_step_three_date_tv,
            order_final_step_description_tv, order_final_step_date_tv;
    private View order_step_two_progress, order_step_three_progress, order_final_step_progress;

    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private String orderTo, orderId, customerName;
    private Double subTotal = 0.00;

    private DecimalFormat decimalFormat;
    private SimpleDateFormat simpleDateFormat;

    private FirebaseAuth firebaseAuth;

    private ArrayList<OrderedItem> orderedItems;
    private CustomerOrderedItemAdapter customerOrderedItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_details);

        back_btn_iv = findViewById(R.id.back_btn);
        up_down_btn_iv = findViewById(R.id.up_down_btn);
        order_id_tv = findViewById(R.id.order_id);
        merchant_name_tv = findViewById(R.id.merchant_name);
        merchant_address_tv = findViewById(R.id.merchant_address);
        customer_name_tv = findViewById(R.id.customer_name);
        customer_address_tv = findViewById(R.id.customer_address);
        sub_total_iv = findViewById(R.id.sub_total);
        delivery_fee_tv = findViewById(R.id.delivery_fee);
        order_total_tv = findViewById(R.id.order_total);
        cancel_order_tv = findViewById(R.id.cancel_order);
        order_rating_bar = findViewById(R.id.order_rating_bar);
        rate_order_btn = findViewById(R.id.rate_order_btn);
        merchant_image_iv = findViewById(R.id.merchant_image);
        ordered_items_rv = findViewById(R.id.ordered_items_rv);
        payment_method_divider = findViewById(R.id.payment_method_divider);
        payment_method_lt = findViewById(R.id.payment_method);
        timeline_cv = findViewById(R.id.timeline_cv);

        order_step_one_icon_iv = findViewById(R.id.order_step_one_icon);
        order_step_two_icon_iv = findViewById(R.id.order_step_two_icon);
        order_step_three_icon_iv = findViewById(R.id.order_step_three_icon);
        order_final_step_icon_iv = findViewById(R.id.order_final_step_icon);
        order_step_one_layout = findViewById(R.id.order_step_one_layout);
        order_step_two_layout = findViewById(R.id.order_step_two_layout);
        order_step_three_layout = findViewById(R.id.order_step_three_layout);
        order_final_step_layout = findViewById(R.id.order_final_step_layout);
        order_step_one_description_tv = findViewById(R.id.order_step_one_description);
        order_step_one_date_tv = findViewById(R.id.order_step_one_date);
        order_step_two_description_tv = findViewById(R.id.order_step_two_description);
        order_step_two_date_tv = findViewById(R.id.order_step_two_date);
        order_step_three_description_tv = findViewById(R.id.order_step_three_description);
        order_step_three_date_tv = findViewById(R.id.order_step_three_date);
        order_final_step_description_tv = findViewById(R.id.order_final_step_description);
        order_final_step_date_tv = findViewById(R.id.order_final_step_date);
        order_step_two_progress = findViewById(R.id.order_step_two_progress);
        order_step_three_progress = findViewById(R.id.order_step_three_progress);
        order_final_step_progress = findViewById(R.id.order_final_step_progress);

        //get order id and order to from adapter
        orderId = getIntent().getStringExtra("orderId");
        orderTo = getIntent().getStringExtra("orderTo");

        decimalFormat = new DecimalFormat("#,###.00");
        simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");

        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Please wait");

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
            loadMerchantDetails();
            loadOrderDetails();
            loadOrderedItems();
        } else {
            noInternetConnectionBottomSheet();
        }

        if (timeline_cv.getVisibility() == View.GONE) {
            up_down_btn_iv.setImageResource(R.drawable.ic_arrow_down_grey);
        } else {
            up_down_btn_iv.setImageResource(R.drawable.ic_arrow_up_grey);
        }

        up_down_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeline_cv.getVisibility() == View.GONE) {
                    up_down_btn_iv.setImageResource(R.drawable.ic_arrow_up_grey);
                    timeline_cv.setVisibility(View.VISIBLE);
                } else {
                    up_down_btn_iv.setImageResource(R.drawable.ic_arrow_down_grey);
                    timeline_cv.setVisibility(View.GONE);
                }
            }
        });

        rate_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsConnectedToInternet.isConnectedToInternet(CustomerOrderDetailsActivity.this)) {
                    rateOrder();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });

        cancel_order_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomerOrderDetailsActivity.this);
                builder.setTitle("Order Cancellation")
                        .setMessage("Are you sure, do you want to cancel order '#" + orderId + "' ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (IsConnectedToInternet.isConnectedToInternet(CustomerOrderDetailsActivity.this)) {
                                    cancelOrder();
                                } else {
                                    noInternetConnectionBottomSheet();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //dismiss the alert dialog
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadMerchantDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String merchantName = "" + snapshot.child("merchantName").getValue();
                        String merchantAddress = "" + snapshot.child("merchantAddress").getValue();
                        String merchantImage = "" + snapshot.child("merchantImage").getValue();

                        //set data
                        merchant_name_tv.setText(merchantName);
                        merchant_address_tv.setText(merchantAddress);

                        try {
                            Picasso.get().load(merchantImage).placeholder(R.drawable.placeholder_color).into(merchant_image_iv);
                        } catch (Exception e) {
                            merchant_image_iv.setImageResource(R.drawable.placeholder_color);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderId = "" + snapshot.child("orderId").getValue();
                        String orderBy = "" + snapshot.child("orderBy").getValue();
                        String orderPlacedDateTime = "" + snapshot.child("orderPlacedDateTime").getValue();
                        String orderConfirmedDateTime = "" + snapshot.child("orderConfirmedDateTime").getValue();
                        String orderProcessedDateTime = "" + snapshot.child("orderProcessedDateTime").getValue();
                        String orderDeliveredDateTime = "" + snapshot.child("orderDeliveredDateTime").getValue();
                        String orderCancelledDateTime = "" + snapshot.child("orderCancelledDateTime").getValue();
                        String orderTotal = "" + snapshot.child("orderTotal").getValue();
                        String orderStatus = "" + snapshot.child("orderStatus").getValue();
                        String orderRating = "" + snapshot.child("orderRating").getValue();
                        String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        String deliveryAddress = "" + snapshot.child("deliveryAddress").getValue();

                        //set data
                        order_id_tv.setText(orderId);
                        customer_address_tv.setText(deliveryAddress);
                        delivery_fee_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(deliveryFee)));
                        order_total_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(orderTotal)));

                        if (!orderStatus.equals("Delivered")) {
                            rate_order_btn.setVisibility(View.GONE);
                            order_rating_bar.setVisibility(View.GONE);
                        } else {
                            if (orderRating.equals("") || orderRating.equals("null")) {
                                order_rating_bar.setVisibility(View.GONE);
                                rate_order_btn.setVisibility(View.VISIBLE);
                            } else {
                                rate_order_btn.setVisibility(View.GONE);
                                order_rating_bar.setVisibility(View.VISIBLE);
                                order_rating_bar.setRating(Float.parseFloat(orderRating));
                            }
                        }

                        if (orderStatus.equals("Order Placed")) {
                            cancel_order_tv.setVisibility(View.VISIBLE);
                        } else {
                            cancel_order_tv.setVisibility(View.GONE);
                        }

                        if (orderPlacedDateTime.equals("") || orderPlacedDateTime.equals("null")) {
                            order_step_one_date_tv.setText("-");
                        } else {
                            order_step_one_date_tv.setText(simpleDateFormat.format(Long.parseLong(orderPlacedDateTime)));
                        }

                        if (orderConfirmedDateTime.equals("") || orderConfirmedDateTime.equals("null")) {
                            order_step_two_date_tv.setText("-");
                        } else {
                            order_step_two_date_tv.setText(simpleDateFormat.format(Long.parseLong(orderConfirmedDateTime)));
                        }

                        if (orderProcessedDateTime.equals("") || orderProcessedDateTime.equals("null")) {
                            order_step_three_date_tv.setText("-");
                        } else {
                            order_step_three_date_tv.setText(simpleDateFormat.format(Long.parseLong(orderProcessedDateTime)));
                        }

                        if (orderStatus.equals("Delivered")) {
                            if (orderDeliveredDateTime.equals("") || orderDeliveredDateTime.equals("null")) {
                                order_final_step_date_tv.setText("-");
                            } else {
                                order_final_step_date_tv.setText(simpleDateFormat.format(Long.parseLong(orderDeliveredDateTime)));
                            }

                        } else if (orderStatus.equals("Cancelled")) {
                            if (orderCancelledDateTime.equals("") || orderCancelledDateTime.equals("null")) {
                                order_final_step_date_tv.setText("-");
                            } else {
                                order_final_step_date_tv.setText(simpleDateFormat.format(Long.parseLong(orderCancelledDateTime)));
                            }

                            payment_method_divider.setVisibility(View.GONE);
                            payment_method_lt.setVisibility(View.GONE);
                            cancel_order_tv.setVisibility(View.GONE);
                            order_rating_bar.setVisibility(View.GONE);
                            rate_order_btn.setVisibility(View.GONE);
                        }

                        loadCustomerDetails();
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
        ordered_items_rv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(orderTo).child("Orders").child(orderId).child("Items")
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
                        //setup adapter
                        customerOrderedItemAdapter = new CustomerOrderedItemAdapter(CustomerOrderDetailsActivity.this, orderedItems);
                        //set adapter
                        ordered_items_rv.setAdapter(customerOrderedItemAdapter);
                        //set subtotal
                        sub_total_iv.setText("LKR " + decimalFormat.format(subTotal));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCustomerDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        customerName = "" + snapshot.child("fullName").getValue();

                        //set data
                        customer_name_tv.setText(customerName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderStatusTimeline(String orderStatus) {

        if (orderStatus.equals("Order Placed")) {
            //step one
            order_step_one_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_one_description_tv.setText(orderStatus);
            //step two
            order_step_two_icon_iv.setVisibility(View.VISIBLE);
            order_step_two_icon_iv.setImageResource(R.drawable.ic_order_status_processing);
            order_step_two_progress.setVisibility(View.VISIBLE);
            order_step_two_progress.setBackgroundColor(getResources().getColor(R.color.faded_pink));
            order_step_two_layout.setVisibility(View.VISIBLE);
            order_step_two_description_tv.setText("Order still not confirmed by the merchant");
            order_step_two_date_tv.setText("-");
            //step three
            order_step_three_icon_iv.setVisibility(View.GONE);
            order_step_three_progress.setVisibility(View.GONE);
            order_step_three_layout.setVisibility(View.GONE);
            //final step
            order_final_step_icon_iv.setVisibility(View.GONE);
            order_final_step_progress.setVisibility(View.GONE);
            order_final_step_layout.setVisibility(View.GONE);

        } else if (orderStatus.equals("Order Confirmed")) {
            //step one
            order_step_one_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_one_description_tv.setText("Order Placed");
            //step two
            order_step_two_icon_iv.setVisibility(View.VISIBLE);
            order_step_two_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_two_progress.setVisibility(View.VISIBLE);
            order_step_two_progress.setBackgroundColor(getResources().getColor(R.color.google_green));
            order_step_two_layout.setVisibility(View.VISIBLE);
            order_step_two_description_tv.setText(orderStatus);
            //step three
            order_step_three_icon_iv.setVisibility(View.VISIBLE);
            order_step_three_icon_iv.setImageResource(R.drawable.ic_order_status_processing);
            order_step_three_progress.setVisibility(View.VISIBLE);
            order_step_three_progress.setBackgroundColor(getResources().getColor(R.color.faded_pink));
            order_step_three_layout.setVisibility(View.VISIBLE);
            order_step_three_description_tv.setText("Order Processing");
            order_step_three_date_tv.setText("-");
            //final step
            order_final_step_icon_iv.setVisibility(View.GONE);
            order_final_step_progress.setVisibility(View.GONE);
            order_final_step_layout.setVisibility(View.GONE);

        } else if (orderStatus.equals("Order Processed")) {
            //step one
            order_step_one_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_one_description_tv.setText("Order Placed");
            //step two
            order_step_two_icon_iv.setVisibility(View.VISIBLE);
            order_step_two_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_two_progress.setVisibility(View.VISIBLE);
            order_step_two_progress.setBackgroundColor(getResources().getColor(R.color.google_green));
            order_step_two_layout.setVisibility(View.VISIBLE);
            order_step_two_description_tv.setText("Order Confirmed");
            //step three
            order_step_three_icon_iv.setVisibility(View.VISIBLE);
            order_step_three_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_three_progress.setVisibility(View.VISIBLE);
            order_step_three_progress.setBackgroundColor(getResources().getColor(R.color.google_green));
            order_step_three_layout.setVisibility(View.VISIBLE);
            order_step_three_description_tv.setText(orderStatus);
            //final step
            order_final_step_icon_iv.setVisibility(View.VISIBLE);
            order_final_step_icon_iv.setImageResource(R.drawable.ic_order_status_processing);
            order_final_step_progress.setVisibility(View.VISIBLE);
            order_final_step_progress.setBackgroundColor(getResources().getColor(R.color.faded_pink));
            order_final_step_layout.setVisibility(View.VISIBLE);
            order_final_step_description_tv.setText("Out of Delivery");
            order_final_step_date_tv.setText("-");

        } else if (orderStatus.equals("Delivered")) {
            //step one
            order_step_one_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_one_description_tv.setText("Order Placed");
            //step two
            order_step_two_icon_iv.setVisibility(View.VISIBLE);
            order_step_two_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_two_progress.setVisibility(View.VISIBLE);
            order_step_two_progress.setBackgroundColor(getResources().getColor(R.color.google_green));
            order_step_two_layout.setVisibility(View.VISIBLE);
            order_step_two_description_tv.setText("Order Confirmed");
            //step three
            order_step_three_icon_iv.setVisibility(View.VISIBLE);
            order_step_three_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_three_progress.setVisibility(View.VISIBLE);
            order_step_three_progress.setBackgroundColor(getResources().getColor(R.color.google_green));
            order_step_three_layout.setVisibility(View.VISIBLE);
            order_step_three_description_tv.setText("Order Processed");
            //final step
            order_final_step_icon_iv.setVisibility(View.VISIBLE);
            order_final_step_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_final_step_progress.setVisibility(View.VISIBLE);
            order_final_step_progress.setBackgroundColor(getResources().getColor(R.color.google_green));
            order_final_step_layout.setVisibility(View.VISIBLE);
            order_final_step_description_tv.setText(orderStatus);

        } else if (orderStatus.equals("Cancelled")) {
            //step one
            order_step_one_icon_iv.setImageResource(R.drawable.ic_order_status_success);
            order_step_one_description_tv.setText("Order Placed");
            //step two
            order_step_two_icon_iv.setVisibility(View.GONE);
            order_step_two_progress.setVisibility(View.GONE);
            order_step_two_layout.setVisibility(View.GONE);
            //step three
            order_step_three_icon_iv.setVisibility(View.GONE);
            order_step_three_progress.setVisibility(View.GONE);
            order_step_three_layout.setVisibility(View.GONE);
            //final step
            order_final_step_icon_iv.setVisibility(View.VISIBLE);
            order_final_step_icon_iv.setImageResource(R.drawable.ic_order_status_cancelled);
            order_final_step_progress.setVisibility(View.VISIBLE);
            order_final_step_progress.setBackgroundColor(getResources().getColor(R.color.red));
            order_final_step_layout.setVisibility(View.VISIBLE);
            order_final_step_description_tv.setText(orderStatus);
        }
    }

    private void rateOrder() {
        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(this);

        //inflate view for bottom sheet
        View view = LayoutInflater.from(this).inflate(R.layout.customer_rate_order_layout, null);

        //set view to bottom sheet
        roundedBottomSheetDialog.setContentView(view);
        roundedBottomSheetDialog.setCanceledOnTouchOutside(false);

        ImageView close_btn_iv = view.findViewById(R.id.close_btn);
        final RatingBar order_rating_bar = view.findViewById(R.id.order_rating_bar);
        Button submit_rating_btn = view.findViewById(R.id.submit_rating_btn);

        roundedBottomSheetDialog.show();


        submit_rating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float getRating = order_rating_bar.getRating();
                submitRating(getRating);
            }
        });

        close_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roundedBottomSheetDialog.dismiss();
            }
        });
    }

    private void submitRating(final float getRating) {
        progressDialog.setMessage("Submitting your rating");
        progressDialog.show();

        if (getRating != 0.0) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("orderRating", "" + getRating);

            //submit rating
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(orderTo).child("Orders").child(orderId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            String timestamp = "" + System.currentTimeMillis();

                            //setup hashMap to store customers' rating at one place
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("orderId", "" + orderId);
                            hashMap.put("rating", "" + getRating);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("customerUid", "" + firebaseAuth.getUid());

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.child(orderTo).child("Ratings").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            roundedBottomSheetDialog.dismiss();
                                            //rating updated
                                            TastyToast.makeText(CustomerOrderDetailsActivity.this, "Thank you for your rating!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            roundedBottomSheetDialog.dismiss();
                            //if rating not updated
                            TastyToast.makeText(CustomerOrderDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });

        } else {
            progressDialog.dismiss();
            //if customer missed to rate
            TastyToast.makeText(CustomerOrderDetailsActivity.this, "You can't submit rating with 0.0", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
        }
    }

    private void cancelOrder() {
        //cancel order
        progressDialog.setMessage("Cancelling order");
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", "Cancelled");
        hashMap.put("orderCancelledDateTime", "" + timestamp);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(orderTo).child("Orders").child(orderId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        //if order cancelled
//                        TastyToast.makeText(CustomerOrderDetailsActivity.this, "Order #" + orderId + " has been cancelled!", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        prepareCancelByCustomerNotificationMessage(orderId, customerName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //if any error happened
                        TastyToast.makeText(CustomerOrderDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                    }
                });
    }

    private void prepareCancelByCustomerNotificationMessage(String orderId, String customerName) {
        //when customer cancel the order, notification will be send to the merchant

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "Order Cancelled #" + orderId;
        String NOTIFICATION_MESSAGE = "The order has been cancelled by " + customerName;
        String NOTIFICATION_TYPE = "CancelOrderByCustomer";

        //prepare json (what to send and where to send)
        JSONObject notificationObj = new JSONObject();
        JSONObject notificationBodyObj = new JSONObject();

        try {
            //what to send
            notificationBodyObj.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyObj.put("customerUid", firebaseAuth.getUid());
            notificationBodyObj.put("merchantUid", orderTo);
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
                TastyToast.makeText(CustomerOrderDetailsActivity.this, "Order #" + orderId + " has been cancelled!", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
                if (!IsConnectedToInternet.isConnectedToInternet(CustomerOrderDetailsActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadMerchantDetails();
                    loadOrderDetails();
                    loadOrderedItems();
                }
            }
        });
    }
}