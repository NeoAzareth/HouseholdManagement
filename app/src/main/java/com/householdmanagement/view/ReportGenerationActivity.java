package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.householdmanagement.R;
import com.householdmanagement.controller.Bill;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Member;
import com.householdmanagement.controller.ReportGenerationAsyncTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This class displays report criteria of report,handles and collect user's selection,and pass
 * report criteria to ReportGenerationAsyncTask class.
 *
 * @author  Sicheng zhu
 * @version  1.0
 */
public class ReportGenerationActivity extends Activity implements View.OnClickListener, OnItemSelectedListener {

    // Interface components
    private Button goButton;
    private Spinner memberSpinner;
    private Spinner categorySpinner;
    private Spinner monthSpinner;

    // Adapters to help display data to spinners
    ArrayAdapter<String> memberAdapter;
    ArrayAdapter<String> categoryAdapter;
    ArrayAdapter<String> monthAdapter;

    // Selected values for member, category, and month
    private String memberString;
    private String categoryString;
    private String monthString;

    // Indexes of selected spinner item
    private int memberSpinnerIndex;
    private int categorySpinnerIndex;
    private int monthSpinnerIndex;

    // Lists save data to be shown on spinners
    private List<String> memberList = new ArrayList<String>();
    private List<String> monthList = new ArrayList<String>();
    private List<Member> memberObjectList = new ArrayList<Member>();
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_generation_layout);

        //  button to generate report
        goButton = (Button)findViewById(R.id.go_button);
        goButton.setOnClickListener(this);

        // Call getMemberList method to retrieve all members from the same house
        memberList = getMemberList();

        // Set up spinner to display all members for a specific household
        // and for user to select any option for report generation
        memberSpinner = (Spinner) findViewById(R.id.member_spinner);
        memberAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, memberList);
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memberSpinner.setAdapter(memberAdapter);
        memberSpinner.setOnItemSelectedListener(this);
        // The default selection is the first option
        memberString = memberList.get(0);

        // Set up spinner to display all categories for a specific household
        // and for user to select any option for report generation
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        String[] categoryItems = getResources().getStringArray(R.array.allCategoryArr);
        categoryAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, categoryItems);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setOnItemSelectedListener(this);
        // The default selection is "All" categories
        categoryString = "All";

        // Call getMonthList method to retrieve a list of months starting from current month to
        // 12 months back
        monthList = getMonthList();

        // Set up spinner to display all months for a specific household
        // and for user to select any option for report generation
        monthSpinner = (Spinner) findViewById(R.id.month_spinner);
        monthAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setOnItemSelectedListener(this);

        // Allow screen rotation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        // Keyboard is hidden when activity displayed on screen
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // When user leave this app, save indexes of member, category and month spinner selected
    // and save in shared preference
    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences("SaveReportCriteria",
                Activity.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("memberSpinnerIndex", memberSpinnerIndex);
        editor.putInt("categorySpinnerIndex", categorySpinnerIndex);
        editor.putInt("monthSpinnerIndex", monthSpinnerIndex);

        editor.commit();
    }

    // When user come back to this app, retrieve values from shared preference,
    // and display the same values user selected before leave this app
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences= getSharedPreferences("SaveReportCriteria",
                Activity.MODE_PRIVATE);

        memberSpinner.setSelection(sharedPreferences.getInt("memberSpinnerIndex", 0), true);
        categorySpinner.setSelection(sharedPreferences.getInt("categorySpinnerIndex", 0), true);
        monthSpinner.setSelection(sharedPreferences.getInt("monthSpinnerIndex", 0), true);
    }

    // Event handling for go button
    // When user click go button, ReportGenerationAsyncTask will query database, and
    // return results to Report activity for data display
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.go_button:
                int householdID = DataHolder.getInstance().getHouseholdID();
                int userID = 0;

                // If user selects "All" at member spinner, set userID as -1
                if(memberString == "All")
                    userID = -1;
                // If user selects a specific member, retrieve user ID from memberList array
                else
                    for(int i = 1; i < memberList.size(); i++) {
                        if(memberString == memberList.get(i))
                            userID = memberObjectList.get(i).getUserID();
                    }

                // Call ReportGenerationAsyncTask class to query database based on user's selection
                // and display report on Report activity
                new ReportGenerationAsyncTask(this,this).execute(Integer.toString(householdID),
                        Integer.toString(userID),categoryString, monthString);
                break;
        }
    }

    // Event handler for all three spinner
    // When user select a value at any spinner, record the chosen value as a string, and the chosen index
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Adapter selectedAdapter = parent.getAdapter();

        if(selectedAdapter == memberAdapter) {
            memberString = memberAdapter.getItem(position);
            memberSpinnerIndex = position;
        }

        else if(selectedAdapter == categoryAdapter) {
            categoryString = categoryAdapter.getItem(position);
            categorySpinnerIndex = position;
        }

        else if(selectedAdapter == monthAdapter) {
            monthString = monthAdapter.getItem(position);
            monthSpinnerIndex = position;
        }
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

    // Get and return a list of all members full name of this household to be shown on memberSpinner
    private ArrayList getMemberList() {
        // Add a dummy member here to hold the first place in memberObjectList so that the following
        // members in memberObjectList matches itself in memberStringList
        Member selectedMember = new Member(0, null, null, null, null, new ArrayList<Bill>());
        memberObjectList.add(selectedMember);

        // Get member object from DataHolder and put into memberObjectList iteratively
        for(Member member: DataHolder.getInstance().getHousehold().getMembers())
            memberObjectList.add(member);

        // Create memberStringList to store all members' full name
        ArrayList<String> memberStringList = new ArrayList<>();
        // The first option on spinner is All
        memberStringList.add("All");

        // Get members's full name from memberObjectList and put into memberStringList iteratively
        for(int i = 1; i < memberObjectList.size(); i++)
            memberStringList.add(memberObjectList.get(i).getFullName());

        return memberStringList;
    }

    // Get and return a list of months starting from current month to 12 months back
    // to be shown on monthSpinner using format "YYYY-MM"
    private ArrayList getMonthList() {
        Calendar calendar = Calendar.getInstance();
        ArrayList<String> monthStringList = new ArrayList<>();
        Date tasktime;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM");

        for(int i = 0; i < 12; i++) {
            tasktime = calendar.getTime();
            monthStringList.add(sDateFormat.format(tasktime));
            calendar.add(Calendar.MONTH, -1);
        }

        return monthStringList;
    }
}

//http://blog.sina.com.cn/s/blog_46798aa80102vuh6.html
//http://www.360doc.com/content/11/0808/11/7471983_138871427.shtml