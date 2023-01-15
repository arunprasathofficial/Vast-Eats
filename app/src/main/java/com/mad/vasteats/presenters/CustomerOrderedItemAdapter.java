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
import com.mad.vasteats.models.OrderedItem;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomerOrderedItemAdapter extends RecyclerView.Adapter<CustomerOrderedItemAdapter.HolderCustomerOrderedItem> {

    private Context context;
    private ArrayList<OrderedItem> orderedItems;
    private DecimalFormat decimalFormat;

    public CustomerOrderedItemAdapter(Context context, ArrayList<OrderedItem> orderedItems) {
        this.context = context;
        this.orderedItems = orderedItems;
    }

    @NonNull
    @Override
    public HolderCustomerOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_ordered_item_list_layout, parent, false);
        return new HolderCustomerOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCustomerOrderedItem holder, int position) {
        decimalFormat = new DecimalFormat("#,###.00");

        //get data
        String foodId, foodImage, foodQty, foodRate, foodName, foodTotal;
        OrderedItem orderedItem = orderedItems.get(position);
        foodId = orderedItem.getFoodId();
        foodImage = orderedItem.getFoodImage();
        foodQty = orderedItem.getFoodQty();
        foodRate = orderedItem.getFoodRate();
        foodName = orderedItem.getFoodName();
        foodTotal = orderedItem.getFoodTotal();

        //set data
        holder.food_name_tv.setText(foodName);
        holder.food_qty_tv.setText(foodQty + " X " + decimalFormat.format(Double.parseDouble(foodRate)));
        holder.food_total_price_tv.setText(decimalFormat.format(Double.parseDouble(foodTotal)));

        try {
            Picasso.get().load(foodImage).placeholder(R.drawable.placeholder_grey).into(holder.food_image_iv);
        } catch (Exception e) {
            holder.food_image_iv.setImageResource(R.drawable.placeholder_grey);
        }
    }

    @Override
    public int getItemCount() {
        return orderedItems.size();
    }

    class HolderCustomerOrderedItem extends RecyclerView.ViewHolder {

        private ImageView food_image_iv;
        private TextView food_name_tv, food_total_price_tv, food_qty_tv;

        public HolderCustomerOrderedItem(@NonNull View itemView) {
            super(itemView);

            food_image_iv = itemView.findViewById(R.id.food_image);
            food_name_tv = itemView.findViewById(R.id.food_name);
            food_total_price_tv = itemView.findViewById(R.id.food_total_price);
            food_qty_tv = itemView.findViewById(R.id.food_qty);
        }
    }
}
