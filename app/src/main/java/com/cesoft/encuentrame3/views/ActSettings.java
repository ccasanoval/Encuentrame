package com.cesoft.encuentrame3.views;

import android.content.pm.PackageInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by César Casanova
public class ActSettings extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = ActSettings.class.getSimpleName();
    private static final String TITLE = "title";

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private int id;
        SettingsFragment(int id) {
            this.id = id;
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            try {
                setPreferencesFromResource(id, rootKey);
                if(id == R.xml.settings_about) {
                    Preference preference = findPreference("version");
                    if(preference != null) {
                        App app = App.getInstance();
                        PackageInfo pInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                        String version = getString(R.string.app_vers, pInfo.versionName);
                        preference.setSummary(version);
                    }
                }
            }
            catch (Exception e) {
                Log.e(TAG, "onCreatePreferences:e:---------------------------------------------", e);
            }
        }
    }
    private String titleMain = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_settings);

        if(savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            ActSettings.SettingsFragment frg;
            int page = getIntent().getIntExtra(Constantes.SETTINGS_PAGE, -1);
            switch(page) {
                case R.id.nav_about:
                    frg = new ActSettings.SettingsFragment(R.xml.settings_about);
                    titleMain = getString(R.string.nav_about);
                    break;
                case R.id.nav_voice:
                    frg = new ActSettings.SettingsFragment(R.xml.settings_voice);
                    titleMain = getString(R.string.nav_voice);
                    break;
                default:
                case R.id.nav_config:
                    frg = new ActSettings.SettingsFragment(R.xml.settings_options);
                    titleMain = getString(R.string.nav_config);
                    break;
            }
            fm.beginTransaction().replace(R.id.settings, frg).commit();
            setTitle(titleMain);
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
