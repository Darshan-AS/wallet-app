package com.example.android.wallet;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.wallet.data.WalletContract.TransactionEntry;

public class OverviewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static int TRANSACTIONS_LOADER_ID = 0;
    private static int OVERVIEW_LOADER_ID = 1;
    private WalletAdapter walletAdapter;
    private ListView transactionsList;
    private ViewGroup headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OverviewActivity.this, TransactionActivity.class);
                startActivity(intent);
            }
        });

        transactionsList = findViewById(R.id.transactions_list);
        transactionsList.setEmptyView(findViewById(R.id.empty_view));
        headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.overview_header, transactionsList, false);
        transactionsList.addHeaderView(headerView);
        walletAdapter = new WalletAdapter(this, null);
        transactionsList.setAdapter(walletAdapter);

        getLoaderManager().initLoader(TRANSACTIONS_LOADER_ID, null, this);
        getLoaderManager().initLoader(OVERVIEW_LOADER_ID, null, this);

        transactionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(OverviewActivity.this, TransactionActivity.class);
                Uri uri = ContentUris.withAppendedId(TransactionEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TRANSACTIONS_LOADER_ID)
            return new CursorLoader(this, TransactionEntry.CONTENT_URI, null, null, null, TransactionEntry.COLUMN_TRANSACTION_DATE + " DESC");
        else if (id == OVERVIEW_LOADER_ID)
            return new CursorLoader(this, Uri.withAppendedPath(TransactionEntry.CONTENT_URI, TransactionEntry.PATH_CATEGORY), null, null, null, null);
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == TRANSACTIONS_LOADER_ID ) {
            walletAdapter.swapCursor(cursor);
            getLoaderManager().restartLoader(OVERVIEW_LOADER_ID, null, this);
        }
        else if (loader.getId() == OVERVIEW_LOADER_ID) {
            int total_income = 0, total_expense = 0;
            while (cursor.moveToNext()) {
                int category = cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_CATEGORY));
                if (category == TransactionEntry.CATEGORY_INCOME)
                    total_income = cursor.getInt(cursor.getColumnIndex("sum(" + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")"));
                else if (category == TransactionEntry.CATEGORY_EXPENSE)
                    total_expense = cursor.getInt(cursor.getColumnIndex("sum(" + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")"));
            }
            TextView totalIncomeView = headerView.findViewById(R.id.total_income);
            totalIncomeView.setText(String.valueOf(total_income));
            TextView totalExpenseView = headerView.findViewById(R.id.total_expense);
            totalExpenseView.setText(String.valueOf(total_expense));
            TextView amountRemainingView = headerView.findViewById(R.id.amount_remaining);
            amountRemainingView.setText(String.valueOf(total_income - total_expense));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == TRANSACTIONS_LOADER_ID)
            walletAdapter.swapCursor(null);
        else if (loader.getId() == OVERVIEW_LOADER_ID)
            transactionsList.removeHeaderView(headerView);
    }
}
