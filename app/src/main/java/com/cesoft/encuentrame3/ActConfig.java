package com.cesoft.encuentrame3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import com.cesoft.encuentrame3.util.Log;

import java.util.List;

/**
 * A PreferenceActivity that presents a set of application settings. On handset devices, settings are presented as a single list.
 * On tablets, settings are split by category, with category headers shown to the left of the list of settings.
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActConfig extends ActConfigBase
{
	private static final String TAG = ActConfig.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setupActionBar();
	}

	@Override
	public void onHeaderClick(Header header, int position)
	{
		super.onHeaderClick(header, position);
		if(header.id == R.id.pref_logout)
		{
			//Util.logout();		//int pid = android.os.Process.myPid();   	//android.os.Process.killProcess(pid);
			//Util.return2Main(ActConfig.this, true, getString(R.string.ok_guardar));
			setResult(Activity.RESULT_OK, new Intent());
			finish();
		}
	}

	private void setupActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null)
			actionBar.setDisplayHomeAsUpEnabled(true);// Show the Up button in the action bar.
	}

	@Override
	public boolean onIsMultiPane()
	{
		return isXLargeTablet(this);
	}

	// Helper method to determine if the device has an extra-large screen. For example, 10" tablets are extra-large.
	private static boolean isXLargeTablet(Context context)
	{
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}


	// A preference value change listener that updates the preference's summary to reflect its new value.
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();
        if(preference instanceof ListPreference)
        {
            // For list preferences, look up the correct display value in the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            // Set the summary to reflect the new value.
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        }
        else if(preference instanceof RingtonePreference)
        {
            // For ringtone preferences, look up the correct display value using RingtoneManager.
            if(TextUtils.isEmpty(stringValue))
            {
                // Empty values correspond to 'silent' (no ringtone).
                preference.setSummary(R.string.pref_ringtone_silent);
            }
            else
            {
                Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                if(ringtone == null)
                {
                    // Clear the summary if there was a lookup error.
                    preference.setSummary(null);
                }
                else
                {
                    // Set the summary to reflect the new ringtone display name.
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            }
        }
        else
        {
            // For all other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    };

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target)
	{
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	// Binds a preference's summary to its value. More specifically, when the preference's value is changed, its summary (line of text below the preference title)
	// is updated to reflect the value. The summary is also immediately updated upon calling this method. The exact display format is dependent on the type of preference.
	private static void bindPreferenceSummaryToValue(Preference preference)
	{
		try
		{
			// Set the listener to watch for value changes.
			preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
			// Trigger the listener immediately with the preference's current value.
			sBindPreferenceSummaryToValueListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
		}
		catch(Exception e)
		{
			Log.e(TAG, String.format("bindPreferenceSummaryToValue:e:%s",e), e);
			Log.e(TAG, String.format("bindPreferenceSummaryToValue:e:title:%s",preference.getTitle()));
		}
	}

	// This method stops fragment injection in malicious applications. Make sure to deny any unknown fragments here.
	protected boolean isValidFragment(String fragmentName)
	{
		return PreferenceFragment.class.getName().equals(fragmentName)
				|| GeneralPreferenceFragment.class.getName().equals(fragmentName)
				|| OpcionesPreferenceFragment.class.getName().equals(fragmentName)
				|| NotificationPreferenceFragment.class.getName().equals(fragmentName)
				;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if(id == android.R.id.home)
		{
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//-------------------------------- GENERAL --------------------------------
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			setHasOptionsMenu(true);

			try{
				PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
				Preference customPref = findPreference("version");//Look at pref_general.xml
				customPref.setSummary(String.format(getString(R.string.app_vers), pInfo.versionName));
			}catch(Exception e){Log.e(TAG, String.format("GeneralPreferenceFragment:onCreate:e:%s",e), e);}
		}
		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			return false;
		}
	}

	//-------------------------------- NOTIFICATION --------------------------------
	// This fragment shows notification preferences only. It is used when the activity is showing a two-pane settings UI.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);
			setHasOptionsMenu(true);
			// Bind the summaries of EditText/List/Dialog/Ringtone preferences to their values. When their values change, their summaries are
			// updated to reflect the new value, per the Android Design guidelines.
			bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
		}
		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			int id = item.getItemId();
			if(id == android.R.id.home)
			{
				startActivity(new Intent(getActivity(), ActConfig.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}

	//-------------------------------- OPCIONES --------------------------------
	// This fragment shows data and sync preferences only. It is used when the activity is showing a two-pane settings UI.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class OpcionesPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_data_sync);
			setHasOptionsMenu(true);
		}
		@Override
		public boolean onOptionsItemSelected(MenuItem item)
		{
			int id = item.getItemId();
			if(id == android.R.id.home)
			{
				startActivity(new Intent(getActivity(), ActConfig.class));
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
	}
}
