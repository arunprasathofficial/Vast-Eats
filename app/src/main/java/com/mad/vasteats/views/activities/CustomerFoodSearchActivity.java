package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.models.Food;
import com.mad.vasteats.presenters.CustomerFoodAdapter;

import java.util.ArrayList;

public class CustomerFoodSearchActivity extends AppCompatActivity {

    private ImageView back_btn_iv;
    private EditText food_search_et;
    private RecyclerView foods_list_rv;
    private LinearLayout waiting_to_search_msg_lt, food_not_found_msg_lt;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private String merchantUid;

    private ArrayList<Food> foodArrayList;
    private CustomerFoodAdapter customerFoodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_food_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        back_btn_iv = findViewById(R.id.back_btn);
        food_search_et = findViewById(R.id.food_search);
        foods_list_rv = findViewById(R.id.foods_list_rv);
        waiting_to_search_msg_lt = findViewById(R.id.waiting_to_search_msg);
        food_not_found_msg_lt = findViewById(R.id.food_not_found_msg);

        merchantUid = getIntent().getStringExtra("merchantUid");

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
            loadMerchantFoods();
        } else {
            noInternetConnectionBottomSheet();
        }

        food_search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence != null && charSequence.length() > 0) {
                        customerFoodAdapter.getFilter().filter(charSequence);
                        if (customerFoodAdapter.getItemCount() == 0) {
                            waiting_to_search_msg_lt.setVisibility(View.GONE);
                            foods_list_rv.setVisibility(View.GONE);
                            food_not_found_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            waiting_to_search_msg_lt.setVisibility(View.GONE);
                            food_not_found_msg_lt.setVisibility(View.GONE);
                            foods_list_rv.setVisibility(View.VISIBLE);
                        }
                    } else {
                        food_not_found_msg_lt.setVisibility(View.GONE);
                        foods_list_rv.setVisibility(View.GONE);
                        waiting_to_search_msg_lt.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadMerchantFoods() {
        foodArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        foods_list_rv.setLayoutManager(linearLayoutManager);

        //get all foods of merchant
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(merchantUid).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        foodArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            Food food = dataSnapshot.getValue(Food.class);
                            foodArrayList.add(food);
                        }
                        //setup adapter
                        customerFoodAdapter = new CustomerFoodAdapter(CustomerFoodSearchActivity.this, foodArrayList);
                        //set adapter
                        foods_list_rv.setAdapter(customerFoodAdapter);

                        foods_list_rv.setVisibility(View.GONE);
                        waiting_to_search_msg_lt.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
                if (!IsConnectedToInternet.isConnectedToInternet(CustomerFoodSearchActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadMerchantFoods();
                }
            }
        });
    }
}