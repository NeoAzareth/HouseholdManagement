package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import com.householdmanagement.view.SettingsActivity;
import com.householdmanagement.model.DBConnection;
import com.householdmanagement.view.SignInOrUpActivity;

import java.net.URLEncoder;

/**
 * PassUpdate class is a background sync task that takes user email, current password input
 * and new password input as parameters, send them to php script for validation and update DB
 * it takes user email and password first to validate user identity, if user identity verified,
 * then use the third parameter new password to update user password in DB.
 *
 * @author  Huangxiao Lin
 * @version  1.0
 */
public class PassUpdate extends AsyncTask<String,Void,String> {

    private Context context;
    private Activity activity;
    private DBConnection con = new DBConnection();
    private Toast successToast;

    //the constructor expects context and activity for feedback
    public PassUpdate(Context context, Activity activity){
        this.activity = activity;
        this.context = context;
    }


    @Override
    protected String doInBackground(String... params) {

        //get the email from dataholder
        String email = DataHolder.getInstance().getUserEmail();
        //2 params are passed from UpdatePasswordActivity
        String password = params[0];
        String newPass = params[1];

        try {
            //start with a string and encode the values
            //this will be read by the script as $_POST data
            //e.g. here we have $_POST['email'] = email

            String updateData = URLEncoder.encode("email", "UTF-8") + "="
                    + URLEncoder.encode(email, "UTF-8");

            updateData += "&" + URLEncoder.encode("password", "UTF-8") + "="
                    + URLEncoder.encode(password,"UTF-8");

            updateData += "&" + URLEncoder.encode("newPassword", "UTF-8") + "="
                    + URLEncoder.encode(newPass,"UTF-8");


            //call the dbtransaction method and save it to a string
            String updateResult = con.dbTransaction(Links.DEFAULT,updateData);
            Log.d("check result",updateResult);

            //here we checked if the result is completed
            //if completed then password updated, encode email and password value to DataHolder
            //If return other than completed, that could mean anything from email not in
            // DB, wrong password or could not process query
            if (updateResult.equals("completed")){

                String credentials = URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(email, "UTF-8");
                credentials += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(newPass, "UTF-8");
                DataHolder.getInstance().setUserCredentials(credentials);
                DataHolder.getInstance().setUserEmail(email);
                DataHolder.getInstance().setUserPass(newPass);
                //reset user email and password into SharedPreferences
                setPreferences(email,newPass);

                //return ok to onPostExecute
                return "ok";

            }
            else {//error is returned for any other reason
                //return error message to onPostExecute
                return "Current Password Entered Is Wrong";
            }
        }
        catch (Exception e){
            Log.d("Login task",e.toString());
            //return expection message to onPostExecute
            return "Something went wrong, try again later";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        //if the result value is ok show password updated message, return to SettingActivity
        if (result.equals("ok")) {
            Validator.showToast(context.getApplicationContext(),"Password updated.");
            Intent intent=new Intent(context.getApplicationContext(),SettingsActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }

        else {
            //if the string is not ok the result is showed as a toast
            Validator.showToast(context.getApplicationContext(),result);
        }
    }

    public void setPreferences(String email,String password){
        SharedPreferences savedValues =
                activity.getSharedPreferences("SavedValues",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedValues.edit();
        editor.putString("email",email);
        editor.putString("pass",password);

        editor.commit();
        DataHolder.getInstance().setSavedValues(savedValues);
    }
}

