package com.example.android.wallet.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.wallet.data.WalletContract.TransactionEntry;


public class WalletProvider extends ContentProvider {

    public static final int TRANSACTIONS = 1;
    public static final int TRANSACTION_ID = 2;
    public static final int TRANSACTION_CATEGORY = 3;

    private WalletDbHelper walletDbHelper;
    public static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(WalletContract.CONTENT_AUTHORITY, WalletContract.PATH_TRANSACTIONS, TRANSACTIONS);
        uriMatcher.addURI(WalletContract.CONTENT_AUTHORITY, WalletContract.PATH_TRANSACTIONS + "/#", TRANSACTION_ID);
        uriMatcher.addURI(WalletContract.CONTENT_AUTHORITY, WalletContract.PATH_TRANSACTIONS + "/category", TRANSACTION_CATEGORY);
    }

    @Override
    public boolean onCreate() {
        walletDbHelper = new WalletDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase walletDb = walletDbHelper.getReadableDatabase();
        final int match = uriMatcher.match(uri);
        Cursor cursor;
        switch (match) {
            case TRANSACTIONS:
                cursor = walletDb.query(TransactionEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TRANSACTION_ID:
                selection = TransactionEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = walletDb.query(TransactionEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TRANSACTION_CATEGORY:
                projection = new String[] {TransactionEntry.COLUMN_TRANSACTION_CATEGORY, "sum(" + TransactionEntry.COLUMN_TRANSACTION_AMOUNT + ")"};
                String groupBy = TransactionEntry.COLUMN_TRANSACTION_CATEGORY;
                sortOrder = TransactionEntry.COLUMN_TRANSACTION_CATEGORY + " ASC";
                cursor = walletDb.query(TransactionEntry.TABLE_NAME, projection, selection, selectionArgs, groupBy, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Error querying. Invalid URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                assert values != null;
                return insertTransaction(uri, values);
            default:
                throw new IllegalArgumentException("Error inserting. Invalid URI: " + uri);
        }
    }

    private Uri insertTransaction(Uri uri, ContentValues values) {
        Integer amount = values.getAsInteger(TransactionEntry.COLUMN_TRANSACTION_AMOUNT);
        if (amount != null && amount < 0)
            throw new IllegalArgumentException("Amount can't br negative");

        int category = values.getAsInteger(TransactionEntry.COLUMN_TRANSACTION_CATEGORY);
        if (!TransactionEntry.isValidCategory(category))
            throw new IllegalArgumentException("Invalid category");

        SQLiteDatabase walletDb = walletDbHelper.getWritableDatabase();
        long id = walletDb.insert(TransactionEntry.TABLE_NAME, null, values);
        if (id == -1)
            Log.e(WalletProvider.class.getSimpleName(), "Error inserting transaction");
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                assert values != null;
                return updateTransaction(uri, values, selection, selectionArgs);
            case TRANSACTION_ID:
                selection = TransactionEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                assert values != null;
                return updateTransaction(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Error updating: Invalid URI: " + uri);
        }
    }

    private int updateTransaction(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(TransactionEntry.COLUMN_TRANSACTION_AMOUNT)) {
            Integer amount = values.getAsInteger(TransactionEntry.COLUMN_TRANSACTION_AMOUNT);
            if (amount != null && amount < 0)
                throw new IllegalArgumentException("Amount can't br negative");
        }

        if (values.containsKey(TransactionEntry.COLUMN_TRANSACTION_CATEGORY)) {
            int category = values.getAsInteger(TransactionEntry.COLUMN_TRANSACTION_CATEGORY);
            if (!TransactionEntry.isValidCategory(category))
                throw new IllegalArgumentException("Invalid category");
        }

        if (values.size() == 0)
            return 0;

        SQLiteDatabase walletDb = walletDbHelper.getWritableDatabase();
        int rowsUpdated = walletDb.update(TransactionEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated == -1)
            Log.e(WalletProvider.class.getSimpleName(), "Error updating rows");

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase walletDb = walletDbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = uriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                rowsDeleted = walletDb.delete(TransactionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSACTION_ID:
                selection = TransactionEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = walletDb.delete(TransactionEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Error deleting: Invalid URI: " + uri);
        }

        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                return TransactionEntry.CONTENT_LIST_TYPE;
            case TRANSACTION_ID:
                return TransactionEntry.CONTENT_ITEM_TYPE;
            case TRANSACTION_CATEGORY:
                return TransactionEntry.CONTENT_CATEGORY_TYPE;
            default:
                throw new IllegalStateException("Unknown URI: " + uri);
        }
    }
}
