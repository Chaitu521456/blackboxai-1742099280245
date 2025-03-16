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
import com.example.smart_city_pulse.adapters.FeedbackAdapter;
import com.example.smart_city_pulse.database.DatabaseHelper;
import com.example.smart_city_pulse.database.DatabaseManager;
import com.example.smart_city_pulse.models.Feedback;
import com.example.smart_city_pulse.utils.Constants;
import com.example.smart_city_pulse.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class FeedbackListActivity extends AppCompatActivity {
    private RecyclerView feedbackRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Spinner serviceTypeSpinner;
    private TextView emptyView, averageRatingText;
    
    private DatabaseManager databaseManager;
    private SessionManager sessionManager;
    private FeedbackAdapter feedbackAdapter;
    
    private String currentServiceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);

        // Initialize managers
        databaseManager = new DatabaseManager(this);
        sessionManager = new SessionManager(this);

        // Initialize views
        initializeViews();
        setupServiceTypeSpinner();
        loadFeedback();

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Feedback & Ratings");
    }

    private void initializeViews() {
        feedbackRecyclerView = findViewById(R.id.feedbackRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        serviceTypeSpinner = findViewById(R.id.serviceTypeSpinner);
        emptyView = findViewById(R.id.emptyView);
        averageRatingText = findViewById(R.id.averageRatingText);

        feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFeedback();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupServiceTypeSpinner() {
        List<String> serviceTypes = new ArrayList<>();
        serviceTypes.add("All Services");
        for (String service : Constants.SERVICE_TYPES) {
            serviceTypes.add(service);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            serviceTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(adapter);

        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                currentServiceType = "All Services".equals(selected) ? null : selected;
                loadFeedback();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFeedback() {
        SQLiteDatabase db = databaseManager.getDatabaseManager().getReadableDatabase();
        List<Feedback> feedbackList = new ArrayList<>();

        String[] columns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_RATING,
            DatabaseHelper.COLUMN_COMMENT,
            DatabaseHelper.COLUMN_SERVICE_TYPE,
            DatabaseHelper.COLUMN_USER_ID,
            DatabaseHelper.COLUMN_CREATED_AT
        };

        String selection = null;
        String[] selectionArgs = null;

        if (currentServiceType != null) {
            selection = DatabaseHelper.COLUMN_SERVICE_TYPE + " = ?";
            selectionArgs = new String[]{currentServiceType};
        }

        if (!sessionManager.isOfficer()) {
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
            DatabaseHelper.TABLE_FEEDBACK,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        );

        float totalRating = 0;
        int count = 0;

        while (cursor.moveToNext()) {
            Feedback feedback = new Feedback();
            feedback.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
            feedback.setRating(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_RATING)));
            feedback.setComment(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_COMMENT)));
            feedback.setServiceType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SERVICE_TYPE)));
            feedback.setUserId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
            feedback.setCreatedAt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATED_AT)));
            feedbackList.add(feedback);

            totalRating += feedback.getRating();
            count++;
        }
        cursor.close();

        // Update average rating
        if (count > 0) {
            float averageRating = totalRating / count;
            averageRatingText.setText(String.format("Average Rating: %.1f ★", averageRating));
            averageRatingText.setVisibility(View.VISIBLE);
        } else {
            averageRatingText.setVisibility(View.GONE);
        }

        // Update UI
        if (feedbackList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            feedbackRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            feedbackRecyclerView.setVisibility(View.VISIBLE);
            
            feedbackAdapter = new FeedbackAdapter(feedbackList);
            feedbackRecyclerView.setAdapter(feedbackAdapter);
        }
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
