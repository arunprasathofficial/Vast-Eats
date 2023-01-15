package com.mad.vasteats.presenters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mad.vasteats.R;
import com.mad.vasteats.helpers.CustomerSearchFilterFood;
import com.mad.vasteats.models.Food;
import com.mad.vasteats.views.activities.CustomerFoodDetailsActivity;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomerFoodAdapter extends RecyclerView.Adapter<CustomerFoodAdapter.HolderCustomerFood> implements Filterable {

    private Context context;
    public ArrayList<Food> foodArrayList, filterList;
    private CustomerSearchFilterFood customerSearchFilterFood;

    public CustomerFoodAdapter(Context context, ArrayList<Food> foodArrayList) {
        this.context = context;
        this.foodArrayList = foodArrayList;
        this.filterList = foodArrayList;
    }

    @NonNull
    @Override
    public HolderCustomerFood onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_food_view_layout, parent, false);
        return new HolderCustomerFood(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCustomerFood holder, int position) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");

        //get data
        Food food = foodArrayList.get(position);
        final String foodId = food.getFoodId();
        String foodImage = food.getFoodImage();
        String foodName = food.getFoodName();
        String foodCategory = food.getFoodCategory();
        String foodDescription = food.getFoodDescription();
        String foodOriginalPrice = food.getFoodOriginalPrice();
        String discountAvailable = food.getDiscountAvailable();
        String foodDiscountedPrice = food.getFoodDiscountedPrice();
        String foodDiscountTitle = food.getFoodDiscountTitle();
        String foodAvailable = food.getFoodAvailable();
        String timestamp = food.getTimestamp();

        Double foodOriginalPriceAsCurrency = Double.parseDouble(foodOriginalPrice);
        Double foodDiscountedPriceAsCurrency = Double.parseDouble(foodDiscountedPrice);

        //set data
        holder.food_name_tv.setText(foodName);
        holder.food_description_tv.setText(foodDescription);
        holder.original_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodOriginalPriceAsCurrency)));
        holder.selling_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodDiscountedPriceAsCurrency)));
        holder.discount_title_tv.setText(foodDiscountTitle);

        if (foodAvailable.equals("true")) {
            holder.available_status_tv.setText("Available");
            holder.available_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_green));
        } else {
            holder.available_status_tv.setText("Unavailable");
            holder.available_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_red));
        }

        if (discountAvailable.equals("true")) {
            holder.discount_title_tv.setVisibility(View.VISIBLE);
            holder.original_price_tv.setVisibility(View.VISIBLE);
        } else {
            holder.discount_title_tv.setVisibility(View.GONE);
            holder.original_price_tv.setVisibility(View.GONE);
            holder.selling_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodOriginalPriceAsCurrency)));
        }

        try {
            Picasso.get().load(foodImage).placeholder(R.drawable.placeholder_grey).into(holder.food_image_iv);
        } catch (Exception e) {
            holder.food_image_iv.setImageResource(R.drawable.placeholder_grey);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CustomerFoodDetailsActivity.class);
                intent.putExtra("foodId", foodId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (customerSearchFilterFood == null) {
            customerSearchFilterFood = new CustomerSearchFilterFood(this, filterList);
        }
        return customerSearchFilterFood;
    }

    class HolderCustomerFood extends RecyclerView.ViewHolder {

        private ImageView food_image_iv;
        private TextView food_name_tv, food_description_tv, original_price_tv, selling_price_tv,
                available_status_tv, discount_title_tv;

        public HolderCustomerFood(@NonNull View itemView) {
            super(itemView);

            food_image_iv = itemView.findViewById(R.id.food_image);
            food_name_tv = itemView.findViewById(R.id.food_name);
            food_description_tv = itemView.findViewById(R.id.food_description);
            original_price_tv = itemView.findViewById(R.id.original_price);
            selling_price_tv = itemView.findViewById(R.id.selling_price);
            available_status_tv = itemView.findViewById(R.id.available_status);
            discount_title_tv = itemView.findViewById(R.id.discount_title);
        }
    }
}
