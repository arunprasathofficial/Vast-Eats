package com.mad.vasteats.views.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.mad.vasteats.models.Category;
import com.mad.vasteats.presenters.MerchantCategoryAdapter;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.HashMap;

public class MerchantCategoriesFragment extends Fragment {

    public MerchantCategoriesFragment() {
        // Required empty public constructor
    }

    private TextView total_categories_tv, add_category_tv;
    private RecyclerView categories_rv;
    private LinearLayout add_new_category_msg_lt;
    private Button add_new_category_btn;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private ArrayList<Category> categoryArrayList;
    private MerchantCategoryAdapter merchantCategoryAdapter;

    FirebaseAuth firebaseAuth;

    private String categoryName;
    private boolean activeStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_merchant_categories, container, false);

        total_categories_tv = view.findViewById(R.id.total_categories);
        add_category_tv = view.findViewById(R.id.add_category);
        categories_rv = view.findViewById(R.id.categories_rv);
        add_new_category_msg_lt = view.findViewById(R.id.add_new_category_msg);
        add_new_category_btn = view.findViewById(R.id.add_new_category_btn);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Adding Category");

        if (IsConnectedToInternet.isConnectedToInternet(getContext())) {
            loadAllCategories();
        } else {
            noInternetConnectionBottomSheet();
        }

        add_new_category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategoryBottomSheet();
            }
        });

        add_category_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCategoryBottomSheet();
            }
        });

        return view;
    }

    private void loadAllCategories() {
        categoryArrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        categories_rv.setLayoutManager(linearLayoutManager);

        //get all categories
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Categories").orderByChild("categoryName")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryArrayList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            categoryArrayList.add(category);
                        }
                        //setup adapter
                        merchantCategoryAdapter = new MerchantCategoryAdapter(getContext(), categoryArrayList);
                        //set adapter
                        categories_rv.setAdapter(merchantCategoryAdapter);

                        if (merchantCategoryAdapter.getItemCount() == 0) {
                            categories_rv.setVisibility(View.GONE);
                            add_category_tv.setVisibility(View.GONE);
                            add_new_category_msg_lt.setVisibility(View.VISIBLE);
                        } else {
                            add_new_category_msg_lt.setVisibility(View.GONE);
                            categories_rv.setVisibility(View.VISIBLE);
                            add_category_tv.setVisibility(View.VISIBLE);
                        }
                        total_categories_tv.setText("Showing All (" + categoryArrayList.size() + ")");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void addCategoryBottomSheet() {
        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(getContext());

        //inflate view for bottom sheet
        View view = LayoutInflater.from(getContext()).inflate(R.layout.merchant_add_category_layout, null);

        //set view to bottom sheet
        roundedBottomSheetDialog.setContentView(view);
        roundedBottomSheetDialog.setCanceledOnTouchOutside(false);

        //init ui views
        ImageView close_btn_iv = view.findViewById(R.id.close_btn);
        EditText category_name_et = view.findViewById(R.id.category_name);
        SwitchCompat active_status_switch = view.findViewById(R.id.active_status_switch);
        Button add_category_btn = view.findViewById(R.id.add_category_btn);

        roundedBottomSheetDialog.show();

        close_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roundedBottomSheetDialog.dismiss();
            }
        });

        add_category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData(category_name_et, active_status_switch);
            }
        });
    }

    private void inputData(EditText category_name_et, SwitchCompat active_status_switch) {
        categoryName = category_name_et.getText().toString().trim();
        activeStatus = active_status_switch.isChecked();

        //validate data

        if (TextUtils.isEmpty(categoryName)) {
            TastyToast.makeText(getContext(), "Category Name is Required", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
            return;
        }

        progressDialog.setMessage("Please wait");
        progressDialog.show();
        addCategory();
    }

    private void addCategory() {
        final String timestamp = "" + System.currentTimeMillis();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("categoryId", "" + timestamp);
            hashMap.put("categoryName", "" + categoryName);
            hashMap.put("activeStatus", "" + activeStatus);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + firebaseAuth.getUid());

        //get categories
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dbCatName = null;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            dbCatName = "" + dataSnapshot.child("categoryName").getValue();
                        }

                        if (categoryName.equals(dbCatName)) {
                            progressDialog.dismiss();
                            TastyToast.makeText(getContext(), "" + categoryName + " is already exists.", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();

                        } else {
                            //add category to db
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.child(firebaseAuth.getUid()).child("Categories").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            //db updated
                                            TastyToast.makeText(getContext(), "Category added successfully!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                            roundedBottomSheetDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            //failed to updating db
                                            TastyToast.makeText(getContext(), "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                                            roundedBottomSheetDialog.dismiss();
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
                    loadAllCategories();
                }
            }
        });
    }
}