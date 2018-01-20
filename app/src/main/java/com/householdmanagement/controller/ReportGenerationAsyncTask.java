package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.householdmanagement.model.DBConnection;
import com.householdmanagement.view.ReportActivity;

import java.net.URLEncoder;

/**
 * This class interact with database. Get data from ReportGenerationActivity, handle query of
 * database, and pass query results to ReportActivity.
 *
 * @author  Sicheng Zhu
 * @version  1.0
 */
public class ReportGenerationAsyncTask extends AsyncTask<String,Void,String>{

    private Context context;
    private Activity activity;
    private String query;
    private DBConnection conn = new DBConnection();
    private String data;
    private String[] record;
    private String[][] field = null;

    // Constructor to get context and activity from ReportGenerationActivity class
    public ReportGenerationAsyncTask(Context context, Activity activity){
        this.activity = activity;
        this.context = context;
    }

    // As a background task, a new thread is created to run this method
    // This method takes parameters from ReportGenerationActivity, query database based on
    // parameters, and pass various strings to onPostExecute depends on query results
    @Override
    protected String doInBackground(String... params) {

        // Get parameters from ReportGenerationActivity for query
        String householdID = params[0];
        String userID = params[1];
        String billCategory = params[2];
        String billDate = params[3];

        try {
            // If user selects all members and all category as criteria, use the following SQL statement
            if(userID.equals("-1") && billCategory.equals("All"))
                query = "SELECT u.FirstName, u.LastName, b.BillAmount, b.BillDesc FROM hhm_bills b " +
                        "INNER JOIN hhm_users u ON u.UserID = b.UserID " +
                        "WHERE b.HouseholdID = "+ householdID +" AND b.BillDate LIKE '" + billDate + "%'";

            // If user selects all members and one specific category as criteria, use the following SQL statement
            else if(userID.equals("-1") && !billCategory.equals("All"))
                query = "SELECT u.FirstName, u.LastName, b.BillAmount, b.BillDesc FROM hhm_bills b " +
                        "INNER JOIN hhm_users u ON u.UserID = b.UserID " +
                        "WHERE b.HouseholdID = "+ householdID +" AND b.BillDate LIKE '" + billDate + "%' "
                        + "AND b.BillCategory = '" + billCategory + "'";

            // If user selects one specific member and all category as criteria, use the following SQL statement
            else if(!userID.equals("-1") && billCategory.equals("All"))
                query = "SELECT u.FirstName, u.LastName, b.BillAmount, b.BillDesc FROM hhm_bills b " +
                        "INNER JOIN hhm_users u ON u.UserID = b.UserID " +
                        "WHERE b.HouseholdID = "+ householdID +" AND b.BillDate LIKE '" + billDate + "%' "
                        + "AND b.UserID = " + userID;

            // If user selects one specific member and a specific category as criteria, use the following SQL statement
            else if(!userID.equals("-1") && !billCategory.equals("All"))
                query = "SELECT u.FirstName, u.LastName, b.BillAmount, b.BillDesc FROM hhm_bills b " +
                        "INNER JOIN hhm_users u ON u.UserID = b.UserID " +
                        "WHERE b.HouseholdID = "+ householdID +" AND b.BillDate LIKE '" + billDate + "%' "
                        + "AND b.BillCategory = '" + billCategory + "' AND b.UserID = " + userID;

            // If parameters passed from ReportGeneration are not expected, pass error message to
            // onPostExecute method
            else
                return "Something went wrong, try again later";

            // The expected result could be more than one records, so pass 1 as argument to PHP script
            String rows = "1";

            //post data is define as below
            //start with a string and encode the values
            //this will be read by the script as $_POST data
            //e.g. here we have $_POST['email'] = email
            data = URLEncoder.encode("retrieve", "UTF-8") + "="
                + URLEncoder.encode(query,"UTF-8");
            data += "&" + URLEncoder.encode("rows", "UTF-8") + "="
                + URLEncoder.encode(rows,"UTF-8");
            data += "&" + DataHolder.getInstance().getUserCredentials();

            // call the dbTransaction method and save database records if any to a string
            String records = conn.dbTransaction(Links.RETRIEVE,data);
            System.out.println(records);
            // here we checked the records is not null and the value is not equal to no record, false,and failed
            // no record means no record found in database match user's report criteria
            // failed means something went wrong with querying database
            // false could mean anything from email not in DB, wrong password or could not process query
            if (records != null && records != "" && !records.equals("no records") &&
            !records.equals("failed") && !records.equals("false")){
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
                field = new String[record.length][4];

                if(record != null)
                    for(int i = 0; i < record.length; i++)
                        field[i] = record[i].split("-c-");

                //returns ok to handle the post processing
                return "ok";

            // If no records match user's criteria, send "empty" message to onPostExecute method
            } else if (records.equals("no records")) {
                record = null;
                return "empty";
            }

            // error is returned for any other reason
            else
                return "Something went wrong, try again later";

        // error is returned for exceptions
        } catch (Exception e){
            Log.d("Report Generation task",e.toString());
            return "Something went wrong, try again later";
        }
    }

    // This method will handle the result of doInBackground method
    // If the query to database succeed,pass query result to ReportActivity, otherwise,
    // show a toast on ReportGenerationActivity
    @Override
    protected void onPostExecute(String result) {
        // If there are some records in database match user's criteria, send records to ReportActivity
        if (result.equals("ok")) {
            Intent intent = new Intent(context.getApplicationContext(), ReportActivity.class);

            Bundle bundle = new Bundle();
            bundle.putSerializable("field", field);
            intent.putExtras(bundle);

            activity.startActivity(intent);
        }

        // If no record found, show a toast message on ReportGenerationActivity
        else if(result.equals("empty"))
            Validator.showToast(context.getApplicationContext(),
                    "No record found. Please try other criteria.");

        // If there are anything wrong, show a toast message on ReportGenerationActivity
        else
            Validator.showToast(context.getApplicationContext(), result);
    }
}
