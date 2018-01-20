package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;



import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.HandleDeleteOrLeave;

/**
 * This activity is for user to leave or delete a household.
 * User needs to enter password for a such behavior.
 *
 * @author  Huangxiao Lin
 * @version  1.0
 */
public class LeaveOrDeleteActivity extends Activity implements View.OnClickListener {

    private EditText confirmDeletePassword;
    private Button confirmButton;
    private TextView deleteLargeText;
    private TextView deleteMediumText;
    private Button cancelButton;
    private String userLevel;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_confirmation);
        confirmDeletePassword=(EditText)findViewById(R.id.confirmDeletePassword);
        deleteLargeText=(TextView)findViewById(R.id.deleteLargeText);
        deleteMediumText=(TextView)findViewById(R.id.deleteMediumText);
        confirmButton=(Button)findViewById(R.id.confirmButton);
        cancelButton=(Button)findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        //check userLevel to decide text
        userLevel=DataHolder.getInstance().getMember().getUserLevel();

        if(userLevel.equals("admin")){
            deleteLargeText.setText("Delete Your Household");
            deleteMediumText.setText("Enter Password To Delete Household");
        }
        else{
            deleteLargeText.setText("Leave Your Current Household");
            deleteMediumText.setText("Enter Password To Leave Household");
        }

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
        switch (v.getId()) {
            case R.id.cancelButton:
                Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.confirmButton:
                //on click validate input first
                String confirmDeletePasswordInput = confirmDeletePassword.getText().toString();
                new HandleDeleteOrLeave(this,this).execute(confirmDeletePasswordInput,userLevel);
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
