package com.householdmanagement.controller;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Dataholder is a class that allows variables and objects to be stored so that all activities can
 * use them
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public class DataHolder {
    //credentials are the user email and password encoded and ready to be passed to the connection
    //class
    private String userCredentials;
    //user id
    private int userID;
    //household id
    private int householdID;
    //user email
    private String userEmail;
    //user pass
    private String userPass;
    //member object
    private Member member;
    //household object
    private Household household;
    //this is just a reference to the SharedPreferences savedValues
    private SharedPreferences savedValues;

    //this helps set the reference since this is not an activity and has no context it cannot get
    //the sharedpreferences on its own
    public void setSavedValues(SharedPreferences savedValues){
        this.savedValues = savedValues;
    }


    //getters and setters
    public String getUserCredentials(){return userCredentials;}
    public void setUserCredentials(String credentials){this.userCredentials = credentials;}

    public String getUserEmail(){return userEmail;}
    public void setUserEmail(String email){this.userEmail = email;}

    public String getUserPass(){return userPass;}
    public void setUserPass(String pass){this.userPass = pass;}

    public int getUserID(){return userID;}
    public void setUserID(int userID){this.userID = userID;}

    public int getHouseholdID(){return householdID;}
    public void setHouseholdID(int householdID){this.householdID = householdID;}

    public Member getMember(){return member;}
    public void setMember(Member member){this.member = member;}

    public Household getHousehold(){return  household;}
    public void setHousehold(Household household){this.household = household;}


    //a dataholder object is created in memory and is accessed by the getInstance method
    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}

    public void logout() {
        member = null;
        household = null;
        Editor editor = savedValues.edit();

        if(!savedValues.getBoolean("rememberEmail",false)){
            editor.remove("email");
        }
        editor.remove("pass");
        editor.commit();
    }
}
