package com.example.smart_city_pulse.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smart_city_pulse.R;
import com.example.smart_city_pulse.adapters.ComplaintAdapter;
import com.example.smart_city_pulse.database.DatabaseHelper;
import com.example.smart_city_pulse.database.DatabaseManager;
import com.example.smart_city_pulse.models.Complaint;
import com.example.smart_city_pulse.utils.Constants;
import com.example.smart_city_pulse.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ComplaintsListActivity extends AppCompatActivity {
    private RecyclerView complaintsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Spinner filterSpinner;
    private TextView emptyView;
    
    private DatabaseManager databaseManager;
    private SessionManager sessionManager;
    private ComplaintAdapter complaintAdapter;
    
    private String currentStatus;
    private boolean isOfficer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints_list);

        // Initialize managers
        databaseManager = new DatabaseManager(this);
        sessionManager = new SessionManager(this);
        isOfficer = sessionManager.isOfficer();

        // Get status from intent if provided
        currentStatus = getIntent().getStringExtra("status");
        
        // Initialize views
        initializeViews();
        setupFilterSpinner();
        loadComplaints();

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(currentStatus != null ? 
            currentStatus + " Complaints" : "All Complaints");
    }

    private void initializeViews() {
        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        filterSpinner = findViewById(R.id.filterSpinner);
        emptyView = findViewById(R.id.emptyView);

        complaintsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadComplaints();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupFilterSpinner() {
        List<String> filters = new ArrayList<>();
        filters.add("All");
        filters.add(Constants.STATUS_PENDING);
        filters.add(Constants.STATUS_IN_PROGRESS);
        filters.add(Constants.STATUS_RESOLVED);
        filters.add(Constants.STATUS_REJECTED);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            filters
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        // Set initial selection if status provided
        if (currentStatus != null) {
            int position = filters.indexOf(currentStatus);
            if (position != -1) {
                filterSpinner.setSelection(position);
            }
        }

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                currentStatus = "All".equals(selected) ? null : selected;
                loadComplaints();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadComplaints() {
        SQLiteDatabase db = databaseManager.getDatabaseManager().getReadableDatabase();
        List<Complaint> complaints = new ArrayList<>();

        String[] columns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_TITLE,
            DatabaseHelper.COLUMN_DESCRIPTION,
            DatabaseHelper.COLUMN_LOCATION,
            DatabaseHelper.COLUMN_STATUS,
            DatabaseHelper.COLUMN_USER_ID,
            DatabaseHelper.COLUMN_CREATED_AT
        };

        String selection = null;
        String[] selectionArgs = null;

        if (currentStatus != null) {
            selection = DatabaseHelper.COLUMN_STATUS + " = ?";
            selectionArgs = new String[]{currentStatus};
        }

        if (!isOfficer) {
            if (selection != null) {
                selection += " AND " + DatabaseHelper.COLUMN_USER_ID + " = ?";
                String[] newArgs = new String[selectionArgs.length + 1];
                System.arraycopy(selectionArgs, 0, newArgs, 0, selectionArgs.length);
                newArgs[selectionArgs.length] = String.valueOf(sessionManager.getUserId());
                selectionArgs = newArgs;
            } else {
                selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(sessionManager.getUserId())};
            }
        }

        String orderBy = DatabaseHelper.COLUMN_CREATED_AT + " DESC";

        Cursor cursor = db.query(
            DatabaseHelper.TABLE_COMPLAINTS,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        );

        while (cursor.moveToNext()) {
            Complaint complaint = new Complaint();
            complaint.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
            complaint.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE)));
            complaint.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)));
            complaint.setLocation(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATION)));
            complaint.setStatus(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS)));
            complaint.setUserId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
            complaint.setCreatedAt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT)));
            complaints.add(complaint);
        }
        cursor.close();

        // Update UI
        if (complaints.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            complaintsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            complaintsRecyclerView.setVisibility(View.VISIBLE);
            
            complaintAdapter = new ComplaintAdapter(complaints);
            complaintAdapter.setOnComplaintClickListener(complaint -> {
                // Handle complaint click
                // Start ComplaintDetailActivity
            });
            complaintsRecyclerView.setAdapter(complaintAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isOfficer) {
            getMenuInflater().inflate(R.menu.menu_complaints_list, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
