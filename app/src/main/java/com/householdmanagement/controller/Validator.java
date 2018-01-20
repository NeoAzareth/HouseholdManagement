package com.householdmanagement.controller;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator holds input validations methods
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public class Validator {

    //pattern objects used by the isValidPattern
    //first name can be used for any single words
    private static final Pattern FIRST_NAME = Pattern.compile("[a-zA-z]+([a-zA-Z]+)*");
    //last name can be used to validate sentences that do not include special characters
    private static final Pattern LAST_NAME = Pattern.compile("[a-zA-z]+([ '-][a-zA-Z]+)*");
    //password is used to validate password strength
    //at least eight characters long
    //at least one upper case letter
    //at least one lower case letter
    //at least one number
    //no whitespace allowed
    private static final Pattern PASSWORD
            = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$");

    /***
     * is valid pattern takes an enum code and a string as parameters to determine if the
     * given string matches a regular expression as part of validation
     * @param patternsEnum an  enum from Patterns
     * @param toMatch the string to be validated
     * @return boolean
     */
    public static boolean isValidPattern(Enum<Patterns> patternsEnum, String toMatch){
        Pattern p = null;
        if(patternsEnum.equals(Patterns.FIRST_NAME)){
            p = FIRST_NAME;
        } else if (patternsEnum.equals(Patterns.LAST_NAME)) {
            p = LAST_NAME;
        } else if (patternsEnum.equals(Patterns.PASSWORD)) {
            p = PASSWORD;
        }

        Matcher matcher = p.matcher(toMatch);
        if(matcher.matches()){
            return true;
        } else {
            return false;
        }
    }

    /***
     * checks if the email has a valid pattern
     * @return boolean
     */
    public static boolean isValidEmail(String email){
        Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
        Matcher matcher = pattern.matcher(email.toUpperCase());
        if(matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * helper function that checks if the passwords match
     * @param pass string to be compared
     * @param pass2 string to compare to
     * @return boolean
     */
    public static boolean passwordsMatch(String pass, String pass2){

        if(pass.equals(pass2)) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * helper function that checks if any field is missing
     * @param args takes any number of string parameters to be validated for null or empty values
     * @return boolean
     */
    public static boolean missingInfo(String... args){

        for (String arg: args) {
            if(isNullOrEmpty(arg)){
                return true;
            }
        }

        return false;
    }

    /**
     * helper function that checks if any given string is empty or null
     * @param field
     * @return
     */
    public static boolean isNullOrEmpty(String field){
        if (field.equals(null) || field.equals("")){
            return true;
        } else {
            return false;
        }
    }

    /**
     * isValidFloat check if a given string can be cast as a float
     * @param s
     * @return
     */
    public static boolean isValidFloat(String s){
        Float f;
        try{
            f = Float.parseFloat(s);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /***
     * isValidAmount checks if the given string after parsed is below a given float amount
     * this function will crash if used before isValidFloat and the string can not be parsed
     * @param s
     * @param maxAmount
     * @return
     */
    public static boolean isValidAmount(String s,Float maxAmount){
        Float f = Float.parseFloat(s);

        if(f<maxAmount){
            return true;
        } else {
            return false;
        }
    }

    /***
     * isNotZeroOrNegative check that a given string after parsed to float is not 0 or negative
     * this function will crash if used before isValidFloat and the string can not be parsed
     * @param s
     * @return
     */
    public static boolean isNotZeroOrNegative(String s){
        Float f = Float.parseFloat(s);

        if(f <= 0.0){
            return false;
        } else {
            return true;
        }
    }

    /***
     * isValidLength checks if a given string is shorter or equal than a given length
     * @param s
     * @param length
     * @return
     */
    public static boolean isValidLength(String s, int length){
        if(s.length()<= length){
            return true;
        } else {
            return false;
        }
    }

    /**
     * hasInvalidScapeChars checks any given amount of strings for scape characters \n \t \r
     * @param params
     * @return true if found
     */
    public static boolean hasInvalidScapeChars(String... params){
        String[] unwantedChars = {"\n","\t","\r"};
        for(String s:params){
            for (String c:unwantedChars){
                if(s.contains(c)){
                    return true;
                }
            }
        }
        return false;
    }

    /***
     * hasExtraWhiteSpaceBetweenWords checks any given amount of strings for extra
     * white space within words
     * @param params
     * @return
     */
    public static boolean hasExtraWhiteSpaceBetweenWords(String... params){

        for(String s:params){
            if(s.trim().contains("  ")){
                return true;
            }
        }
        return false;
    }


    /**
     * function that cleans a string for space characters such as \n \r \t
     * as well as any extra white space within the string and both beginning and end of the string
     * @param s
     * @return
     */
    public static String clearWhiteSpace(String s){
        //replaces all instances of \n \t and \r with nothing ("")
        String[] unwanted = {"\n","\t","\r"};
        for(String string:unwanted){
            s = s.replaceAll(string," ");
        }

        s = s.trim();

        //replaces all extra white space within the string to a single white space
        Boolean hasExtra = true;
        while(hasExtra){
            if(s.contains("  ")){
                s=s.replaceAll("  "," ");
            } else {
                hasExtra = false;
            }
        }
        return s;
    }
















    public static Boolean isNetworkAvailable(Activity activity){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /***
     * isTwoDigitAtMost checks if given string has at most two digits after decimal point,
     * so that the given string is a valid currency number
     * @param s an integer or float number
     * @return true if this number has maximum two digits after decimal point
     */
    public static boolean isTwoDigitAtMost(String s) {
        if(s.indexOf(".") == -1)
            return true;

        else if(s.length() - (s.indexOf(".") + 1) <= 2)
            return true;

        else
            return false;
    }

    /**
     * helper function that shows a toast for user feedback
     * @param message
     */
    public static void showToast(Context context,String message){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

}


