package com.cesoft.encuentrame3;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActLogin extends AppCompatActivity
{
	protected static final int ENTER=0;
	protected static final int REGISTER=1;
	protected static final int RECOVER=2;

	// The android.support.v4.view.PagerAdapter will provide fragments for each of the sections. We use a FragmentPagerAdapter derivative, which will keep every loaded fragment in memory.
	// If this becomes too memory intensive, it may be best to switch to a android.support.v4.app.FragmentStatePagerAdapter
	private TabLayout tabLayout;
	public void selectTabEnter() {
		TabLayout.Tab tab = tabLayout.getTabAt(ENTER);
		if(tab != null) tab.select();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_login);

		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		ViewPager viewPager = findViewById(R.id.container);
		if(viewPager != null)
			viewPager.setAdapter(sectionsPagerAdapter);
		tabLayout = findViewById(R.id.tabs);
		if(tabLayout != null)
		{
			tabLayout.setupWithViewPager(viewPager);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		tabLayout = null;
	}

	// A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	private class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			return FrgLogin.newInstance(position, ActLogin.this);
		}
		@Override
		public int getCount()
		{
			return 3;
		}
		@Override
		public CharSequence getPageTitle(int position)
		{
			switch(position)
			{
			case ENTER:		return getString(R.string.enter_lbl);
			case REGISTER:	return getString(R.string.register_lbl);
			case RECOVER:	return getString(R.string.recover_lbl);
			default:		return null;
			}
		}
	}

	protected void goMain()
	{
		Intent intent = new Intent(getBaseContext(), ActMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	private ProgressDialog progressDialog;
	public void iniEsperaLogin()
	{
		progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
	}
	public void finEsperaLogin()
	{
		if(progressDialog!=null)progressDialog.dismiss();
	}

}
