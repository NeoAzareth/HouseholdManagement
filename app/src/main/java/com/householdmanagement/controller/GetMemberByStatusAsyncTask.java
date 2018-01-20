package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.householdmanagement.model.DBConnection;
import com.householdmanagement.view.AddMemberActivity;
import com.householdmanagement.view.DeleteMemberActivity;
import com.householdmanagement.view.ResetMemberActivity;

import java.net.URLEncoder;

/**
 * This class get all potential members who want to join the same household,or get all current
 * members.Once get expected list of members, admin can change potential members' status or delete
 * member.
 *
 * @author  Sicheng Zhu
 * @version  1.0
 */
public class GetMemberByStatusAsyncTask extends AsyncTask<String,Void,String> {
    private DBConnection conn = new DBConnection();
    private String query;
    private String[] record;
    private String[][] field = null;
    private Context context;
    private Activity activity;
    private Intent intent;
    private Bundle bundle;
    private String userStatus;

    public GetMemberByStatusAsyncTask(Context context, Activity activity) {
        this.activity = activity;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        //get the householdID and userStatus
        String householdID = params[0];
        userStatus = params[1];

        //all the interactions with the db must be enclosed on try and catch
        try {

            //define a string for the query
            // select all members from the same household
            if (userStatus.equals(""))
                query = "SELECT FirstName, LastName, Email FROM hhm_users " +
                    "WHERE UserLevel <> 'admin' AND UserStatus NOT IN('pending','not in') " +
                        "AND HouseholdID = " + householdID;
//                query = "SELECT FirstName, LastName, Email, userStatus, HouseholdID FROM sm16_users " +
//                    "WHERE HouseholdID = " + householdID;

                //that is necessary so that parameter is treated as a string by mysql

            else if (userStatus.equals("pending"))
                query = "SELECT FirstName, LastName, Email FROM hhm_users " +
//                                                "WHERE HouseholdID = " + householdID;
                        "WHERE HouseholdID = " + householdID + " AND UserStatus = '" + userStatus +"'";

            else if (userStatus.equals("done"))
                query = "SELECT FirstName, LastName, Email, UserStatus FROM hhm_users " +
                        "WHERE HouseholdID = " + householdID + " AND UserStatus = '" + userStatus +"'";
//                query = "SELECT Email FROM sm16_users " +

            else
                return "parameter error";

            //rows is only used for retrieval
            //this means that you expect several rows as a result
            //pass "1" for many and any other value for only one row
            String rows = "1";

            //post data is define as below
            //start with a string and encode the values
            //this will be read by the script as $_POST data
            //e.g. here we have $_POST['email'] = email
            String data = URLEncoder.encode("retrieve", "UTF-8") + "="
                    + URLEncoder.encode(query,"UTF-8");
            data += "&" + URLEncoder.encode("rows", "UTF-8") + "="
                    + URLEncoder.encode(rows,"UTF-8");
            data += "&" + DataHolder.getInstance().getUserCredentials();

            //call the dbtransaction method and save it to a string
            String records = conn.dbTransaction(Links.RETRIEVE,data);
            System.out.println(records);
            //here we checked the line is not null and the value is not false
            //false means the transaction failed
            //that could mean anything from email not in DB, wrong password or could not process query
            if (records != null && records != "" && !records.equals("no records") &&
                    !records.equals("failed") && !records.equals("false")) {
                // records are all records in database match user's criteria
                // record is one row of matched record in database
                // in here we expect a string like: 1-r-2-r-3
                // where three database records are concatenated by -r-
                // the records string is separated into record array
                record = records.split("-r-");

                // field is two dimensional array stores fields of database records
                // each record has four fields, like 1-c-2-c-3-c-4
                // where four database fields are concatenated by -c-
                // the record string is further separated into field array

                if(userStatus.equals("") || userStatus.equals("pending"))
                    field = new String[record.length][3];
                else if(userStatus.equals("done"))
                    field = new String[record.length][4];
                else
                    return "userStatus error";

                if(record != null)
                    for(int i = 0; i < record.length; i++)
                        field[i] = record[i].split("-c-");

                //returns ok to handle the post processing
                return "ok";

                // If no records match user's criteria, send "empty" message to onPostExecute method
            } else if (records.equals("no records")) {
                field = null;
                return "empty";
            }

            // error is returned for any other reason
            else
                return "error";

        } catch (Exception e){
            Log.d("Get member(status) task",e.toString());
            return "exception";
        }
    }

    // This method will handle the result of doInBackground method
    // If the query to database succeed,pass query result to ReportActivity, otherwise,
    // show a toast on ReportGenerationActivity
    @Override
    protected void onPostExecute(String result) {
        // If there are some records in database or no record
        // match user's criteria, send records to ReportActivity
        if (result.equals("ok") || result.equals("empty")) {
            if(userStatus == "")
                intent = new Intent(context.getApplicationContext(), DeleteMemberActivity.class);

            else if(userStatus.equals("done"))
                intent = new Intent(context.getApplicationContext(), ResetMemberActivity.class);

            else if(userStatus.equals("pending"))
                intent = new Intent(context.getApplicationContext(), AddMemberActivity.class);

            bundle = new Bundle();
            bundle.putSerializable("field", field);
            intent.putExtras(bundle);

            activity.startActivity(intent);
        }

        // If there are anything wrong, show a toast message on ReportGenerationActivity
        else
            Validator.showToast(context.getApplicationContext(), result);
    }
}
