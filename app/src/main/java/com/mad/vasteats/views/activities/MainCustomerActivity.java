package com.mad.vasteats.views.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.Constants;
import com.mad.vasteats.helpers.easyDB;
import com.mad.vasteats.views.fragments.CustomerAccountFragment;
import com.mad.vasteats.views.fragments.CustomerHomeFragment;
import com.mad.vasteats.views.fragments.CustomerOrdersFragment;

public class MainCustomerActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_customer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        bottomNavigationView = findViewById(R.id.bottom_nav_bar);

        easyDB.init(this);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.customer_home) {
                    CustomerHomeFragment customerHomeFragment = new CustomerHomeFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.customer_frame_layout, customerHomeFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.customer_my_orders) {
                    CustomerOrdersFragment customerOrdersFragment = new CustomerOrdersFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.customer_frame_layout, customerOrdersFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.customer_my_account) {
                    CustomerAccountFragment customerAccountFragment = new CustomerAccountFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.customer_frame_layout, customerAccountFragment);
                    fragmentTransaction.commit();
                }
                return true;
            }
        });

        //set home fragment as default fragment
        bottomNavigationView.setSelectedItemId(R.id.customer_home);
    }
}