package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;

import com.householdmanagement.R;
import com.householdmanagement.model.DBConnection;
import com.householdmanagement.view.NewUserSelectionActivity;
import com.householdmanagement.view.OverviewActivity;

import java.net.URLEncoder;

/**
 * login class takes string as parameters and returns an string. It attempts to retrieve a user id
 * and household id as prof that the given email and password exists in the database.
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public class Login extends AsyncTask<String,Void,String> {

    private Context context;
    private Activity activity;
    private DBConnection con = new DBConnection();
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;

    //the constructor expects context and activity for feedback
    public Login(Context context, Activity activity){
        this.activity = activity;
        this.context = context;
    }

    public Login(Context context, Activity activity,RelativeLayout relativeLayout){
        this.activity = activity;
        this.context = context;
        this.relativeLayout = relativeLayout;
    }

    @Override
    protected void onPreExecute(){

        if(relativeLayout != null){
            progressBar = new ProgressBar(context);
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
    protected String doInBackground(String... params) {



        //get the email and password
        String email = params[0];
        String password = params[1];

        //all the interactions with the db must be enclosed on try and catch
        try {

            //define a string for the query
            String query = "SELECT UserID, HouseholdID FROM hhm_users " +
                    "WHERE Email ='"+ email+"'";//notice how email is enclosed by single quotes
            //that is necessary so that parameter is treated as a string by mysql

            //rows is only used for retrieval
            //this means that you expect several rows as a result
            //pass "1" for many and any other value for only one row
            String rows = "0";

            //post data is define as below
            //start with a string and encode the values
            //this will be read by the script as $_POST data
            //e.g. here we have $_POST['email'] = email
            String data = URLEncoder.encode("email", "UTF-8") + "="
                    + URLEncoder.encode(email, "UTF-8");

            data += "&" + URLEncoder.encode("password", "UTF-8") + "="
                    + URLEncoder.encode(password,"UTF-8");

            data += "&" + URLEncoder.encode("retrieve", "UTF-8") + "="
                    + URLEncoder.encode(query,"UTF-8");

            data += "&" + URLEncoder.encode("rows", "UTF-8") + "="
                    + URLEncoder.encode(rows,"UTF-8");

            //call the dbtransaction method and save it to a string
            String line = con.dbTransaction(Links.RETRIEVE,data);

            //here we checked the line is not null and the value is not false
            //false means the transaction failed
            //that could mean anything from email not in DB, wrong password or could not process query
            if (line != null && !line.equals("false")
                    && !line.equals("failed") && !line.equals("no records")){
                //in here we expect a string like: 1-c-1
                //where the first value is the userid and the second is the householdid
                //the data is separeted into an array
                String[] info = line.split("-c-");
                //now the userid is in info[0] which we set to the int variable userid
                int userID = Integer.parseInt(info[0]);
                //and the householdid is in info[1] which we set to the int variable householdid

                //this part was added later
                //an error is produced when the user does not belong to a houshold
                //the way this error is fixed is by catching the exception which means the
                //user has not household id in the database
                int householdID;
                try{
                    householdID =Integer.parseInt(info[1]);
                } catch (Exception ArrayIndexOutOfBoundsException) {
                    householdID = 0;
                }

                //these two values are saved into the DataHolder class so that they can be accessed
                //by all activities
                DataHolder.getInstance().setUserID(userID);
                DataHolder.getInstance().setHouseholdID(householdID);
                //if successful the user credentials are encoded and saved in the dataholder class
                //as well. this is also necessary because CRUD PHP transactions require user authorization
                String credentials = URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(email, "UTF-8");
                credentials += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(password,"UTF-8");
                DataHolder.getInstance().setUserCredentials(credentials);
                DataHolder.getInstance().setUserEmail(email);
                DataHolder.getInstance().setUserPass(password);

                setPreferences(email,password);

                //here we build a household object if the household id is not equals 0
                //which means the user it is not associated with household
                if (householdID != 0) {
                    AppObjectBuilder.buildHouseholdObject(householdID);
                } else if (userID != 0) {
                    //otherwise build member is called
                    AppObjectBuilder.buildMemberObject(userID);
                } else {
                    //if nothing can be created a message is returned
                    return "Something went wrong";
                }

                //returns ok to handle the post processing
                return "ok";

            } else {//error is returned for any other reason
                return "Wrong email and/or password";
            }

        } catch (Exception e){
            Log.d("Login task",e.toString());
            return "Something went wrong, try again later";
        }
    }

    @Override
    protected void onCancelled(){
        removeProgressBar();
        Validator.showToast(context,"There is no network connection \n" +
                "Please check your settings and try again...");
    }

    @Override
    protected void onPostExecute(String result) {
        removeProgressBar();

        //if the value is ok
        if (result.equals("ok")) {

            //the household id is retrieve from the dataholder
            int householdID = DataHolder.getInstance().getHouseholdID();
            Member member = DataHolder.getInstance().getMember();


            //if the value is not 0 the overview activity is called
            if (householdID != 0 && !member.getUserStatus().equals("pending")) {
                //save userstatus to sharepreferences
                //We probably can wrap all these code to a class later
                //and create method to save different data respectively
                SharedPreferences savedValues =
                        activity.getSharedPreferences("SavedValues",Context.MODE_PRIVATE);
                Editor editor = savedValues.edit();
                editor.putString("userStatus",DataHolder.getInstance().getMember().getUserStatus());

                editor.commit();
                Intent intent=new Intent(context.getApplicationContext(),OverviewActivity.class);
                activity.startActivity(intent);
            } else {//otherwise the newuserselection activity is called
                Intent intent=new Intent(context.getApplicationContext(),NewUserSelectionActivity.class);
                //set flag for clearing activity stack to prevent user go back to overview or other activities
                //after delete household or leave household
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }

        } else {
            //if the string is not ok the result is showed as a toast
            Validator.showToast(context.getApplicationContext(),result);
        }

    }

    private void removeProgressBar(){
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    public void setPreferences(String email,String password){
        SharedPreferences savedValues =
                activity.getSharedPreferences("SavedValues",Context.MODE_PRIVATE);
        Editor editor = savedValues.edit();
        editor.putString("email",email);
        editor.putString("pass",password);

        editor.commit();
        DataHolder.getInstance().setSavedValues(savedValues);
    }
}


