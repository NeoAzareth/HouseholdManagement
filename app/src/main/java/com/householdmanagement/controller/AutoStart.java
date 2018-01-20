package com.householdmanagement.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This AutoStart class to receive broadcast when system reboot in order to start alarm service
 *
 * @author  Huangxiao Lin
 * @version  1.0
 */
public class AutoStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent servicenIntent = new Intent(context,AlarmService.class);
        context.startService(servicenIntent);
    }
}