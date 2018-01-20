package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.householdmanagement.model.DBConnection;
import com.householdmanagement.view.LeaveOrDeleteActivity;

import java.net.URLEncoder;

/**
 * HandleDeleteOrLeave class handles user deletes household or leave household action
 * in the background.
 * <p>
 * Pass intent to new user selection page and instantiate login if household delete
 * successfully.
 * <p>
 * Pop up error message if failed to leave or delete household.
 *
 * @author  Huangxiao Lin
 * @version  1.0
 */
public class HandleDeleteOrLeave extends AsyncTask<String,Void,String>{

    private Context context;
    private Activity activity;
    private DBConnection con = new DBConnection();

    //the constructor expects context and activity for feedback
    public HandleDeleteOrLeave(Context context, Activity activity){
        this.activity = activity;
        this.context = context;
    }


    @Override
    protected String doInBackground(String... params) {

        //get the email from dataholder
        String email = DataHolder.getInstance().getUserEmail();
        //2 params are passed from LeaveOrDelete Activity

        //user password
        String password = params[0];
        //user level
        String userLevel = params[1];

        if(userLevel.equals("admin")){
            try {

                //set householdID to null, user level to member where UserID=user's ID
                // Then delete household and its foreign key referenced bill table
                String query = "UPDATE hhm_users "+
                        "SET UserStatus = 'not in', "+
                        "HouseholdID = NULL, UserLevel = 'member' "+
                        "WHERE HouseholdID = " + DataHolder.getInstance().getHouseholdID()+";"+
                        "DELETE from hhm_households WHERE HouseholdID = "+
                        DataHolder.getInstance().getHouseholdID();

                //start with a string and encode the values
                //this will be read by the script as $_POST data
                //e.g. here we have $_POST['email'] = email

                String updateData = URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(email, "UTF-8");

                updateData += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(password,"UTF-8");

                updateData += "&" + URLEncoder.encode("query", "UTF-8") + "="
                        + URLEncoder.encode(query,"UTF-8");



                //call the dbtransaction method and save it to a string
                String updateResult = con.dbTransaction(Links.DEFAULT,updateData);
                System.out.println(updateResult);

                //here we checked if the result is completed
                //if completed then user leave or delete the household successfully, pass "ok to onPostExecute"
                //If return other than completed, that could mean anything from email not in
                // DB, wrong password or could not process query
                if (updateResult.equals("completed")){

                    //return ok to onPostExecute
                    return "ok";

                }
                else {//error is returned for any other reason
                    //return error message to onPostExecute
                    return "Password Entered Is Wrong";
                }
            }
            catch (Exception e){
                Log.d("Login task",e.toString());
                //return expection message to onPostExecute
                return "Something went wrong, try again later";
            }

        }
        else{
            try {
                //if not an admin, set user status to not in, householdID to null
                String query = "UPDATE hhm_users "+
                        "SET UserStatus = 'not in', "+
                        "HouseholdID = NULL "+
                        "WHERE UserID = " + DataHolder.getInstance().getUserID()+";"+
                        "DELETE from hhm_bills WHERE UserID = "+
                        DataHolder.getInstance().getUserID();

                //start with a string and encode the values
                //this will be read by the script as $_POST data
                //e.g. here we have $_POST['email'] = email

                String updateData = URLEncoder.encode("email", "UTF-8") + "="
                        + URLEncoder.encode(email, "UTF-8");

                updateData += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(password,"UTF-8");

                updateData += "&" + URLEncoder.encode("query", "UTF-8") + "="
                        + URLEncoder.encode(query,"UTF-8");


                //call the dbtransaction method and save it to a string
                String updateResult = con.dbTransaction(Links.DEFAULT,updateData);

                //here we checked if the result is completed
                //if completed then user leave or delete the household successfully, pass "ok to onPostExecute"
                //If return other than completed, that could mean anything from email not in
                // DB, wrong password or could not process query
                if (updateResult.equals("completed")){



                    //return ok to onPostExecute
                    return "ok";

                }
                else {//error is returned for any other reason
                    //return error message to onPostExecute
                    return "Password Entered Is Wrong";
                }
            }
            catch (Exception e){
                Log.d("Login task",e.toString());
                //return expection message to onPostExecute
                return "Something went wrong, try again later";
            }
        }


    }

    @Override
    protected void onPostExecute(String result) {
        //if the result value is ok show user is no longer in a household, call login to get user updated info
        //to dataholder
        if (result.equals("ok")) {
            Validator.showToast(context.getApplicationContext(),"You Are Alone Now!");
            //stop notification service here
            Intent alarmIntent=new Intent(activity, AlarmService.class);
            context.stopService(alarmIntent);
            new Login(context,activity).execute(DataHolder.getInstance().getUserEmail(),DataHolder.getInstance().getUserPass());
            //kill the activity to prevent user coming back
            activity.finish();

        }

        else {
            //if the string is not ok the result is showed as a toast
            Validator.showToast(context.getApplicationContext(),result);
        }
    }
}

