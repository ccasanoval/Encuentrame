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
	private static final int OK=0, KO=1, TO=2;
	private static final int ENTER=0, REGISTER=1, RECOVER=2;
	private static ActLogin _this;

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
		_this = this;
		Login.setSvcContext(this);
		Login.init(new Login.AuthListener()
		{
			@Override	public void onExito(FirebaseUser usr){}
			@Override	public void onFallo(){}
		});

		//Crea servicio si no está ya creado, dentro del servicio se llama a login
		startService(new Intent(this, CesService.class));
		// Rute Widget Svc
		WidgetRutaService.startServ(this);
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

	private ProgressDialog _progressDialog;
	public void iniEsperaLogin()
	{
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
		//if(v!=null)v.setEnabled(false);
	}
	public void finEsperaLogin(int i)
	{
		switch(i)
		{
		case OK:
			//Toast.makeText(this, getString(R.string.login_ok), Toast.LENGTH_LONG).show();
			break;
		case KO:
			Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
			break;
		case TO:
			if(_progressDialog.isShowing())
			Toast.makeText(this, getString(R.string.login_timeout), Toast.LENGTH_LONG).show();
			break;
		}
		if(_progressDialog!=null)_progressDialog.dismiss();
System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ finEsperaLogin ++++"+i+"+++++++++++++++++++++++++++++++++++++++++");
		//if(v!=null)v.setEnabled(true);
	}

	//----------------------------------------------------------------------------------------------
	//
	//----------------------------------------------------------------------------------------------
	public static class PlaceholderFragment extends Fragment
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		public PlaceholderFragment(){}

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
			if(Login.isLogged())_this.goMain();//TODO:Comprobar si ejecuta lo de abajo, si lo hace mejor añadir return...
System.err.println("-------------------------------------LOGIN:onCreateView.........................................................................");

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
			case ENTER://----------------------- ----------------------------------------------------
				txtLogin.setText(Login.getUsuario());
				txtEmail.setVisibility(View.GONE);
				txtPassword2.setVisibility(View.GONE);
				lblTitulo.setText(getString(R.string.enter_lbl));
				btnSend.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Login.login(txtLogin.getText().toString(), txtPassword.getText().toString(), ActLogin._this,
								new Login.AuthListener()
								{
									@Override
									public void onExito(FirebaseUser usr)
									{
										if(usr != null)//TODO: string
											Toast.makeText(ActLogin._this, "Bienvenido: "+usr.getEmail(), Toast.LENGTH_LONG).show();
										else
											Toast.makeText(ActLogin._this, "LOG KK2", Toast.LENGTH_LONG).show();
										ActLogin._this.goMain();
									}
									@Override
									public void onFallo()
									{
										Toast.makeText(ActLogin._this, "LOG KK:", Toast.LENGTH_LONG).show();
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
							Toast.makeText(ActLogin._this, getString(R.string.register_bad_pass), Toast.LENGTH_LONG).show();
							return;
						}
						Login.addUser(txtLogin.getText().toString(), txtPassword.getText().toString(), ActLogin._this,
								new Login.AuthListener()
								{
									@Override
									public void onExito(FirebaseUser usr)
									{
										if(usr != null)
										Toast.makeText(ActLogin._this, "REG OK:"+usr.getDisplayName()+":"+usr.getEmail(), Toast.LENGTH_LONG).show();
									}
									@Override
									public void onFallo()
									{
										Toast.makeText(ActLogin._this, "REG KK:", Toast.LENGTH_LONG).show();
									}
								});
					}
				});
				break;

			case RECOVER://-------------------------------------------------------------------------
				//txtLogin.setVisibility(View.GONE);
				txtEmail.setVisibility(View.GONE);
				//
				txtPassword.setVisibility(View.GONE);
				txtPassword2.setVisibility(View.GONE);
				lblTitulo.setText(getString(R.string.recover_lbl));
				btnSend.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						/*//Backendless.UserService.restorePassword(txtEmail.getText().toString(), new AsyncCallback<Void>()
						Backendless.UserService.restorePassword(txtLogin.getText().toString(), new AsyncCallback<Void>()
						{
							public void handleResponse(Void response)
							{
								System.err.println("RECOVER-----------------OK:" + response);
								//Snackbar.make(rootView, getString(R.string.recover_ok), Snackbar.LENGTH_LONG).setAction(getString(R.string.recover_lbl), null).show();
								Toast.makeText(rootView.getContext(), R.string.recover_ok, Toast.LENGTH_LONG).show();
								TabLayout.Tab t = _this._tabLayout.getTabAt(ENTER);
								if(t!=null)t.select();
							}
							public void handleFault(BackendlessFault fault)
							{
								System.err.println("RECOVER-----------------FAILED:" + fault + " : " + fault.getCode());
								//Snackbar.make(rootView, getString(R.string.recover_ko), Snackbar.LENGTH_LONG).setAction(getString(R.string.recover_lbl), null).show();
								Toast.makeText(rootView.getContext(), R.string.recover_ko, Toast.LENGTH_LONG).show();
							}
						});*/
					}
				});
				break;
			}

			return rootView;
		}
	}

/*

	/////////////////////////////////// REGISTER /////////////////////////////////////////////////////////////
	public static class RegisterBECallback<BackendlessUser> extends BackendlessCallback<BackendlessUser>
	{
		private Context context;
		private ProgressDialog progressDialog;
		public RegisterBECallback(Context context)
		{
			this.context = context;
			progressDialog = ProgressDialog.show(context, "", context.getString(R.string.cargando), true);
		}
		@Override
		public void handleResponse(BackendlessUser usr)
		{
			System.err.println("REGISTER-----------------" + usr);
			//Snackbar.make(rootView, getString(R.string.register_ok), Snackbar.LENGTH_LONG).setAction(getString(R.string.register_lbl), null).show();
			Toast.makeText(context, R.string.register_ok, Toast.LENGTH_LONG).show();
			TabLayout.Tab t = _this._tabLayout.getTabAt(ENTER);
			if(t!=null)t.select();
			progressDialog.cancel();
		}
		@Override
		public void handleFault(BackendlessFault fault)
		{
System.err.println("ActLogin:RegisterBECallback:FAILED:" + fault + " : " + fault.getCode() + " : " + fault.getMessage());
			Toast.makeText(context, R.string.register_ko + "\n"+fault.getMessage(), Toast.LENGTH_LONG).show();//TODO:mejorar cadena...
			progressDialog.cancel();
		}
	}

	////////////////////////////////////// LOGIN //////////////////////////////////////////////////////////
	public static class LoginBECallback<BackendlessUser> extends BackendlessCallback<BackendlessUser>
	{
		private static final int LOGIN_TIMEOUT = 9000;
		private ActLogin _win;
		private Handler _handler = new Handler();
		private Runnable _runnable = new Runnable()
		{
			@Override
			public void run()
			{
//System.err.println("LOGIN TIME OUT------------------------------------------" + Util.isLogged());
				_win.finEsperaLogin(TO);
			}
		};
		public LoginBECallback(ActLogin win)
		{
			_win = win;
			_handler.postDelayed(_runnable, LOGIN_TIMEOUT);//Temporizador para quitar dialogo carga si Backendless no retorna... que no lo hace cuando le mandas cuenta incorrecta
			_win.iniEsperaLogin();
		}
		@Override
		public void handleResponse(BackendlessUser backendlessUser)
		{
//System.err.println("ENTER------------------------------------------------------------------------------------" + backendlessUser);
			//super.handleResponse(backendlessUser);
			_win.finEsperaLogin(OK);
			_win.goMain();
		}
		@Override
		public void handleFault(BackendlessFault fault)
		{
			//NOTE: Nunca llega aqui, no tiene timeout ni falla con login incorrecto!!!!!! Hago algo mal con Backendless??????
//System.err.println("ENTER ERROR------------------------------------------------------------------------ 1");
//System.err.println("ENTER ERROR------------------------------------------------------------------------ " + fault.getMessage());
			_win.finEsperaLogin(KO);
		}
	}




	////////////////////////////////////// LOGIN ///////////////////////////////////////////////////
	public static class LoginCallback implements Firebase.AuthResultHandler
	{
		private static final int LOGIN_TIMEOUT = 9000;
		private ActLogin _win;
		private Handler _handler = new Handler();
		private Runnable _runnable = new Runnable()
		{
			@Override
			public void run()
			{
System.err.println("LOGIN TIME OUT------------------------------------------" + Login.isLogged());
				_win.finEsperaLogin(TO);
			}
		};
		public LoginCallback(ActLogin win)
		{
			_win = win;
			_handler.postDelayed(_runnable, LOGIN_TIMEOUT);//Temporizador para quitar dialogo carga si Backendless no retorna... que no lo hace cuando le mandas cuenta incorrecta
			_win.iniEsperaLogin();
		}

		@Override
		public void onAuthenticated(AuthData usr)
		{
System.err.println("------------------------------------------" + Login.isLogged());
			_win.finEsperaLogin(OK);
			_win.goMain();
		}
		@Override
		public void onAuthenticationError(FirebaseError err)
		{
			_win.finEsperaLogin(KO);
		}
	}

	/////////////////////////////////// REGISTER ///////////////////////////////////////////////////
	public static class RegisterCallback implements Firebase.ValueResultHandler
	{
		private Context context;
		private ProgressDialog progressDialog;
		public RegisterCallback(Context context)
		{
			this.context = context;
			progressDialog = ProgressDialog.show(context, "", context.getString(R.string.cargando), true);
		}

		@Override
		public void onSuccess(Object result)
		{
System.err.println("REGISTER-----------------" + result);
			//Snackbar.make(rootView, getString(R.string.register_ok), Snackbar.LENGTH_LONG).setAction(getString(R.string.register_lbl), null).show();
			Toast.makeText(context, R.string.register_ok, Toast.LENGTH_LONG).show();
			TabLayout.Tab t = _win._tabLayout.getTabAt(ENTER);
			if(t!=null)t.select();
			progressDialog.cancel();
		}

		@Override
		public void onError(FirebaseError err)
		{
			System.err.println("ActLogin:RegisterBECallback:FAILED:" + err + " : " + err.getCode() + " : " + err.getMessage());
			Toast.makeText(context, R.string.register_ko + "\n"+err.getMessage(), Toast.LENGTH_LONG).show();//TODO:mejorar cadena...
			progressDialog.cancel();
		}
	}

	/////////////////////////////////// RESTORE ////////////////////////////////////////////////////
	public static class RestoreCallback implements Firebase.ResultHandler
	{
		private Context context;
		private ProgressDialog progressDialog;
		public RestoreCallback(Context context)
		{
			this.context = context;
			progressDialog = ProgressDialog.show(context, "", context.getString(R.string.cargando), true);
		}
		@Override
		public void onSuccess()
		{
			System.err.println("RECOVER-----------------OK:");
			//Snackbar.make(rootView, getString(R.string.recover_ok), Snackbar.LENGTH_LONG).setAction(getString(R.string.recover_lbl), null).show();
			Toast.makeText(context, R.string.recover_ok, Toast.LENGTH_LONG).show();
			TabLayout.Tab t = _win._tabLayout.getTabAt(ENTER);
			if(t!=null)t.select();
		}
		@Override
		public void onError(FirebaseError err)
		{
			System.err.println("RECOVER-----------------FAILED:" + err + " : " + err.getCode());
			//Snackbar.make(rootView, getString(R.string.recover_ko), Snackbar.LENGTH_LONG).setAction(getString(R.string.recover_lbl), null).show();
			Toast.makeText(context, R.string.recover_ko, Toast.LENGTH_LONG).show();
		}
	}*/


}
