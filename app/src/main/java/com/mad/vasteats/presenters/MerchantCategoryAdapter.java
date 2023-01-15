package com.mad.vasteats.presenters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.models.Category;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.HashMap;

public class MerchantCategoryAdapter extends RecyclerView.Adapter<MerchantCategoryAdapter.HolderMerchantCategory> {

    private Context context;
    private ArrayList<Category> categoryArrayList;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    public MerchantCategoryAdapter(Context context, ArrayList<Category> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolderMerchantCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.merchant_category_view_layout, parent, false);
        return new HolderMerchantCategory(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMerchantCategory holder, int position) {
        //get data
        Category category = categoryArrayList.get(position);
        final String catId = category.getCategoryId();
        String catName = category.getCategoryName();
        String activeStatus = category.getActiveStatus();
        String uid = category.getUid();

        //set data
        holder.category_name_tv.setText(catName);

        if (activeStatus.equals("true")) {
            holder.active_status_tv.setText("Active");
            holder.active_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_green));
        } else {
            holder.active_status_tv.setText("Inactive");
            holder.active_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_red));
        }

        holder.update_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCategoryBottomSheet(catId, catName, activeStatus, uid);
            }
        });

        holder.remove_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryId = categoryArrayList.get(holder.getAdapterPosition()).getCategoryId();
                String categoryName = categoryArrayList.get(holder.getAdapterPosition()).getCategoryName();

                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Delete")
                        .setMessage("Are you sure, do you want to delete category '" + categoryName + "' ?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //deleting category using its id
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                databaseReference.child(uid).child("Categories").child(categoryId).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                TastyToast.makeText(view.getContext(), "" + categoryName + " Deleted!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                TastyToast.makeText(view.getContext(), "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //dismiss the alert dialog
                                dialogInterface.dismiss();
                            }
                        }).show();

                notifyDataSetChanged();
            }
        });
    }

    private void updateCategoryBottomSheet(String catId, String catName, String activeStatus, String uid) {
        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(context);

        //inflate view for bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.merchant_update_category_layout, null);

        //set view to bottom sheet
        roundedBottomSheetDialog.setContentView(view);
        roundedBottomSheetDialog.setCanceledOnTouchOutside(false);

        //init ui views
        ImageView close_btn_iv = view.findViewById(R.id.close_btn);
        EditText category_name_et = view.findViewById(R.id.category_name);
        SwitchCompat active_status_switch = view.findViewById(R.id.active_status_switch);
        Button update_category_btn = view.findViewById(R.id.update_category_btn);

        category_name_et.setText(catName);

        if (activeStatus.equals("true")) {
            active_status_switch.setChecked(true);
        } else {
            active_status_switch.setChecked(false);
        }

        roundedBottomSheetDialog.show();

        close_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roundedBottomSheetDialog.dismiss();
            }
        });

        update_category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData(category_name_et, active_status_switch, catId, uid);
            }
        });
    }

    private void inputData(EditText category_name_et, SwitchCompat active_status_switch, String catId, String uid) {
        String catName = category_name_et.getText().toString().trim();
        Boolean activeStatus = active_status_switch.isChecked();

        //validate data
        if (TextUtils.isEmpty(catName)) {
            TastyToast.makeText(context, "Category Name is Required", TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
            return;
        }

        updateCategory(catName, activeStatus, catId, uid);
    }

    private void updateCategory(String catName, Boolean activeStatus, String catId, String uid) {
        //setup data to update category
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("categoryName", "" + catName);
        hashMap.put("activeStatus", "" + activeStatus);

        //update product to db
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(uid).child("Categories").child(catId).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //db updated
                        TastyToast.makeText(context, "Category updated successfully!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                        roundedBottomSheetDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to updating db
                        TastyToast.makeText(context, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        roundedBottomSheetDialog.dismiss();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    class HolderMerchantCategory extends RecyclerView.ViewHolder {

        private TextView category_name_tv, active_status_tv;
        private ImageView remove_btn_iv, update_btn_iv;

        public HolderMerchantCategory(@NonNull View itemView) {
            super(itemView);

            category_name_tv = itemView.findViewById(R.id.category_name);
            active_status_tv = itemView.findViewById(R.id.active_status);
            remove_btn_iv = itemView.findViewById(R.id.remove_btn);
            update_btn_iv = itemView.findViewById(R.id.update_btn);
        }
    }
}
