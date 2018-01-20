package com.householdmanagement.view;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.householdmanagement.R;
import com.householdmanagement.model.DBConnection;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.Login;
import com.householdmanagement.controller.Patterns;
import com.householdmanagement.controller.Validator;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This page allows user to register an account.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class RegisterActivity extends Activity {

    //instance fields
    private EditText firstnameTextField;
    private EditText lastnameTextField;
    private EditText emailTextField;
    private EditText passwordTextField;
    private EditText confirmPasswordTextField;
    private Button registerButton;
    private String firstName,lastName,email,pass,conPass;
    private DBConnection con = new DBConnection();
    private RelativeLayout registrationRL;
    private ProgressBar progressBar;
    private SharedPreferences savedValues;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        //set references to widgets
        firstnameTextField = (EditText)findViewById(R.id.first_name_editview) ;
        lastnameTextField = (EditText)findViewById(R.id.last_name_editview);
        emailTextField = (EditText)findViewById(R.id.email_editview);
        passwordTextField = (EditText)findViewById(R.id.password_editview);
        confirmPasswordTextField = (EditText)findViewById(R.id.confirm_password_editview);
        registerButton = (Button)findViewById(R.id.register_button);
        registrationRL = (RelativeLayout)findViewById(R.id.register_relative_layout);


        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        registrationRL.addView(progressBar);


        //set the listener
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//anonymous listener

                //retrieve fields
                firstName = firstnameTextField.getText().toString();
                lastName = lastnameTextField.getText().toString();
                email = emailTextField.getText().toString();
                pass = passwordTextField.getText().toString();
                conPass = confirmPasswordTextField.getText().toString();

                //call validate input
                validateInput();
            }
        });

        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = savedValues.edit();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // exit and back
    @Override
    public void onPause() {
        firstName = firstnameTextField.getText().toString();
        lastName = lastnameTextField.getText().toString();
        email = emailTextField.getText().toString();
        editor.putString("FirstName",firstName);
        editor.putString("LastName",lastName);
        editor.putString("RegEmail",email);
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        firstnameTextField.setText(savedValues.getString("FirstName",""));
        lastnameTextField.setText(savedValues.getString("LastName",""));
        emailTextField.setText(savedValues.getString("RegEmail",""));
    }

    /***
     * validate the user input
     */
    public void validateInput(){
        if (Validator.missingInfo(firstName,lastName,email,pass,conPass)) {
            //check if any of the fields are empty
            Validator.showToast(getApplicationContext(),"All fields are required!");
        } else if (!Validator.isValidPattern(Patterns.FIRST_NAME,firstName)) {
            Validator.showToast(getApplicationContext(),"Invalid characters on First name!");
        } else if (!Validator.isValidPattern(Patterns.LAST_NAME,lastName)){
            Validator.showToast(getApplicationContext(),"Invalid characters on Last name!");
        } else if (!Validator.isValidEmail(email)){
            Validator.showToast(getApplicationContext(),"Invalid email address");
        } else if (!Validator.isValidPattern(Patterns.PASSWORD,pass)) {
            Validator.showToast(getApplicationContext(),
                    "Password must contain: \n" +
                            "At least 8 characters long \n" +
                            "At least 1 number \n" +
                            "At least 1 upper case letter \n" +
                            "At least 1 lower case letter \n" +
                            "No whitespace allowed");
        } else if (!Validator.passwordsMatch(pass,conPass)) {//check if the passwords match
            Validator.showToast(getApplicationContext(),"Passwords don't match");
        } else {//check if the email is already in use
            new isEmailOnDb().execute(email);
        }
    }

    /**
     * private async classisemailondb that checks if any email is already in use
     * on success calls the inner async task RegisterUser
     */
    class isEmailOnDb extends AsyncTask<String,Void,Boolean>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            //retrieve email
            String userEmail = params[0];

            try {
                //query
                String query = "SELECT UserID FROM hhm_users " +
                        "WHERE Email ='"+ userEmail+"'";

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
                Log.d("isEmailOnDBAsynClass",e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {//if the boolean is true we proceed with registration
                //and call register User
                new registerUser().execute();
            } else {
                //otherwise a toast is shown
                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                Validator.showToast(getApplicationContext(),"Invalid email or already registered.");
            }
        }
    }

    /***
     * register user runs as asynctask and attempts to register a new user into the database
     * on success calls the Login class
     */
    class registerUser extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                //encode the post data
                String data = URLEncoder.encode("LastName","UTF-8") + "="
                        + URLEncoder.encode(lastName,"UTF-8");
                data += "&" + URLEncoder.encode("FirstName","UTF-8") + "="
                        + URLEncoder.encode(firstName,"UTF-8");
                data += "&" + URLEncoder.encode("Email","UTF-8") + "="
                        + URLEncoder.encode(email,"UTF-8");
                data += "&" + URLEncoder.encode("UserPW","UTF-8") + "="
                        + URLEncoder.encode(pass,"UTF-8");

                //attempt connection
                String regResult = con.dbTransaction(Links.REGISTER,data);

                //if the value is success true is returned
                if (regResult.equals("success")) {
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
        protected void onPostExecute(Boolean result) {
            firstnameTextField.setText("");
            lastnameTextField.setText("");
            emailTextField.setText("");
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if(result){//if true is received a toast is shown and login and redirect user is called
                Validator.showToast(getApplicationContext(),"Registration Complete.");
                new Login(getApplicationContext(),RegisterActivity.this,registrationRL)
                        .execute(email,pass);
            } else {
                Validator.showToast(getApplicationContext(),"Registration failed, try again later.");
            }
        }
    }
}