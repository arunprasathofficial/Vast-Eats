package com.mad.vasteats.views.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.models.Food;
import com.mad.vasteats.presenters.MerchantFoodAdapter;
import com.mad.vasteats.views.activities.MerchantAddFoodActivity;

import java.util.ArrayList;

public class MerchantFoodsFragment extends Fragment {

    public MerchantFoodsFragment() {
        // Required empty public constructor
    }

    private ImageView foods_search_btn_iv, foods_filter_btn_iv;
    private TextView category_type_tv, add_more_foods_tv;
    private RecyclerView foods_rv;
    private LinearLayout add_new_food_msg_lt, no_foods_found_msg_lt;
    private Button add_new_food_btn;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private ArrayList<Food> foodArrayList;
    private ArrayList<String> categoryArrayList;
    private MerchantFoodAdapter merchantFoodAdapter;

    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_foods, container, false);

        foods_search_btn_iv =view.findViewById(R.id.foods_search_btn);
        foods_filter_btn_iv =view.findViewById(R.id.foods_filter_btn);
        category_type_tv =view.findViewById(R.id.category_type);
        add_more_foods_tv =view.findViewById(R.id.add_more_foods);
        foods_rv =view.findViewById(R.id.foods_rv);
        add_new_food_msg_lt =view.findViewById(R.id.add_new_food_msg);
        no_foods_found_msg_lt =view.findViewById(R.id.no_foods_found_msg);
        add_new_food_btn =view.findViewById(R.id.add_new_food_btn);

        firebaseAuth = FirebaseAuth.getInstance();

        if (IsConnectedToInternet.isConnectedToInternet(getContext())) {
            loadActiveCategories();
            loadAllFoods();
        } else {
            noInternetConnectionBottomSheet();
        }

        add_new_food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MerchantAddFoodActivity.class);
                getActivity().startActivity(intent);
            }
        });

        add_more_foods_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), MerchantAddFoodActivity.class);
                getActivity().startActivity(intent);
            }
        });

        foods_search_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MerchantFoodSearchFragment merchantFoodSearchFragment = new MerchantFoodSearchFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.merchant_frame_layout, merchantFoodSearchFragment);
                fragmentTransaction.commit();
            }
        });

        foods_filter_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select Food Category")
                        .setItems(categoryArrayList.toArray(new String[0]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //get selected category
                                String filteredCategory = categoryArrayList.get(i);

                                if (!IsConnectedToInternet.isConnectedToInternet(getContext())) {
                                    noInternetConnectionBottomSheet();
                                    return;
                                }

                                if (filteredCategory.equals("All")) {
                                    loadAllFoods();
                                } else {
                                    //load filtered foods
                                    loadFilteredFoods(filteredCategory);
                                }
                            }
                        }).show();
            }
        });

        return view;
    }

    private void loadActiveCategories() {
        categoryArrayList = new ArrayList<>();

        //get all categories
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Categories")
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

    private void loadAllFoods() {
        foodArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        foods_rv.setLayoutManager(linearLayoutManager);

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
                        foods_rv.setAdapter(merchantFoodAdapter);

                        if (merchantFoodAdapter.getItemCount() == 0) {
                            foods_rv.setVisibility(View.GONE);
                            no_foods_found_msg_lt.setVisibility(View.GONE);
                            add_new_food_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            add_new_food_btn.setVisibility(View.GONE);
                            no_foods_found_msg_lt.setVisibility(View.GONE);
                            foods_rv.setVisibility(View.VISIBLE);
                            add_more_foods_tv.setVisibility(View.VISIBLE);
                        }
                        category_type_tv.setText("Showing All (" + foodArrayList.size() + ")");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredFoods(final String filteredCategory) {
        foodArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        foods_rv.setLayoutManager(linearLayoutManager);

        //get filtered foods
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Foods")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        foodArrayList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            String foodCategory = "" + dataSnapshot.child("foodCategory").getValue();

                            if (filteredCategory.equals(foodCategory)) {
                                Food food = dataSnapshot.getValue(Food.class);
                                foodArrayList.add(food);
                            }
                        }
                        //setup adapter
                        merchantFoodAdapter = new MerchantFoodAdapter(getContext(), foodArrayList);
                        //set adapter
                        foods_rv.setAdapter(merchantFoodAdapter);

                        if (merchantFoodAdapter.getItemCount() == 0) {
                            foods_rv.setVisibility(View.GONE);
                            add_new_food_msg_lt.setVisibility(View.GONE);
                            no_foods_found_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            no_foods_found_msg_lt.setVisibility(View.GONE);
                            foods_rv.setVisibility(View.VISIBLE);
                        }
                        category_type_tv.setText("Showing " + filteredCategory + " (" + foodArrayList.size() + ")");
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
                    loadActiveCategories();
                    loadAllFoods();
                }
            }
        });
    }
}