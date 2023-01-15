package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.helpers.easyDB;
import com.mad.vasteats.models.Food;
import com.mad.vasteats.presenters.CustomerFoodAdapter;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomerMerchantDetailsActivity extends AppCompatActivity {

    private ImageView back_btn_iv, cart_btn_iv, search_foods_btn_iv, filter_btn_iv, merchant_cover_image_iv,
            phone_btn_iv, map_btn_iv;
    private TextView category_type_tv, merchant_name_tv, merchant_city_name_tv, merchant_ratings_tv,
            delivery_fee_tv, merchant_address_tv, unavailable_msg_tv;
    private static TextView cart_count_tv;
    private RecyclerView foods_list_rv;
    private LinearLayout no_foods_found_msg_lt;
    private CircularImageView merchant_image_iv;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private int coverImages;
    private String merchantUid, merchantName, deliveryFee, merchantPhoneNo, customerAddress, merchantAddress;

    private FirebaseAuth firebaseAuth;

    private DecimalFormat decimalFormat;
    private Double deliveryFeePriceAsCurrency;

    private ArrayList<String> categoryArrayList;
    private ArrayList<Food> foodArrayList;
    private CustomerFoodAdapter customerFoodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_merchant_details);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        back_btn_iv = findViewById(R.id.back_btn);
        cart_btn_iv = findViewById(R.id.cart_btn);
        search_foods_btn_iv = findViewById(R.id.search_foods_btn);
        filter_btn_iv = findViewById(R.id.filter_btn);
        merchant_cover_image_iv = findViewById(R.id.merchant_cover_image);
        phone_btn_iv = findViewById(R.id.phone_btn);
        map_btn_iv = findViewById(R.id.map_btn);
        cart_count_tv = findViewById(R.id.cart_count);
        category_type_tv = findViewById(R.id.category_type);
        merchant_name_tv = findViewById(R.id.merchant_name);
        merchant_city_name_tv = findViewById(R.id.merchant_city_name);
        merchant_ratings_tv = findViewById(R.id.merchant_ratings);
        delivery_fee_tv = findViewById(R.id.delivery_fee);
        merchant_address_tv = findViewById(R.id.merchant_address);
        unavailable_msg_tv = findViewById(R.id.unavailable_msg);
        foods_list_rv = findViewById(R.id.foods_list_rv);
        no_foods_found_msg_lt = findViewById(R.id.no_foods_found_msg);
        merchant_image_iv = findViewById(R.id.merchant_image);

        //get random cover images and uid of merchant intent
        coverImages = getIntent().getIntExtra("coverImages", 0);
        merchantUid = getIntent().getStringExtra("merchantUid");

        firebaseAuth = FirebaseAuth.getInstance();

        easyDB.init(this);

        SharedPreferences merchantSharedPreferences = this.getSharedPreferences("merchantUid_Pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor merchantEditor = merchantSharedPreferences.edit();
        merchantEditor.putString("merchantUid",merchantUid);
        merchantEditor.apply();

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
            getCustomerAddress();
            loadMerchantDetails();
            loadActiveCategories();
            loadMerchantFoods();
            deleteCartData();
            cartCount();
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

        cart_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goes to my cart activity
                Intent intent = new Intent(CustomerMerchantDetailsActivity.this, CustomerCartActivity.class);
                startActivity(intent);
            }
        });

        phone_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhone();
            }
        });

        map_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        filter_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomerMerchantDetailsActivity.this);
                builder.setTitle("Filter Food Category")
                        .setItems(categoryArrayList.toArray(new String[0]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //get selected category
                                String filteredCategory = categoryArrayList.get(i);

                                if (!IsConnectedToInternet.isConnectedToInternet(CustomerMerchantDetailsActivity.this)) {
                                    noInternetConnectionBottomSheet();
                                    return;
                                }

                                if (filteredCategory.equals("All")) {
                                    loadMerchantFoods();
                                } else {
                                    //load filtered foods
                                    loadFilteredFoods(filteredCategory);
                                }
                            }
                        }).show();
            }
        });

        search_foods_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerMerchantDetailsActivity.this, CustomerFoodSearchActivity.class);
                intent.putExtra("merchantUid", merchantUid);
                startActivity(intent);
            }
        });
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(merchantPhoneNo))));
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String addressAPI = "https://maps.google.com/maps?saddr=" + customerAddress + "&daddr=" + merchantAddress;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(addressAPI));
        startActivity(intent);
    }

    private void getCustomerAddress() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //get user data
                            customerAddress = "" + dataSnapshot.child("completeAddress").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMerchantDetails() {
        merchant_cover_image_iv.setImageResource(coverImages);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get merchant data
                merchantName = "" + snapshot.child("merchantName").getValue();
                merchantPhoneNo = "" + snapshot.child("phoneNo").getValue();
                merchantAddress = "" + snapshot.child("merchantAddress").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String merchantCity = "" + snapshot.child("merchantCity").getValue();
                String merchantImage = "" + snapshot.child("merchantImage").getValue();
                String merchantOpen = "" + snapshot.child("merchantOpen").getValue();
                String online = "" + snapshot.child("online").getValue();

                decimalFormat = new DecimalFormat("#,###.00");
                deliveryFeePriceAsCurrency = Double.parseDouble(deliveryFee);

                //set data
                try {
                    Picasso.get().load(merchantImage).placeholder(R.drawable.placeholder_color).into(merchant_image_iv);
                } catch (Exception e) {
                    merchant_image_iv.setImageResource(R.drawable.placeholder_color);
                }

                merchant_name_tv.setText(merchantName);
                merchant_city_name_tv.setText(merchantCity);
                delivery_fee_tv.setText("LKR " + String.valueOf(decimalFormat.format(deliveryFeePriceAsCurrency)) + " Fee");
                merchant_address_tv.setText(merchantAddress);

                if (merchantOpen.equals("false") || online.equals("false")) {
                    unavailable_msg_tv.setVisibility(View.VISIBLE);
                } else {
                    unavailable_msg_tv.setVisibility(View.GONE);
                }

                loadMerchantRating();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMerchantRating() {
    }

    private void loadActiveCategories() {
        categoryArrayList = new ArrayList<>();

        //get all categories
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).child("Categories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryArrayList.clear();
                        String categoryName = null;
                        categoryArrayList.add("All");
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String activeStatus = "" + dataSnapshot.child("activeStatus").getValue();
                            categoryName = "" + dataSnapshot.child("categoryName").getValue();

                            if (activeStatus.equals("true")) {
                                categoryArrayList.add(categoryName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMerchantFoods() {
        //init list
        foodArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        foods_list_rv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding foods
                        foodArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Food food = dataSnapshot.getValue(Food.class);
                            foodArrayList.add(food);
                        }
                        //setup adapter
                        customerFoodAdapter = new CustomerFoodAdapter(CustomerMerchantDetailsActivity.this, foodArrayList);
                        //set adapter to Recyclerview
                        foods_list_rv.setAdapter(customerFoodAdapter);

                        if (customerFoodAdapter.getItemCount() == 0) {
                            foods_list_rv.setVisibility(View.GONE);
                            no_foods_found_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            no_foods_found_msg_lt.setVisibility(View.GONE);
                            foods_list_rv.setVisibility(View.VISIBLE);
                        }
                        category_type_tv.setText("Showing All (" + foodArrayList.size() + ")");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredFoods(final String filteredCategory) {
        //init list
        foodArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        foods_list_rv.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding foods
                        foodArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String foodCategory = "" + dataSnapshot.child("foodCategory").getValue();

                            if (foodCategory.equals(filteredCategory)) {
                                Food food = dataSnapshot.getValue(Food.class);
                                foodArrayList.add(food);
                            }
                        }
                        //setup adapter
                        customerFoodAdapter = new CustomerFoodAdapter(CustomerMerchantDetailsActivity.this, foodArrayList);
                        //set adapter to Recyclerview
                        foods_list_rv.setAdapter(customerFoodAdapter);

                        if (customerFoodAdapter.getItemCount() == 0) {
                            foods_list_rv.setVisibility(View.GONE);
                            no_foods_found_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            no_foods_found_msg_lt.setVisibility(View.GONE);
                            foods_list_rv.setVisibility(View.VISIBLE);
                        }
                        category_type_tv.setText("Showing " + filteredCategory + " (" + foodArrayList.size() + ")");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void cartCount() {
        int count = easyDB.easyDB.getAllData().getCount();
        if (count <= 0) {
            cart_count_tv.setVisibility(View.GONE);
        } else {
            cart_count_tv.setVisibility(View.VISIBLE);
            cart_count_tv.setText("" + count);
        }
    }

    private void deleteCartData() {
        easyDB.easyDB.deleteAllDataFromTable();
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
                if (!IsConnectedToInternet.isConnectedToInternet(CustomerMerchantDetailsActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    getCustomerAddress();
                    loadMerchantDetails();
                    loadMerchantFoods();
                    deleteCartData();
                    cartCount();
                }
            }
        });
    }
}