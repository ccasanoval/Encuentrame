package com.cesoft.encuentrame3;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
//https://www.firebase.com/docs/web/guide/user-auth.html
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActLogin extends AppCompatActivity
{
	private static final String TAG = "CESoft:ActLogin:";
	private static final int ENTER=0, REGISTER=1, RECOVER=2;
	//private static ActLogin _this;

	// The android.support.v4.view.PagerAdapter will provide fragments for each of the sections. We use a FragmentPagerAdapter derivative, which will keep every loaded fragment in memory.
	// If this becomes too memory intensive, it may be best to switch to a android.support.v4.app.FragmentStatePagerAdapter
	public TabLayout _tabLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_login);

		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		SectionsPagerAdapter SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		ViewPager viewPager = (ViewPager)findViewById(R.id.container);
		if(viewPager != null)
		viewPager.setAdapter(SectionsPagerAdapter);
		_tabLayout = (TabLayout)findViewById(R.id.tabs);
		if(_tabLayout != null)
		{
			_tabLayout.setupWithViewPager(viewPager);
			_tabLayout.setSelectedTabIndicatorHeight(10);
		}

		//
		//--- INICIAR
		//Crea servicio si no está ya creado, dentro del servicio se llama a login
		startService(new Intent(this, CesService.class));
		// Rute Widget Svc
		WidgetRutaService.startServ(this);
	}

	public void onDestroy()
	{
		super.onDestroy();
		_tabLayout = null;
	}

	// A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			return PlaceholderFragment.newInstance(position, ActLogin.this);
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
			case ENTER:
				return getString(R.string.enter_lbl);
			case REGISTER:
				return getString(R.string.register_lbl);
			case RECOVER:
				return getString(R.string.recover_lbl);
			}
			return null;
		}
	}

	private void goMain()
	{
		Intent intent = new Intent(getBaseContext(), ActMain.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	private ProgressDialog _progressDialog;
	public void iniEsperaLogin()
	{
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
	}
	public void finEsperaLogin()
	{
		if(_progressDialog!=null)_progressDialog.dismiss();
	}

	//----------------------------------------------------------------------------------------------
	//
	//----------------------------------------------------------------------------------------------
	public static class PlaceholderFragment extends Fragment
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		ActLogin _main;

		// Returns a new instance of this fragment for the given section number.
		public static PlaceholderFragment newInstance(int sectionNumber, ActLogin main)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			fragment._main = main;
			return fragment;
		}

		public PlaceholderFragment(){}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			if(Login.isLogged())_main.goMain();

			final int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
			final View rootView = inflater.inflate(R.layout.act_login_frag, container, false);
			final TextView lblTitulo = (TextView)rootView.findViewById(R.id.lblTitulo);
			final EditText txtPassword = (EditText)rootView.findViewById(R.id.txtPassword);
			final EditText txtPassword2 = (EditText)rootView.findViewById(R.id.txtPassword2);
			final EditText txtEmail = (EditText)rootView.findViewById(R.id.txtEmail);
			final Button btnSend = (Button)rootView.findViewById(R.id.btnSend);

			final TextInputLayout lblPassword = (TextInputLayout)rootView.findViewById(R.id.lblPassword);
			final TextInputLayout lblPassword2 = (TextInputLayout)rootView.findViewById(R.id.lblPassword2);

			switch(sectionNumber)
			{
			case ENTER://----------------------- ----------------------------------------------------
				lblPassword2.setVisibility(View.GONE);
				lblTitulo.setText(getString(R.string.enter_lbl));
				btnSend.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						_main.iniEsperaLogin();
						Login.login(_main.getApplicationContext(), txtEmail.getText().toString(), txtPassword.getText().toString(),
								new Login.AuthListener()
								{
									@Override
									public void onExito(FirebaseUser usr)
									{
										_main.finEsperaLogin();
										Toast.makeText(_main, String.format(getString(R.string.login_ok), usr.getEmail()), Toast.LENGTH_LONG).show();
										_main.goMain();
									}
									@Override
									public void onFallo(Exception e)
									{
										_main.finEsperaLogin();
										Toast.makeText(_main, getString(R.string.login_error), Toast.LENGTH_LONG).show();
									}
								});
					}
				});
				break;

			case REGISTER://------------------------------------------------------------------------
				lblTitulo.setText(getString(R.string.register_lbl));
				btnSend.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if( ! txtPassword.getText().toString().equals(txtPassword2.getText().toString()))
						{
							Toast.makeText(_main, getString(R.string.register_bad_pass), Toast.LENGTH_LONG).show();
							return;
						}
						_main.iniEsperaLogin();
						Login.addUser(txtEmail.getText().toString(), txtPassword.getText().toString(),
								new Login.AuthListener()
								{
									@Override
									public void onExito(FirebaseUser usr)
									{
										_main.finEsperaLogin();
										if(usr != null)//TODO: string value
										Toast.makeText(_main, getString(R.string.register_ok)+"  "+usr.getEmail(), Toast.LENGTH_LONG).show();
									}
									@Override
									public void onFallo(Exception e)
									{
										_main.finEsperaLogin();//TODO: Añadir %s en la cadena
										Toast.makeText(_main, getString(R.string.register_ko)+"  "+e, Toast.LENGTH_LONG).show();
									}
								});
					}
				});
				break;

			case RECOVER://-------------------------------------------------------------------------
				lblPassword.setVisibility(View.GONE);
				lblPassword2.setVisibility(View.GONE);
				lblTitulo.setText(getString(R.string.recover_lbl));
				btnSend.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Login.restoreUser(txtEmail.getText().toString(),
							new Login.AuthListener()
							{
								@Override
								public void onExito(FirebaseUser usr)
								{
									//System.err.println("ActLogin:RECOVER:"+usr);
									Toast.makeText(rootView.getContext(), R.string.recover_ok, Toast.LENGTH_LONG).show();
									TabLayout.Tab t = _main._tabLayout.getTabAt(ENTER);
									if(t!=null)t.select();
								}
								@Override
								public void onFallo(Exception e)
								{
									Log.e(TAG, String.format("RECOVER:e:%s",e), e);//TODO: formatear y tradiucir mensaje?
									Toast.makeText(rootView.getContext(), R.string.recover_ko +"  "+ e.toString(), Toast.LENGTH_LONG).show();
								}
							});
					}
				});
				break;
			}

			return rootView;
		}
	}
}
