package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.view.View.OnClickListener;

import com.householdmanagement.R;
import com.householdmanagement.controller.Bill;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Member;

/**
 * This page allows user to select a bill that he/she wants to edit.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class EditBillListActivity extends Activity implements OnClickListener{

    //instance fields
    private TableLayout editBillsTableLayout;
    private Member user = DataHolder.getInstance().getMember();
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_bill_list_layout);

        //set reference to widget
        editBillsTableLayout = (TableLayout)findViewById(R.id.editBillsTableLayout);

        //calls populate table
        populateTable();

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

    /**
     * populates the table with the user bills info
     */
    public void populateTable(){
        int rowNum = 1;
        for(Bill bill:user.getBills()){
            TableRow tr = bill.getBillAsOverviewTR(this);
            tr.setClickable(true);
            tr.setOnClickListener(this);
            tr.setPadding(0,20,0,20);
            if(rowNum % 2 != 0){
                tr.setBackgroundColor(Color.argb(255, 224, 243, 250));
            } else {
                tr.setBackgroundColor(Color.argb(250, 255, 255, 255));
            }
            editBillsTableLayout.addView(tr);
            rowNum++;
        }
    }

    @Override
    public void onClick(View v) {
        //gets the tag which references a bill by id from the view that was pressed
        int tag = (Integer)v.getTag();
        Intent intent = new Intent(getApplicationContext(),EditBillDetailActivity.class);
        //puts the tag for the intent to use
        intent.putExtra("tag",tag);
        startActivity(intent);
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
}
