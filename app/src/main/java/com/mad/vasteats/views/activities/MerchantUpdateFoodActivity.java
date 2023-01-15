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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
import com.mad.vasteats.models.Category;
import com.mad.vasteats.models.Food;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MerchantUpdateFoodActivity extends AppCompatActivity {

    private ImageView back_btn_iv, food_image_iv;
    private EditText food_name_et, food_description_et, original_price_et, discount_title_et,
            discounted_price_et;
    private TextView food_category_tv, change_image_tv;
    private SwitchCompat discount_switch, availability_switch;
    private Button update_food_btn;
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

    private String foodId;

    private ArrayList<String> categoryArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_update_food);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //get id of the food from intent
        foodId = getIntent().getStringExtra("foodId");

        back_btn_iv = findViewById(R.id.back_btn);
        food_image_iv = findViewById(R.id.food_image);
        change_image_tv = findViewById(R.id.change_image);
        food_name_et = findViewById(R.id.food_name);
        food_description_et = findViewById(R.id.food_description);
        original_price_et = findViewById(R.id.original_price);
        discount_title_et = findViewById(R.id.discount_title);
        discounted_price_et = findViewById(R.id.discounted_price);
        food_category_tv = findViewById(R.id.food_category);
        discount_switch = findViewById(R.id.discount_switch);
        availability_switch = findViewById(R.id.availability_switch);
        update_food_btn = findViewById(R.id.update_food_btn);

        discount_title_et.setVisibility(View.GONE);
        discounted_price_et.setVisibility(View.GONE);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Updating Food");

        //init permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

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

        discount_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    discount_title_et.setVisibility(View.VISIBLE);
                    discounted_price_et.setVisibility(View.VISIBLE);
                } else {
                    discount_title_et.setVisibility(View.GONE);
                    discounted_price_et.setVisibility(View.GONE);
                }
            }
        });

        change_image_tv.setOnClickListener(new View.OnClickListener() {
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

        update_food_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToInternet.isConnectedToInternet(MerchantUpdateFoodActivity.this)) {
                    //update food
                    inputData();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });
    }

    private void loadFoodDetails() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseAuth.getUid()).child("Foods").child(foodId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String foodName = "" + snapshot.child("foodName").getValue();
                        String foodDescription = "" + snapshot.child("foodDescription").getValue();
                        String foodDiscountTitle = "" + snapshot.child("foodDiscountTitle").getValue();
                        String foodImage = "" + snapshot.child("foodImage").getValue();
                        String foodCategory = "" + snapshot.child("foodCategory").getValue();
                        String foodDiscountedPrice = "" + snapshot.child("foodDiscountedPrice").getValue();
                        String discountAvailable = "" + snapshot.child("discountAvailable").getValue();
                        String foodOriginalPrice = "" + snapshot.child("foodOriginalPrice").getValue();
                        String foodAvailable = "" + snapshot.child("foodAvailable").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        //set data to views
                        if (discountAvailable.equals("true")) {
                            discount_switch.setChecked(true);

                            discount_title_et.setVisibility(View.VISIBLE);
                            discounted_price_et.setVisibility(View.VISIBLE);
                        } else {
                            discount_switch.setChecked(false);

                            discount_title_et.setVisibility(View.GONE);
                            discounted_price_et.setVisibility(View.GONE);
                        }

                        if (foodAvailable.equals("true")) {
                            availability_switch.setChecked(true);
                        } else {
                            availability_switch.setChecked(false);
                        }

                        food_name_et.setText(foodName);
                        food_category_tv.setText(foodCategory);
                        food_description_et.setText(foodDescription);
                        original_price_et.setText(foodOriginalPrice);
                        discount_title_et.setText(foodDiscountTitle);
                        discounted_price_et.setText(foodDiscountedPrice);

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
                        AlertDialog.Builder builder = new AlertDialog.Builder(MerchantUpdateFoodActivity.this);
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
                            if (ContextCompat.checkSelfPermission(MerchantUpdateFoodActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                                    ContextCompat.checkSelfPermission(MerchantUpdateFoodActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                //if camera permission allowed, can pick image from camera
                                pickFromCamera();
                            } else {
                                //if camera permission not allowed, request permission
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (ContextCompat.checkSelfPermission(MerchantUpdateFoodActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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

    String foodName, foodCategory, foodDescription, foodOriginalPrice,
            foodDiscountedPrice, foodDiscountTitle;
    boolean discountAvailable, foodAvailable;

    private void inputData() {
        foodName = food_name_et.getText().toString().trim();
        foodCategory = food_category_tv.getText().toString().trim();
        foodDescription = food_description_et.getText().toString().trim();
        foodOriginalPrice = original_price_et.getText().toString().trim();
        foodDiscountedPrice = discounted_price_et.getText().toString().trim();
        foodDiscountTitle = discount_title_et.getText().toString().trim();
        discountAvailable = discount_switch.isChecked();
        foodAvailable = availability_switch.isChecked();

        //validate data
        if (TextUtils.isEmpty(foodName)) {
            TastyToast.makeText(this, "Food Name is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (TextUtils.isEmpty(foodCategory)) {
            TastyToast.makeText(this, "Food Category is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (TextUtils.isEmpty(foodDescription)) {
            TastyToast.makeText(this, "Food Description is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (TextUtils.isEmpty(foodOriginalPrice)) {
            TastyToast.makeText(this, "Food Price is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            return;
        }

        if (discountAvailable) {
            if (TextUtils.isEmpty(foodDiscountedPrice)) {
                TastyToast.makeText(this, "Discounted Price is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
                return;
            }

            if (TextUtils.isEmpty(foodDiscountTitle)) {
                TastyToast.makeText(this, "Discount Title is Required", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
                return;
            }
        }

        updateFood();
    }

    private void updateFood() {
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        //updating food
        if (imageUri == null) {
            //update info without image
            //setup data to update food
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("foodName", "" + foodName);
            hashMap.put("foodCategory", "" + foodCategory);
            hashMap.put("foodDescription", "" + foodDescription);
            hashMap.put("foodOriginalPrice", "" + foodOriginalPrice);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("foodDiscountTitle", "" + foodDiscountTitle);
            hashMap.put("foodDiscountedPrice", "" + foodDiscountedPrice);
            hashMap.put("foodAvailable", "" + foodAvailable);

            //update food to db
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(firebaseAuth.getUid()).child("Foods").child(foodId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            //db updated
                            TastyToast.makeText(MerchantUpdateFoodActivity.this, "Food updated successfully!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            //failed to updating db
                            TastyToast.makeText(MerchantUpdateFoodActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });

        } else {
            //update info with image
            //name and path of image
            String filePathAndName = "foods_images/" + firebaseAuth.getUid() + "/" + foodId; //overwrite previous image using same id

            //uploaded image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                //setup data to update food
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("foodImage", "" + downloadImageUri); ////url of uploaded image
                                hashMap.put("foodName", "" + foodName);
                                hashMap.put("foodCategory", "" + foodCategory);
                                hashMap.put("foodDescription", "" + foodDescription);
                                hashMap.put("foodOriginalPrice", "" + foodOriginalPrice);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("foodDiscountTitle", "" + foodDiscountTitle);
                                hashMap.put("foodDiscountedPrice", "" + foodDiscountedPrice);
                                hashMap.put("foodAvailable", "" + foodAvailable);

                                //update food to db
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                databaseReference.child(firebaseAuth.getUid()).child("Foods").child(foodId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                                //db updated
                                                TastyToast.makeText(MerchantUpdateFoodActivity.this, "Food updated successfully!", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                                onBackPressed();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                //failed to updating db
                                                TastyToast.makeText(MerchantUpdateFoodActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            TastyToast.makeText(MerchantUpdateFoodActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });
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
                if (!IsConnectedToInternet.isConnectedToInternet(MerchantUpdateFoodActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadFoodDetails();
                }
            }
        });
    }
}