package com.householdmanagement.controller;

/**
 * Links enumeration class lists all types of data interaction with back-end server for this app.
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public enum Links {
    //enums used to limit the parameter to:
    //retrieve - used to retrieve any kind of data from the DB
    //Default - used to insert, update and delete data from the DB
    //register - exclusively used to register a new user
    //spreadsheet - used exclusively to generate a spreadsheet report
    RETRIEVE,DEFAULT,REGISTER,SPREADSHEET
}
