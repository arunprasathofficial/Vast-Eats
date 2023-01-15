package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.ArrayList;
import java.util.HashMap;

public class MerchantAddFoodActivity extends AppCompatActivity {

    private ImageView back_btn_iv, food_image_iv;
    private EditText food_name_et, food_description_et, original_price_et, discount_title_et,
            discounted_price_et;
    private TextView food_category_tv;
    private SwitchCompat discount_switch, availability_switch;
    private Button add_food_btn;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] cameraPermissions;
    private String[] storagePermissions;

    private Uri imageUri;

    private FirebaseAuth firebaseAuth;

    private ArrayList<String> categoryArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_add_food);

        back_btn_iv = findViewById(R.id.back_btn);
        food_image_iv = findViewById(R.id.food_image);
        food_name_et = findViewById(R.id.food_name);
        food_description_et = findViewById(R.id.food_description);
        original_price_et = findViewById(R.id.original_price);
        discount_title_et = findViewById(R.id.discount_title);
        discounted_price_et = findViewById(R.id.discounted_price);
        food_category_tv = findViewById(R.id.food_category);
        discount_switch = findViewById(R.id.discount_switch);
        availability_switch = findViewById(R.id.availability_switch);
        add_food_btn = findViewById(R.id.add_food_btn);

        discounted_price_et.setVisibility(View.GONE);
        discount_title_et.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Adding Food");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goes back to previous activity or fragment
                onBackPressed();
            }
        });

        food_image_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick image
                showImagePickDialog();
            }
        });

        food_category_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //select food category
                categoryDialog();
            }
        });

        discount_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    discounted_price_et.setVisibility(View.VISIBLE);
                    discount_title_et.setVisibility(View.VISIBLE);
                } else {
                    discounted_price_et.setVisibility(View.GONE);
                    discount_title_et.setVisibility(View.GONE);
                }
            }
        });

        add_food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToInternet.isConnectedToInternet(MerchantAddFoodActivity.this)) {
                    //add food
                    inputData();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });
    }

    private void showImagePickDialog() {
        //options to display in dialog
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle clicks
                        if (i == 0) {
                            //camera clicked
                            if (ContextCompat.checkSelfPermission(MerchantAddFoodActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                                    ContextCompat.checkSelfPermission(MerchantAddFoodActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                //if camera permission allowed, can pick image from camera
                                pickFromCamera();
                            } else {
                                //if camera permission not allowed, request permission
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (ContextCompat.checkSelfPermission(MerchantAddFoodActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                //if camera permission allowed, can pick image from camera
                                pickFromGallery();
                            } else {
                                //if camera permission not allowed, request permission
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
    }

    private void pickFromGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(gallery, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image
                imageUri = data.getData();
                //set to imageview
                food_image_iv.setImageURI(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                food_image_iv.setImageURI(imageUri);
            }
        }
    }

    private void categoryDialog() {
        categoryArrayList = new ArrayList<>();

        //get all categories
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Categories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryArrayList.clear();
                        String categoryName = null;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String activeStatus =  "" + dataSnapshot.child("activeStatus").getValue();
                            categoryName = "" + dataSnapshot.child("categoryName").getValue();

                            if (activeStatus.equals("true")) {
                                categoryArrayList.add(categoryName);
                            }
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(MerchantAddFoodActivity.this);
                        builder.setTitle("Select Food Category").setItems(categoryArrayList.toArray(new String[0]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selectedCategory = categoryArrayList.get(i);
                                food_category_tv.setText(selectedCategory);
                            }
                        }).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    String food_name, food_category, food_description, original_price, discounted_price, discount_title;
    boolean discount_available, food_available;

    private void inputData() {
        food_name = food_name_et.getText().toString().trim();
        food_category = food_category_tv.getText().toString().trim();
        food_description = food_description_et.getText().toString().trim();
        original_price = original_price_et.getText().toString().trim();
        discounted_price = discounted_price_et.getText().toString().trim();
        discount_title = discount_title_et.getText().toString().trim();
        discount_available = discount_switch.isChecked();
        food_available = availability_switch.isChecked();

        //validate data
        if (imageUri == null) {
            TastyToast.makeText(this, "Food Image is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (TextUtils.isEmpty(food_name)) {
            TastyToast.makeText(this, "Food Name is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (TextUtils.isEmpty(food_category)) {
            TastyToast.makeText(this, "Food Category is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (TextUtils.isEmpty(food_description)) {
            TastyToast.makeText(this, "Food Description is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (TextUtils.isEmpty(original_price)) {
            TastyToast.makeText(this, "Food Price is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (discount_available) {
            if (TextUtils.isEmpty(discount_title)) {
                TastyToast.makeText(this, "Discount Title is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
                return;
            }

            if (TextUtils.isEmpty(discounted_price)) {
                TastyToast.makeText(this, "Discounted Price is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
                return;
            }

        } else {
            discounted_price = "0.00";
            discount_title = "";
        }

        addFood();
    }

    private void addFood() {
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        final String timestamp = "" + System.currentTimeMillis();

        //update info with image
        //name and path of image
        String filePathAndName = "foods_images/" + firebaseAuth.getUid() + "/" + timestamp;

        //upload image
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //get url of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadImageUri = uriTask.getResult();

                        if (uriTask.isSuccessful()) {
                            //setup data to add food
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("foodId", "" + timestamp);
                            hashMap.put("foodImage", "" + downloadImageUri); ////url of uploaded image
                            hashMap.put("foodName", "" + food_name);
                            hashMap.put("foodCategory", "" + food_category);
                            hashMap.put("foodDescription", "" + food_description);
                            hashMap.put("foodOriginalPrice", "" + original_price);
                            hashMap.put("discountAvailable", "" + discount_available);
                            hashMap.put("foodDiscountTitle", "" + discount_title);
                            hashMap.put("foodDiscountedPrice", "" + discounted_price);
                            hashMap.put("foodAvailable", "" + food_available);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("uid", "" + firebaseAuth.getUid());

                            //add food to db
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                            databaseReference.child(firebaseAuth.getUid()).child("Foods").child(timestamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            //db updated
                                            TastyToast.makeText(MerchantAddFoodActivity.this, "Food added successfully!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                            onBackPressed();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            //failed to updating db
                                            TastyToast.makeText(MerchantAddFoodActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        TastyToast.makeText(MerchantAddFoodActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
                if (!IsConnectedToInternet.isConnectedToInternet(MerchantAddFoodActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    inputData();
                }
            }
        });
    }
}