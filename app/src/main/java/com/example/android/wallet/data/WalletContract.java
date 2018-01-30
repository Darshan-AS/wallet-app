package com.example.android.wallet.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class WalletContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.wallet";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TRANSACTIONS = "transactions";

    public static final class TransactionEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRANSACTIONS);
        public static final String TABLE_NAME = "transactions";
        public static final String PATH_CATEGORY = "category";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TRANSACTION_AMOUNT = "amount";
        public static final String COLUMN_TRANSACTION_CATEGORY = "category";
        public static final String COLUMN_TRANSACTION_DESCRIPTION = "description";
        public static final String COLUMN_TRANSACTION_DATE = "date";
        public static final String COLUMN_TRANSACTION_LOCATION = "location";
        public static final String COLUMN_TRANSACTION_IMAGE = "image";

        public static final int CATEGORY_INCOME = 0;
        public static final int CATEGORY_EXPENSE = 1;

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTIONS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSACTIONS;
        public static final String CONTENT_CATEGORY_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        public static boolean isValidCategory(int category) {
            return category == CATEGORY_INCOME || category == CATEGORY_EXPENSE;
        }
    }
}
