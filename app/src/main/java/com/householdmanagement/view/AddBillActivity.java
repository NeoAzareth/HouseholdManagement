package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.SyncHousehold;
import com.householdmanagement.controller.Validator;
import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;

/**
 * This page allows user to add a bill details.
 *
 * @version  1.0
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 */
public class AddBillActivity extends Activity {
    //instance fields
    private EditText description_editText;
    private EditText amount_editText;
    private Spinner category_spinner;
    private Button add_bill_button;
    private String desc,amountString,category;
    private int userID = DataHolder.getInstance().getUserID();
    private int hhID = DataHolder.getInstance().getHouseholdID();
    private DBConnection con = new DBConnection();
    private Intent intent;
    private RelativeLayout addBillRL;
    private ProgressBar progressBar;
    private SharedPreferences savedValues;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_bill_layout);
        //get references to the widgets
        description_editText =(EditText)findViewById(R.id.billDescriptionET);
        amount_editText = (EditText)findViewById(R.id.billAmountET);
        category_spinner = (Spinner)findViewById(R.id.billCategorySpinner);
        add_bill_button = (Button)findViewById(R.id.addBillToDBButton);
        addBillRL = (RelativeLayout)findViewById(R.id.add_bill_relative_layout);

        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = savedValues.edit();

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        addBillRL.addView(progressBar);

        //set the adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,R.array.categoryarr,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(adapter);

        //set the listener for the button
        add_bill_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve the values from the widgets
                desc = description_editText.getText().toString().trim();
                amountString = amount_editText.getText().toString().trim();
                category = (String) category_spinner.getSelectedItem();

                //validate the user input
                if(Validator.missingInfo(desc,amountString)){
                    Validator.showToast(getApplicationContext(),"All fields are required");
                } else if (Validator.hasExtraWhiteSpaceBetweenWords(desc,amountString)){
                    Validator.showToast(getApplicationContext(),
                            "No more than one blank space within words!");
                } else if (Validator.hasInvalidScapeChars(desc,amountString)){
                    Validator.showToast(getApplicationContext()
                            ,"No extra lines and no tab white space allowed!");
                } else if (!Validator.isValidLength(desc,100)){
                    Validator.showToast(getApplicationContext(),"20 words or less!");
                } else if (!Validator.isValidFloat(amountString)){
                    Validator.showToast(getApplicationContext(),"Enter a valid number for amount");
                } else if (!Validator.isNotZeroOrNegative(amountString)){
                    Validator.showToast(getApplicationContext(),"Amount must be greater than 0");
                } else if (!Validator.isValidAmount(amountString,999.99f)) {
                    Validator.showToast(getApplicationContext(),"Amount must be less than 1000.00");
                } else {
                    new AddBill().execute();
                }
            }
        });

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // exit and back
    @Override
    public void onPause() {
        desc = description_editText.getText().toString().trim();
        editor.putString("AddBillDesc",desc);
        category = (String) category_spinner.getSelectedItem();
        editor.putString("AddBillCategory",category);
        amountString = amount_editText.getText().toString().trim();
        editor.putString("AddBillAmount",amountString);
        editor.commit();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        description_editText.setText(savedValues.getString("AddBillDesc",""));
        amount_editText.setText(savedValues.getString("AddBillAmount",""));
        setCategorySpinner();
    }

    // This method create menu
    // Depends on user's role (admin or general member), show corresponding menu
    // The main difference of two menus is general member won't see admin option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(DataHolder.getInstance().getMember().getUserLevel().equals("admin"))
            getMenuInflater().inflate(R.menu.admin_menu, menu);
        else
            getMenuInflater().inflate(R.menu.member_menu, menu);

        return true;
    }

    // This method handle user's click on a specific item
    // Redirect to corresponding activity once one item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.overview_menu_item:
                intent = new Intent(getApplicationContext(),OverviewActivity.class);
                startActivity(intent);
                break;
            case R.id.manage_bills_menu_item:
                intent = new Intent(getApplicationContext(),ManageBillActivity.class);
                startActivity(intent);
                break;
            case R.id.report_menu_item:
                intent = new Intent(getApplicationContext(),ReportGenerationActivity.class);
                startActivity(intent);
                break;
            case R.id.settings_menu_item:
                intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.admin_menu_item:
                intent = new Intent(getApplicationContext(),AdminActivity.class);
                startActivity(intent);
                break;
            case R.id.log_out_menu_item:
                DataHolder.getInstance().logout();
                intent = new Intent(getApplicationContext(),SignInOrUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void setCategorySpinner(){
        int selection = 0;
        String category = savedValues.getString("AddBillCategory","food");
        switch (category){
            case "food":
                selection = 0;
                break;
            case "utility":
                selection = 1;
                break;
            case "maintenance":
                selection = 2;
                break;
            case "other":
                selection = 3;
                break;
        }
        category_spinner.setSelection(selection);
    }

    /***
     * AsyncTask that handles inserting a bill object into the database
     */
    class AddBill extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                Float amount = Float.parseFloat(amountString);
                String query = "INSERT INTO hhm_bills " +
                        "VALUES(NULL," + amount
                        +",'"+ desc +"','"+ category
                        +"',NOW(),"+hhID+","+userID+")";

                String data = DataHolder.getInstance().getUserCredentials();

                data += "&" + URLEncoder.encode("query","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");

                String result = con.dbTransaction(Links.DEFAULT,data);
                System.out.println(result);
                if(result.equals("completed")){
                    return true;
                } else {
                    return false;
                }

            } catch (Exception e){
                Log.d("AddBill",e.toString());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            description_editText.setText("");
            amount_editText.setText("");
            category_spinner.setSelection(0);
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if(result){
                Validator.showToast(getApplicationContext(),"Bill successfully added!");
                Intent intent=new Intent(getApplicationContext(),ManageBillActivity.class);
                new SyncHousehold(intent,AddBillActivity.this,addBillRL).execute(hhID);
            } else {
                Validator.showToast(getApplicationContext(),"Could not add try again later...");
            }
        }
    }
}