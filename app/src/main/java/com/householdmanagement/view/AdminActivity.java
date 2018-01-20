package com.householdmanagement.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.SetNewRentAsyncTask;

import com.householdmanagement.controller.SyncHousehold;
import com.householdmanagement.controller.Validator;

/**
 * This page allows admin of a household to manage a house.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class AdminActivity extends Activity implements View.OnClickListener {
    private Button manageMembersButton;
    private Button changeRentButton;
    private Intent intent;
    private String newRent;
    private RelativeLayout relativeLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_layout);

        manageMembersButton = (Button) findViewById(R.id.manage_members_button);
        changeRentButton = (Button) findViewById(R.id.change_rent_button);

        manageMembersButton.setOnClickListener(this);
        changeRentButton.setOnClickListener(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.admin_layout);
        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        relativeLayout.addView(progressBar);

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
            case R.id.manage_members_button:
                Intent intent = new Intent(getApplicationContext(), ManageMemberActivity.class);
                startActivity(intent);
                break;

            case R.id.change_rent_button:
                showNewRentEntryAlertDialog();
                break;
        }
    }

    private void showNewRentEntryAlertDialog() {
        AlertDialog.Builder newRentAlertDialog = new AlertDialog.Builder(AdminActivity.this);

        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.change_rent_dialog_layout, null);
        final EditText changeRentEditText = (EditText) textEntryView.findViewById(R.id.change_rent_edit_text);

        newRentAlertDialog.setTitle("Please enter new amount for this month");
        newRentAlertDialog.setIcon(R.drawable.ic_settings);
        newRentAlertDialog.setCancelable(false);
        newRentAlertDialog.setView(textEntryView);

        newRentAlertDialog.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        newRentAlertDialog.setNegativeButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newRent = changeRentEditText.getText().toString();

                if (Validator.missingInfo(newRent))
                    showErrorMessageAlertDialog("New rent shouldn't be empty.");
                else if (Validator.hasExtraWhiteSpaceBetweenWords(newRent))
                    showErrorMessageAlertDialog("White space is not allowed.");
                else if (Validator.hasInvalidScapeChars(newRent))
                    showErrorMessageAlertDialog("No extra lines and no tab white space allowed!");
                else if (!Validator.isValidFloat(newRent))
                    showErrorMessageAlertDialog("Please enter a valid number for new rent.");
                else if (!Validator.isNotZeroOrNegative(newRent))
                    showErrorMessageAlertDialog("New rent must be greater than 0.");
                else if (!Validator.isTwoDigitAtMost(newRent))
                    showErrorMessageAlertDialog("Only two digits maximum after decimal point allowed.");
                else {
                    int householdID = DataHolder.getInstance().getHouseholdID();
                    new SetNewRentAsyncTask(getApplicationContext(), AdminActivity.this, relativeLayout).execute(
                            Integer.toString(householdID), newRent);

                    intent = new Intent(getApplicationContext(),AdminActivity.class);
                    new SyncHousehold(intent,AdminActivity.this)
                            .execute(householdID);
                }
            }
        });

        newRentAlertDialog.show();
    }

    private void showErrorMessageAlertDialog(String errorMessage) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AdminActivity.this);

        alertDialog.setTitle(errorMessage);
        alertDialog.setCancelable(false);

        TextView tv = new TextView(AdminActivity.this);
        tv.setText(errorMessage);
        tv.setTextSize(20);
        tv.setPadding(10, 10, 10, 10);
        tv.setTextColor(Color.parseColor("#ff0000"));


        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showNewRentEntryAlertDialog();
            }
        });

        alertDialog.setCustomTitle(tv);
        alertDialog.show();
    }

    // This method create menu
    // Depends on user's role (admin or general member), show corresponding menu
    // The main difference of two menus is general member won't see admin option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DataHolder.getInstance().getMember().getUserLevel().equals("admin"))
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
                intent = new Intent(getApplicationContext(), OverviewActivity.class);
                startActivity(intent);
                break;
            case R.id.manage_bills_menu_item:
                intent = new Intent(getApplicationContext(), ManageBillActivity.class);
                startActivity(intent);
                break;
            case R.id.report_menu_item:
                intent = new Intent(getApplicationContext(), ReportGenerationActivity.class);
                startActivity(intent);
                break;
            case R.id.settings_menu_item:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.admin_menu_item:
                intent = new Intent(getApplicationContext(), AdminActivity.class);
                startActivity(intent);
                break;
            case R.id.log_out_menu_item:
                DataHolder.getInstance().logout();
                intent = new Intent(getApplicationContext(), SignInOrUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)  {
//        if (keyCode == KeyEvent.KEYCODE_BACK ) {
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            return true;
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }

//    @Override
//    public void onBackPressed() {
//        // your code.
//        super.onBackPressed();
//    }
}

//http://blog.csdn.net/beyond0525/article/details/8951642