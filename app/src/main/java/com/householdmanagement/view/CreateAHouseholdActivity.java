package com.householdmanagement.view;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

import com.householdmanagement.model.DBConnection;
import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.Login;
import com.householdmanagement.controller.Validator;

import java.net.URLEncoder;

/**
 * This page allows user to create a new house.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class CreateAHouseholdActivity extends Activity {
    //instance fields
    private EditText householdNameEditText;
    private EditText householdRentEditText;
    private Button createButton;
    private String name,rent;
    private DBConnection con = new DBConnection();
    private RelativeLayout createHouseholdRL;
    private ProgressBar progressBar;
    private SharedPreferences savedValues;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_a_household_layout);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //get references to the widgets
        householdNameEditText = (EditText)findViewById(R.id.householdNameEditText);
        householdRentEditText = (EditText)findViewById(R.id.householdRentEditText);
        createButton = (Button)findViewById(R.id.createHouseholdButton);
        createHouseholdRL = (RelativeLayout)findViewById(R.id.create_household_rlayout);

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        createHouseholdRL.addView(progressBar);

        //set the listener for the button
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = householdNameEditText.getText().toString().trim();
                rent = householdRentEditText.getText().toString().trim();
                validateInput();
            }
        });

        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = savedValues.edit();
    }


    // exit and back
    @Override
    public void onPause() {
        name = householdNameEditText.getText().toString().trim();
        editor.putString("HouseholdName","");
        rent = householdRentEditText.getText().toString().trim();
        editor.putString("HouseholdRent","");
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        householdNameEditText.setText(savedValues.getString("HouseholdName",""));
        householdRentEditText.setText(savedValues.getString("HouseholdRent",""));

        super.onResume();
    }

    /**
     * validate input validates all fields and the runs IsHouseholdNameInUse to continue
     */
    public void validateInput(){
        if(Validator.missingInfo(name,rent)){
            Validator.showToast(getApplicationContext(),
                    "All fields are required");
        } else if (Validator.hasInvalidScapeChars(name,rent)){
            Validator.showToast(getApplicationContext()
                    ,"No extra empty lines or tab white space allowed");
        } else if (Validator.hasExtraWhiteSpaceBetweenWords(name,rent)){
            Validator.showToast(getApplicationContext()
                    ,"No more than one blank space allowed within words");
        } else if(!Validator.isValidLength(name,50)){
            Validator.showToast(getApplicationContext(),
                    "The name must be shorter than 50 characters");
        } else if (!Validator.isValidFloat(rent)){
            Validator.showToast(getApplicationContext(),
                    "Invalid rent amount");
        } else {
            new IsHouseholdNameInUse().execute(name);
        }
    }

    /***
     * Async task that checks if a given household name exists in the database
     * calls register household on success
     */
    class IsHouseholdNameInUse extends AsyncTask<String,Void,Boolean>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String hhName = params[0];

            try {
                //query
                String query = "SELECT HouseholdID FROM hhm_households " +
                        "WHERE HouseholdName ='"+ hhName+"'";

                //encode post data
                String data = URLEncoder.encode("reg", "UTF-8") + "="
                        + URLEncoder.encode(query, "UTF-8");
                data += "&" + URLEncoder.encode("verify", "UTF-8") + "="
                        + URLEncoder.encode("true","UTF-8");

                //attempt connection
                String result = con.dbTransaction(Links.REGISTER,data);

                //if the string in result is false the email is not in use in the DB
                if (result.equals("false")) {
                    return false;
                } else {
                    return true;
                }

            } catch (Exception e) {
                Log.d("IsHouseholdNameInUse",e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(result){
                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                Validator.showToast(getApplicationContext(),
                        "That name is already in use...");
            } else {
                new RegisterHousehold().execute();
            }
        }
    }

    /***
     * Async task that registers the household in the database
     * calls the login Async task on success
     */
    class RegisterHousehold extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {
            //prepare any apostrophes for database insertion
            name = name.replaceAll("'","''");
            try {
                //query
                String query = "INSERT INTO hhm_households VALUES(NULL,'"
                        + name + "','" + rent + "')";

                //encode the post data
                String data = DataHolder.getInstance().getUserCredentials();

                data += "&" + URLEncoder.encode("query","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");
                data += "&" + URLEncoder.encode("RegHH","UTF-8") + "=" +
                        URLEncoder.encode("true","UTF-8");

                //attempt connection
                String regResult = con.dbTransaction(Links.DEFAULT,data);

                if(regResult.equals("completed")){
                    return true;
                } else {
                    return false;
                }

            } catch (Exception e) {
                Log.d("Register User",e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            householdRentEditText.setText("");
            householdNameEditText.setText("");
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if(result){
                Validator.showToast(getApplicationContext(),
                        "Registration successful, redirecting to overview.");
                String email = DataHolder.getInstance().getUserEmail();
                String pass = DataHolder.getInstance().getUserPass();
                new Login(getApplicationContext(),CreateAHouseholdActivity.this,createHouseholdRL)
                        .execute(email,pass);
            }else{
                Validator.showToast(getApplicationContext(),"Registration failed...");
            }
        }
    }
}
