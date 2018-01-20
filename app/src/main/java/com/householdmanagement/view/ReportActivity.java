package com.householdmanagement.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This ReportActivity class show actual report about members' expenses based on user's criteria.
 *
 * @author  Sicheng zhu
 * @version  1.0
 */
public class ReportActivity extends Activity implements View.OnClickListener {
    private Button backButton;
    private Intent intent;
    private String[][] field = null;
    private ListView recordListview;
    private TextView totalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_layout);

        backButton = (Button)findViewById(R.id.back_button);
        backButton.setOnClickListener(this);

        recordListview = (ListView) findViewById(R.id.record_listview);
        totalTextView = (TextView) findViewById(R.id.total_textview);

        // Get all database records from ReportGenerationAsyncTask class
        Object[] stringArray = (Object[]) getIntent().getExtras().getSerializable("field");

        // Further divide all records if any records received
        if(stringArray != null) {
            field = new String[stringArray.length][];

            for(int i = 0;i < stringArray.length;i++)
                field[i] = (String[])stringArray[i];
        }
        // If no records received, show error message
        else
            Toast.makeText(getApplicationContext(), "Something went wrong.\n"
                    + "Please click Back button and try again.", Toast.LENGTH_LONG).show();

        // create the report table item mapping
        String[] from = new String[] {"User", "Amount", "Description"};
        int[] to = new int[] {R.id.userID_textview, R.id.billAmount_textview, R.id.description_textview};

        // put the list of all records into hashmap
        List<HashMap<String, String>> reportMap = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < field.length; i++){
            HashMap<String, String> rowMap = new HashMap<String, String>();

            rowMap.put("User", field[i][0] + " " +field[i][1]);
            rowMap.put("Amount", "$" + field[i][2]);
            rowMap.put("Description", field[i][3]);

            reportMap.add(rowMap);
        }

        // Use SimpleAdapter class to show list of records on ReportActivity, and draw background colors
        // for each line
        SimpleAdapter adapter = new SimpleAdapter(this, reportMap, R.layout.report_record_layout, from, to) {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (position % 2 != 0)
                    view.setBackgroundColor(Color.argb(255, 224, 243, 250));
                else
                    view.setBackgroundColor(Color.argb(250, 255, 255, 255));

                return view;
            }
        };
        recordListview.setAdapter(adapter);

        // Display total expenses from all members in a specific month
        String totalInStringFormat = calculateTotal(field);
        if(totalInStringFormat.equals("")) {
            totalTextView.setText("Total: $0");
            Toast.makeText(getApplicationContext(), "Something went wrong.\n"+
                    "Please click back button and try again.",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            totalTextView.setText("Total: $" + totalInStringFormat);
        }


        for(int i = 0; i < field.length; i++)
            for(int j = 0; j < 4; j++)
                Log.d("field",field[i][j]);

        // Allow screen rotation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        // Keyboard is hidden when activity displayed on screen
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

    // Click event handler for back button
    // If clicked, go back to ReportGenerationActivity
    @Override
    public void onClick(View v){
        switch (v.getId()) {
            case R.id.back_button:
                Intent intent=new Intent(getApplicationContext(),ReportGenerationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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

    /* This method calculate all members' expenses in the same household for a specific month
     * @param stringArray a two dimensional array stores all database fields that match user's criteria
     */
    private String calculateTotal(String[][] stringArray) {
        if(stringArray == null || stringArray.length == 0)
            return "";

        for(int i = 0; i < stringArray.length; i++)
            if(stringArray[i] == null || stringArray[i].length < 3)
                return "";

        for(int i = 0; i < stringArray.length; i++)
            if(Float.parseFloat(stringArray[i][2]) < 0)
                return "";

        float total = 0;
        for(int i = 0; i <stringArray.length; i++)
            total += Float.parseFloat(stringArray[i][2]);

        DecimalFormat dFormat = new DecimalFormat("##0.00");
        return dFormat.format(total);
    }
}



//http://www.open-open.com/lib/view/open1333372619624.html

