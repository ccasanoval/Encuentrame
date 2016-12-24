package com.cesoft.encuentrame3;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.models.Ruta;

import java.util.Date;
import javax.inject.Inject;

//https://guides.codepath.com/android/Handling-Scrolls-with-CoordinatorLayout
//https://developer.android.com/training/permissions/requesting.html
//MOCK LOCATIONS ON DEVICE : http://stackoverflow.com/questions/2531317/android-mock-location-on-device
//TODO: Avisar con TextToVoice y permitir no hacerlo mediante las opciones....
//TODO: Cambiar ListView por recyclerview
//TODO: comprobar cuando dos moviles funcionan con la misma clave, hay problema? o solo sandras's
//TODO: AVISO: no molestar mas por hoy
//TODO: main window=> Number or routes, places and geofences...
//TODO: Egg?
//TODO: Google auth?
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: Develop a web app for points management : connect to backendless by REST API...
//TODO: Preparar para tablet
//TODO: Opcion que diga no preguntar por activar GPS (en tablet que no tiene gps... es un coñazo)
//http://developer.android.com/intl/es/training/basics/supporting-devices/screens.html
// small, normal, large, xlarge   ///  low (ldpi), medium (mdpi), high (hdpi), extra high (xhdpi)

////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity
{
	private static final String TAG = "CESoft:ActMain:";
	public static final String PAGINA = "pagina", MENSAJE = "mensaje", DIRTY = "dirty";

	private ViewPager _viewPager;

	//@Inject	Application _app;//http://stackoverflow.com/questions/24656967/how-to-inject-into-static-classes-using-dagger

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		//_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_content);//TODO: Eliminar de layout

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setSubtitle(Login.getCurrentUserName());

		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		_viewPager = (ViewPager)findViewById(R.id.container);
		if(_viewPager != null)
		_viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
		if(tabLayout != null)
		{
			tabLayout.setupWithViewPager(_viewPager);
			tabLayout.setSelectedTabIndicatorHeight(10);
		}
		//tabLayout.setSelectedTabIndicatorColor();
		//tabLayout.setTabTextColors();

		try
		{
			Integer nPagina = getIntent().getIntExtra(PAGINA, -1);
			if(nPagina >= Util.LUGARES && nPagina <= Util.AVISOS)
				_viewPager.setCurrentItem(nPagina);
			String sMensaje = getIntent().getStringExtra(MENSAJE);
			if(sMensaje != null && !sMensaje.isEmpty())
				Toast.makeText(ActMain.this, sMensaje, Toast.LENGTH_LONG).show();
		}
		catch(Exception e){Log.e(TAG, String.format("onCreate:e:%s",e), e);}
	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_viewPager = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_act_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(PlaceholderFragment._apf[_viewPager.getCurrentItem()] == null)
		{
			PlaceholderFragment._apf[_viewPager.getCurrentItem()] = PlaceholderFragment.newInstance(_viewPager.getCurrentItem(), ActMain.this);
			Log.e(TAG, "onOptionsItemSelected:PlaceholderFragment._apf["+_viewPager.getCurrentItem()+"] == NULL     !!!!????");
		}

		Intent i;// Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id)
		{
		case R.id.action_config:
			PlaceholderFragment._apf[_viewPager.getCurrentItem()].startActivityForResult(new Intent(this, ActConfig.class), Util.CONFIG);
			return true;
		case R.id.action_mapa:
			i = new Intent(this, ActMaps.class);
			i.putExtra(Util.TIPO, _viewPager.getCurrentItem());//_sectionNumber
			startActivity(i);
			return true;
		case R.id.action_buscar:
			PlaceholderFragment._apf[_viewPager.getCurrentItem()].buscar();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	 */
	////////////////////////////////////////////////////////////////////////////////////////////////
	// FRAGMEN PAGER ADAPTER
	////////////////////////////////////////////////////////////////////////////////////////////////
	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}
		@Override
		public Fragment getItem(int position)
		{
			return PlaceholderFragment.newInstance(position, ActMain.this);
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
			case Util.LUGARES:	return getString(R.string.lugares);
			case Util.RUTAS:	return getString(R.string.rutas);
			case Util.AVISOS:	return getString(R.string.avisos);
			}
			return null;
		}
	}

	/*Si está este, no se llama al de PlaceholderFragment
	@Override public void onActivityResult(int requestCode, int resultCode, Intent data){}*/
	////////////////////////////////////////////////////////////////////////////////////////////////
	// LUGARES / RUTAS / AVISOS
	////////////////////////////////////////////////////////////////////////////////////////////////
	private static Filtro[] _aFiltro = new Filtro[3];
	public static class PlaceholderFragment extends Fragment implements IListaItemClick
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		private static PlaceholderFragment[] _apf = new PlaceholderFragment[3];

		//private	Util _util;
		@Inject Util _util;
		@Inject Login _login;
		private Application _app;
		private ActMain _main;
		private int _sectionNumber = Util.LUGARES;//Util.NADA;
		private View _rootView;
		private ListView _listView;

		// Returns a new instance of this fragment for the given section number.
		public static PlaceholderFragment newInstance(final int sectionNumber, ActMain main)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			fragment.setRetainInstance(true);
			_apf[sectionNumber] = fragment;
			fragment._sectionNumber = sectionNumber;
			fragment._main = main;
			fragment._app = main.getApplication();
			//fragment._util = ((App)main.getApplication()).getGlobalComponent().util();
			((App)main.getApplication()).getGlobalComponent().inject(fragment);
			return fragment;
		}

		public PlaceholderFragment(){}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			Bundle args = getArguments();
			final int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
			_rootView = inflater.inflate(R.layout.act_main_frag, container, false);
			_listView = (ListView)_rootView.findViewById(R.id.listView);
			final TextView textView = new TextView(_rootView.getContext());

			if(_sectionNumber < 0)
			{
				Log.e(TAG, "PlaceholderFragment:onCreateView:----------------------------------------");
				Intent intent = new Intent(_main.getBaseContext(), ActLogin.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				_main.startActivity(intent);
				_main.finish();
				_main = null;
				return null;
			}
			if(_aFiltro[_sectionNumber] == null)
				_aFiltro[_sectionNumber] = new Filtro(_sectionNumber);


			FloatingActionButton fab = (FloatingActionButton) _rootView.findViewById(R.id.fabNuevo);
			fab.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					switch(sectionNumber)//switch(_viewPager.getCurrentItem())
					{
					case Util.LUGARES://TODO: evitar esta referencia a main!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
						startActivityForResult(new Intent(_main, ActLugar.class), Util.LUGARES);
						break;
					case Util.RUTAS:
						startActivityForResult(new Intent(_main, ActRuta.class), Util.RUTAS);
						break;
					case Util.AVISOS:
						startActivityForResult(new Intent(_main, ActAviso.class), Util.AVISOS);
						break;
					}
				}
			});

			switch(sectionNumber)
			{
			case Util.LUGARES://-------------------------------------------------------------------------
				textView.setText(getString(R.string.lugares));
				refreshLugares();
				break;
			case Util.RUTAS://---------------------------------------------------------------------------
				textView.setText(getString(R.string.rutas));
				refreshRutas();
				break;
			case Util.AVISOS://--------------------------------------------------------------------------
				textView.setText(getString(R.string.avisos));
				refreshAvisos();
				break;
			}

			/// Actualizar lista de rutas
			if(sectionNumber == Util.RUTAS)
			{
				Util.setRefreshCallback(this);// CesService(on new track point) -> Util(refres ruta) -> this fragment(broadcast -> refresh lista rutas)
				_MessageReceiver = new BroadcastReceiver()
				{
					@Override
					public void onReceive(Context context, Intent intent)
					{
						refreshRutas();
					}
				};
			}

			_listView.addHeaderView(textView);
			return _rootView;
		}
		//______________________________________________________________________________________________
		@Override
		public void onDestroy()
		{
			super.onDestroy();
			_MessageReceiver = null;
			if(_sectionNumber == Util.RUTAS)
				Util.setRefreshCallback(null);
			if(_apf != null && _apf[_sectionNumber]!=null)_apf[_sectionNumber] = null;
		}
		//______________________________________________________________________________________________
		@Override
		public void onResume()
		{
  			super.onResume();
			if(_sectionNumber == Util.RUTAS)
			{
				if(_apf[Util.RUTAS] != null)
				LocalBroadcastManager.getInstance(_apf[Util.RUTAS].getContext()).registerReceiver(_MessageReceiver, new IntentFilter(RUTA_REFRESH));
				refreshRutas();
			}
		}
		@Override
		public void onPause()
		{
			if(_sectionNumber == Util.RUTAS && _apf[Util.RUTAS]!= null)
				LocalBroadcastManager.getInstance(_apf[Util.RUTAS].getContext()).unregisterReceiver(_MessageReceiver);
  			super.onPause();
		}

		//// implements IListaItemClick
		//______________________________________________________________________________________________
		@Override
		public void onItemEdit(int tipo, Objeto obj)
		{
			Intent i;
			switch(tipo)
			{
			case Util.LUGARES:
				i = new Intent(_main, ActLugar.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, Util.LUGARES);
				break;
			case Util.RUTAS:
				try{
				i = new Intent(_main, ActRuta.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, Util.RUTAS);
				}
				catch(Exception e){Log.e(TAG, "------------------PlaceholderFragment:onItemEdit:e:"+e, e);}
				break;
			case Util.AVISOS:
				i = new Intent(_main, ActAviso.class);
				i.putExtra(Aviso.NOMBRE, obj);
				startActivityForResult(i, Util.AVISOS);
				break;
			}
		}
		@Override
		public void onItemMap(int tipo, Objeto obj)
		{
			Intent i;
			switch(tipo)
			{
			case Util.LUGARES:
				i = new Intent(_main, ActMaps.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, Util.LUGARES);
				break;
			case Util.RUTAS:
				try{
				i = new Intent(_main, ActMaps.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, Util.RUTAS);
				}catch(Exception e){Log.e(TAG, String.format("onItemMap:RUTAS:e:%s   main=%s",e, _main), e);}
				break;
			case Util.AVISOS:
				i = new Intent(_main, ActMaps.class);
				i.putExtra(Aviso.NOMBRE, obj);
				startActivityForResult(i, Util.AVISOS);
				break;
			}
		}
		public void buscar()
		{
			try
			{
				Intent i = new Intent(_app, ActBuscar.class);//_main.getApplicationContext
				i.putExtra(Filtro.FILTRO, _aFiltro[_sectionNumber]);
				startActivityForResult(i, Util.BUSCAR);
			}catch(Exception e){Log.e(TAG, String.format("buscar:e:%s",e), e);}
		}
		// Recoge el resultado de startActivityForResult
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			if(resultCode != RESULT_OK)return;

			if(requestCode == Util.CONFIG)
			{
				_login.logout();

				Intent intent = new Intent(_main.getBaseContext(), ActLogin.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				_main.finish();
				_main = null;
				return;
			}

			if(data != null)
			{
				try
				{
				String sMensaje = data.getStringExtra(MENSAJE);
				if(sMensaje != null && !sMensaje.isEmpty())
					Toast.makeText(_main, sMensaje, Toast.LENGTH_LONG).show();
				}catch(Exception e){Log.e(TAG, "onActivityResult:e:"+e,e);}
				if( ! data.getBooleanExtra(DIRTY, true))return;

				Filtro filtro = data.getParcelableExtra(Filtro.FILTRO);
				if(filtro != null && filtro.getTipo() != Util.NADA)
				{
					_aFiltro[_sectionNumber] = filtro;
					if( ! filtro.isOn())
						Toast.makeText(_main, getString(R.string.sin_filtro), Toast.LENGTH_SHORT).show();
				}
			}

			if(requestCode == Util.BUSCAR)requestCode=_aFiltro[_sectionNumber].getTipo();
			switch(requestCode)
			{
			case Util.LUGARES:	refreshLugares(); break;
			case Util.RUTAS:	refreshRutas(); break;
			case Util.AVISOS:	refreshAvisos(); break;
			}
		}

		// 4 IListaItemClick
		private BroadcastReceiver _MessageReceiver;
		private static final String RUTA_REFRESH = "ces";

		public void onRefreshListaRutas()
		{
			//ActMain._this.runOnUiThread(new Runnable(){public void run(){refreshRutas();}});//No funciona, ha de hacerse mediante broadcast...
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(RUTA_REFRESH));
		}


		//__________________________________________________________________________________________
		public void refreshLugares()
		{
			if(_aFiltro[_sectionNumber].isOn())
			{
				checkFechas();
				Lugar.getLista(_acLugar, _aFiltro[_sectionNumber]);
			}
			else
				Lugar.getLista(_acLugar);
		}
		private Objeto.ObjetoListener<Lugar> _acLugar = new Objeto.ObjetoListener<Lugar>()
		{
			@Override
			public void onData(Lugar[] aLugares)
			{
				long n = aLugares.length;
				if(n < 1)
				{
					try
					{
						if(_main._viewPager.getCurrentItem() == Util.LUGARES)
							try{Toast.makeText(_main, getString(R.string.lista_vacia), Toast.LENGTH_SHORT).show();}
							catch(Exception e){Log.e(TAG, String.format("LUGARES:handleResponse:e:%s",e),e);}//java.lang.IllegalStateException: Fragment PlaceholderFragment{41e3b090} not attached to Activity
					}
					catch(Exception e){Log.e(TAG, String.format("_acLugar:%s",e), e);}
				}
				_listView.setAdapter(new LugarArrayAdapter(_rootView.getContext(), aLugares, PlaceholderFragment.this));
			}
			@Override
			public void onError(String err)//FirebaseError err)
			{
				Log.e(TAG, String.format("LUGARES2:GET:e:%s",err));
			}
		};

		//__________________________________________________________________________________________
		public void refreshRutas()
		{
			if(_aFiltro[_sectionNumber].isOn())
			{
				checkFechas();
				Ruta.getLista(_acRuta, _aFiltro[_sectionNumber], _util.getTrackingRoute());
			}
			else
				Ruta.getLista(_acRuta);
		}
		private Objeto.ObjetoListener<Ruta> _acRuta = new Objeto.ObjetoListener<Ruta>()
		{
			@Override
			public void onData(Ruta[] aRutas)
			{
				long n = aRutas.length;
				if(n < 1)
				{
					try
					{
						if(_main._viewPager!= null && _main._viewPager.getCurrentItem() == Util.RUTAS)
							try{Toast.makeText(_main, getString(R.string.lista_vacia), Toast.LENGTH_SHORT).show();}
							catch(Exception e){Log.e(TAG, String.format("RUTAS:handleResponse:e:%s",e), e);}
					}catch(Exception e){Log.e(TAG, String.format("_acRuta:e:%s",e),e);}
				}
				RutaArrayAdapter r = new RutaArrayAdapter(_rootView.getContext(), aRutas, PlaceholderFragment.this, _util);
				_listView.setAdapter(r);
				r.notifyDataSetChanged();
				if(android.os.Build.VERSION.SDK_INT>=19)
				_listView.scrollListBy(_listView.getMaxScrollAmount());
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("RUTAS2:GET:e:%s",err));
			}
		};

		//__________________________________________________________________________________________
		public void refreshAvisos()
		{
			if(_aFiltro[_sectionNumber].isOn())
			{
				checkFechas();
				Aviso.getLista(_acAviso, _aFiltro[_sectionNumber]);
			}
			else
				Aviso.getLista(_acAviso);
		}
		private Objeto.ObjetoListener<Aviso> _acAviso = new Objeto.ObjetoListener<Aviso>()
		{
			@Override
			public void onData(Aviso[] aAvisos)
			{
				long n = aAvisos.length;
				if(n < 1)
				{
					if(_main._viewPager.getCurrentItem() == Util.AVISOS)//if(_sectionNumber == Util.AVISOS)
					try{Toast.makeText(_main, getString(R.string.lista_vacia), Toast.LENGTH_SHORT).show();}
					catch(Exception e){Log.e(TAG, String.format("AVISOS:handleResponse:e:%s",e), e);}
				}
				_listView.setAdapter(new AvisoArrayAdapter(_rootView.getContext(), aAvisos, PlaceholderFragment.this));
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("AVISOS2:GET:e:%s",err));
			}
		};

		//__________________________________________________________________________________________
		private void checkFechas()
		{
			Date ini = _aFiltro[_sectionNumber].getFechaIni();
			Date fin = _aFiltro[_sectionNumber].getFechaFin();
			if(ini!=null && fin!=null && ini.getTime() > fin.getTime())
			{
				_aFiltro[_sectionNumber].setFechaIni(fin);
				_aFiltro[_sectionNumber].setFechaFin(ini);
			}
		}
	}
}