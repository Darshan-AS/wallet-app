package com.example.android.wallet.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.wallet.data.WalletContract.TransactionEntry;


public class WalletDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wallet.db";
    private static final int DATABASE_VERSION = 1;

    public WalletDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TransactionEntry.TABLE_NAME + " ( "
                + TransactionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + " INTEGER NOT NULL DEFAULT 0, "
                + TransactionEntry.COLUMN_TRANSACTION_CATEGORY + " INTEGER NOT NULL, "
                + TransactionEntry.COLUMN_TRANSACTION_DESCRIPTION + " TEXT, "
                + TransactionEntry.COLUMN_TRANSACTION_DATE + " TEXT, "
                + TransactionEntry.COLUMN_TRANSACTION_LOCATION + " TEXT, "
                + TransactionEntry.COLUMN_TRANSACTION_IMAGE + " BLOB "
                + " );";
        db.execSQL(SQL_CREATE_TRANSACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
