package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

public class UpdateCustomerAccountActivity extends AppCompatActivity {

    private ImageView back_btn_iv, my_location_btn;
    private CircularImageView customer_image_iv;
    private TextView change_image_tv;
    private EditText customer_name_et, customer_address_et, phone_number_et;
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

    private String city;

    private Uri imageUri;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_customer_account);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        back_btn_iv = findViewById(R.id.back_btn);
        my_location_btn = findViewById(R.id.my_location_btn);
        customer_image_iv = findViewById(R.id.customer_image);
        change_image_tv = findViewById(R.id.change_image);
        customer_name_et = findViewById(R.id.customer_name);
        customer_address_et = findViewById(R.id.customer_address);
        phone_number_et = findViewById(R.id.phone_number);
        update_account_btn = findViewById(R.id.update_account_btn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Updating Account");

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (!IsConnectedToInternet.isConnectedToInternet(this)) {
            noInternetConnectionBottomSheet();
        } else {
            checkUser();
        }

        back_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goes back to previous activity or fragment
                onBackPressed();
            }
        });

        my_location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check is connected to internet
                if (IsConnectedToInternet.isConnectedToInternet(UpdateCustomerAccountActivity.this)) {
                    //check is location service enabled
                    if (IsLocationServiceEnabled.isLocationServiceEnabled(UpdateCustomerAccountActivity.this)) {
                        //get or select current location
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(UpdateCustomerAccountActivity.this);
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
                if (IsConnectedToInternet.isConnectedToInternet(UpdateCustomerAccountActivity.this)) {
                    //update customer
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
            loadCustomerDetails();
        }
    }

    private void loadCustomerDetails() {
        //load user info and set to view
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            String fullName = "" + dataSnapshot.child("fullName").getValue();
                            String phoneNo = "" + dataSnapshot.child("phoneNo").getValue();
                            String completeAddress = "" + dataSnapshot.child("completeAddress").getValue();
                            String userImage = "" + dataSnapshot.child("userImage").getValue();

                            try {
                                Picasso.get().load(userImage).placeholder(R.drawable.ic_acc_placeholder_grey).into(customer_image_iv);
                            } catch (Exception e) {
                                customer_image_iv.setImageResource(R.drawable.ic_acc_placeholder_grey);
                            }

                            customer_name_et.setText(fullName);
                            phone_number_et.setText(phoneNo);
                            customer_address_et.setText(completeAddress);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(UpdateCustomerAccountActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(UpdateCustomerAccountActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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
                                .build(UpdateCustomerAccountActivity.this);
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
                            if (ContextCompat.checkSelfPermission(UpdateCustomerAccountActivity.this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                                    ContextCompat.checkSelfPermission(UpdateCustomerAccountActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                //if camera permission allowed, can pick image from camera
                                pickFromCamera();
                            } else {
                                //if camera permission not allowed, request permission
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (ContextCompat.checkSelfPermission(UpdateCustomerAccountActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
                customer_image_iv.setImageURI(imageUri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //set to imageview
                customer_image_iv.setImageURI(imageUri);
            }
            if (requestCode == Constants.PLACE_PICKER_REQUEST) {
                AddressData addressData = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                Geocoder geocoder = new Geocoder(UpdateCustomerAccountActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(addressData.getLatitude(), addressData.getLongitude(), 1);
                    city = addresses.get(0).getLocality();
                    customer_address_et.setText(addresses.get(0).getAddressLine(0));
                } catch (Exception e) {
                    TastyToast.makeText(UpdateCustomerAccountActivity.this, e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                }
            }
        }
    }

    String fullName, phoneNo, completeAddress;

    private void validateData() {
        fullName = customer_name_et.getText().toString().trim();
        phoneNo = phone_number_et.getText().toString().trim();
        completeAddress = customer_address_et.getText().toString().trim();

        //set error icon
        Drawable errorIcon = getResources().getDrawable(R.drawable.ic_error_red);
        errorIcon.setBounds(0, 0, errorIcon.getIntrinsicWidth(), errorIcon.getIntrinsicHeight());

        //regex validation pattern
        String phoneRegex = "\\d{10}";

        if (!TextUtils.isEmpty(fullName)) {
            if (!TextUtils.isEmpty(phoneNo)) {
                if (phoneNo.matches(phoneRegex)) {
                    if (!TextUtils.isEmpty(completeAddress)) {
                        updateAccount();
                    } else {
                        customer_address_et.setError("Please enter your address or tap GPS button to detect current location!", errorIcon);
                    }
                } else {
                    phone_number_et.setError("invalid phone number format!", errorIcon);
                }
            } else {
                phone_number_et.setError("Please enter your phone number!", errorIcon);
            }
        } else {
            customer_name_et.setError("Please enter your full name!", errorIcon);
        }
    }

    private void updateAccount() {
        progressDialog.setMessage("Updating Account");
        progressDialog.show();

        //updating account
        if (imageUri == null) {
            //update info without image
            if (city == null) {
                //setup data to update
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("fullName", "" + fullName);
                hashMap.put("phoneNo", "" + phoneNo);

                //update data to db
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                //db updated
                                TastyToast.makeText(UpdateCustomerAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                onBackPressed();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                //failed to updating db
                                TastyToast.makeText(UpdateCustomerAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                            }
                        });
            } else {
                //setup data to update
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("fullName", "" + fullName);
                hashMap.put("phoneNo", "" + phoneNo);
                hashMap.put("completeAddress", "" + completeAddress);
                hashMap.put("city", "" + city);

                //update data to db
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                //db updated
                                TastyToast.makeText(UpdateCustomerAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                onBackPressed();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                //failed to updating db
                                TastyToast.makeText(UpdateCustomerAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
                                if (city == null) {
                                    //setup data to update
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("userImage", "" + downloadImageUri); //url of uploaded image
                                    hashMap.put("fullName", "" + fullName);
                                    hashMap.put("phoneNo", "" + phoneNo);

                                    //update data to db
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                    databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    //db updated
                                                    TastyToast.makeText(UpdateCustomerAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                                    onBackPressed();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    //failed to updating db
                                                    TastyToast.makeText(UpdateCustomerAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
                                                }
                                            });
                                } else {
                                    //setup data to update
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("userImage", "" + downloadImageUri); //url of uploaded image
                                    hashMap.put("fullName", "" + fullName);
                                    hashMap.put("phoneNo", "" + phoneNo);
                                    hashMap.put("completeAddress", "" + completeAddress);
                                    hashMap.put("city", "" + city);

                                    //update data to db
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
                                    databaseReference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    progressDialog.dismiss();
                                                    //db updated
                                                    TastyToast.makeText(UpdateCustomerAccountActivity.this, "Account Updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                                                    onBackPressed();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    //failed to updating db
                                                    TastyToast.makeText(UpdateCustomerAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
                            TastyToast.makeText(UpdateCustomerAccountActivity.this, "" + e.getMessage(), TastyToast.LENGTH_SHORT, TastyToast.ERROR).show();
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
                if (!IsConnectedToInternet.isConnectedToInternet(UpdateCustomerAccountActivity.this)) {
                    roundedBottomSheetDialog.show();
                }
            }
        });
    }
}