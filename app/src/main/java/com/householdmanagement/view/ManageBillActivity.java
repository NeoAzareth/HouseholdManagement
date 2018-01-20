package com.householdmanagement.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.householdmanagement.R;
import com.householdmanagement.controller.Bill;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.Login;
import com.householdmanagement.controller.Member;
import com.householdmanagement.controller.SyncHousehold;
import com.householdmanagement.controller.AlarmService;
import com.householdmanagement.controller.Validator;
import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;

/**
 * This page allows user to manage their current month's bill.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class ManageBillActivity extends Activity implements View.OnClickListener {
    //instance fields
    private Button addBillButton;
    private Button editBillButton;
    private Button deleteBillButton,doneButton;
    private Intent intent;
    private ImageView imageView;
    private TableLayout manageBillsTableLayout;
    private Member user = DataHolder.getInstance().getMember();
    private TextView doneTextView,manageBillsHeadingTV;
    private RelativeLayout manageBillsRL;
    private ProgressBar progressBar;
    private DBConnection con = new DBConnection();
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener dialogClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_bill_layout);

        //set references to the widgets
        imageView = (ImageView)findViewById(R.id.manageBillsImageView);
        manageBillsTableLayout = (TableLayout)findViewById(R.id.manageBillsTableLayout);
        addBillButton = (Button)findViewById(R.id.add_bill_button);
        editBillButton = (Button)findViewById(R.id.edit_bill_button);
        deleteBillButton = (Button)findViewById(R.id.delete_bill_button);
        doneButton = (Button)findViewById(R.id.doneButton);
        doneTextView = (TextView)findViewById(R.id.doneTextView);
        manageBillsRL = (RelativeLayout)findViewById(R.id.manage_bills_relative_layout);
        manageBillsHeadingTV = (TextView)findViewById(R.id.manageBillsHeadingTV);

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        manageBillsRL.addView(progressBar);
        progressBar.setVisibility(View.GONE);

        builder = new AlertDialog.Builder(this);
        dialogClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        new ChangeStatusToDone().execute();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        builder.setPositiveButton("Yes I am!",dialogClickListener)
                .setNegativeButton("On second thought",dialogClickListener);
        builder.setTitle("Are you sure?");
        builder.setMessage("You will not be able to modify your bills...");

        //calls showImageOrTable method
        showImageOrTable();

        //calls removeEditAndDeleteButtons
        removeEditAndDeleteButtons();

        //set the listeners
        addBillButton.setOnClickListener(this);
        editBillButton.setOnClickListener(this);
        deleteBillButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

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

    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.add_bill_button:
                intent=new Intent(getApplicationContext(),AddBillActivity.class);
                startActivity(intent);
                break;

            case R.id.edit_bill_button:
                intent=new Intent(getApplicationContext(),EditBillListActivity.class);
                startActivity(intent);
                break;

            case R.id.delete_bill_button:
                intent=new Intent(getApplicationContext(),DeleteBillActivity.class);
                startActivity(intent);
                break;

            case R.id.doneButton:
                builder.show();
                break;

        }
    }

    /***
     * removes or shows the edit and delete buttons if the user has no bills or if it
     * has bills respectively
     */
    public void removeEditAndDeleteButtons(){
        if (user.getUserStatus().equals("done")) {
            addBillButton.setVisibility(View.GONE);
            editBillButton.setVisibility(View.GONE);
            deleteBillButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);
            doneTextView.setVisibility(View.GONE);
            manageBillsHeadingTV.setText("You have changed your status to done... \n" +
                    "Contact your Household admin to reset it.");
            manageBillsHeadingTV.setGravity(Gravity.LEFT);
        } else if (user.getBills().size() == 0) {
            editBillButton.setVisibility(View.GONE);
            deleteBillButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.VISIBLE);
            doneTextView.setVisibility(View.VISIBLE);
            manageBillsHeadingTV.setText("What do you want to do today?");
            manageBillsHeadingTV.setGravity(Gravity.CENTER);
        } else {
            addBillButton.setVisibility(View.VISIBLE);
            editBillButton.setVisibility(View.VISIBLE);
            deleteBillButton.setVisibility(View.VISIBLE);
            doneButton.setVisibility(View.VISIBLE);
            doneTextView.setVisibility(View.VISIBLE);
            manageBillsHeadingTV.setText("What do you want to do today?");
            manageBillsHeadingTV.setGravity(Gravity.CENTER);
        }
    }

    /***
     * show the image table or bills table with the same logic as the previous method
     */
    public void showImageOrTable(){
        if(user.getBills().size()>0){
            imageView.setVisibility(View.GONE);
            manageBillsTableLayout.setVisibility(View.VISIBLE);
            int rowNum = 1;
            for(Bill bill:user.getBills()){
                TableRow tr = bill.getBillAsOverviewTR(this);
                if(rowNum % 2 != 0){
                    tr.setBackgroundColor(Color.argb(255, 224, 243, 250));
                } else {
                    tr.setBackgroundColor(Color.argb(250, 255, 255, 255));
                }
                manageBillsTableLayout.addView(tr);
                rowNum++;
            }
        } else {
            manageBillsTableLayout.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
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

    class ChangeStatusToDone extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try{
                String query = "UPDATE hhm_users SET UserStatus = 'done' "+
                        "WHERE UserID = " + user.getUserID();

                String data = DataHolder.getInstance().getUserCredentials();

                data += "&" + URLEncoder.encode("query","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");

                String result = con.dbTransaction(Links.DEFAULT,data);

                if(result.equals("false")|| result.equals("failed")){
                    return false;
                } else if(result.equals("completed")){
                    String ID = Integer.toString(DataHolder.getInstance().getHouseholdID());

                    String newPostData = DataHolder.getInstance().getUserCredentials();

                    newPostData += "&" + URLEncoder.encode("hhID","UTF-8") + "=" +
                            URLEncoder.encode(ID,"UTF-8");

                    String spreadSheetResult =con.dbTransaction(Links.SPREADSHEET,newPostData);

                    Log.d("Spread Sheet",spreadSheetResult);
                    return true;
                }

            } catch (Exception e){
                Log.d("ChangeUserStatus",e.toString());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            if(result) {
                Validator.showToast(getApplicationContext(),"Status changed");

                //stop notification service here
                Intent alarmIntent=new Intent(ManageBillActivity.this, AlarmService.class);
                stopService(alarmIntent);

                //set userStatus to done in SharePreferences
                SharedPreferences savedValues =
                        ManageBillActivity.this.getSharedPreferences("SavedValues", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = savedValues.edit();
                editor.putString("userStatus","done");
                editor.commit();
                DataHolder.getInstance().setSavedValues(savedValues);

                String email = DataHolder.getInstance().getUserEmail();
                String pass = DataHolder.getInstance().getUserPass();
                new Login(getApplicationContext(),ManageBillActivity.this).execute(email,pass);
            } else {
                Validator.showToast(getApplicationContext()
                        ,"Something went wrong try again later...");
            }
        }
    }

}