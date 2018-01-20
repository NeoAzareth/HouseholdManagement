package com.householdmanagement.controller;


import android.content.Context;
import android.view.Gravity;
import android.widget.TableRow.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;

import com.householdmanagement.R;

import java.text.NumberFormat;

/**
 * The Bill class contains properties to get attributes and methods that return table row views.
 *
 * @author   Israel Santiago, Sicheng Zhu, Huangxiao Lin
 * @version 1.0
 * @see Member class, Household class
 */
public class Bill {
    //properties
    private int billID;
    private float billAmount;
    private String billDesc, billCategory, billDate;
    private NumberFormat currecy = NumberFormat.getCurrencyInstance();

    /***
     * empty constructor
     */
    public Bill(){}

    /***
     * bill constructor with 5 parameters
     * @param billID
     * @param billAmount
     * @param billDesc bill description
     * @param billCategory
     * @param billDate
     */
    public Bill(int billID, float billAmount, String billDesc
            ,String billCategory, String billDate) {
        this.billID = billID;
        this.billAmount = billAmount;
        this.billDesc = billDesc;
        this.billCategory = billCategory;
        this.billDate = billDate;
    }


    //getters
    public int getBillID(){return billID;}

    public String getBillAmount() {return Float.toString(billAmount);}
    public String getBillAmountFormatted() {
        currecy.setMaximumFractionDigits(2);
        currecy.setMinimumFractionDigits(2);
        return currecy.format(billAmount);
    }

    public String getBillCategory() {return billCategory;}

    public String getBillDesc(){
        return billDesc;
    }

    public String getBillDate(){
        return billDate;
    }

    /***
     * getBillAsRowForDeletion returns a tablerow with bill properties for deletion
     * @param context activity context
     * @return Table row view
     */
    public TableRow getBillAsRowForDeletion(Context context){
        TableRow tr = new TableRow(context);
        tr.setTag(billID);
        tr.setLayoutParams(new TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView descTV = new TextView(context);
        descTV.setText(billDesc);
        descTV.setTextAppearance(R.style.style_table_elements);
        descTV.setLayoutParams(new LayoutParams(
                0,LayoutParams.WRAP_CONTENT,
                2f
        ));
        descTV.setPadding(15,20,0,20);
        tr.addView(descTV);

        TextView deleteBill = new TextView(context);
        deleteBill.setText("...me?");
        deleteBill.setLayoutParams(new LayoutParams(
                0,LayoutParams.WRAP_CONTENT,
                1f
        ));
        deleteBill.setTextAppearance(R.style.style_table_elements);
        deleteBill.setPadding(15,20,0,20);
        tr.addView(deleteBill);

        return tr;
    }

    /***
     * getBillAsOverviewTR returns a row with most of the properties of the bill
     * @param context activity context
     * @return table row with most properties
     */
    public TableRow getBillAsOverviewTR(Context context){
        TableRow tr = new TableRow(context);
        tr.setTag(billID);
        tr.setLayoutParams(new TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView descTV = new TextView(context);
        descTV.setText(billDesc);
        descTV.setLayoutParams(new LayoutParams(
                0,LayoutParams.WRAP_CONTENT,
                2f
        ));
        descTV.setTextAppearance(R.style.style_table_elements);
        descTV.setPadding(15,15,0,15);
        tr.addView(descTV);

        TextView amountTV = new TextView(context);
        amountTV.setText(getBillAmountFormatted());
        amountTV.setGravity(Gravity.RIGHT);
        amountTV.setLayoutParams(new LayoutParams(
                0,LayoutParams.WRAP_CONTENT,
                1f
        ));
        amountTV.setTextAppearance(R.style.style_table_elements);
        amountTV.setPadding(15,15,0,15);
        tr.addView(amountTV);

        TextView categoryTV = new TextView(context);
        categoryTV.setText(
                Character.toUpperCase(billCategory.charAt(0)) + billCategory.substring(1));
        categoryTV.setLayoutParams(new LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,1f
        ));
        categoryTV.setTextAppearance(R.style.style_table_elements);
        categoryTV.setPadding(15,15,15,15);
        categoryTV.setGravity(Gravity.RIGHT);
        tr.addView(categoryTV);

        return tr;
    }
}