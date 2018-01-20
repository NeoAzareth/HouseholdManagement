package com.householdmanagement.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.content.SharedPreferences.Editor;

import java.util.Calendar;

/**
 * This class provides alarm for notification.
 *
 * @author  Huangxiao Lin
 * @version  1.0
 */
public class AlarmService extends Service {
    private SharedPreferences savedValues;
    private String userStatus;

    @Override
    public void onCreate(){

        //get userStatus value from SharedPreferences
        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        userStatus = savedValues.getString("userStatus",null);

        //set up alarmTime for the alarm to trigger at a specific time
        Calendar now=Calendar.getInstance();
        Calendar alarmTime = Calendar.getInstance();
        //set alarm on a specific day of each month
        //if the date is passed, it will still alarm
        alarmTime.set(Calendar.DAY_OF_MONTH,10);
        alarmTime.set(Calendar.HOUR_OF_DAY,12);
        alarmTime.set(Calendar.MINUTE,25);

        Intent intent = new Intent(this,NotificationReceiver.class);
        //keep the second parameter under control, we will need it to cancel alarm
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Log.d("userStatus",userStatus);
        // check if userStatus is not done, if it is not done, then trigger an alarm
        if(userStatus.equals("not done")) {
            //(1)is the parameter explaination for the method, (2)is the example for make interval
            //to half day, feel free to change and test
            //(1)alarmManager.setRepeating(alarmmanager constant, trigger time,  trigger interval, pending intent);
            //(2)alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,alarmTime.getTimeInMillis(),AlarmManager.INTERVAL_HALF_DAY,pendingIntent);
            //set repeating alarm for each 10 secs for testing
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), 10 * 1000, pendingIntent);
            Log.d("set alarm", "successful");
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startID){
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        //set up the same PendingIntent for cancellation
        Intent intent = new Intent(this,NotificationReceiver.class);
        PendingIntent cancelPendingIntent=PendingIntent.getBroadcast(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(cancelPendingIntent);
        Log.d("alarm cancelled","cancelled");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

