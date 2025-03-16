package com.example.smart_city_pulse;

import android.app.Application;
import android.content.Context;

import com.example.smart_city_pulse.database.DatabaseManager;
import com.example.smart_city_pulse.utils.SessionManager;

public class SmartCityPulseApp extends Application {
    private static SmartCityPulseApp instance;
    private DatabaseManager databaseManager;
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initializeComponents();
    }

    private void initializeComponents() {
        // Initialize Database Manager
        databaseManager = new DatabaseManager(this);

        // Initialize Session Manager
        sessionManager = new SessionManager(this);

        // Set default exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handleUncaughtException(throwable);
        });
    }

    private void handleUncaughtException(Throwable throwable) {
        // Log the exception
        throwable.printStackTrace();

        // Here you could implement crash reporting (e.g., Firebase Crashlytics)
        // or custom error handling logic
    }

    public static SmartCityPulseApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public void onTerminate() {
        // Close database connections
        if (databaseManager != null) {
            databaseManager.closeDatabase();
        }
        super.onTerminate();
    }

    // Method to check if user is logged in
    public boolean isUserLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    // Method to get current user ID
    public int getCurrentUserId() {
        return sessionManager != null ? sessionManager.getUserId() : -1;
    }

    // Method to get current user type
    public String getCurrentUserType() {
        return sessionManager != null ? sessionManager.getUserType() : null;
    }

    // Method to check if current user is an officer
    public boolean isCurrentUserOfficer() {
        return sessionManager != null && sessionManager.isOfficer();
    }

    // Method to logout user
    public void logoutUser() {
        if (sessionManager != null) {
            sessionManager.logout();
        }
    }

    // Method to get app version
    public String getAppVersion() {
        try {
            return getPackageManager()
                .getPackageInfo(getPackageName(), 0)
                .versionName;
        } catch (Exception e) {
            return "1.0.0";
        }
    }

    // Method to clear app data
    public void clearAppData() {
        // Clear session
        if (sessionManager != null) {
            sessionManager.logout();
        }

        // Clear database
        if (databaseManager != null) {
            databaseManager.closeDatabase();
        }

        // Clear shared preferences
        getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
    }
}
