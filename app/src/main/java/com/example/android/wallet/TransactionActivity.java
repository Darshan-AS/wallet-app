package com.example.android.wallet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.android.wallet.data.WalletContract.TransactionEntry;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TransactionActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText amountEditText;
    private RadioGroup categoryRadioGroup;
    private EditText descriptionEditText;
    private EditText dateEditText;
    private EditText locationEditText;
    private ImageView galleryImageView;
    private ImageView cameraImageView;
    private ImageView imageView;

    private static final int LOADER_ID = 1;
    private static final int REQUEST_IMAGE_FROM_CAMERA = 1;
    private static final int REQUEST_IMAGE_FROM_GALLERY = 2;
    private static final String FILE_PROVIDER_CONTENT_AUTHORITY = "com.example.android.fileprovider";
    private Uri imageUri = null;
    private String date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
    private int category = -1;
    private Uri uriReceived = null;
    private boolean transactionHasChanged = false;

    private View.OnClickListener selectDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            Dialog dialog = new DatePickerDialog(TransactionActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth);
                    Date d = c.getTime();
                    date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(d);
                    dateEditText.setText(formatDate(date));
                    if (uriReceived == null)
                        transactionHasChanged = true;
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        }
    };

    private RadioGroup.OnCheckedChangeListener categoryChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (uriReceived == null)
                transactionHasChanged = true;
            switch (checkedId) {
                case R.id.income:
                    category = TransactionEntry.CATEGORY_INCOME;
                    break;
                case R.id.expense:
                    category = TransactionEntry.CATEGORY_EXPENSE;
                    break;
            }
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            transactionHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_activity);

        uriReceived = getIntent().getData();
        if (uriReceived == null) {
            setTitle(getString(R.string.add_transaction_title));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_transaction_title));
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }

        amountEditText = findViewById(R.id.amount);
        categoryRadioGroup = findViewById(R.id.category);
        descriptionEditText = findViewById(R.id.description);
        dateEditText = findViewById(R.id.select_date);
        locationEditText = findViewById(R.id.location);
        galleryImageView = findViewById(R.id.get_from_gallery);
        cameraImageView = findViewById(R.id.get_from_camera);
        imageView = findViewById(R.id.image);

        dateEditText.setOnClickListener(selectDateClickListener);
        categoryRadioGroup.setOnCheckedChangeListener(categoryChangeListener);
        amountEditText.setOnTouchListener(onTouchListener);
        categoryRadioGroup.setOnTouchListener(onTouchListener);
        descriptionEditText.setOnTouchListener(onTouchListener);
        dateEditText.setOnTouchListener(onTouchListener);
        locationEditText.setOnTouchListener(onTouchListener);
        galleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUEST_IMAGE_FROM_GALLERY);
            }
        });
        cameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File imageFile = null;
                    try {
                        imageFile = createImageFile();
                    } catch (IOException e) {
                        Log.e(TransactionActivity.class.getSimpleName(), "Error creating image file", e);
                    }
                    if (imageFile != null) {
                        imageUri = FileProvider.getUriForFile(TransactionActivity.this, FILE_PROVIDER_CONTENT_AUTHORITY, imageFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_FROM_CAMERA);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_FROM_CAMERA && resultCode == RESULT_OK)
            bindImageToView();
        else if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            imageUri = data.getData();
            Bitmap imageBitmap;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                Log.e(TransactionActivity.class.getSimpleName(), "Error retrieving Image from external storage", e);
            }
        }
    }

    private void bindImageToView() {
        Bitmap imageBitmap;
        try {
            imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            imageView.setImageBitmap(imageBitmap);
        } catch (IOException e) {
            Log.e(TransactionActivity.class.getSimpleName(), "Error retrieving Image from external storage", e);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "Transaction_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.transition_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveTransaction();
                return true;
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:
                if (!transactionHasChanged) {
                    NavUtils.navigateUpFromSameTask(TransactionActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListner =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(TransactionActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListner);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        super.onPrepareOptionsPanel(view, menu);
        if (uriReceived == null) {
            MenuItem deleteMenuItem = menu.findItem(R.id.delete);
            deleteMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, uriReceived, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst())
            return;
        cursor.moveToFirst();
        int amount = cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_AMOUNT));
        int category = cursor.getInt(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_CATEGORY));
        String description = cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_DESCRIPTION));
        String date = cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_DATE));
        String location = cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_LOCATION));
        String image = cursor.getString(cursor.getColumnIndex(TransactionEntry.COLUMN_TRANSACTION_IMAGE));

        amountEditText.setText(String.valueOf(amount));
        if (category == TransactionEntry.CATEGORY_INCOME)
            categoryRadioGroup.check(R.id.income);
        else if (category == TransactionEntry.CATEGORY_EXPENSE)
            categoryRadioGroup.check(R.id.expense);
        descriptionEditText.setText(description);
        dateEditText.setText(formatDate(date));
        locationEditText.setText(location);
        if (image != null) {
            imageUri = Uri.parse(image);
            bindImageToView();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        amountEditText.setText(null);
        categoryRadioGroup.clearCheck();
        descriptionEditText.setText(null);
        dateEditText.setText(R.string.today);
        locationEditText.setText(null);
        imageView.setImageBitmap(null);
    }

    @Override
    public void onBackPressed() {
        if (!transactionHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog unsavedChangesDialog = builder.create();
        unsavedChangesDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_transaction_dialog_message);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTransaction();
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog deleteConfirmationDialog = builder.create();
        deleteConfirmationDialog.show();
    }

    private void saveTransaction() {
        String amountString = amountEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String imageUriString = null;

        if (amountString.isEmpty()) {
            amountEditText.setError("Amount is required");
            return;
        }

        if (!TransactionEntry.isValidCategory(category)) {
            RadioButton lastRadioButton = findViewById(R.id.expense);
            lastRadioButton.setError("Category not selected");
            return;
        }

        if (imageUri  != null)
            imageUriString = imageUri.toString();

        int amount = Integer.parseInt(amountString);

        ContentValues values = new ContentValues();
        values.put(TransactionEntry.COLUMN_TRANSACTION_AMOUNT, amount);
        values.put(TransactionEntry.COLUMN_TRANSACTION_CATEGORY, category);
        values.put(TransactionEntry.COLUMN_TRANSACTION_DESCRIPTION, description);
        values.put(TransactionEntry.COLUMN_TRANSACTION_DATE, date);
        values.put(TransactionEntry.COLUMN_TRANSACTION_LOCATION, location);
        values.put(TransactionEntry.COLUMN_TRANSACTION_IMAGE, imageUriString);


        if (uriReceived == null) {
            Uri uri = getContentResolver().insert(TransactionEntry.CONTENT_URI, values);
            if (uri == null)
                Toast.makeText(this, R.string.error_inserting_transaction, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.transaction_inserted_successfully, Toast.LENGTH_LONG).show();
        } else {
            int rowsUpdated = getContentResolver().update(uriReceived, values, null, null);
            if (rowsUpdated == 0)
                Toast.makeText(this, R.string.error_updating_transaction, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.transaction_updated_successfully, Toast.LENGTH_LONG).show();
        }

        finish();
    }

    private void deleteTransaction() {
        if (uriReceived == null)
            return;

        int rowsDeleted = getContentResolver().delete(uriReceived, null, null);
        if (rowsDeleted == 0)
            Toast.makeText(this, R.string.error_deleting_transaction, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, R.string.transaction_deleted_successfully, Toast.LENGTH_LONG).show();
    }

    private String formatDate(String dateString) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(dateString);
            return new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            Log.e(TransactionActivity.class.getSimpleName(), "Error parsing date", e);
        }
        return dateString;
    }
}
