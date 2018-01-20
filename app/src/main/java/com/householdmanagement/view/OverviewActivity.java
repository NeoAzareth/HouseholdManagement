package com.householdmanagement.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.householdmanagement.R;
import com.householdmanagement.controller.Bill;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Household;
import com.householdmanagement.controller.Member;
import com.householdmanagement.controller.AlarmService;
import com.householdmanagement.controller.SyncHousehold;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Executor;

/**
 * This overview page shows all users in this household, all bills for each user for current and
 * past months.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0.
 */
public class OverviewActivity extends Activity {

    //instance variables
    private Member user = DataHolder.getInstance().getMember();
    private Household household = DataHolder.getInstance().getHousehold();
    private TextView greetUserTextView, houseHoldNameTextView, householdRentTextView;
    private TableLayout myBillsTL, membersStatusTL;
    private TableRow myBillsLabels, membersStatusLabels;
    private Intent intent;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date;
    private RelativeLayout overviewRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overview_layout);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMMM dd, yyy");

        //set references to widgets
        greetUserTextView = (TextView)findViewById(R.id.greetUserTextView);
        houseHoldNameTextView = (TextView)findViewById(R.id.householdNameTextView);
        householdRentTextView = (TextView)findViewById(R.id.houseHoldRentTextView);
        myBillsLabels = (TableRow)findViewById(R.id.labelsTableRow);
        membersStatusLabels = (TableRow)findViewById(R.id.labelsMemberStatusTR);

        myBillsTL = (TableLayout)findViewById(R.id.myBillsTableLayout);
        membersStatusTL = (TableLayout)findViewById(R.id.membersStatusTableLayout);

        overviewRelativeLayout = (RelativeLayout)findViewById(R.id.overview_relative_layout);

        setWidgets();

        //below code is to trigger the notification
        Intent alarmIntent=new Intent(this, AlarmService.class);
        startService(alarmIntent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onPause(){

        super.onPause();
    }


    @Override
    public void onResume(){
        super.onResume();

        new SyncHousehold(this,overviewRelativeLayout)
                .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR
                        ,DataHolder.getInstance().getHouseholdID());

        new UpdateView().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
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
     * populates the table members with the current household members other than the current user
     */
    public void populateMembersStatusTable(){
        if(household.getMembers().size() >1){
            int rowNum = 1;
            for(Member member:household.getMembers()){
                if(member.getUserID() != DataHolder.getInstance().getUserID()){
                    TableRow tr = member.getUserStatusAsRow(this);
                    if(rowNum % 2 != 0){
                        tr.setBackgroundColor(Color.argb(255, 224, 243, 250));
                    } else {
                        tr.setBackgroundColor(Color.argb(250, 255, 255, 255));
                    }
                    membersStatusTL.addView(tr);
                    rowNum++;
                }
            }
        } else {
            membersStatusLabels.setVisibility(View.GONE);
            TableRow tr = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText("No other members here...");
            tv.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT,4f
            ));
            tr.addView(tv);
            tr.setBackgroundColor(Color.argb(250, 255, 153, 153));
            tr.setPadding(20,20,0,20);
            membersStatusTL.addView(tr);
        }
    }

    /***
     * populates the bills table with the current user bills
     */
    public void populateMyBillsTable(){
        if(user.getBills().size() >0){
            int rowNum = 1;
            for (Bill bill:user.getBills()){
                TableRow tr = bill.getBillAsOverviewTR(this);
                if(rowNum % 2 != 0){
                    tr.setBackgroundColor(Color.argb(255, 224, 243, 250));
                } else {
                    tr.setBackgroundColor(Color.argb(250, 255, 255, 255));
                }
                myBillsTL.addView(tr);
                rowNum++;
            }
        } else {
            myBillsLabels.setVisibility(View.GONE);
            TableRow tr = new TableRow(this);
            TextView tv = new TextView(this);
            tv.setText("No bills found...");
            tv.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT,4f
            ));
            tr.addView(tv);
            tr.setBackgroundColor(Color.argb(255, 255, 153, 153));
            tr.setPadding(20,20,0,20);
            myBillsTL.addView(tr);
        }
    }

    public void setWidgets(){

        date = dateFormat.format(calendar.getTime());

        //sets some widgets to greet the user
        String greetUser = "Hello " + user.getFullName() + ".";
        greetUserTextView.setText(greetUser);
        String welcomeToHH = "Welcome to "+
                household.getHouseholdName()+ " overview page,";
        houseHoldNameTextView.setText(welcomeToHH);
        String showRent = "Rent as of "+ date +
                ": $"+Float.toString(household.getHouseHoldRent());
        householdRentTextView.setText(showRent );

        myBillsTL.removeAllViews();
        populateMyBillsTable();

        membersStatusTL.removeAllViews();
        populateMembersStatusTable();

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        intent = new Intent(getApplicationContext(),OverviewActivity.class);
        int householdID = DataHolder.getInstance().getHouseholdID();
        new SyncHousehold(intent,OverviewActivity.this)
                .execute(householdID);
        new UpdateView().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    class UpdateView extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        public void onPostExecute(Void v){
            setWidgets();
        }

    }
}

