package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.helpers.easyDB;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class CustomerFoodDetailsActivity extends AppCompatActivity {

    private ImageView back_btn_iv, food_image_iv;
    private Button add_to_cart_btn;
    private TextView unavailable_msg_tv, food_name_tv, food_category_tv, discount_title_tv, selling_price_tv,
            original_price_tv, available_status_tv, food_description_tv;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private String merchantUid,foodId,foodName, foodDescription, foodDiscountTitle, foodImage, foodCategory,
            foodDiscountedPrice, discountAvailable, foodOriginalPrice, foodAvailable, timestamp, uid, merchantOpen, online;

    private int quantity = 1;
    private Double rate = 0.00;
    private Double finalTotal = 0.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_food_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        back_btn_iv = findViewById(R.id.back_btn);
        food_image_iv = findViewById(R.id.food_image);
        add_to_cart_btn = findViewById(R.id.add_to_cart_btn);
        unavailable_msg_tv = findViewById(R.id.unavailable_msg);
        food_name_tv = findViewById(R.id.food_name);
        food_category_tv = findViewById(R.id.food_category);
        discount_title_tv = findViewById(R.id.discount_title);
        selling_price_tv = findViewById(R.id.selling_price);
        original_price_tv = findViewById(R.id.original_price);
        available_status_tv = findViewById(R.id.available_status);
        food_description_tv = findViewById(R.id.food_description);

        //get id of merchant
        SharedPreferences merchantSharedPreferences = this.getSharedPreferences("merchantUid_Pref", Context.MODE_PRIVATE);
        merchantUid = merchantSharedPreferences.getString("merchantUid", "");

        //get id of the food
        foodId = getIntent().getStringExtra("foodId");

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
            loadMerchantDetails();
            loadFoodDetails();
        } else {
            noInternetConnectionBottomSheet();
        }

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goes back to previous activity or fragment
                onBackPressed();
            }
        });

        add_to_cart_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show qty dialog
                addToCartBottomSheet();
            }
        });
    }

    private void loadMerchantDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                merchantOpen = "" + snapshot.child("merchantOpen").getValue();
                online = "" + snapshot.child("online").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadFoodDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).child("Foods").child(foodId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");

                        //get data
                        foodName = "" + snapshot.child("foodName").getValue();
                        foodDescription = "" + snapshot.child("foodDescription").getValue();
                        foodDiscountTitle = "" + snapshot.child("foodDiscountTitle").getValue();
                        foodImage = "" + snapshot.child("foodImage").getValue();
                        foodCategory = "" + snapshot.child("foodCategory").getValue();
                        foodDiscountedPrice = "" + snapshot.child("foodDiscountedPrice").getValue();
                        discountAvailable = "" + snapshot.child("discountAvailable").getValue();
                        foodOriginalPrice = "" + snapshot.child("foodOriginalPrice").getValue();
                        foodAvailable = "" + snapshot.child("foodAvailable").getValue();
                        timestamp = "" + snapshot.child("timestamp").getValue();
                        uid = "" + snapshot.child("uid").getValue();

                        Double foodOriginalPriceAsCurrency;
                        Double foodDiscountedPriceAsCurrency;

                        if (foodOriginalPrice.equals("null") || foodDiscountedPrice.equals("null")) {
                            foodOriginalPrice = "0.00";
                            foodDiscountedPrice = "0.00";
                        }
                        foodOriginalPriceAsCurrency = Double.parseDouble(foodOriginalPrice);
                        foodDiscountedPriceAsCurrency = Double.parseDouble(foodDiscountedPrice);

                        //set data
                        food_name_tv.setText(foodName);
                        food_category_tv.setText(foodCategory);
                        food_description_tv.setText(foodDescription);
                        original_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodOriginalPriceAsCurrency)));
                        selling_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodDiscountedPriceAsCurrency)));
                        discount_title_tv.setText(foodDiscountTitle);

                        if (foodAvailable.equals("true")) {
                            available_status_tv.setText("Available");
                            available_status_tv.setTextColor(ContextCompat.getColor(CustomerFoodDetailsActivity.this, R.color.google_green));
                        } else {
                            available_status_tv.setText("Unavailable");
                            available_status_tv.setTextColor(ContextCompat.getColor(CustomerFoodDetailsActivity.this, R.color.google_red));
                        }

                        if (discountAvailable.equals("true")) {
                            discount_title_tv.setVisibility(View.VISIBLE);
                            original_price_tv.setVisibility(View.VISIBLE);
                        } else {
                            discount_title_tv.setVisibility(View.GONE);
                            original_price_tv.setVisibility(View.GONE);
                            selling_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodOriginalPriceAsCurrency)));
                        }

                        try {
                            Picasso.get().load(foodImage).placeholder(R.drawable.placeholder_grey).into(food_image_iv);
                        } catch (Exception e) {
                            food_image_iv.setImageResource(R.drawable.placeholder_grey);
                        }

                        if (merchantOpen.equals("false") || online.equals("false")) {
                            add_to_cart_btn.setVisibility(View.GONE);
                            unavailable_msg_tv.setVisibility(View.VISIBLE);
                        } else {
                            if (foodAvailable.equals("true")) {
                                add_to_cart_btn.setVisibility(View.VISIBLE);
                                unavailable_msg_tv.setVisibility(View.GONE);
                            } else {
                                add_to_cart_btn.setVisibility(View.GONE);
                                unavailable_msg_tv.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void addToCartBottomSheet() {
        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(this);

        //inflate view for bottom sheet
        View view = LayoutInflater.from(this).inflate(R.layout.customer_add_to_cart_dialog_layout, null);

        //set view to bottom sheet
        roundedBottomSheetDialog.setContentView(view);
        roundedBottomSheetDialog.setCanceledOnTouchOutside(false);

        ImageView close_btn_iv, minus_btn_iv, plus_btn_iv;
        final TextView cart_qty_tv;
        Button add_to_cart_btn;

        close_btn_iv = view.findViewById(R.id.close_btn);
        minus_btn_iv = view.findViewById(R.id.minus_btn);
        plus_btn_iv = view.findViewById(R.id.plus_btn);
        cart_qty_tv = view.findViewById(R.id.cart_qty);
        add_to_cart_btn = view.findViewById(R.id.add_to_cart_btn);

        cart_qty_tv.setText("" + quantity);

        if (discountAvailable.equals("true")) {
            rate = Double.parseDouble(foodDiscountedPrice);
        } else {
            rate = Double.parseDouble(foodOriginalPrice);
        }

        finalTotal = rate;

        roundedBottomSheetDialog.show();

        close_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close bottom sheet
                roundedBottomSheetDialog.dismiss();
            }
        });

        plus_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalTotal = finalTotal + rate;
                quantity++;
                cart_qty_tv.setText("" + quantity);
            }
        });

        minus_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity > 1) {
                    finalTotal = finalTotal - rate;
                    quantity--;
                    cart_qty_tv.setText("" + quantity);
                }
            }
        });

        add_to_cart_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFoodToCart();
            }
        });
    }

    private void addFoodToCart() {
        String cartFoodQty = String.valueOf(quantity);
        String cartFoodRate = String.valueOf(rate);
        String cartFoodTotal = String.valueOf(finalTotal);

        //add food to cart
        Cursor res = easyDB.easyDB.searchInColumn("foodId", foodId, -1);
        if (res == null) {
            Boolean b = easyDB.easyDB
                    .addData("foodId", foodId)
                    .addData("foodName", foodName)
                    .addData("foodImage", foodImage)
                    .addData("cartFoodQty", cartFoodQty)
                    .addData("cartFoodRate", cartFoodRate)
                    .addData("cartFoodTotal", cartFoodTotal)
                    .doneDataAdding();

            //update cart count badge
            CustomerMerchantDetailsActivity.cartCount();

            roundedBottomSheetDialog.dismiss();
            TastyToast.makeText(this, foodName + " added to your cart successfully!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
            onBackPressed();
        } else {
            roundedBottomSheetDialog.dismiss();
            TastyToast.makeText(this, foodName + " is already in your cart!", TastyToast.LENGTH_SHORT, TastyToast.WARNING);
        }
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
                if (!IsConnectedToInternet.isConnectedToInternet(CustomerFoodDetailsActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadMerchantDetails();
                    loadFoodDetails();
                }
            }
        });
    }
}