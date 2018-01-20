package com.householdmanagement.controller;

import android.util.Log;

import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * The AppObjectBuilder class is used to store user information in an object, just like "cookie" of
 * web application.
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public class AppObjectBuilder {

    private static DBConnection dbConnection = new DBConnection();
    private static final AppObjectBuilder builder = new AppObjectBuilder();
    public static AppObjectBuilder getInstance() {return builder;}

    /***
     * retrieve members is a method that uses the connection class in order to retrieve all members
     * in the database associated with the parameter householdid
     * @param householdID
     * @return array of members
     */
    public static ArrayList<Member> retrieveMembers(int householdID){
        ArrayList<Member> members = new ArrayList<>();

        try {

            //query
            String query = "SELECT UserID, LastName, FirstName, UserLevel, UserStatus " +
                    "FROM hhm_users WHERE HouseholdID ="+ householdID;

            //the "1" is used to tell the PHP script we expect several rows as a result
            String rows = "1";

            //encode the post values
            String data = URLEncoder.encode("retrieve","UTF-8") + "="
                    + URLEncoder.encode(query,"UTF-8");
            data += "&" +URLEncoder.encode("rows","UTF-8") + "="
                    + URLEncoder.encode(rows,"UTf-8");
            data +=  "&" + DataHolder.getInstance().getUserCredentials();

            //dbtransaction is called
            String result = dbConnection.dbTransaction(Links.RETRIEVE,data);

            //if the result is not null or the string is equals to false
            if(result != null && !result.equals("false")
                    && !result.equals("failed") && !result.equals("no records")){
                //the result is separated in a new array of string in which each element is a row
                String[] lines = result.split("-r-");
                //loop through each element to create Member objects
                for (String line:lines
                        ) {
                    //prepare object
                    Member member;
                    // the element is separated in another array of string in which each element is
                    //a column
                    String[] info = line.split("-c-");
                    //first element in the info array is the user id
                    int id = Integer.parseInt(info[0]);
                    //second element is the last name
                    String ln = info[1];
                    //third element is the first name
                    String fn = info[2];
                    //fourth element is the user level
                    String ul = info[3];
                    //fifth element is the user status
                    String us = info[4];
                    //finally we call the retrieveBills method and pass it the user id to retrieve
                    //the user bills
                    ArrayList<Bill> bills = retrieveBills(id);

                    //if the user level is admin the user is instantiated as an admin
                    if(ul.equals("admin")){
                        member = new Admin(id,ln,fn,ul,us,bills);
                    } else {
                        member = new Member(id,ln,fn,ul,us,bills);
                    }
                    // in here we save processing and save the member as current user
                    if(id == DataHolder.getInstance().getUserID()){
                        DataHolder.getInstance().setMember(member);

                    }
                    //we add the member to the members array
                    members.add(member);
                }
            }
        } catch (Exception e){
            Log.d("Retrieve members method",e.toString());
        }

        return members;
    }

    /***
     * buildObjects is the method that retrieves an entire household from the database
     * @param householdID
     */
    public static void buildHouseholdObject(int householdID){

        try {
            //query
            String query = "SELECT HouseholdName, HhRentAmount " +
                    "FROM hhm_households WHERE HouseholdID ="+ householdID;
            //we expect only one row
            String rows = "0";

            //encode the data for POST
            String data = URLEncoder.encode("retrieve","UTF-8") + "="
                    + URLEncoder.encode(query,"UTF-8");
            data += "&" +URLEncoder.encode("rows","UTF-8") + "="
                    + URLEncoder.encode(rows,"UTF-8");
            data +=  "&" + DataHolder.getInstance().getUserCredentials();

            //attempt connection
            String line = dbConnection.dbTransaction(Links.RETRIEVE,data);

            Household household;

            if (line != null && !line.equals("false")
                    && !line.equals("failed") && !line.equals("no records")){
                //the line is separated into columns
                String[] info = line.split("-c-");
                //first element is the household name
                String hn = info[0];
                //second element is the household rent
                float hr = Float.parseFloat(info[1]);
                //retrieveMembers is called
                ArrayList<Member> members = retrieveMembers(householdID);
                //object is instantiated and save in the dataholder
                household = new Household(hn,householdID,hr,members);
                DataHolder.getInstance().setHousehold(household);
            }

        } catch (Exception e){
            Log.d("Build Objects method",e.toString());
        }
    }

    /***
     * Retrieve bills queries the database for user bills
     * @param userID
     * @return array of bill objects
     */
    public static ArrayList<Bill> retrieveBills(int userID) {
        ArrayList<Bill> bills = new ArrayList<>();

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        String currentMonthAndYear = df.format(c.getTime());

        try {
            //query
            String query = "SELECT BillID, BillAmount, BillDesc, BillCategory, BillDate " +
                    "FROM hhm_bills WHERE UserID ="+ userID +
                    " AND BillDate LIKE '" + currentMonthAndYear + "%'";

            //we expect several rows so we passed a "1" as rows values
            String rows = "1";

            //data is encoded to passed as POST
            String data = URLEncoder.encode("retrieve","UTF-8") + "="
                    + URLEncoder.encode(query,"UTF-8");
            data += "&" +URLEncoder.encode("rows","UTF-8") + "="
                    + URLEncoder.encode(rows,"UTf-8");
            data +=  "&" + DataHolder.getInstance().getUserCredentials();

            //attempt connection
            String result = dbConnection.dbTransaction(Links.RETRIEVE,data);


            if(result != null && !result.equals("false")
                    && !result.equals("failed") && !result.equals("no records")){
                //split result as rows
                String[] lines = result.split("-r-");
                for (String line:lines
                        ) {
                    Bill bill;
                    //split result as columns
                    String[] info = line.split("-c-");
                    //1st element is the bill id
                    int id = Integer.parseInt(info[0]);
                    //2nd element is the bill amount
                    float ba = Float.parseFloat(info[1]);
                    //3rd element is the description
                    String bde= info[2];
                    //4th element is the category
                    String bc = info[3];
                    //5th element is the date
                    String bda = info[4];
                    //bill is instantiated and added to a bill array
                    bill = new Bill(id,ba,bde,bc,bda);
                    bills.add(bill);
                }
            }
        } catch (Exception e){
            Log.d("Retrieve bills", e.toString());
        }

        return  bills;
    }

    /***
     * build member is only called if the member does not belong to a household
     * @param userID
     */
    public static void buildMemberObject(int userID){
        try {
            //query
            String query = "SELECT LastName, FirstName, UserLevel, UserStatus " +
                    "FROM hhm_users WHERE UserID ="+ userID;

            //we expect one row so we passed 0
            String rows = "0";

            //encode data
            String data = URLEncoder.encode("retrieve","UTF-8") + "="
                    + URLEncoder.encode(query,"UTF-8");
            data += "&" +URLEncoder.encode("rows","UTF-8") + "="
                    + URLEncoder.encode(rows,"UTf-8");
            data +=  "&" + DataHolder.getInstance().getUserCredentials();

            //attempt connection
            String line = dbConnection.dbTransaction(Links.RETRIEVE,data);

            if(line != null && !line.equals("false")
                    && !line.equals("failed") && !line.equals("no records")){
                Member member;
                //split the result in columns
                String[] info = line.split("-c-");
                //last name
                String ln = info[0];
                //first name
                String fn = info[1];
                //user level
                String ul = info[2];
                //user status
                String us = info[3];
                //call retrieve bills method
                ArrayList<Bill> bills = retrieveBills(userID);

                if(ul.equals("admin")){//if the user is an admin instantiate as Admin
                    member = new Admin(userID,ln,fn,ul,us,bills);
                } else {
                    member = new Member(userID,ln,fn,ul,us,bills);
                }
                //save the Object in the dataholder
                DataHolder.getInstance().setMember(member);
            }
        } catch (Exception e){
            Log.d("Build member method", e.toString());
        }
    }
}

