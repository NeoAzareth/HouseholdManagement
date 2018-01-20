package com.householdmanagement.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.Login;
import com.householdmanagement.controller.Validator;
import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

/**
 * This page is the very first page of our app. It allows users to register or log in.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class SignInOrUpActivity extends Activity implements View.OnClickListener {

    //instance fields
    private EditText emailEditText;
    private EditText passwordEditText, resetPasswordEditText;
    private TextView signUpTextView,passResetTextView;
    private Button signInButton;
    private SharedPreferences savedValues;
    private CheckBox rememberEmailCheckBox;
    private String email,pass;
    private Editor editor;
    private DBConnection con = new DBConnection();
    private AlertDialog.Builder dialog;
    private View textEntryView;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_or_up_layout);


        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = savedValues.edit();

        //dialog
        setDialogProperties("");

        //set references to widgets
        signInButton = (Button)findViewById(R.id.sign_in_button);
        emailEditText = (EditText) findViewById(R.id.email_textfield);
        passwordEditText = (EditText)findViewById(R.id.password_textfield);
        signUpTextView = (TextView)findViewById(R.id.sign_up);
        passResetTextView = (TextView)findViewById(R.id.passResetTV);
        rememberEmailCheckBox = (CheckBox)findViewById(R.id.rememberEmailCheckBox);
        relativeLayout = (RelativeLayout) findViewById(R.id.login_layout);

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        relativeLayout.addView(progressBar);

        //set the listeners
        signInButton.setOnClickListener(this);
        signUpTextView.setOnClickListener(this);
        passResetTextView.setOnClickListener(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    // exit and back
    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        email = savedValues.getString("email",null);
        pass = savedValues.getString("pass",null);

        if(email != null && pass != null){
            emailEditText.setText(email);
            passwordEditText.setText(pass);
            new Login(this,this,relativeLayout).execute(email,pass);
        }

        boolean rememberEmail = savedValues.getBoolean("rememberEmail",false);
        if(rememberEmail){
            rememberEmailCheckBox.setChecked(true);
            emailEditText.setText(email);
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.sign_up:
                Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
                break;

            case R.id.passResetTV:
                dialog.show();
                break;

            case R.id.sign_in_button:
                //on click retrieves the values from the edit text fields
                email = emailEditText.getText().toString();
                pass = passwordEditText.getText().toString();

                if (Validator.missingInfo(email,pass)){
                    Validator.showToast(this,"All fields are required");
                } else {
                    if(rememberEmailCheckBox.isChecked()){
                        editor.putBoolean("rememberEmail",true);
                        editor.commit();
                    } else {
                        editor.remove("rememberEmail");
                        editor.commit();
                    }
                    //calls the login async class to attempt login
                    new Login(this,this,relativeLayout).execute(email,pass);
                }
                break;
        }
    }

    private void validateEmail(){
        if(Validator.missingInfo(email)){
            setDialogProperties("Enter your email address!");
            dialog.show();
        } else if (!Validator.isValidEmail(email)) {
            setDialogProperties("Enter a valid email address!");
            dialog.show();
        } else {
            new IsEmailOnDb().execute(email);
        }
    }

    private void setDialogProperties(String feedback){
        dialog = new AlertDialog.Builder(SignInOrUpActivity.this);
        LayoutInflater inflater = LayoutInflater.from(SignInOrUpActivity.this);
        textEntryView = inflater.inflate(R.layout.change_rent_dialog_layout, null);
        resetPasswordEditText = (EditText)textEntryView.findViewById(R.id.change_rent_edit_text);

        dialog.setTitle("Enter your email address: ");
        dialog.setMessage(feedback);
        dialog.setCancelable(false);
        dialog.setView(textEntryView);

        dialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                email = resetPasswordEditText.getText().toString();
                validateEmail();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    /**
     * private async classisemailondb that checks if any email is already in use
     * on success calls the inner async task RegisterUser
     */
    class IsEmailOnDb extends AsyncTask<String,Void,Boolean> {

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
                } else if(result.equals("true")){
                    return true;
                }

            } catch (Exception e) {
                Log.d("isEmailOnDBAsynClass",e.toString());

            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                String newPass = randomPass();
                new ResetPassword().execute(newPass);

            } else {
                //otherwise a toast is shown
                if(progressBar != null){
                    progressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
                setDialogProperties("Email not found... try again?");
                dialog.show();
            }
        }
    }

    private String randomPass(){
        //array to store random generated chars
        ArrayList<Character> passwordChars = new ArrayList<>();
        //random object
        Random rand = new Random();
        //randomize chars 4 times
        for(int x = 1;x<=4;x++) {
            //4 upper case letters
            int randomChar = 65 + rand.nextInt(26);
            passwordChars.add((char)randomChar);
            //4 numbers
            randomChar = 48 + rand.nextInt(10);
            passwordChars.add((char)randomChar);
            //4 lower case letters
            randomChar = 97 + rand.nextInt(26);
            passwordChars.add((char)randomChar);
        }
        //initialize string to be returned
        String randomPass = "";
        //shuffle the chars and append to the string to be returned
        while(passwordChars.size() != 0){
            int randomChar = 0 + rand.nextInt(passwordChars.size());
            randomPass += passwordChars.get(randomChar);
            passwordChars.remove(randomChar);
        }

        return randomPass;
    }

    class ResetPassword extends AsyncTask<String,Void,Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String newPass = params[0];

                String data = URLEncoder.encode("newPassword","UTF-8") + "=" +
                        URLEncoder.encode(newPass,"UTF-8");
                data += "&" + URLEncoder.encode("sendEmail","UTF-8") + "=" +
                        URLEncoder.encode("true","UTF-8");
                data += "&" + URLEncoder.encode("Email","UTF-8") + "=" +
                        URLEncoder.encode(email,"UTF-8");

                String result = con.dbTransaction(Links.REGISTER,data);

                if(result.equals("completed")){
                    return true;
                } else {
                    return false;
                }

            } catch(Exception e){
                Log.d("ResetPasswordASync",e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if(result){
                Validator.showToast(getApplicationContext()
                        ,"You will receive and email with your new password shortly.");
            } else {
                Validator.showToast(getApplicationContext()
                        ,"Something went wrong, try again later...");
            }
        }
    }
}