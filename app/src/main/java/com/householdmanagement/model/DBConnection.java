package com.householdmanagement.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.util.Log;

import com.householdmanagement.controller.Links;

/**
 * Simple connection to a predefined database.
 *
 * @author  Israel Santiago
 * @version  1.0
 */
public class DBConnection {

    //three php scripts that handle:
    //retrieval/read data
    private final String RETRIEVE = "https://neoazareth.com/HHManageWebApp/mobileRetrieve.php";
    //default handles insert, update and delete
    private final String DEFAULT = "https://neoazareth.com/HHManageWebApp/mobileTransaction.php";
    //register handles user registration only
    private final String REGISTER = "https://neoazareth.com/HHManageWebApp/mobileReg.php";
    //keycode is expected by the script to verify the registration is coming from the app
    private final String KEY_CODE = "HHWebAppPass";

    private final String SPREADSHEET = "https://neoazareth.com/HHManageWebApp/mobileSpreadsheet.php";

    //empty constructor
    public DBConnection() {}

    /**
     * dbTransaction handles all db communications
     * @param linkCode; an enum from links that is compared to choose the right url to query
     * @param postData; the data that is passed to page through the $_POST super global
     * @return DB info or an error handling string
     */
    public String dbTransaction(Enum<Links> linkCode, String postData) {
        String link = "";
        //linkcode is compared to choose the right url
        if (linkCode.equals(Links.RETRIEVE)) {
            link = RETRIEVE;
        } else if (linkCode.equals(Links.DEFAULT)) {
            link = DEFAULT;
        } else if (linkCode.equals(Links.REGISTER)) {
            link = REGISTER;
        } else if (linkCode.equals(Links.SPREADSHEET)){
            link = SPREADSHEET;
        }

        //the method attempts DB connection
        try {
            //this line is used for registration only
            if (link.equals(REGISTER)) {
                //adds a keycode to the postdata to be verified by the script and allow
                //registration
                postData += "&" + URLEncoder.encode("keyCode","UTF-8") + "="
                        + URLEncoder.encode(KEY_CODE,"UTF-8");
            }

            //the page where I took these lines do not explain what exactly they do
            //the link to the tutorial is in the project document
            //I have a vague idea of what is going on but I don't want to mislead
            URL url = new URL(link);//set the link to communicate

            //open connection
            URLConnection conn = url.openConnection();

            //I think this sets a variable to true that means data is going to be past to through
            //the post into the PHP page/script
            conn.setDoOutput(true);

            //object to past the post data
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            //past the data
            wr.write(postData);
            //delete data
            wr.flush();

            //object that reads the data
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            //data is stored in a string
            String result = reader.readLine();

            //result is returned
            return result;
        } catch (Exception e) {
            //if any of the steps fails an exception is catch and the error is retrieved in the log
            Log.d("DBconnection",e.toString());
            return "Connection failed";
        }
    }
}

