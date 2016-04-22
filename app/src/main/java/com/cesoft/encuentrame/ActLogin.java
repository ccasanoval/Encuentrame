package com.cesoft.encuentrame;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Create several users and test user access to each object...
public class ActLogin extends AppCompatActivity
{
	private static final int ENTER=0, REGISTER=1, RECOVER=2;
	private static ActLogin _win;

	// The android.support.v4.view.PagerAdapter will provide fragments for each of the sections. We use a FragmentPagerAdapter derivative, which will keep every loaded fragment in memory.
	// If this becomes too memory intensive, it may be best to switch to a android.support.v4.app.FragmentStatePagerAdapter
	public TabLayout _tabLayout;

	AsyncCallback<BackendlessUser> resLogin = new AsyncCallback<BackendlessUser>()
	{
		@Override
		public void handleResponse(BackendlessUser backendlessUser)
		{
			System.err.println("ActLogin:222ENTER-----------(desde CesService)-----------" + backendlessUser);
		}
		@Override
		public void handleFault(BackendlessFault fault)
		{
			System.out.println("ActLogin:222CesService:Login:f: -------------------------------------------------- " + fault.getMessage());
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_login);
		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		SectionsPagerAdapter SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		ViewPager viewPager = (ViewPager)findViewById(R.id.container);
		viewPager.setAdapter(SectionsPagerAdapter);
		_tabLayout = (TabLayout)findViewById(R.id.tabs);
		_tabLayout.setupWithViewPager(viewPager);

		/* NOT IN GIT FOR SECURITY REASONS
		package com.cesoft.encuentrame;
		public class BackendSettings
		{
			public static final String APP = "";
			public static final String KEY = "";
			public static final String VER = "v1";
		}
		*/
		_win = this;
		startService(new Intent(this, CesService.class));
System.err.println("ActLogin--------1:"+Util.isLogged()+" 2:"+Util.getUsuario());
		if( ! Util.isLogged()){Util.initBackendless(this);Util.login(resLogin);}//TODO: algo mas inteligente para no repetir init o login?
System.err.println("ActLogin--------3:"+Util.isLogged()+" 4:"+Util.getUsuario());
		if(Util.isLogged())goMain();
	}

	// A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			return PlaceholderFragment.newInstance(position);
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

	//----------------------------------------------------------------------------------------------
	public static class PlaceholderFragment extends Fragment
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		public PlaceholderFragment(){}
		//AsyncCallback<BackendlessUser> resLogin = new LoginBECallback<>(_win);

		// Returns a new instance of this fragment for the given section number.
		public static PlaceholderFragment newInstance(int sectionNumber)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			if(Util.isLogged())_win.goMain();

			final int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
			final View rootView = inflater.inflate(R.layout.act_login_frag, container, false);
			final TextView lblTitulo = (TextView)rootView.findViewById(R.id.lblTitulo);
			final EditText txtLogin = (EditText)rootView.findViewById(R.id.txtLogin);txtLogin.requestFocus();
			final EditText txtPassword = (EditText)rootView.findViewById(R.id.txtPassword);
			final EditText txtPassword2 = (EditText)rootView.findViewById(R.id.txtPassword2);
			final EditText txtEmail = (EditText)rootView.findViewById(R.id.txtEmail);
			final Button btnSend = (Button)rootView.findViewById(R.id.btnSend);

			switch(sectionNumber)
			{
			case ENTER://---------------------------------------------------------------------------
				txtLogin.setText(Util.getUsuario());
				txtEmail.setVisibility(View.GONE);
				txtPassword2.setVisibility(View.GONE);
				lblTitulo.setText(getString(R.string.enter_lbl));
				btnSend.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Util.login(txtLogin.getText().toString(), txtPassword.getText().toString(), new LoginBECallback<BackendlessUser>(_win));
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
							Snackbar.make(rootView, getString(R.string.register_bad_pass), Snackbar.LENGTH_LONG).setAction(getString(R.string.register_lbl), null).show();
							return;
						}
						BackendlessUser user = new BackendlessUser();
						user.setPassword(txtPassword.getText().toString());
						user.setEmail(txtEmail.getText().toString());
						user.setProperty("name", txtLogin.getText().toString());
						Backendless.UserService.register(user, new RegisterBECallback<BackendlessUser>(_win)
						{
							@Override
							public void handleResponse(BackendlessUser backendlessUser)
							{
								super.handleResponse(backendlessUser);
								System.err.println("REGISTER-----------------" + backendlessUser);
								//startActivity( new Intent(_win.getBaseContext(), ActLogin.class ) );
								//_win.finish();
								Snackbar.make(rootView, getString(R.string.register_ok), Snackbar.LENGTH_LONG).setAction(getString(R.string.register_lbl), null).show();
								//startActivity(new Intent(_win.getBaseContext(), ActMain.class));//NO: tiene que comprobar el correo
								TabLayout.Tab t = _win._tabLayout.getTabAt(ENTER);
								if(t!=null)t.select();
							}
						});
					}
				});
				break;

			case RECOVER://-------------------------------------------------------------------------
				txtLogin.setVisibility(View.GONE);
				txtPassword.setVisibility(View.GONE);
				txtPassword2.setVisibility(View.GONE);
				lblTitulo.setText(getString(R.string.recover_lbl));
				btnSend.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Backendless.UserService.restorePassword(txtEmail.getText().toString(), new AsyncCallback<Void>()
						{
							public void handleResponse(Void response)
							{
								System.err.println("RECOVER-----------------OK:" + response);
								Snackbar.make(rootView, getString(R.string.recover_ok), Snackbar.LENGTH_LONG).setAction(getString(R.string.recover_lbl), null).show();
								TabLayout.Tab t = _win._tabLayout.getTabAt(ENTER);
								if(t!=null)t.select();
							}
							public void handleFault(BackendlessFault fault)
							{
								System.err.println("RECOVER-----------------FAILED:" + fault + " : " + fault.getCode());
								Snackbar.make(rootView, getString(R.string.recover_ko), Snackbar.LENGTH_LONG).setAction(getString(R.string.recover_lbl), null).show();
							}
						});
					}
				});
				break;
			}

			return rootView;
		}
	}



	/////////////////////////////////// REGISTER /////////////////////////////////////////////////////////////
	public static class RegisterBECallback<T> extends BackendlessCallback<T>
	{
		private Context context;
		private ProgressDialog progressDialog;
		public RegisterBECallback(Context context)
		{
			this.context = context;
			progressDialog = ProgressDialog.show(context, "", context.getString(R.string.cargando), true);
		}
		@Override
		public void handleResponse(T response)
		{
			progressDialog.cancel();
		}
		@Override
		public void handleFault(BackendlessFault fault)
		{
System.err.println("ActLogin:RegisterBECallback:FAILED:" + fault + " : " + fault.getCode());
			progressDialog.cancel();
			Toast.makeText(context, fault.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	////////////////////////////////////// LOGIN //////////////////////////////////////////////////////////
	public static class LoginBECallback<BackendlessUser> extends BackendlessCallback<BackendlessUser>
	{
		private ActLogin win;
		private ProgressDialog progressDialog;

private Handler handler = new Handler();
private Runnable runnable = new Runnable() {
   @Override
   public void run()
   {
	   System.err.println("ActLogin: RUNNNNNNNNNNNNNNNNNN-----------------------------------------------------------------------------------------");
      	//handler.postDelayed(this, 100);
   }
};
		public LoginBECallback(ActLogin win)
		{
			this.win = win;
			progressDialog = ProgressDialog.show(win, "", win.getString(R.string.cargando), true);
//TODO: Hacer temporizador para quitar dialogo carga...
handler.postDelayed(runnable, 5000);
		}
		@Override
		public void handleResponse(BackendlessUser backendlessUser)
		{
			//super.handleResponse(backendlessUser);
			progressDialog.cancel();
System.err.println("ENTER--------------------" + backendlessUser);
			//TODO: Observers!!!!!!!!!!!!!!!!!!!!!!!!!!!! para pasar de pantalla de login... y cerrar icono de espera...
			win.goMain();
		}
		@Override
		public void handleFault(BackendlessFault fault)
		{
			progressDialog.cancel();
			//TODO: Nunca llega aqui, no tiene timeout ni falla!!!!!!! Hacer uno...
System.out.println("ActLogin:Backendless reported an error:---------------------------------------------------------------- " + fault.getMessage());
		}
	}

}
