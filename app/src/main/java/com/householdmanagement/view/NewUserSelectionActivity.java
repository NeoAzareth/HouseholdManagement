package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Household;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.Login;
import com.householdmanagement.controller.Member;
import com.householdmanagement.controller.Validator;
import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;

/**
 * This page allows new user to select whether to create or join a house.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version  1.0
 */
public class NewUserSelectionActivity extends Activity implements View.OnClickListener {
    //instance fields
    private Button createAHouseholdButton;
    private Button joinAHouseholdButton;
    private Intent intent;
    private TextView selectActivityGreeting;
    private Member user = DataHolder.getInstance().getMember();
    private DBConnection con = new DBConnection();
    private RelativeLayout newUserSelectionRL;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_user_selection_layout);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //set references to widgets
        createAHouseholdButton = (Button)findViewById(R.id.create_a_household_button);
        joinAHouseholdButton = (Button)findViewById(R.id.join_a_household_button);
        selectActivityGreeting = (TextView)findViewById(R.id.selectActivityGreeting);
        newUserSelectionRL = (RelativeLayout)findViewById(R.id.new_user_selection_rlayout);

        progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        newUserSelectionRL.addView(progressBar);

        //calls the ifPendingSetAlternateLayout
        ifPendingSetAlternateLayout();
        createAHouseholdButton.setOnClickListener(this);
    }


    // exit and back
    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        ifPendingSetAlternateLayout();
    }

    // This method create menu
    // Depends on user's role (admin or general member), show corresponding menu
    // The main difference of two menus is general member won't see admin option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(user.getUserStatus().equals("pending"))
            getMenuInflater().inflate(R.menu.member_pending_menu, menu);
        else if(DataHolder.getInstance().getMember().getUserLevel().equals("admin"))
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
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.create_a_household_button:
                intent=new Intent(getApplicationContext(),CreateAHouseholdActivity.class);
                startActivity(intent);
                break;

            case R.id.join_a_household_button:
                intent=new Intent(getApplicationContext(),JoinAHouseholdActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * this method checks if the user status is pending, if so blocks the create household button
     * and set the join household button to cancel pending status
     */
    public void ifPendingSetAlternateLayout(){
        if(user.getUserStatus().equals("pending")){
            Household household = DataHolder.getInstance().getHousehold();
            selectActivityGreeting.setText("You are currently pending to join "+
                    household.getHouseholdName()+ " ");
            createAHouseholdButton.setEnabled(false);
            joinAHouseholdButton.setText("Cancel");
            joinAHouseholdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //sets the listener to call a different method
                    new setUserToNotInStatus().execute();
                }
            });
        } else {
            selectActivityGreeting.setText("Welcome "+user.getFullName()+". \n What do you want to do next?");
            createAHouseholdButton.setEnabled(true);
            joinAHouseholdButton.setText("Join a household");
            joinAHouseholdButton.setOnClickListener(this);
        }
    }

    /***
     * Async task that set the user status on the database to not in and household id to null
     * calls Login Async task on success
     */
    class setUserToNotInStatus extends AsyncTask<Void,Void,Boolean>{

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
                String query = "UPDATE hhm_users "+
                        "SET UserStatus = 'not in', "+
                        "HouseholdID = NULL "+
                        "WHERE UserID = " + user.getUserID();

                //postdata
                String data = DataHolder.getInstance().getUserCredentials();

                data += "&" + URLEncoder.encode("query","UTF-8") + "="
                        + URLEncoder.encode(query,"UTF-8");

                //attempt connection
                String result = con.dbTransaction(Links.DEFAULT,data);

                if(result.equals("completed")) {
                    return true;
                } else {
                    return false;
                }

            } catch (Exception e){
                Log.d("setUserToNotInStatus",e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(progressBar != null){
                progressBar.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
            if(result){
                Validator.showToast(getApplicationContext(),"Successfully Canceled! \n" +
                        "Redirecting...");
                String email = DataHolder.getInstance().getUserEmail();
                String pass = DataHolder.getInstance().getUserPass();
                new Login(getApplicationContext(),NewUserSelectionActivity.this,
                        newUserSelectionRL).execute(email,pass);
            } else {
                Validator.showToast(getApplicationContext(),"Something went wrong...");
            }
        }
    }
}