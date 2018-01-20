package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;

/**
 * This page is settings page.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class SettingsActivity extends Activity implements View.OnClickListener {
    private Button updatePasswordButton;
    private Button leaveOrDeleteHouseholdButton;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        updatePasswordButton = (Button)findViewById(R.id.update_password_button);
        leaveOrDeleteHouseholdButton = (Button)findViewById(R.id.leave_or_delete_household_button);

        updatePasswordButton.setOnClickListener(this);
        leaveOrDeleteHouseholdButton.setOnClickListener(this);

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
            case R.id.update_password_button:
                Intent intent=new Intent(getApplicationContext(),UpdatePasswordActivity.class);
                startActivity(intent);
                break;

            case R.id.leave_or_delete_household_button:
                Intent deleteIntent = new Intent(getApplicationContext(),LeaveOrDeleteActivity.class);
                startActivity(deleteIntent);
                break;
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
}
