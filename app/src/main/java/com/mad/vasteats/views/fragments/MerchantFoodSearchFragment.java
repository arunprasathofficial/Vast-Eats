package com.mad.vasteats.views.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.models.Food;
import com.mad.vasteats.presenters.MerchantFoodAdapter;

import java.util.ArrayList;

public class MerchantFoodSearchFragment extends Fragment {

    public MerchantFoodSearchFragment() {
        // Required empty public constructor
    }

    private ImageView back_btn_iv;
    private EditText food_search_et;
    private RecyclerView foods_list_rv;
    private LinearLayout waiting_to_search_msg_lt, food_not_found_msg_lt;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private ArrayList<Food> foodArrayList;
    private MerchantFoodAdapter merchantFoodAdapter;

    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_food_search, container, false);

        back_btn_iv = view.findViewById(R.id.back_btn);
        food_search_et = view.findViewById(R.id.food_search);
        foods_list_rv = view.findViewById(R.id.foods_list_rv);
        waiting_to_search_msg_lt = view.findViewById(R.id.waiting_to_search_msg);
        food_not_found_msg_lt = view.findViewById(R.id.food_not_found_msg);

        firebaseAuth = FirebaseAuth.getInstance();

        if (IsConnectedToInternet.isConnectedToInternet(getContext())) {
            loadFoods();
        } else {
            noInternetConnectionBottomSheet();
        }

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MerchantFoodsFragment merchantFoodsFragment = new MerchantFoodsFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.merchant_frame_layout, merchantFoodsFragment);
                fragmentTransaction.commit();
            }
        });


        food_search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence != null && charSequence.length() > 0) {
                        merchantFoodAdapter.getFilter().filter(charSequence);
                        if (merchantFoodAdapter.getItemCount() == 0) {
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

        return view;
    }

    private void loadFoods() {
        foodArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        foods_list_rv.setLayoutManager(linearLayoutManager);

        //get all foods
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        foodArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            Food food = dataSnapshot.getValue(Food.class);
                            foodArrayList.add(food);
                        }
                        //setup adapter
                        merchantFoodAdapter = new MerchantFoodAdapter(getContext(), foodArrayList);
                        //set adapter
                        foods_list_rv.setAdapter(merchantFoodAdapter);


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
                    loadFoods();
                }
            }
        });
    }
}