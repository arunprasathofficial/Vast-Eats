package com.mad.vasteats.views.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mad.vasteats.R;

import java.text.DecimalFormat;

public class CustomerOrderConfirmationActivity extends AppCompatActivity {

    private TextView order_id_tv, order_amount_tv;
    private Button continue_ordering_btn;

    private String orderId, orderAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_confirmation);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        order_id_tv = findViewById(R.id.order_id);
        order_amount_tv = findViewById(R.id.order_amount);
        continue_ordering_btn = findViewById(R.id.continue_ordering_btn);

        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");

        orderId = getIntent().getStringExtra("orderId");
        orderAmount = getIntent().getStringExtra("orderAmount");

        order_id_tv.setText("#" + orderId);
        order_amount_tv.setText("LKR " + String.valueOf(decimalFormat.format(Double.parseDouble(orderAmount))));

        continue_ordering_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goes to home
                Intent intent = new Intent(CustomerOrderConfirmationActivity.this, MainCustomerActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}