package com.householdmanagement.controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import com.householdmanagement.R;
import com.householdmanagement.view.ManageBillActivity;
import com.householdmanagement.view.OverviewActivity;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.view.SignInOrUpActivity;

/**
 * This NotificationReceiver class sends notifications when certain events is triggered.
 *
 * @author  Huangxiao Lin
 * @version  1.0
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent repeating_intent = new Intent(context, SignInOrUpActivity.class);

        //if the application is not closed and user logged in
        if(DataHolder.getInstance().getMember()!=null){
            repeating_intent = new Intent(context, ManageBillActivity.class);
        }
        repeating_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent=PendingIntent.getActivity(context,1,repeating_intent,PendingIntent.FLAG_UPDATE_CURRENT);

        //build up notification object
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.billtrack_logo)
                .setContentTitle("Household Management")
                .setContentText("Please click Done button to finish editing bill")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        final int NOTIFICATION_ID=1;
        notificationManager.notify(NOTIFICATION_ID,notification);
    }
}

