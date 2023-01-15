package com.mad.vasteats.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.IsConnectedToInternet;
import com.mad.vasteats.helpers.easyDB;
import com.mad.vasteats.models.CartItem;
import com.mad.vasteats.presenters.CustomerCartItemAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomerCartActivity extends AppCompatActivity {

    private ImageView back_btn_iv;
    private RecyclerView cart_items_rv;
    private CardView cart_details_cv;
    public TextView items_count_tv, cart_total_tv;
    private Button checkout_btn;
    private LinearLayout empty_cart_msg_lt;
    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    public Double cartTotal = 0.00;
    private DecimalFormat decimalFormat;

    private ArrayList<CartItem> cartItems;
    private CustomerCartItemAdapter customerCartItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_cart);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        back_btn_iv = findViewById(R.id.back_btn);
        cart_items_rv = findViewById(R.id.cart_items_rv);
        cart_details_cv = findViewById(R.id.cart_details_cv);
        items_count_tv = findViewById(R.id.items_count);
        cart_total_tv = findViewById(R.id.cart_total);
        checkout_btn = findViewById(R.id.checkout_btn);
        empty_cart_msg_lt = findViewById(R.id.empty_cart_msg);

        if (IsConnectedToInternet.isConnectedToInternet(this)) {
            loadCartItems();
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

        checkout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //goes to checkout activity
                Intent intent = new Intent(CustomerCartActivity.this, CustomerCheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadCartItems() {
        //init list
        cartItems = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        cart_items_rv.setLayoutManager(linearLayoutManager);

        decimalFormat = new DecimalFormat("#,###.00");

        //get all records from AndroidEasySQL DB
        Cursor res = easyDB.easyDB.getAllData();
        while (res.moveToNext()) {
            String rowId = res.getString(0);
            String fId = res.getString(1);
            String fTitle = res.getString(2);
            String fImage = res.getString(3);
            String fQty = res.getString(4);
            String fRate = res.getString(5);
            String fTotal = res.getString(6);

            cartTotal = cartTotal + Double.parseDouble(fTotal);

            CartItem cartItem = new CartItem(
                    "" + rowId,
                    "" + fId,
                    "" + fTitle,
                    "" + fImage,
                    "" + fQty,
                    "" + fRate,
                    "" + fTotal
            );

            cartItems.add(cartItem);
        }

        //setup adapter
        customerCartItemAdapter = new CustomerCartItemAdapter(this, cartItems);
        //set to recyclerview
        cart_items_rv.setAdapter(customerCartItemAdapter);

        if (customerCartItemAdapter.getItemCount() == 0) {
            cart_items_rv.setVisibility(View.GONE);
            cart_details_cv.setVisibility(View.GONE);
            empty_cart_msg_lt.setVisibility(View.VISIBLE);
        } else {
            empty_cart_msg_lt.setVisibility(View.GONE);
            cart_items_rv.setVisibility(View.VISIBLE);
            cart_details_cv.setVisibility(View.VISIBLE);
        }

        cart_total_tv.setText("LKR " + String.valueOf(decimalFormat.format(cartTotal)));
        items_count_tv.setText("" + cartItems.size());
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
                if (!IsConnectedToInternet.isConnectedToInternet(CustomerCartActivity.this)) {
                    roundedBottomSheetDialog.show();
                } else {
                    loadCartItems();
                }
            }
        });
    }
}