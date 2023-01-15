package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.mad.vasteats.helpers.IsLocationServiceEnabled;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UpdateMerchantAccountActivity extends AppCompatActivity {

    private ImageView back_btn_iv, my_location_btn_iv;
    private CircularImageView merchant_image_iv;
    private TextView change_image_tv;
    private EditText full_name_et, merchant_name_et, phone_number_et, merchant_address_et, delivery_fee_et;
    private SwitchCompat open_status_switch;
    private Button update_account_btn;
    private ProgressDialog progressDialog;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] locationPermissions;
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private String merchantCurrentCity;

    private Uri imageUri;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_merchant_account);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        back_btn_iv = findViewById(R.id.back_btn);
        my_location_btn_iv = findViewById(R.id.my_location_btn);
        merchant_image_iv = findViewById(R.id.merchant_image);
        change_image_tv = findViewById(R.id.change_image);
        full_name_et = findViewById(R.id.full_name);
        merchant_name_et = findViewById(R.id.merchant_name);
        phone_number_et = findViewById(R.id.phone_number);
        merchant_address_et = findViewById(R.id.merchant_address);
        delivery_fee_et = findViewById(R.id.delivery_fee);
        open_status_switch = findViewById(R.id.open_status_switch);
        update_account_btn = findViewById(R.id.update_account_btn);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Updating Merchant");

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        //init permissions array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
            checkUser();
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

        my_location_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check is connected to internet
                if (IsConnectedToInternet.isConnectedToInternet(UpdateMerchantAccountActivity.this)) {
                    //check is location service enabled
                    if (IsLocationServiceEnabled.isLocationServiceEnabled(UpdateMerchantAccountActivity.this)) {
                        //get or select current location
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(UpdateMerchantAccountActivity.this);
                        getLocation();
                    } else {
                        noLocationServiceBottomSheet();
                    }
                } else {
                    noInternetConnectionBottomSheet();
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

        update_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (IsConnectedToInternet.isConnectedToInternet(UpdateMerchantAccountActivity.this)) {
                    //update merchant
                    validateData();
                } else {
                    noInternetConnectionBottomSheet();
                }
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        } else {
            loadMerchantDetails();
        }
    }

    private void loadMerchantDetails() {
        //load user info and set to view
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            String deliveryFee = "" + dataSnapshot.child("deliveryFee").getValue();
                            String fullName = "" + dataSnapshot.child("fullName").getValue();
                            String phoneNo = "" + dataSnapshot.child("phoneNo").getValue();
                            String merchantAddress = "" + dataSnapshot.child("merchantAddress").getValue();
                            String merchantCity = "" + dataSnapshot.child("merchantCity").getValue();
                            String merchantImage = "" + dataSnapshot.child("merchantImage").getValue();
                            String merchantName = "" + dataSnapshot.child("merchantName").getValue();
                            String merchantOpen = "" + dataSnapshot.child("merchantOpen").getValue();

                            try {
                                Picasso.get().load(merchantImage).placeholder(R.drawable.placeholder_color).into(merchant_image_iv);
                            } catch (Exception e) {
                                merchant_image_iv.setImageResource(R.drawable.placeholder_color);
                            }

                            full_name_et.setText(fullName);
                            merchant_name_et.setText(merchantName);
                            phone_number_et.setText(phoneNo);
                            merchant_address_et.setText(merchantAddress);
                            delivery_fee_et.setText(deliveryFee);

                            if (merchantOpen.equals("true")) {
                                open_status_switch.setChecked(true);
                            } else {
                                open_status_switch.setChecked(false);
                            }

                            merchantCurrentCity = merchantCity;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(UpdateMerchantAccountActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(UpdateMerchantAccountActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Intent intent = new PlacePicker.IntentBuilder()
                                .setLatLong(location.getLatitude(), location.getLongitude())
                                .setMapZoom(18.0f)
                                .setAddressRequired(true)
                                .setMapType(MapType.NORMAL)
                                .setPrimaryTextColor(R.color.white)
                                .setSecondaryTextColor(R.color.white)
                                .setBottomViewColor(R.color.blue_grey)
                                .setMarkerImageImageColor(R.color.google_red)
                                .setFabColor(R.color.google_blue)
                                .build(UpdateMerchantAccountActivity.this);
                        startActivityForResult(intent, Constants.PLACE_PICKER_REQUEST);
                    }
                }
            });
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        } else {
            requestLocationPermission();
        }
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
                            if (ContextCompat.checkSelfPermission(UpdateMerchantAccountActivity.this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                                    ContextCompat.checkSelfPermission(UpdateMerchantAccountActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                //if camera permission allowed, can pick image from camera
                                pickFromCamera();
                            } else {
                                //if camera permission not allowed, request permission
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (ContextCompat.checkSelfPermission(UpdateMerchantAccountActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                TastyToast.makeText(this, "Please grant the location permission!", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            }
        }

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                pickFromCamera();
            } else {
                TastyToast.makeText(this, "Please grant the camera permission!", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            }
        }

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery();
            } else {
                TastyToast.makeText(this, "Please grant the storage permission!", TastyToast.LENGTH_SHORT, TastyToast.WARNING).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //get picked image
                imageUri = data.getData();
                //set to imageview
                merchant_image_iv.setImageURI(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                merchant_image_iv.setImageURI(imageUri);
            }
            if (requestCode == Constants.PLACE_PICKER_REQUEST) {
                AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                Geocoder geocoder = new Geocoder(UpdateMerchantAccountActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(addressData.getLatitude(), addressData.getLongitude(), 1);
                    merchantCurrentCity = addresses.get(0).getLocality();
                    merchant_address_et.setText(addresses.get(0).getAddressLine(0));
                } catch (Exception e) {
                    TastyToast.makeText(UpdateMerchantAccountActivity.this, e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                }
            }
        }
    }

    String fullName, merchantName, phoneNo, deliveryFee, merchantAddress;
    boolean merchantOpenStatus;

    private void validateData() {
        fullName = full_name_et.getText().toString().trim();
        merchantName = merchant_name_et.getText().toString().trim();
        phoneNo = phone_number_et.getText().toString().trim();
        deliveryFee = delivery_fee_et.getText().toString().trim();
        merchantAddress = merchant_address_et.getText().toString().trim();
        merchantOpenStatus = open_status_switch.isChecked();

        //set error icon
        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_red);
        errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        //regex validation pattern
        String phoneRegex = "\\d{10}";

        if (!TextUtils.isEmpty(fullName)) {
            if (!TextUtils.isEmpty(merchantName)) {
                if (!TextUtils.isEmpty(phoneNo)) {
                    if (phoneNo.matches(phoneRegex)) {
                        if (!TextUtils.isEmpty(deliveryFee)) {
                            if (!TextUtils.isEmpty(merchantAddress)) {
                                updateAccount();
                            } else {
                                merchant_address_et.setError("Please enter merchant address or tap GPS button to detect current location!", errorIcon);
                            }
                        } else {
                            delivery_fee_et.setError("Please enter delivery fee!", errorIcon);
                        }
                    } else {
                        phone_number_et.setError("invalid phone number format!", errorIcon);
                    }
                } else {
                    phone_number_et.setError("Please enter your phone number!", errorIcon);
                }
            } else {
                merchant_name_et.setError("Please enter merchant name!", errorIcon);
            }
        } else {
            full_name_et.setError("Please enter your full name!", errorIcon);
        }
    }

    private void updateAccount() {
        progressDialog.setMessage("Please wait");
        progressDialog.show();

        //updating account
        if (imageUri == null) {
            //update info without image
            if (merchantCurrentCity == null) {
                //setup data to update
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("fullName", "" + fullName);
                hashMap.put("merchantName", "" + merchantName);
                hashMap.put("phoneNo", "" + phoneNo);
                hashMap.put("deliveryFee", "" + deliveryFee);
                hashMap.put("merchantOpen", "" + merchantOpenStatus);

                //update data to db
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                //db updated
                                TastyToast.makeText(UpdateMerchantAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                onBackPressed();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                //failed to updating db
                                TastyToast.makeText(UpdateMerchantAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                            }
                        });
            } else {
                //setup data to update
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("fullName", "" + fullName);
                hashMap.put("merchantName", "" + merchantName);
                hashMap.put("phoneNo", "" + phoneNo);
                hashMap.put("deliveryFee", "" + deliveryFee);
                hashMap.put("merchantAddress", "" + merchantAddress);
                hashMap.put("merchantCity", "" + merchantCurrentCity);
                hashMap.put("merchantOpen", "" + merchantOpenStatus);

                //update data to db
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                //db updated
                                TastyToast.makeText(UpdateMerchantAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                onBackPressed();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                //failed to updating db
                                TastyToast.makeText(UpdateMerchantAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                            }
                        });
            }
        } else {
            //update info with image
            //name and path of image
            String filePathAndName = "users_images/" + firebaseAuth.getUid();

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
                                if (merchantCurrentCity == null) {
                                    //setup data to update
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("merchantImage", "" + downloadImageUri); ////url of uploaded image
                                    hashMap.put("fullName", "" + fullName);
                                    hashMap.put("merchantName", "" + merchantName);
                                    hashMap.put("phoneNo", "" + phoneNo);
                                    hashMap.put("deliveryFee", "" + deliveryFee);
                                    hashMap.put("merchantOpen", "" + merchantOpenStatus);

                                    //update data to db
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                    databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    //db updated
                                                    TastyToast.makeText(UpdateMerchantAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                                    onBackPressed();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    //failed to updating db
                                                    TastyToast.makeText(UpdateMerchantAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                                                }
                                            });
                                } else {
                                    //setup data to update
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("merchantImage", "" + downloadImageUri); ////url of uploaded image
                                    hashMap.put("fullName", "" + fullName);
                                    hashMap.put("merchantName", "" + merchantName);
                                    hashMap.put("phoneNo", "" + phoneNo);
                                    hashMap.put("deliveryFee", "" + deliveryFee);
                                    hashMap.put("merchantAddress", "" + merchantAddress);
                                    hashMap.put("merchantCity", "" + merchantCurrentCity);
                                    hashMap.put("merchantOpen", "" + merchantOpenStatus);

                                    //update data to db
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                    databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    //db updated
                                                    TastyToast.makeText(UpdateMerchantAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                                    onBackPressed();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    //failed to updating db
                                                    TastyToast.makeText(UpdateMerchantAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                                                }
                                            });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            TastyToast.makeText(UpdateMerchantAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                        }
                    });
        }
    }

    private void noLocationServiceBottomSheet() {
        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(this);

        //inflate view for bottom sheet
        View view = LayoutInflater.from(this).inflate(R.layout.no_location_service_layout, null);

        //set view to bottom sheet
        roundedBottomSheetDialog.setContentView(view);
        roundedBottomSheetDialog.setCanceledOnTouchOutside(false);

        //init ui views
        Button settings_btn = view.findViewById(R.id.settings_btn);

        roundedBottomSheetDialog.show();

        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roundedBottomSheetDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
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
                if (!IsConnectedToInternet.isConnectedToInternet(UpdateMerchantAccountActivity.this)) {
                    roundedBottomSheetDialog.show();
                }
            }
        });
    }
}