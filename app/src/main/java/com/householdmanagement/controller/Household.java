package com.householdmanagement.controller;

import java.util.ArrayList;

/**
 * Household class builds household objects contains properties accessors.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class Household {
    //object properties
    private String householdName;
    private int householdID;
    private float householdRent;
    private ArrayList<Member> members;

    /***
     * empty constructor
     */
    public Household(){}

    /***
     * constructor with 4 parameters
     * @param householdName
     * @param householdID
     * @param householdRent
     * @param members an array with member objects
     */
    public Household(String householdName, int householdID
            , float householdRent, ArrayList<Member> members){
        this.householdName = householdName;
        this.householdID = householdID;
        this.householdRent = householdRent;
        this.members = members;
    }

    //getters
    public String getHouseholdName(){
        return householdName;
    }

    public float getHouseHoldRent(){
        return householdRent;
    }

    public ArrayList<Member> getMembers(){
        return this.members;
    }
}