package com.householdmanagement;

import com.householdmanagement.controller.Patterns;
import com.householdmanagement.controller.Validator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class does various validation Junit testing.
 *
 * @author Israel Santiago
 * @version  1.0
 */
public class ValidatorUnitTest {

    @Test
    public void assert_isValidFirstName() {
        String name = "israel";
        assertTrue(Validator.isValidPattern(Patterns.FIRST_NAME,name));
    }

    @Test
    public void assert_isValidFirstName2() {
        String name = "i";
        assertTrue(Validator.isValidPattern(Patterns.FIRST_NAME,name));
    }

    @Test
    public void assert_isNotValidFirstName() {
        String name = "";
        assertFalse(Validator.isValidPattern(Patterns.FIRST_NAME,name));
    }

    @Test
    public void assert_isNotValidFirstName2() {
        String name = "Isr@el";
        assertFalse(Validator.isValidPattern(Patterns.FIRST_NAME,name));
    }

    @Test
    public void assert_isValidFloat() {
        String amount = "98";
        assertTrue(Validator.isValidFloat(amount));
    }

    @Test
    public void assert_isValidFloat2() {
        String amount = ".45";
        assertTrue(Validator.isValidFloat(amount));
    }

    @Test
    public void assert_isNotValidFloat() {
        String amount = "Hello";
        assertFalse(Validator.isValidFloat(amount));
    }

    @Test
    public void assert_isNotValidFloat2() {
        String amount = "";
        assertFalse(Validator.isValidFloat(amount));
    }

    @Test
    public void assert_isValidAmount() {
        String amount = "208.16";
        assertTrue(Validator.isValidAmount(amount,1000.0f));
    }

    @Test
    public void assert_equalString() {
        String text = "   colin       \n\t\r";
        assertEquals(Validator.clearWhiteSpace(text),"colin");
    }

    @Test
    public void cleanStringWhiteSpaceTest() {
        String text = "  Israel             \n\tSantiago \r    ";
        assertEquals("Israel Santiago",Validator.clearWhiteSpace(text));
    }
}
