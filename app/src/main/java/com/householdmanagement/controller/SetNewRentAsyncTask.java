package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.householdmanagement.model.DBConnection;
import com.householdmanagement.view.AdminActivity;

import java.net.URLEncoder;

/**
 * This class set new house rent using asynchronously.
 *
 * @author  Sicheng Zhu
 * @version  1.0
 */
public class SetNewRentAsyncTask extends AsyncTask<String,Void,String> {
    private Context context;
    private Activity activity;
    private DBConnection conn = new DBConnection();
    private String householdID;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;

    public SetNewRentAsyncTask(Context context, Activity activity, RelativeLayout relativeLayout) {
        this.context = context;
        this.activity = activity;
        this.relativeLayout = relativeLayout;
    }

    @Override
    protected String doInBackground(String... params) {
        householdID = params[0];
        String newAmount = params[1];

        try {
            String query = "UPDATE hhm_households SET HhRentAmount = " + newAmount +
                    " WHERE HouseholdID = " + householdID;

            String rows = "0";

            //postdata
            String data = URLEncoder.encode("query", "UTF-8") + "="
                    + URLEncoder.encode(query,"UTF-8");
            data += "&" + URLEncoder.encode("rows", "UTF-8") + "="
                    + URLEncoder.encode(rows,"UTF-8");
            data += "&" + DataHolder.getInstance().getUserCredentials();

            //call the dbtransaction method and save it to a string
            String result = conn.dbTransaction(Links.DEFAULT,data);

            //here we checked the line is not null and the value is not false
            //false means the transaction failed
            //that could mean anything from email not in DB, wrong password or could not process query
            if(result == null)
                return "error";
            else if(result.equals("completed"))
                //returns completed to handle the post processing
                return "completed";
            else if(result.equals("false"))
                return "wrong";
            else if(result.equals("failed"))
                return "failed";
            else //error is returned for any other reason
                return "error";
        } catch (Exception e){
            Log.d("Update new rent",e.toString());
            return "error";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if(relativeLayout != null) {
            progressBar = new ProgressBar(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            progressBar.setLayoutParams(params);
            progressBar.setVisibility(View.VISIBLE);
            relativeLayout.addView(progressBar);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        if (result.equals("completed")) {
            Validator.showToast(context.getApplicationContext(),
                    "Update new rent succeed.");
            AppObjectBuilder.getInstance().buildHouseholdObject(Integer.parseInt(householdID));

        } else
            Validator.showToast(context.getApplicationContext(),
                    "Something went wrong, please try later.");
    }
}
