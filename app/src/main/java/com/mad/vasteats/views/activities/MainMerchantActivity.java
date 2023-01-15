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
import com.mad.vasteats.views.fragments.MerchantAccountFragment;
import com.mad.vasteats.views.fragments.MerchantCategoriesFragment;
import com.mad.vasteats.views.fragments.MerchantHomeFragment;
import com.mad.vasteats.views.fragments.MerchantOrdersFragment;
import com.mad.vasteats.views.fragments.MerchantFoodsFragment;

public class MainMerchantActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_merchant);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        bottomNavigationView = findViewById(R.id.bottom_nav_bar);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.merchant_home) {
                    MerchantHomeFragment merchantHomeFragment = new MerchantHomeFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.merchant_frame_layout, merchantHomeFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.merchant_categories) {
                    MerchantCategoriesFragment merchantCategoriesFragment = new MerchantCategoriesFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.merchant_frame_layout, merchantCategoriesFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.merchant_foods) {
                    MerchantFoodsFragment merchantFoodsFragment = new MerchantFoodsFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.merchant_frame_layout, merchantFoodsFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.merchant_orders) {
                    MerchantOrdersFragment merchantOrdersFragment = new MerchantOrdersFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.merchant_frame_layout, merchantOrdersFragment);
                    fragmentTransaction.commit();
                } else if (id == R.id.merchant_account) {
                    MerchantAccountFragment merchantAccountFragment = new MerchantAccountFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.merchant_frame_layout, merchantAccountFragment);
                    fragmentTransaction.commit();
                }
                return true;
            }
        });

        //set home fragment as default fragment
        bottomNavigationView.setSelectedItemId(R.id.merchant_home);
    }
}