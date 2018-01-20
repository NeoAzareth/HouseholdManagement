package com.householdmanagement.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

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
 * This page allows user to remove a bill in his/her account.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class DeleteBillActivity extends Activity implements View.OnClickListener{
    //instance fields
    private Intent intent;
    private TableLayout deleteBillsTL;
    private Member user = DataHolder.getInstance().getMember();
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener dialogClickListener;
    private int billTag;
    private DBConnection con = new DBConnection();
    private RelativeLayout deleteBillRL;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_bill_layout);

        //this condition checks if there are bills to show an delete
        //this is necessary because if there is only one bill and it gets deleted
        //the activity will show and empty list...
        //so instead of that it redirects to managebills activity
        if(user.getBills().size() == 0){
            Intent intent = new Intent(getApplicationContext(),ManageBillActivity.class);
            startActivity(intent);
        }

        deleteBillRL = (RelativeLayout)findViewById(R.id.delete_bill_relative_layout);

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        deleteBillRL.addView(progressBar);

        // creates an alertdialog in order to confirm deletion
        builder = new AlertDialog.Builder(this);
        dialogClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //call delete bill if yes is press
                        new DeleteBill().execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        builder.setPositiveButton("Yes",dialogClickListener)
                .setNegativeButton("No",dialogClickListener);

        //get reference to widget
        deleteBillsTL = (TableLayout)findViewById(R.id.deleteBillsTableLayout);

        //call populate table to show bills
        populateDeleteBillsTable();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // exit and back
    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();


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

    @Override
    public void onClick(View v) {
        billTag = (int)v.getTag();
        Bill bill = new Bill();
        for(Bill b:user.getBills()){
            if(b.getBillID() == billTag){
                bill = b;
            }
        }
        builder.setMessage("Delete "+ bill.getBillDesc()+ "?").show();
    }

    /***
     * populates the delete bills table
     */
    public void populateDeleteBillsTable(){
        int rowNum = 1;
        for(Bill bill:user.getBills()){
            TableRow tr = bill.getBillAsRowForDeletion(this);
            tr.setOnClickListener(this);
            tr.setPadding(0,20,0,20);
            if(rowNum % 2 != 0){
                tr.setBackgroundColor(Color.argb(255, 224, 243, 250));
            } else {
                tr.setBackgroundColor(Color.argb(250, 255, 255, 255));
            }
            deleteBillsTL.addView(tr);
            rowNum++;
        }
    }

    /***
     * Async task DeleteBill deletes a bill from the database
     * calls SyncHousehold on success
     */
    class DeleteBill extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                //query
                String query = "DELETE FROM hhm_bills WHERE BillID = "+ billTag;

                //postdata
                String data = DataHolder.getInstance().getUserCredentials();

                data += "&" + URLEncoder.encode("query","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");

                //attempt connection
                String result = con.dbTransaction(Links.DEFAULT,data);

                //manage the result
                if(result.equals("completed")){
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e){
                Log.d("DeleteBill",e.toString());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){

            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            if(result){
                intent = new Intent(getApplicationContext(),DeleteBillActivity.class);
                Validator.showToast(getApplicationContext(),"Bill deleted");
                new SyncHousehold(intent,DeleteBillActivity.this,deleteBillRL)
                        .execute(DataHolder.getInstance().getHouseholdID());
            } else {
                Validator.showToast(getApplicationContext(),"Failed to delete...");
            }
        }
    }
}