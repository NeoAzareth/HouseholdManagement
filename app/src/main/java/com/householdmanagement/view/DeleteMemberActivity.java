package com.householdmanagement.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.householdmanagement.R;
import com.householdmanagement.controller.DataHolder;
import com.householdmanagement.controller.Links;
import com.householdmanagement.controller.SyncHousehold;
import com.householdmanagement.controller.Validator;
import com.householdmanagement.model.DBConnection;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class removes members, not admin from a household.
 * Only update information about members to be deleted, not delete this member from database.
 * Current of past bill information will not be updated or deleted
 *
 * @author  Sicheng Zhu
 * @version  1.0
 */
public class DeleteMemberActivity extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private Intent intent;
    private List<HashMap<String, String>> memberMap;
    private SimpleAdapter adapter;
    private ListView memberListview;
    private Object[] stringArray;
    private String[][] field = null;
    private DBConnection conn = new DBConnection();
    private int householdID = DataHolder.getInstance().getHouseholdID();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_member_layout);

        memberListview = (ListView) findViewById(R.id.delete_member_listview);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        populateMemberList();
    }

    @Override
    public void onClick(View v) {

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

    private void populateMemberList() {
        String[] from = new String[] {"FullName", "Email", "DeleteButton"};
        int[] to = new int[] {R.id.fullname_textview, R.id.emailTextview, R.id.actionButton};

        memberMap = getMemberList();

        adapter = new SimpleAdapter(this, memberMap, R.layout.member_list_layout, from, to) {

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                ViewHolder holder;

                if (convertView == null) {
                    holder = new ViewHolder();

                    convertView = super.getView(position, convertView, parent);

                    holder.full_name = (TextView) convertView.findViewById(R.id.fullname_textview);
                    holder.email = (TextView) convertView.findViewById(R.id.emailTextview);
                    holder.deleteButton = (Button) convertView.findViewById(R.id.actionButton);

                    convertView.setTag(holder);
                }
                else
                    holder = (ViewHolder) convertView.getTag();

                if (position % 2 != 0)
                    convertView.setBackgroundColor(Color.argb(255, 224, 243, 250));
                else
                    convertView.setBackgroundColor(Color.argb(250, 255, 255, 255));

                holder.full_name.setText(memberMap.get(position).get("FullName"));
                holder.email.setText(memberMap.get(position).get("Email"));
                holder.deleteButton.setText(memberMap.get(position).get("DeleteButton"));

                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showInfo(position);
                    }
                });

                return convertView;
            }
        };
        memberListview.setAdapter(adapter);
    }

    private List<HashMap<String, String>> getMemberList() {
        memberMap = new ArrayList<HashMap<String, String>>();

        stringArray = (Object[]) getIntent().getExtras().getSerializable("field");

        if(stringArray != null) {
            field = new String[stringArray.length][];

            for(int i = 0;i < stringArray.length;i++)
                field[i] = (String[])stringArray[i];
        }

        // If no records received, show error message
        else
            field = new String[0][0];

        for(int i = 0; i < field.length; i++)
            for(int j = 0; j < field[0].length; j++)
                System.out.println(field[i][j]);

        for(int i = 0; i < field.length; i++) {
            HashMap<String, String> rowMap = new HashMap<String, String>();

            rowMap.put("FullName", field[i][0] + " " +field[i][1]);
            rowMap.put("Email", field[i][2]);
            rowMap.put("DeleteButton", "Delete");

            memberMap.add(rowMap);
        }
        return memberMap;
    }


    public void showInfo(final int position) {
        new AlertDialog.Builder(this).setTitle("Alert").setMessage("Are you sure you want to remove this member?")
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteMember().execute(memberMap.get(position).get("Email"));
                        System.out.println(memberMap.get(position).get("Email"));
//                        memberListview.setAdapter(adapter);
                    }
                }).show();
    }

    public final class ViewHolder {
        public TextView full_name;
        public TextView email;
        public Button deleteButton;
    }

    /***
     * Async task DeleteMember resets a member's status in database
     * calls SyncHousehold on success
     */
    class DeleteMember extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];

            try{
                //query
                String query = "UPDATE hhm_users SET UserStatus = 'not in', "+
                        "HouseholdID = NULL "+
                        "WHERE Email = '"+ email + "'";

                //post data
                String data = DataHolder.getInstance().getUserCredentials();

                data += "&" + URLEncoder.encode("query","UTF-8") + "=" +
                        URLEncoder.encode(query,"UTF-8");

                //attempt connection
                String result = conn.dbTransaction(Links.DEFAULT,data);

                //manage the result
                if(result == null) {
                    System.out.println("null error");
                    return "error";
                }

                else if(result.equals("completed"))
                    //returns completed to handle the post processing
                    return "completed";
                else if(result.equals("false")) {
                    System.out.println("wrong");
                    return "wrong";
                }

                else if(result.equals("failed")) {
                    System.out.println("failed");
                    return "failed";
                }

                else {
                    System.out.println("other error");
                    return "error";
                }//error is returned for any other reason

            } catch (Exception e){
                Log.d("Delete member",e.toString());
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String result){
            if(result.equals("completed")){
                intent = new Intent(getApplicationContext(),ManageMemberActivity.class);
                Validator.showToast(getApplicationContext(),"Member deleted");
                new SyncHousehold(intent,DeleteMemberActivity.this)
                        .execute(householdID);
            }
            else
                Validator.showToast(getApplicationContext(),"Failed to delete... Please try again later.");
        }
    }
}
