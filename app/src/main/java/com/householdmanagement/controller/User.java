package com.householdmanagement.controller;

import java.util.ArrayList;

/**
 * User class stores everything about user.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String userLevel;
    private boolean userStatus;
    private ArrayList affliatedHousehold;

    public User(String username, String password, String email, String phoneNumber) {
        this.firstName = username;
        this.lastName = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;

        userStatus = false;
    }


    // Username
    public void setFirstname(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstname() {
        return firstName;
    }

    public void setLastname(String username) {
        this.lastName = lastName;
    }

    public String getLastname() {
        return lastName;
    }


    // Password
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }


    // Email
    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }


    // PhoneNumber
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Level
    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getUserLevel() {
        return userLevel;
    }

    // Status
    public void setUserStatus(boolean userStatus) {
        this.userStatus = userStatus;
    }

    public boolean getUserStatus() {
        return userStatus;
    }

    public void setAffliatedHousehold(ArrayList affliatedHousehold) {
        this.affliatedHousehold = affliatedHousehold;
    }

    public ArrayList getAffliatedHousehold() {
        return affliatedHousehold;
    }
}