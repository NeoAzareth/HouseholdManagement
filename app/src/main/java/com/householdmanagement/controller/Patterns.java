package com.householdmanagement.controller;

/**
 * Enum used to limit the parameter for the validation class method
 * isValidPattern(Enum<Patterns> patternEnum, String toMatch)
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public enum Patterns {
    //patterns can be added to this class
    /****
     * FIRST_NAME is used for the user first name does not allow any special characters, white space
     * or numbers
     * LAST_NAME is used for last name does not allow white space or numbers but it allows some
     * special characters apostrophes, single white space and single dash
     * PASSWORD is use for password strength see Validator class for explanation
     */
    FIRST_NAME,LAST_NAME,PASSWORD
}
