package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * For now sync household is used to refresh the household object - which contains the entire
 * array of objects used by the app, from the database.
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public class SyncHousehold extends AsyncTask<Integer,Void,Void> {

    private Intent intent;
    private Activity activity;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;

    public SyncHousehold(Activity activity,RelativeLayout relativeLayout){
        this.activity = activity;
        this.relativeLayout = relativeLayout;
    }

    public SyncHousehold(Intent intent, Activity activity){
        this.intent = intent;
        this.activity = activity;
    }


    public SyncHousehold(Intent intent, Activity activity, RelativeLayout relativeLayout){
        this.intent = intent;
        this.activity = activity;
        this.relativeLayout = relativeLayout;
    }

    @Override
    protected void onPreExecute(){
        if(relativeLayout != null){
            progressBar = new ProgressBar(activity.getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            progressBar.setLayoutParams(params);
            progressBar.setVisibility(View.VISIBLE);
            relativeLayout.addView(progressBar);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        if(!Validator.isNetworkAvailable(activity)){
            this.cancel(true);
        }
    }

    @Override
    protected Void doInBackground(Integer... params) {
        //expects the first parameter to be the household id
        int hhID = params[0];
        //calls the static method on the AppObjectBuilder to build the object from the db
        AppObjectBuilder.buildHouseholdObject(hhID);
        return null;
    }

    @Override
    protected void onCancelled(){
        removeProgressBar();
        Validator.showToast(activity.getApplicationContext()
                ,"There is no network connection \n" +
                        "Please check your settings and try again...");
    }

    @Override
    protected void onPostExecute(Void v){
        removeProgressBar();
        if(activity != null && intent != null){
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        } else {

        }
    }

    public void removeProgressBar(){
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}