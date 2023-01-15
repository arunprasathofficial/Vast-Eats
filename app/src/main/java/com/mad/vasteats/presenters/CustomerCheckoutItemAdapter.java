package com.mad.vasteats.presenters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.vasteats.R;
import com.mad.vasteats.models.CartItem;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomerCheckoutItemAdapter extends RecyclerView.Adapter<CustomerCheckoutItemAdapter.HolderCustomerCheckoutItem> {

    private Context context;
    private ArrayList<CartItem> cartItems;
    private DecimalFormat decimalFormat;

    public CustomerCheckoutItemAdapter(Context context, ArrayList<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCustomerCheckoutItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_checkout_item_list_layout, parent, false);
        return new HolderCustomerCheckoutItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCustomerCheckoutItem holder, int position) {
        decimalFormat = new DecimalFormat("#,###.00");

        //get data
        String fName, fImage, fQty, fRate, fTotal;
        CartItem cartItem = cartItems.get(position);
        fName = cartItem.getCartFoodName();
        fImage = cartItem.getCartFoodImage();
        fQty = cartItem.getCartFoodQty();
        fRate = cartItem.getCartFoodRate();
        fTotal = cartItem.getCartFoodTotal();

        //set data
        holder.food_name_tv.setText(fName);
        holder.checkout_qty_tv.setText(fQty + " X " + decimalFormat.format(Double.parseDouble(fRate)));
        holder.food_total_price_tv.setText(decimalFormat.format(Double.parseDouble(fTotal)));

        try {
            Picasso.get().load(fImage).placeholder(R.drawable.placeholder_grey).into(holder.food_image_iv);
        } catch (Exception e) {
            holder.food_image_iv.setImageResource(R.drawable.placeholder_grey);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }


    class HolderCustomerCheckoutItem extends RecyclerView.ViewHolder {

        private ImageView food_image_iv;
        private TextView food_name_tv, food_total_price_tv, checkout_qty_tv;

        public HolderCustomerCheckoutItem(@NonNull View itemView) {
            super(itemView);

            food_image_iv = itemView.findViewById(R.id.food_image);
            food_name_tv = itemView.findViewById(R.id.food_name);
            food_total_price_tv = itemView.findViewById(R.id.food_total_price);
            checkout_qty_tv = itemView.findViewById(R.id.checkout_qty);
        }
    }
}
