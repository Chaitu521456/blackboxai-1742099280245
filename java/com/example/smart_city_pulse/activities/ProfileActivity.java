package com.example.smart_city_pulse.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_city_pulse.R;
import com.example.smart_city_pulse.database.DatabaseHelper;
import com.example.smart_city_pulse.database.DatabaseManager;
import com.example.smart_city_pulse.models.User;
import com.example.smart_city_pulse.utils.Constants;
import com.example.smart_city_pulse.utils.SessionManager;
import com.example.smart_city_pulse.utils.Utilities;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1;

    private ImageView profileImage;
    private TextView userTypeText;
    private EditText nameInput, emailInput, phoneInput;
    private Button updateButton, changePasswordButton;
    private FloatingActionButton editImageButton;

    private DatabaseManager databaseManager;
    private SessionManager sessionManager;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize managers
        databaseManager = new DatabaseManager(this);
        sessionManager = new SessionManager(this);

        // Initialize views
        initializeViews();
        loadUserProfile();

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
    }

    private void initializeViews() {
        profileImage = findViewById(R.id.profileImage);
        userTypeText = findViewById(R.id.userTypeText);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        updateButton = findViewById(R.id.updateButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        editImageButton = findViewById(R.id.editImageButton);

        // Set click listeners
        updateButton.setOnClickListener(v -> updateProfile());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        editImageButton.setOnClickListener(v -> selectProfileImage());
    }

    private void loadUserProfile() {
        SQLiteDatabase db = databaseManager.getDatabaseManager().getReadableDatabase();
        
        String[] columns = {
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_EMAIL,
            DatabaseHelper.COLUMN_PHONE,
            DatabaseHelper.COLUMN_USER_TYPE,
            DatabaseHelper.COLUMN_PROFILE_IMAGE
        };
        
        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(sessionManager.getUserId())};

        Cursor cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null
        );

        if (cursor.moveToFirst()) {
            nameInput.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
            emailInput.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL)));
            phoneInput.setText(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE)));
            
            String userType = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_TYPE));
            userTypeText.setText(Utilities.capitalizeFirstLetter(userType));

            String profileImagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PROFILE_IMAGE));
            if (profileImagePath != null && !profileImagePath.isEmpty()) {
                // Load profile image using your preferred image loading library
                // For example: Glide.with(this).load(profileImagePath).into(profileImage);
            }
        }
        cursor.close();

        // Initially disable editing
        setFieldsEditable(false);
    }

    private void setFieldsEditable(boolean editable) {
        nameInput.setEnabled(editable);
        phoneInput.setEnabled(editable);
        editImageButton.setVisibility(editable ? View.VISIBLE : View.GONE);
        updateButton.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void updateProfile() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Utilities.isValidPhone(phone)) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update database
        SQLiteDatabase db = databaseManager.getDatabaseManager().getWritableDatabase();
        String[] whereArgs = {String.valueOf(sessionManager.getUserId())};

        try {
            db.beginTransaction();

            // Update user table
            db.execSQL(
                "UPDATE " + DatabaseHelper.TABLE_USERS + " SET " +
                DatabaseHelper.COLUMN_NAME + " = ?, " +
                DatabaseHelper.COLUMN_PHONE + " = ? " +
                "WHERE " + DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{name, phone, String.valueOf(sessionManager.getUserId())}
            );

            db.setTransactionSuccessful();
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            
            // Update session
            sessionManager.updateUserDetails(name, null);
            
            // Disable editing
            setFieldsEditable(false);
            isEditing = false;
            invalidateOptionsMenu();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    private void selectProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void showChangePasswordDialog() {
        // Show change password dialog
        // Implementation depends on your password change workflow
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                // Handle the selected image
                // Update profile image in database and UI
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        menu.findItem(R.id.action_edit).setVisible(!isEditing);
        menu.findItem(R.id.action_save).setVisible(isEditing);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            isEditing = true;
            setFieldsEditable(true);
            invalidateOptionsMenu();
            return true;
        } else if (id == R.id.action_save) {
            updateProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
