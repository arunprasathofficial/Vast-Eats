package com.mad.vasteats.presenters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.models.Order;
import com.mad.vasteats.views.activities.CustomerOrderDetailsActivity;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.HolderCustomerOrder> {

    private Context context;
    private ArrayList<Order> orderArrayList;

    public CustomerOrderAdapter(Context context, ArrayList<Order> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
    }

    @NonNull
    @Override
    public HolderCustomerOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_order_view_layout, parent, false);
        return new HolderCustomerOrder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCustomerOrder holder, int position) {
        //get data
        Order order = orderArrayList.get(position);
        final String orderId = order.getOrderId();
        String orderPlacedDateTime = order.getOrderPlacedDateTime();
        String deliveryAddress = order.getDeliveryAddress();
        final String orderTo = order.getOrderTo();
        String orderBy = order.getOrderBy();
        String orderStatus = order.getOrderStatus();
        String orderRating = order.getOrderRating();
        String orderTotal = order.getOrderTotal();

        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");
        String formattedDateTime = simpleDateFormat.format(new Date(Long.parseLong(orderPlacedDateTime)));

        //set data
        holder.order_id_tv.setText("#" + orderId);
        holder.order_amount_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(orderTotal)));
        holder.date_time_tv.setText(formattedDateTime);
        holder.to_address_tv.setText(deliveryAddress);

        if (orderStatus.equals("Cancelled")) {
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else if (orderStatus.equals("Delivered")) {
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_green));
        } else if (orderStatus.equals("Order Processed")) {
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.dark_yellow));
        } else if (orderStatus.equals("Order Confirmed")) {
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.orange));
        }

        holder.order_status_tv.setText(orderStatus);

        if (orderStatus.equals("Delivered")) {
            holder.order_rating_bar.setVisibility(View.VISIBLE);
            if (orderRating.equals("") || orderRating.equals("null")) {
                holder.order_rating_bar.setRating(0);
            } else {
                holder.order_rating_bar.setRating(Float.parseFloat(orderRating));
            }
        } else {
            holder.order_rating_bar.setVisibility(View.GONE);
        }

        holder.order_details_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CustomerOrderDetailsActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderTo", orderTo);
                context.startActivity(intent);
            }
        });

        loadMerchantDetails(order, holder);
    }

    private void loadMerchantDetails(Order order, final HolderCustomerOrder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(order.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String merchantName = "" + snapshot.child("merchantName").getValue();
                        String merchantImage = "" + snapshot.child("merchantName").getValue();

                        //set data
                        holder.merchant_name_tv.setText(merchantName);

                        try {
                            Picasso.get().load(merchantImage).placeholder(R.drawable.placeholder_color).into(holder.merchant_image_iv);
                        } catch (Exception e) {
                            holder.merchant_image_iv.setImageResource(R.drawable.placeholder_color);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderArrayList.size();
    }

    class HolderCustomerOrder extends RecyclerView.ViewHolder {

        private TextView order_id_tv, date_time_tv, merchant_name_tv, to_address_tv, order_status_tv,
                order_details_tv, order_amount_tv;
        private CircularImageView merchant_image_iv;
        private RatingBar order_rating_bar;

        public HolderCustomerOrder(@NonNull View itemView) {
            super(itemView);

            order_id_tv = itemView.findViewById(R.id.order_id);
            date_time_tv = itemView.findViewById(R.id.date_time);
            merchant_name_tv = itemView.findViewById(R.id.merchant_name);
            to_address_tv = itemView.findViewById(R.id.to_address);
            order_status_tv = itemView.findViewById(R.id.order_status);
            order_details_tv = itemView.findViewById(R.id.order_details);
            order_amount_tv = itemView.findViewById(R.id.order_amount);
            merchant_image_iv = itemView.findViewById(R.id.merchant_image);
            order_rating_bar = itemView.findViewById(R.id.order_rating_bar);
        }
    }
}
