package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class MerchantFoodDetailsActivity extends AppCompatActivity {

    private ImageView back_btn_iv, update_btn_iv, delete_btn_iv, food_image_iv;
    private TextView food_name_tv, food_category_tv, discount_title_tv, selling_price_tv, original_price_tv,
            available_status_tv, food_description_tv;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private FirebaseAuth firebaseAuth;

    private String foodId, foodName, foodCategory, foodImage, foodDescription, foodDiscountTitle, foodDiscountedPrice,
            foodOriginalPrice, foodAvailable, discountAvailable, timestamp, uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_food_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get id of the food from intent
        foodId = getIntent().getStringExtra("foodId");

        back_btn_iv = findViewById(R.id.back_btn);
        update_btn_iv = findViewById(R.id.update_btn);
        delete_btn_iv = findViewById(R.id.delete_btn);
        food_image_iv = findViewById(R.id.food_image);
        food_name_tv = findViewById(R.id.food_name);
        food_category_tv = findViewById(R.id.food_category);
        discount_title_tv = findViewById(R.id.discount_title);
        selling_price_tv = findViewById(R.id.selling_price);
        original_price_tv = findViewById(R.id.original_price);
        available_status_tv = findViewById(R.id.available_status);
        food_description_tv = findViewById(R.id.food_description);

        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Deleting Food");

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
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

        update_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open update food activity
                Intent intent = new Intent(MerchantFoodDetailsActivity.this, MerchantUpdateFoodActivity.class);
                intent.putExtra("foodId", foodId);
                startActivity(intent);
            }
        });

        delete_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MerchantFoodDetailsActivity.this);
                builder.setTitle("Delete")
                        .setMessage("Are you sure, do you want to delete the food '" + foodName + "' ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (IsConnectedToInternet.isConnectedToInternet(MerchantFoodDetailsActivity.this)) {
                                    deleteFood(foodId);
                                } else {
                                    noInternetConnectionBottomSheet();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //dismiss the alert dialog
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });
    }

    private void loadFoodDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Foods").child(foodId)
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
                        original_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodOriginalPriceAsCurrency)));
                        selling_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodDiscountedPriceAsCurrency)));
                        discount_title_tv.setText(foodDiscountTitle);

                        if (foodDescription.isEmpty()) {
                            food_description_tv.setText("No Description!");
                        } else {
                            food_description_tv.setText(foodDescription);
                        }

                        if (foodAvailable.equals("true")) {
                            available_status_tv.setText("Available");
                            available_status_tv.setTextColor(ContextCompat.getColor(MerchantFoodDetailsActivity.this, R.color.google_green));
                        } else {
                            available_status_tv.setText("Unavailable");
                            available_status_tv.setTextColor(ContextCompat.getColor(MerchantFoodDetailsActivity.this, R.color.google_red));
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteFood(String foodId) {
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        //deleting food using its id
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Foods").child(foodId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        TastyToast.makeText(MerchantFoodDetailsActivity.this, "Food Deleted!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        TastyToast.makeText(MerchantFoodDetailsActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                    }
                });
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
                if (!IsConnectedToInternet.isConnectedToInternet(MerchantFoodDetailsActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadFoodDetails();
                }
            }
        });
    }
}