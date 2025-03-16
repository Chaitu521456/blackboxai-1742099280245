package com.example.smart_city_pulse.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;

import com.example.smart_city_pulse.R;
import com.example.smart_city_pulse.SmartCityPulseApp;
import com.example.smart_city_pulse.utils.Constants;
import com.example.smart_city_pulse.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Add settings fragment
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.settings_container, new SettingsFragment())
            .commit();

        // Set up action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
    }

    public static class SettingsFragment extends PreferenceFragmentCompat 
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SessionManager sessionManager;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            sessionManager = new SessionManager(requireContext());

            // Initialize preferences
            initializePreferences();

            // Set up preference change listeners
            setupPreferenceListeners();
        }

        private void initializePreferences() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

            // Update notification preference summary
            Preference notificationPref = findPreference("notifications_enabled");
            if (notificationPref != null) {
                notificationPref.setSummary(
                    prefs.getBoolean("notifications_enabled", true) ?
                    "Enabled" : "Disabled"
                );
            }

            // Update language preference summary
            Preference languagePref = findPreference("app_language");
            if (languagePref != null) {
                String language = prefs.getString("app_language", "en");
                languagePref.setSummary(getLanguageName(language));
            }

            // Update theme preference summary
            Preference themePref = findPreference("app_theme");
            if (themePref != null) {
                String theme = prefs.getString("app_theme", "light");
                themePref.setSummary(getThemeName(theme));
            }
        }

        private void setupPreferenceListeners() {
            // Clear Data preference
            Preference clearDataPref = findPreference("clear_data");
            if (clearDataPref != null) {
                clearDataPref.setOnPreferenceClickListener(preference -> {
                    showClearDataDialog();
                    return true;
                });
            }

            // About preference
            Preference aboutPref = findPreference("about");
            if (aboutPref != null) {
                String versionName = ((SmartCityPulseApp) requireActivity().getApplication()).getAppVersion();
                aboutPref.setSummary("Version " + versionName);
                aboutPref.setOnPreferenceClickListener(preference -> {
                    showAboutDialog();
                    return true;
                });
            }

            // Privacy Policy preference
            Preference privacyPref = findPreference("privacy_policy");
            if (privacyPref != null) {
                privacyPref.setOnPreferenceClickListener(preference -> {
                    showPrivacyPolicy();
                    return true;
                });
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            if (preference == null) return;

            switch (key) {
                case "notifications_enabled":
                    boolean enabled = sharedPreferences.getBoolean(key, true);
                    preference.setSummary(enabled ? "Enabled" : "Disabled");
                    updateNotificationSettings(enabled);
                    break;

                case "app_language":
                    String language = sharedPreferences.getString(key, "en");
                    preference.setSummary(getLanguageName(language));
                    updateAppLanguage(language);
                    break;

                case "app_theme":
                    String theme = sharedPreferences.getString(key, "light");
                    preference.setSummary(getThemeName(theme));
                    updateAppTheme(theme);
                    break;
            }
        }

        private String getLanguageName(String code) {
            switch (code) {
                case "en": return "English";
                case "es": return "Spanish";
                case "fr": return "French";
                default: return "English";
            }
        }

        private String getThemeName(String theme) {
            switch (theme) {
                case "light": return "Light";
                case "dark": return "Dark";
                case "system": return "System Default";
                default: return "Light";
            }
        }

        private void updateNotificationSettings(boolean enabled) {
            // Implement notification settings update
            Toast.makeText(requireContext(), 
                "Notifications " + (enabled ? "enabled" : "disabled"),
                Toast.LENGTH_SHORT).show();
        }

        private void updateAppLanguage(String language) {
            // Implement language change
            Toast.makeText(requireContext(),
                "Language changed to " + getLanguageName(language),
                Toast.LENGTH_SHORT).show();
            requireActivity().recreate();
        }

        private void updateAppTheme(String theme) {
            // Implement theme change
            Toast.makeText(requireContext(),
                "Theme changed to " + getThemeName(theme),
                Toast.LENGTH_SHORT).show();
            requireActivity().recreate();
        }

        private void showClearDataDialog() {
            // Show confirmation dialog before clearing data
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear App Data")
                .setMessage("Are you sure you want to clear all app data? This action cannot be undone.")
                .setPositiveButton("Clear Data", (dialog, which) -> {
                    ((SmartCityPulseApp) requireActivity().getApplication()).clearAppData();
                    Toast.makeText(requireContext(), "App data cleared", Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        }

        private void showAboutDialog() {
            // Show app information dialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("About Smart City Pulse")
                .setMessage("Smart City Pulse is a citizen engagement platform that " +
                    "enables efficient communication between citizens and city officials.\n\n" +
                    "Version: " + ((SmartCityPulseApp) requireActivity().getApplication()).getAppVersion())
                .setPositiveButton("OK", null)
                .show();
        }

        private void showPrivacyPolicy() {
            // Show privacy policy
            // You could either show a dialog or start a new activity with WebView
            Toast.makeText(requireContext(), "Privacy Policy", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
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
