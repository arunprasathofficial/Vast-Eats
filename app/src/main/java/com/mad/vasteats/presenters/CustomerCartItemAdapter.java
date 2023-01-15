package com.mad.vasteats.presenters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog;
import com.mad.vasteats.R;
import com.mad.vasteats.helpers.easyDB;
import com.mad.vasteats.models.CartItem;
import com.mad.vasteats.views.activities.CustomerCartActivity;
import com.mad.vasteats.views.activities.CustomerMerchantDetailsActivity;
import com.sdsmdg.tastytoast.TastyToast;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomerCartItemAdapter extends RecyclerView.Adapter<CustomerCartItemAdapter.HolderCustomerCartItem> {

    private Context context;
    private ArrayList<CartItem> cartItems;

    private DecimalFormat decimalFormat;
    private int updatedQty = 1;
    private Double eachRate = 0.00;
    private Double updatedTotal = 0.00;

    private RoundedBottomSheetDialog roundedBottomSheetDialog;

    public CustomerCartItemAdapter(Context context, ArrayList<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCustomerCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.customer_cart_food_view_layout, parent, false);
        return new HolderCustomerCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCustomerCartItem holder, int position) {
        decimalFormat = new DecimalFormat("#,###.00");

        final String fId, fName, fImage, fQty, fRate, fTotal;

        //get data
        CartItem cartItem = cartItems.get(holder.getAdapterPosition());
        fId = cartItem.getCartFoodId();
        fName = cartItem.getCartFoodName();
        fImage = cartItem.getCartFoodImage();
        fQty = cartItem.getCartFoodQty();
        fRate = cartItem.getCartFoodRate();
        fTotal = cartItem.getCartFoodTotal();

        final Double foodRate = Double.parseDouble(fRate);
        final Double foodTotal = Double.parseDouble(fTotal);

        //set data
        holder.food_name_tv.setText(fName);
        holder.food_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodRate)));
        holder.food_total_price_tv.setText("LKR " + String.valueOf(decimalFormat.format(foodTotal)));
        holder.food_qty_tv.setText(fQty);

        try {
            Picasso.get().load(fImage).placeholder(R.drawable.placeholder_grey).into(holder.food_image_iv);
        } catch (Exception e) {
            holder.food_image_iv.setImageResource(R.drawable.placeholder_grey);
        }

        holder.remove_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = Integer.parseInt(cartItems.get(holder.getAdapterPosition()).getRowId());
                boolean deleteRow = easyDB.easyDB.deleteRow(id);

                if (deleteRow) {
                    TastyToast.makeText(context, fName + " removed from your cart", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                } else {
                    TastyToast.makeText(context, "Something went wrong...", TastyToast.LENGTH_SHORT, TastyToast.WARNING);
                }

                //refresh cart list
                cartItems.remove(holder.getAdapterPosition());
                notifyItemChanged(holder.getAdapterPosition());
                notifyDataSetChanged();

                ((CustomerCartActivity)context).recreate();

                Double oldCartTotal = ((CustomerCartActivity)context).cartTotal;
                Double newCartTotal = oldCartTotal - Double.parseDouble(fTotal);

                ((CustomerCartActivity)context).cart_total_tv.setText("LKR " + String.valueOf(decimalFormat.format(newCartTotal)));
                ((CustomerCartActivity)context).items_count_tv.setText("" + getItemCount());

                //update cart count badge
                CustomerMerchantDetailsActivity.cartCount();
            }
        });

        holder.update_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCartItem(fId, fName, fImage, fQty, fRate, fTotal, holder.getAdapterPosition());
            }
        });
    }

    private void updateCartItem(String fId, String fName, String fImage, String fQty, String fRate, String fTotal, int adapterPosition) {
        updatedQty = Integer.parseInt(fQty);
        eachRate = Double.parseDouble(fRate);
        updatedTotal = Double.parseDouble(fTotal);

        //bottom sheet
        roundedBottomSheetDialog = new RoundedBottomSheetDialog(context);

        //inflate view for bottom sheet
        View view = LayoutInflater.from(context).inflate(R.layout.customer_update_cart_item_dialog_layout, null);

        //set view to bottom sheet
        roundedBottomSheetDialog.setContentView(view);
        roundedBottomSheetDialog.setCanceledOnTouchOutside(false);

        //declare ui views
        ImageView close_btn_iv, food_image_iv, minus_btn_iv, plus_btn_iv;
        final TextView food_name_tv, cart_qty_tv;
        Button update_qty_btn;

        //init ui views
        close_btn_iv = view.findViewById(R.id.close_btn);
        food_image_iv = view.findViewById(R.id.food_image);
        minus_btn_iv = view.findViewById(R.id.minus_btn);
        plus_btn_iv = view.findViewById(R.id.plus_btn);
        food_name_tv = view.findViewById(R.id.food_name);
        cart_qty_tv = view.findViewById(R.id.cart_qty);
        update_qty_btn = view.findViewById(R.id.update_qty_btn);

        //set data
        try {
            Picasso.get().load(fImage).placeholder(R.drawable.placeholder_grey).into(food_image_iv);
        } catch (Exception e) {
            food_image_iv.setImageResource(R.drawable.placeholder_grey);
        }
        food_name_tv.setText(fName);
        cart_qty_tv.setText(fQty);

        roundedBottomSheetDialog.show();

        close_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                roundedBottomSheetDialog.dismiss();
            }
        });

        plus_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatedTotal = updatedTotal + eachRate;
                updatedQty++;
                cart_qty_tv.setText("" + updatedQty);
            }
        });

        minus_btn_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updatedQty > 1) {
                    updatedTotal = updatedTotal - eachRate;
                    updatedQty--;
                    cart_qty_tv.setText("" + updatedQty);
                }
            }
        });

        update_qty_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = Integer.parseInt(cartItems.get(adapterPosition).getRowId());

                //update cart item
                boolean updated = easyDB.easyDB.updateData("cartFoodQty", updatedQty)
                        .updateData("cartFoodRate", String.valueOf(eachRate))
                        .updateData("cartFoodTotal", String.valueOf(updatedTotal))
                        .rowID(id);

                if (updated) {
                    TastyToast.makeText(context, fName + " quantity updated", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                } else {
                    TastyToast.makeText(context, "Something went wrong...", TastyToast.LENGTH_SHORT, TastyToast.WARNING);
                }

                roundedBottomSheetDialog.dismiss();

                //refresh cart list
                notifyDataSetChanged();
                ((CustomerCartActivity)context).recreate();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class HolderCustomerCartItem extends RecyclerView.ViewHolder {

        private ImageView food_image_iv, remove_btn_iv, update_btn_iv;
        private TextView food_name_tv, food_price_tv, food_qty_tv, food_total_price_tv;

        public HolderCustomerCartItem(@NonNull View itemView) {
            super(itemView);

            food_image_iv = itemView.findViewById(R.id.food_image);
            remove_btn_iv = itemView.findViewById(R.id.remove_btn);
            update_btn_iv = itemView.findViewById(R.id.update_btn);
            food_name_tv = itemView.findViewById(R.id.food_name);
            food_price_tv = itemView.findViewById(R.id.food_price);
            food_qty_tv = itemView.findViewById(R.id.food_qty);
            food_total_price_tv = itemView.findViewById(R.id.food_total_price);
        }
    }
}
