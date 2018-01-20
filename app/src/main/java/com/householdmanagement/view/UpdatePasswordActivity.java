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

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.PassUpdate;
import com.householdmanagement.controller.Patterns;
import com.householdmanagement.controller.Validator;

/**
 * This page allows user to update password
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0.
 */
public class UpdatePasswordActivity extends Activity implements View.OnClickListener{

    private EditText currentPass;
    private EditText newPass;
    private EditText conPass;
    private Button submitButton;
    private Button goBackButton;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_password_layout);
        currentPass=(EditText)findViewById(R.id.currentPassword);
        newPass=(EditText)findViewById(R.id.newPassword);
        conPass=(EditText)findViewById(R.id.conNewPassword);
        submitButton=(Button)findViewById(R.id.submitButton);
        goBackButton=(Button)findViewById(R.id.goBackButton);

        submitButton.setOnClickListener(this);
        goBackButton.setOnClickListener(this);

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
            case R.id.goBackButton:
                Intent intent=new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.submitButton:
                //on click validate input first
                String currentPassInput = currentPass.getText().toString();
                String newPassInput = newPass.getText().toString();
                String conPassInput =conPass.getText().toString();

                //check if all fields have input
                if (Validator.missingInfo(currentPassInput,newPassInput,conPassInput)){
                    Validator.showToast(this,"All fields are required.");
                }
                //check if new pass and confirm pass match
                else if(!Validator.passwordsMatch(newPassInput,conPassInput)) {
                    Validator.showToast(this,"Two passwords don't match.");
                }
                //check new pass pattern
                else if(!Validator.isValidPattern(Patterns.PASSWORD,newPassInput)){
                    Validator.showToast(getApplicationContext(),
                            "Password must contain: \n" +
                                    "At least 8 characters long \n" +
                                    "At least 1 number \n" +
                                    "At least 1 upper case letter \n" +
                                    "At least 1 lower case letter \n" +
                                    "No whitespace allowed");
                }
                //check if new pass and old pass are the same
                else if(Validator.passwordsMatch(newPassInput,currentPassInput)){
                    Validator.showToast(this,"Your old password and new password are the same.");
                }

                //if input passed all tests above, pass them to script
                else{
                    new PassUpdate(this,this).execute(currentPassInput,newPassInput);
                }
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
