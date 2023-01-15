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

public class MerchantOrderedItemAdapter extends RecyclerView.Adapter<MerchantOrderedItemAdapter.HolderMerchantOrderedItem> {

    private Context context;
    private ArrayList<OrderedItem> orderedItems;
    private DecimalFormat decimalFormat;

    public MerchantOrderedItemAdapter(Context context, ArrayList<OrderedItem> orderedItems) {
        this.context = context;
        this.orderedItems = orderedItems;
    }

    @NonNull
    @Override
    public HolderMerchantOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.merchant_ordered_items_layout, parent, false);
        return new HolderMerchantOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMerchantOrderedItem holder, int position) {
        decimalFormat = new DecimalFormat("#,###.00");

        //get data
        final String foodId, foodImage, foodQty, foodRate, foodName, foodTotal;
        OrderedItem orderedItem = orderedItems.get(position);
        foodId = orderedItem.getFoodId();
        foodImage = orderedItem.getFoodImage();
        foodQty = orderedItem.getFoodQty();
        foodRate = orderedItem.getFoodRate();
        foodName = orderedItem.getFoodName();
        foodTotal = orderedItem.getFoodTotal();

        //set data
        holder.food_name_tv.setText(foodName);
        holder.food_price_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(foodRate)));
        holder.food_total_price_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(foodTotal)));
        holder.food_qty_tv.setText(foodQty);

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

    class HolderMerchantOrderedItem extends RecyclerView.ViewHolder {

        private ImageView food_image_iv;
        private TextView food_name_tv, food_price_tv, food_qty_tv, food_total_price_tv;

        public HolderMerchantOrderedItem(@NonNull View itemView) {
            super(itemView);

            food_image_iv = itemView.findViewById(R.id.food_image);
            food_name_tv = itemView.findViewById(R.id.food_name);
            food_price_tv = itemView.findViewById(R.id.food_price);
            food_qty_tv = itemView.findViewById(R.id.food_qty);
            food_total_price_tv = itemView.findViewById(R.id.food_total_price);
        }
    }
}
