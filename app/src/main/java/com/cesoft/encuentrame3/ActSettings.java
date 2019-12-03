package com.cesoft.encuentrame3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActSettings extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "ActSettings";
    private static final String TITLE = "title";

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);
            Preference preference = findPreference("setting_logout");
            if(preference != null)
                preference.setOnPreferenceClickListener(v -> {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.setResult(Activity.RESULT_OK, new Intent());
                        activity.finish();
                    }
                    return true;
                });
        }
    }
    public static class OptionsPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_options, rootKey);
        }
    }
    public static class VoicePreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_voice, rootKey);
        }
    }
    public static class NotificationsPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_notifications, rootKey);
        }
    }
    public static class AboutPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_about, rootKey);
            //getPreferenceScreen()//TODO: change version number...
        }
    }

    private String titleMain = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_settings);
        FragmentManager fm = getSupportFragmentManager();
        if(savedInstanceState == null) {
            fm.beginTransaction()
                    .replace(R.id.settings, new ActSettings.SettingsFragment())
                    .commit();
            titleMain = getTitle().toString();
        }
        else {
            setTitle(savedInstanceState.getCharSequence(TITLE));
        }
        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(getSupportFragmentManager().popBackStackImmediate()) {
            FragmentManager fm = getSupportFragmentManager();
            if(fm.getBackStackEntryCount() == 0)
                setTitle(titleMain);
            return true;
        }
        finish();
        return super.onSupportNavigateUp();
    }

    /// implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        FragmentManager fm = getSupportFragmentManager();
        Bundle args = pref.getExtras();
        Fragment fragment = fm.getFragmentFactory().instantiate(getClassLoader(), pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        fm.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }
}
