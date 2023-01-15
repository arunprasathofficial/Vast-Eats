package com.mad.vasteats.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mad.vasteats.R;
import com.mad.vasteats.views.activities.CustomerOrderDetailsActivity;
import com.mad.vasteats.views.activities.MerchantOrderDetailsActivity;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //all notifications will be received here
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //get data from notification
        String notificationType = remoteMessage.getData().get("notificationType");

        if (notificationType.equals("NewOrder")) {
            String customerUid = remoteMessage.getData().get("customerUid");
            String merchantUid = remoteMessage.getData().get("merchantUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationMessage = remoteMessage.getData().get("notificationMessage");

            if (firebaseUser != null && firebaseAuth.getUid().equals(merchantUid)) {
                showNotification(orderId, merchantUid, customerUid, notificationTitle, notificationMessage, notificationType);
            }
        }
        if (notificationType.equals("CancelOrderByCustomer")) {
            String customerUid = remoteMessage.getData().get("customerUid");
            String merchantUid = remoteMessage.getData().get("merchantUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationMessage = remoteMessage.getData().get("notificationMessage");

            if (firebaseUser != null && firebaseAuth.getUid().equals(merchantUid)) {
                showNotification(orderId, merchantUid, customerUid, notificationTitle, notificationMessage, notificationType);
            }
        }
        if (notificationType.equals("OrderStatusChanged")) {
            String customerUid = remoteMessage.getData().get("customerUid");
            String merchantUid = remoteMessage.getData().get("merchantUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationMessage = remoteMessage.getData().get("notificationMessage");

            if (firebaseUser != null && firebaseAuth.getUid().equals(customerUid)) {
                showNotification(orderId, merchantUid, customerUid, notificationTitle, notificationMessage, notificationType);
            }
        }
    }

    private void showNotification(String orderId, String merchantUid, String customerUid, String notificationTitle, String notificationMessage, String notificationType) {
        //notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //get random notification id
        int notificationId = new Random().nextInt(3000);

        //check is android version is oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel(notificationManager);
        }

        //handle notification actions
        Intent intent = null;
        if (notificationType.equals("NewOrder")) {
            intent = new Intent(this, MerchantOrderDetailsActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", customerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        if (notificationType.equals("CancelOrderByCustomer")) {
            intent = new Intent(this, MerchantOrderDetailsActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", customerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        if (notificationType.equals("OrderStatusChanged")) {
            intent = new Intent(this, CustomerOrderDetailsActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderTo", merchantUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        //set icon
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon_color);
        //notification sound
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.app_icon_color)
                .setLargeIcon(icon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSound(notificationSoundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        //show notification
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description Here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.enableVibration(true);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
