package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.view.Menu;
import android.view.MenuItem;

import com.householdmanagement.R;
import com.householdmanagement.controller.Bill;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.Member;
import com.householdmanagement.controller.SyncHousehold;
import com.householdmanagement.controller.Validator;
import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;

/**
 * This page allows user to edit a bill details.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class EditBillDetailActivity extends Activity {
    //instance fields
    private EditText description_editText;
    private EditText amount_editText;
    private Spinner category_spinner;
    private Button edit_bill_finish_button;
    private Bill billToEdit;
    private Member user = DataHolder.getInstance().getMember();
    private String desc,amountString,category;
    private int hhID = DataHolder.getInstance().getHouseholdID();
    private DBConnection con = new DBConnection();
    private Intent intent;
    private RelativeLayout editBillRL;
    private ProgressBar progressBar;
    private SharedPreferences savedValues;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_bill_detail_layout);

        //get the tag from the intent to populate the field from the bill to edit
        int tag = getIntent().getIntExtra("tag",0);

        //get the bill object out of the userbills bill array
        for(Bill bill:user.getBills()){
            if(bill.getBillID() == tag){
                billToEdit = bill;
            }
        }

        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = savedValues.edit();

        //set references to widgets
        description_editText = (EditText)findViewById(R.id.editBillDescET);
        amount_editText = (EditText)findViewById(R.id.editBillAmountET);
        category_spinner = (Spinner)findViewById(R.id.editBillCatSpinner);
        edit_bill_finish_button = (Button)findViewById(R.id.editBillActButton);
        editBillRL =(RelativeLayout)findViewById(R.id.edit_bill_relative_layout);

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        editBillRL.addView(progressBar);

        //set the adapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,R.array.categoryarr,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        category_spinner.setAdapter(adapter);

        //calls the method setBillValues to populate the fields for user reference
        setBillValues();

        //set the listener
        edit_bill_finish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve data from the widgets
                desc = description_editText.getText().toString();
                amountString = amount_editText.getText().toString();
                category = (String) category_spinner.getSelectedItem();
                //calls the validate user input method
                validateUserInput();
            }
        });

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // exit and back
    @Override
    public void onPause() {
        desc = description_editText.getText().toString().trim();
        editor.putString("BillDesc",desc);
        category = (String) category_spinner.getSelectedItem();
        editor.putString("BillCategory",category);
        amountString = amount_editText.getText().toString().trim();
        editor.putString("BillAmount",amountString);
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(savedValues.getString("BillDesc","").equals("")){
            setBillValues();
        } else {
            description_editText.setText(savedValues.getString("BillDesc",""));
            amount_editText.setText(savedValues.getString("BillAmount",""));
            setCategorySpinner(savedValues.getString("BillCategory","food"));
        }
    }

    /***
     * validates the user input calls UpdateBill on success
     */
    public void validateUserInput(){
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
            new UpdateBill().execute();
        }
    }

    /***
     * set the widgets to the bill to edit values for user reference
     */
    public void setBillValues(){
        description_editText.setText(billToEdit.getBillDesc());
        amount_editText.setText(billToEdit.getBillAmount());
        setCategorySpinner(billToEdit.getBillCategory());
    }

    public void setCategorySpinner(String category){

        int selection = 0;

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

    /***
     * Async task that updates the bill on the database calls SyncHousehold on success
     */
    class UpdateBill extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                //parses the amount to a float
                Float amount = Float.parseFloat(amountString);
                //query
                String query = "UPDATE hhm_bills " +
                        "SET BillAmount = "+amount+"," +
                        "BillDesc = '" +desc+"',"+
                        "BillCategory = '"+category+"',"+
                        "BillDate = NOW() "+
                        "WHERE BillID = "+billToEdit.getBillID();

                //postdata
                String data = DataHolder.getInstance().getUserCredentials();

                data += "&" + URLEncoder.encode("query","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");

                //attempt transaction
                String result = con.dbTransaction(Links.DEFAULT,data);

                if(result.equals("completed")){
                    return true;
                } else {
                    return false;
                }

            } catch (Exception e){
                Log.d("UpdateBill",e.toString());
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
                Validator.showToast(getApplicationContext(),"Bill successfully updated!");
                Intent intent=new Intent(getApplicationContext(),EditBillListActivity.class);
                new SyncHousehold(intent,EditBillDetailActivity.this,editBillRL).execute(hhID);
            } else {
                Validator.showToast(getApplicationContext(),"Could not update try again later...");
            }
        }
    }

}
