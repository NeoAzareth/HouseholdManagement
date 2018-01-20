package com.householdmanagement.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.Login;
import com.householdmanagement.controller.Member;
import com.householdmanagement.controller.Validator;
import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;

/**
 * This page let user to type in household name he/she wants to join.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class JoinAHouseholdActivity extends Activity {
    //instance fields
    private EditText householdName;
    private Button joinButton;
    private String name;
    private DBConnection con = new DBConnection();
    private Member user = DataHolder.getInstance().getMember();
    private RelativeLayout joinHouseholdRL;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_a_household_layout);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //get reference to widgets
        householdName = (EditText)findViewById(R.id.joinHouseholdNameEV);
        joinButton = (Button)findViewById(R.id.joinHouseholdButton);
        joinHouseholdRL = (RelativeLayout)findViewById(R.id.join_household_rlayout);

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        joinHouseholdRL.addView(progressBar);

        //set the listener
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = householdName.getText().toString().trim();
                //no extensive validation is required because the class will check if the
                //household name exists on the database
                if(Validator.isNullOrEmpty(name)){
                    Validator.showToast(getApplicationContext(),"Enter the name of the household!");
                } else if (!Validator.isValidLength(name,50)) {
                    Validator.showToast(getApplicationContext(),"Invalid household name length! " +
                            "must be less than 50");
                } else {
                    //calls the CheckAndRetrieveHHID method
                    new CheckAndRetrieveHHID().execute();
                }
            }
        });

    }

    // exit and back
    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    /***
     * Checks if the household name exists by retrieving its id
     * if succeeds Calls the UpdateUserInfo
     */
    class CheckAndRetrieveHHID extends AsyncTask<Void,Void,Integer>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try{
                //query
                String query = "SELECT HouseholdID FROM hhm_households "+
                        "WHERE HouseholdName = '" + name +"'";

                //postData
                String data = DataHolder.getInstance().getUserCredentials();
                data += "&" + URLEncoder.encode("retrieve","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");

                //attempts connection
                String result = con.dbTransaction(Links.RETRIEVE,data);

                if(result != null && !result.equals("false")
                        && !result.equals("failed") && !result.equals("no records")){
                    String[] info = result.split("-c-");
                    int hhID;
                    try{
                        hhID = Integer.parseInt(info[0]);
                    } catch (Exception ArrayIndexOutOfBoundsException){
                        hhID = 0;
                    }
                    return hhID;
                } else {
                    Log.d("retrieve hh id","failed");
                }
            } catch (Exception e) {
                Log.d("CheckAndRetrieveHHID",e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer id){
            if(id != 0){
                new UpdateUserInfo().execute(id);
            } else {
                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                Validator.showToast(getApplicationContext(),"Household not found...");
            }
        }
    }

    /***
     * Updates the user info to the given id and the status to pending
     */
    class UpdateUserInfo extends AsyncTask<Integer,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Integer... params) {

            try {
                int hhID = params[0];

                //query
                String query = "UPDATE hhm_users SET UserStatus = 'pending', "+
                        "HouseholdID = "+ hhID+" "+
                        "WHERE UserID = "+ user.getUserID();

                //postdata
                String data = DataHolder.getInstance().getUserCredentials();
                data += "&" + URLEncoder.encode("query","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");

                //attempts connection
                String result = con.dbTransaction(Links.DEFAULT,data);

                if(result.equals("completed")){
                    return true;
                } else {
                    return false;
                }

            } catch (Exception e){
                Log.d("UpdateUserInfo",e.toString());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if(result){
                Validator.showToast(getApplicationContext(),
                        "You have been added, pending approval.");
                String email = DataHolder.getInstance().getUserEmail();
                String pass = DataHolder.getInstance().getUserPass();
                new Login(getApplicationContext(),JoinAHouseholdActivity.this,
                        joinHouseholdRL).execute(email,pass);
            } else {
                Log.d("UpdateUserInfo","Failed to update status and hhid");
                Validator.showToast(getApplicationContext(),"Something went wrong... Try again later.");
            }
        }
    }
}