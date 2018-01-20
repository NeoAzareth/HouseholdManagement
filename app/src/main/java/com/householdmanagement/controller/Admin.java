package com.householdmanagement.controller;

import java.util.ArrayList;

/**
 * The Admin class is used to extend user permission to manage a household and permit certain
 * actions with methods only available to admins.
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public class Admin extends Member {

    /**
     * empty constructor
     */
    public Admin() {}

    /***
     * constructor with 6 parameters
     * @param id user id
     * @param ln last name
     * @param fn first name
     * @param ul user level
     * @param us user status
     * @param bills array with user bills
     */
    public Admin(int id, String ln, String fn, String ul, String us, ArrayList<Bill> bills) {
        //calls the member constructor
        super(id,ln,fn,ul,us,bills);
    }
}
