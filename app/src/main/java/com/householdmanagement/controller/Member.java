package com.householdmanagement.controller;

import android.content.Context;
import android.widget.TableRow;
import android.widget.TextView;

import com.householdmanagement.R;

import java.util.ArrayList;

/**
 * Member class construct member objects and contain methods that return properties as a table row.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class Member {

    //object properties
    private String firstName, lastName, userLevel, userStatus;
    private int userID;
    private ArrayList<Bill> bills;

    /***
     * empty constructor
     */
    public Member(){};

    /***
     * constructor with 6 parameters
     * @param userID
     * @param lastName
     * @param firstName
     * @param userLevel the user access level member and admin
     * @param userStatus the user current status "not done", "done","pending","not in"
     * @param bills and array with bill objects belonging to the user
     */
    public Member(int userID, String lastName, String firstName,
                  String userLevel, String userStatus,ArrayList<Bill> bills) {
        this.userID = userID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.userLevel = userLevel;
        this.userStatus = userStatus;
        this.bills = bills;
    }

    //getters
    public int getUserID(){return userID;}

    public String getUserLevel(){
        return this.userLevel;
    }

    public String getUserStatus(){
        return  this.userStatus;
    }

    public String getFirstName(){
        return this.firstName;
    }

    public ArrayList<Bill> getBills(){
        return bills;
    }

    public String getFullName(){
        String fullName = Character.toUpperCase(firstName.charAt(0)) +
                firstName.toLowerCase().substring(1) + " " +
                Character.toUpperCase(lastName.charAt(0)) +
                lastName.toLowerCase().substring(1);

        return fullName;
    }

    /***
     * getUserStatusAsRow constructs a table row with the user properties
     * @param context
     * @return
     */
    public TableRow getUserStatusAsRow(Context context){
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView fullNameTV = new TextView(context);
        fullNameTV.setText(this.getFullName());
        fullNameTV.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT,
                2f
        ));
        fullNameTV.setTextAppearance(R.style.style_table_elements);
        fullNameTV.setPadding(15,15,0,15);
        tr.addView(fullNameTV);

        TextView userLevelTV = new TextView(context);
        userLevelTV.setText(Character.toUpperCase(userLevel.charAt(0)) +
        userLevel.substring(1));
        userLevelTV.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT,
                1f
        ));
        userLevelTV.setTextAppearance(R.style.style_table_elements);
        userLevelTV.setPadding(15,15,0,15);
        tr.addView(userLevelTV);

        TextView userStatusTV = new TextView(context);
        userStatusTV.setText(userStatus);
        userStatusTV.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,1f
        ));
        userStatusTV.setTextAppearance(R.style.style_table_elements);
        userStatusTV.setPadding(15,15,0,15);
        tr.addView(userStatusTV);

        return tr;
    }
}
