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

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.Constants;
import com.mad.vasteats.helpers.SearchFilterMerchant;
import com.mad.vasteats.models.Merchant;
import com.mad.vasteats.views.activities.CustomerMerchantDetailsActivity;
import com.mad.vasteats.views.activities.MerchantFoodDetailsActivity;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class CustomerMerchantAdapter extends RecyclerView.Adapter<CustomerMerchantAdapter.HolderMerchant> implements Filterable {

    private Context context;
    public ArrayList<Merchant> merchantArrayList, filterList;
    private SearchFilterMerchant searchFilterMerchant;

    public CustomerMerchantAdapter(Context context, ArrayList<Merchant> merchantArrayList) {
        this.context = context;
        this.merchantArrayList = merchantArrayList;
        this.filterList = merchantArrayList;
    }

    @NonNull
    @Override
    public HolderMerchant onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_merchant_view_layout, parent, false);
        return new HolderMerchant(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMerchant holder, int position) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");

        Random random = new Random();
        final int randomCoverImage = Constants.coverImages[random.nextInt(Constants.coverImages.length)];
        //get data
        Merchant merchant = merchantArrayList.get(position);
        String merchantImage = merchant.getMerchantImage();
        final String uid = merchant.getUid();
        String email = merchant.getEmail();
        String fullName = merchant.getFullName();
        String merchantName = merchant.getMerchantName();
        String phoneNo = merchant.getPhoneNo();
        String deliveryFee = merchant.getDeliveryFee();
        String merchantAddress = merchant.getMerchantAddress();
        String merchantCity = merchant.getMerchantCity();
        String accountType = merchant.getAccountType();
        String online = merchant.getOnline();
        String merchantOpen = merchant.getMerchantOpen();
        String timestamp = merchant.getTimestamp();

        Double deliveryFeePriceAsCurrency = Double.parseDouble(deliveryFee);

        //set data
        holder.merchant_name_tv.setText(merchantName);
        holder.merchant_cover_image_iv.setImageResource(randomCoverImage);
        holder.merchant_city_name_tv.setText(merchantCity);
        holder.delivery_fee_tv.setText("LKR " + String.valueOf(decimalFormat.format(deliveryFeePriceAsCurrency)) + " Fee");

        if (merchantOpen.equals("true")) {
            holder.open_status_tv.setText("Open Now");
            holder.open_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_green));
        } else {
            holder.open_status_tv.setText("Closed");
            holder.open_status_tv.setTextColor(ContextCompat.getColor(context, R.color.google_red));
        }

        try {
            Picasso.get().load(merchantImage).placeholder(R.drawable.placeholder_color).into(holder.merchant_image_iv);
        } catch (Exception e) {
            holder.merchant_image_iv.setImageResource(R.drawable.placeholder_color);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CustomerMerchantDetailsActivity.class);
                intent.putExtra("merchantUid", uid);
                intent.putExtra("coverImages", randomCoverImage);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return merchantArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (searchFilterMerchant == null) {
            searchFilterMerchant = new SearchFilterMerchant(this, filterList);
        }
        return searchFilterMerchant;
    }

    class HolderMerchant extends RecyclerView.ViewHolder {

        private ImageView merchant_cover_image_iv;
        private CircularImageView merchant_image_iv;
        private TextView merchant_name_tv, open_status_tv, merchant_city_name_tv, merchant_ratings_tv,
                delivery_fee_tv;

        public HolderMerchant(@NonNull View itemView) {
            super(itemView);

            merchant_cover_image_iv = itemView.findViewById(R.id.merchant_cover_image);
            merchant_image_iv = itemView.findViewById(R.id.merchant_image);
            merchant_name_tv = itemView.findViewById(R.id.merchant_name);
            open_status_tv = itemView.findViewById(R.id.open_status);
            merchant_city_name_tv = itemView.findViewById(R.id.merchant_city_name);
            merchant_ratings_tv = itemView.findViewById(R.id.merchant_ratings);
            delivery_fee_tv = itemView.findViewById(R.id.delivery_fee);
        }
    }
}
