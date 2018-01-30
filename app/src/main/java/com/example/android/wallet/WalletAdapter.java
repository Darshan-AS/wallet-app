package com.example.android.wallet;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.wallet.data.WalletContract.TransactionEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class WalletAdapter extends CursorAdapter {

    WalletAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.overview_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int amount = cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_AMOUNT));
        int category = cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_CATEGORY));
        String description = cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_DESCRIPTION));
        String dateString = cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_DATE));
        String location = cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_LOCATION));

        TextView amountView = view.findViewById(R.id.amount);
        amountView.setText(String.valueOf(amount));
        if (category == TransactionEntry.CATEGORY_INCOME)
            amountView.setTextColor(ContextCompat.getColor(context, R.color.income));
        else if (category == TransactionEntry.CATEGORY_EXPENSE)
            amountView.setTextColor(ContextCompat.getColor(context, R.color.expense));

        TextView descriptionView = view.findViewById(R.id.description);
        if (description.isEmpty())
            description = "No Description";
        if (!location.isEmpty()) {
            description = description + " at " + location;
            descriptionView.setText(description);
        } else
            descriptionView.setText(description);

        TextView dateView = view.findViewById(R.id.date);
        Date date;
        String dateToDisplay = dateString;
        try {
            date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(dateString);
            dateToDisplay = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            Log.e(WalletAdapter.class.getSimpleName(), "Error parsing date", e);
        }
        dateView.setText(dateToDisplay);
    }
}
