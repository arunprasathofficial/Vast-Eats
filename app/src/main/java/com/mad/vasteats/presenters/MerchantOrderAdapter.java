package com.mad.vasteats.presenters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.vasteats.R;
import com.mad.vasteats.models.Order;
import com.mad.vasteats.views.activities.MerchantOrderDetailsActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MerchantOrderAdapter extends RecyclerView.Adapter<MerchantOrderAdapter.HolderMerchantOrder> {

    private Context context;
    public ArrayList<Order> orderArrayList;

    public MerchantOrderAdapter(Context context, ArrayList<Order> orderArrayList) {
        this.context = context;
        this.orderArrayList = orderArrayList;
    }

    @NonNull
    @Override
    public HolderMerchantOrder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.merchant_order_view_layout, parent, false);
        return new HolderMerchantOrder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMerchantOrder holder, int position) {
        Order order = orderArrayList.get(position);
        final String orderId = order.getOrderId();
        String orderStatus = order.getOrderStatus();
        String orderPlacedDateTime = order.getOrderPlacedDateTime();
        String orderTotal = order.getOrderTotal();
        final String orderBy = order.getOrderBy();

        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");
        String formattedDateTime = simpleDateFormat.format(new Date(Long.parseLong(orderPlacedDateTime)));

        //set data
        holder.order_id_tv.setText("#" + orderId);
        holder.order_date_tv.setText(formattedDateTime);
        holder.order_amount_tv.setText("LKR " + decimalFormat.format(Double.parseDouble(orderTotal)));

        if (orderStatus.equals("Cancelled")) {
            holder.order_status_tv.setText(orderStatus);
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else if (orderStatus.equals("Delivered")) {
            holder.order_status_tv.setText(orderStatus);
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_green));
        } else if (orderStatus.equals("Order Processed")) {
            holder.order_status_tv.setText("Processed");
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.dark_yellow));
        } else if (orderStatus.equals("Order Confirmed")) {
            holder.order_status_tv.setText("Confirmed");
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.orange));
        } else if (orderStatus.equals("Order Placed")) {
            holder.order_status_tv.setText("Pending");
            holder.order_status_tv.setTextColor(ContextCompat.getColor(context, R.color.blue_grey));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MerchantOrderDetailsActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderBy", orderBy);
                context.startActivity(intent);
            }
        });

        loadCustomerDetails(order, holder);
        loadOrderDetails(order, holder);
    }

    private void loadCustomerDetails(Order order, final HolderMerchantOrder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(order.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String customerName = "" + snapshot.child("fullName").getValue();

                        //set data
                        holder.customer_name_tv.setText(customerName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails(Order order, HolderMerchantOrder holder) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(order.getOrderTo()).child("Orders").child(order.getOrderId()).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String itemsCount = "" + snapshot.getChildrenCount();

                        //set data
                        if (itemsCount.equals("1")) {
                            holder.items_count_tv.setText( "" + itemsCount + " Item");
                        } else {
                            holder.items_count_tv.setText("" + itemsCount + " Items");
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

    class HolderMerchantOrder extends RecyclerView.ViewHolder {

        private TextView customer_name_tv, order_amount_tv, order_date_tv, items_count_tv,
                order_id_tv, order_status_tv;

        public HolderMerchantOrder(@NonNull View itemView) {
            super(itemView);

            customer_name_tv = itemView.findViewById(R.id.customer_name);
            order_amount_tv = itemView.findViewById(R.id.order_amount);
            order_date_tv = itemView.findViewById(R.id.order_date);
            items_count_tv = itemView.findViewById(R.id.items_count);
            order_id_tv = itemView.findViewById(R.id.order_id);
            order_status_tv = itemView.findViewById(R.id.order_status);
        }
    }
}
