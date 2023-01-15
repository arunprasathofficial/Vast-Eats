package com.mad.vasteats.helpers;

import com.mad.vasteats.R;

public class Constants {

    //firebase cloud messaging service
    public static final String FCM_KEY = "{Firebase Cloud Messaging API Key Here}";
    public static final String FCM_TOPIC = "VAST_EATS_PUSH_NOTIFICATIONS";
    public static final String FCM_API = "https://fcm.googleapis.com/fcm/send";

    //filter order status
    public static final String[] filterCustomerOrderStatus = {
            "All",
            "Order Placed",
            "Order Confirmed",
            "Order Processed",
            "Delivered",
            "Cancelled"
    };

    //filter order status
    public static final String[] filterMerchantOrderStatus = {
            "All",
            "Pending",
            "Confirmed",
            "Processed",
            "Delivered",
            "Cancelled"
    };

    //change order status
    public static final String[] changeMerchantOrderStatus = {
            "Pending",
            "Confirmed",
            "Processed",
            "Delivered",
            "Cancelled"
    };

    //merchants view random cover images
    public static final int[] coverImages = {
            R.drawable.merchant_bg_1,
            R.drawable.merchant_bg_2,
            R.drawable.merchant_bg_3,
            R.drawable.merchant_bg_4,
            R.drawable.merchant_bg_5,
            R.drawable.merchant_bg_6
    };
}
