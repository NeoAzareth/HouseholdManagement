package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.GetMemberByStatusAsyncTask;

/**
 * This page allows admin to manage member.
 *
 * @author  Sicheng Zhu
 * @version  1.0
 */
public class ManageMemberActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Button addMemberButton;
    private Button deleteMemberButton;
    private Button resetMemberButton;
    private Intent intent;
    private int householdID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_member_layout);

        addMemberButton = (Button)findViewById(R.id.add_members_button);
        addMemberButton.setOnClickListener(this);

        deleteMemberButton = (Button)findViewById(R.id.delete_members_button);
        deleteMemberButton.setOnClickListener(this);

        resetMemberButton = (Button)findViewById(R.id.reset_members_button);
        resetMemberButton.setOnClickListener(this);

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
    public void onClick(View v) {
        householdID = DataHolder.getInstance().getHouseholdID();
        switch (v.getId()) {
            case R.id.add_members_button:
                new GetMemberByStatusAsyncTask(this,this).execute(Integer.toString(householdID),
                        "pending");
                break;

            case R.id.delete_members_button:
                new GetMemberByStatusAsyncTask(this,this).execute(Integer.toString(householdID),
                        "");
                break;

            case R.id.reset_members_button:
                new GetMemberByStatusAsyncTask(this,this).execute(Integer.toString(householdID),
                        "done");
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
