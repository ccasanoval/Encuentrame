package com.cesoft.encuentrame;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Filtro;
import com.cesoft.encuentrame.models.Login;
import com.cesoft.encuentrame.models.Lugar;
import com.cesoft.encuentrame.models.Objeto;
import com.cesoft.encuentrame.models.Ruta;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

//Version Firebase
/*
{
    "rules": {
        ".read": true,
        ".write": true,
        "aviso": {
          ".indexOn": ["activo", "nombre"]
        },
        "lugar": {
          ".indexOn": ["nombre"]
        },
        "ruta_punto": {
          ".indexOn": ["idRuta"]
        }
    }
}
//TODO: listeners con actualizaciones continuas ????
/*
GoogleService failed to initialize, status: 10, Missing an expected resource: 'R.string.google_app_id' for initializing Google services.
Possible causes are missing google-services.json or com.google.gms.google-services gradle plugin.
Scheduler not set. Not logging error/warn.
Uploading is not possible. App measurement disabled

https://developers.google.com/identity/sign-in/android/start?hl=en
https://developers.google.com/mobile/add?platform=android&cntapi=signin&cntapp=Default%20Demo%20App&cntpkg=com.google.samples.quickstart.signin&cnturl=https:%2F%2Fdevelopers.google.com%2Fidentity%2Fsign-in%2Fandroid%2Fstart%3Fconfigured%3Dtrue&cntlbl=Continue%20with%20Try%20Sign-In
Registered SHA-1s:
74:42:64:98:0E:57:EF:75:02:50:5C:DC:FB:C2:88:B1:EE:8A:4C:A8
*/

//TODO:Login con google OAuth....
//TODO:Login: borrar cuenta.. Esta seguro? confirmar por email?

//TODO:Fragments : mostrar lista de lugares ademas del lugar que se esta editando...
//http://developer.android.com/intl/es/training/basics/fragments/index.html
//http://developer.android.com/intl/es/training/basics/supporting-devices/screens.html
// small, normal, large, xlarge   ///  low (ldpi), medium (mdpi), high (hdpi), extra high (xhdpi)

//TODO: Cambiar : se guardaron los datos del 'registro' por 'lugar', 'ruta' o 'aviso'...
//TODO: Google auth?
//TODO: Muestra mensaje de lista vacia si alguna lista esta vacia al iniciar, incluso cuando el panel no es el que tiene la lista vacia... arreglar para solo mostrar msg si estas en dicho  panel
//TODO: CONFIF: usr/pwd de backendless, delay to tracking routes, geofence radius?... HACER QUE FUNCIONE lo que has configurado...
//TODO: mostrar fecha de creacion y modificacion en vistas...
//TODO: widget para ruta start/stop... widget para guardar punto...
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: main window=> Number or routes, places and geofences...
//TODO: Add photo to lugar & alerta n save it in backendless...
//TODO: Develop a web app for points management : connect to backendless by REST API...

//TODO: Release
//http://developer.android.com/intl/es/tools/publishing/app-signing.html

//TODO: dialogo que pida activar gps! si no esta activo
//http://stackoverflow.com/questions/29801368/how-to-show-enable-location-dialog-like-google-maps

//MOCK LOCATIONS ON DEVICE : http://stackoverflow.com/questions/2531317/android-mock-location-on-device
//BACKENDLESS: Permisos de objeto: Owner: ALL, AuthUser: NEW+UPDATE+DEL, Otros: NADA
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity
{
	public static final String PAGINA = "pagina", MENSAJE = "mensaje", DIRTY = "dirty";
	private static ActMain _this;
	private static CoordinatorLayout _coordinatorLayout;

	private ViewPager _viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);

		_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.main_content);
		_this = this;

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Util.setApplication(getApplication());
		//Util.initPrefs();

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
System.err.println("PAGINA++++++++++++++++"+nPagina);

			String sMensaje = getIntent().getStringExtra(MENSAJE);
			if(sMensaje != null && !sMensaje.isEmpty())
				Snackbar.make(ActMain._coordinatorLayout, sMensaje, Snackbar.LENGTH_LONG).show();
			//if( ! getIntent().getBooleanExtra(DIRTY, true))return;
		}
		catch(Exception e){System.err.println("ActMain:onCreate:e:"+e);}
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
		Intent i;// Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id)
		{
		case R.id.action_config:
				PlaceholderFragment._apf[_viewPager.getCurrentItem()].startActivityForResult(new Intent(ActMain._this, ActConfig.class), Util.CONFIG);
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
			case Util.LUGARES:	return getString(R.string.lugares);
			case Util.RUTAS:	return getString(R.string.rutas);
			case Util.AVISOS:	return getString(R.string.avisos);
			}
			return null;
		}
	}

/*	Si está este, no se llama al de PlaceholderFragment
	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		System.err.println("ActMain:onActivityResult:--------+++++++MAIN++++++++--" + requestCode+":"+resultCode);
	}*/
	////////////////////////////////////////////////////////////////////////////////////////////////
	// LUGARES / RUTAS / AVISOS
	////////////////////////////////////////////////////////////////////////////////////////////////
	private static Filtro[] _aFiltro = new Filtro[3];
	public static class PlaceholderFragment extends Fragment implements IListaItemClick
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		private static PlaceholderFragment[] _apf = new PlaceholderFragment[3];

		private int _sectionNumber = Util.NADA;
		public PlaceholderFragment(){}

		// Returns a new instance of this fragment for the given section number.
		public static PlaceholderFragment newInstance(final int sectionNumber)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			_apf[sectionNumber] = fragment;
			fragment._sectionNumber = sectionNumber;
			return fragment;
		}

		private View _rootView;
		private ListView _listView;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			Bundle args = getArguments();
			final int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
System.err.println("------onCreateView:"+sectionNumber+" ::: "+_sectionNumber+" ::: "+(_sectionNumber < 0 ?"null":_aFiltro[_sectionNumber]));
			_rootView = inflater.inflate(R.layout.act_main_frag, container, false);
			_listView = (ListView)_rootView.findViewById(R.id.listView);
			final TextView textView = new TextView(_rootView.getContext());

			if(_sectionNumber < 0)
			{
System.err.println("----------------------------ActMain:onCreateView:_sectionNumber < 0 : "+_sectionNumber);
				Intent intent = new Intent(_this.getBaseContext(), ActLogin.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				_this.startActivity(intent);
				_this.finish();
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
					case Util.LUGARES:
						startActivityForResult(new Intent(ActMain._this, ActLugar.class), Util.LUGARES);
						break;
					case Util.RUTAS:
						startActivityForResult(new Intent(ActMain._this, ActRuta.class), Util.RUTAS);
						break;
					case Util.AVISOS:
						startActivityForResult(new Intent(ActMain._this, ActAviso.class), Util.AVISOS);
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

		//// implements IListaItemClick
		//______________________________________________________________________________________________
		@Override
		public void onItemEdit(int tipo, Objeto obj)
		{
System.err.println("ActMain:onItemEdit:"+obj);
			Intent i;
			switch(tipo)
			{
			case Util.LUGARES:
				i = new Intent(ActMain._this, ActLugar.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, Util.LUGARES);
				break;
			case Util.RUTAS:
				i = new Intent(ActMain._this, ActRuta.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, Util.RUTAS);
				break;
			case Util.AVISOS:
				i = new Intent(ActMain._this, ActAviso.class);
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
				i = new Intent(ActMain._this, ActMaps.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, Util.LUGARES);
				break;
			case Util.RUTAS:
				i = new Intent(ActMain._this, ActMaps.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, Util.RUTAS);
				break;
			case Util.AVISOS:
				i = new Intent(ActMain._this, ActMaps.class);
				i.putExtra(Aviso.NOMBRE, obj);
				startActivityForResult(i, Util.AVISOS);
				break;
			}
		}
		public void buscar()
		{
			Intent i = new Intent(ActMain._this, ActBuscar.class);
			i.putExtra(Filtro.FILTRO, _aFiltro[_sectionNumber]);
			startActivityForResult(i, Util.BUSCAR);
		}
		// Recoge el resultado de startActivityForResult
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			if(resultCode != RESULT_OK)return;

			if(requestCode == Util.CONFIG)
			{
				Login.logout();
				return;
			}

System.err.println("-----++++++++++++++++----ActMain:onActivityResult:0:"+ requestCode);
			if(data != null)
			{
				String sMensaje = data.getStringExtra(MENSAJE);
				if(sMensaje != null && !sMensaje.isEmpty())
					Snackbar.make(ActMain._coordinatorLayout, sMensaje, Snackbar.LENGTH_LONG).show();//getString(R.string.ok_guardar)
				if( ! data.getBooleanExtra(DIRTY, true))return;

				Filtro filtro = data.getParcelableExtra(Filtro.FILTRO);
				if(filtro != null && filtro.getTipo() != Util.NADA)
				{
					_aFiltro[_sectionNumber] = filtro;
					if( ! filtro.isOn())
						Snackbar.make(ActMain._coordinatorLayout, getString(R.string.sin_filtro), Snackbar.LENGTH_LONG).show();
				}
				//else		_aFiltro[_sectionNumber] = new Filtro(requestCode);//, Filtro.TODOS, "", null, null, null, 0);
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
System.err.println("---------ActMain:onRefreshListaRutas():");
			//ActMain._this.runOnUiThread(new Runnable(){public void run(){refreshRutas();}});//No funciona, ha de hacerse mediante broadcast...
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(RUTA_REFRESH));
		}
		@Override
		public void onResume()
		{
  			super.onResume();
			if(_sectionNumber == Util.RUTAS)
			{
				LocalBroadcastManager.getInstance(_apf[Util.RUTAS].getContext()).registerReceiver(_MessageReceiver, new IntentFilter(RUTA_REFRESH));
				refreshRutas();
			}
		}
		@Override
		public void onPause()
		{
			if(_sectionNumber == Util.RUTAS)
				LocalBroadcastManager.getInstance(_apf[Util.RUTAS].getContext()).unregisterReceiver(_MessageReceiver);
  			super.onPause();
		}

		//__________________________________________________________________________________________
		public void refreshLugares()
		{
			if(_aFiltro[_sectionNumber].isOn())
			{
System.err.println("---------FILTRO:" + _aFiltro[_sectionNumber]);
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
				System.err.println("---------LUGARES2:GET:OK:" + n);
				if(n < 1)
				{
					if(_this._viewPager.getCurrentItem() == Util.LUGARES && _apf[Util.LUGARES].isAdded())
						Snackbar.make(ActMain._coordinatorLayout, getString(R.string.lista_vacia), Snackbar.LENGTH_SHORT).show();
				}
				_listView.setAdapter(new LugarArrayAdapter(_rootView.getContext(), aLugares, PlaceholderFragment.this));//.toArray(new Lugar[0])));
			}
			@Override
			public void onError(String err)//FirebaseError err)
			{
				System.err.println("---------LUGARES2:GET:ERROR:" + err);
			}
		};
		/*private ValueEventListener _acLugar = new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot lugares)
			{
				long n = lugares.getChildrenCount();
				System.err.println("---------LUGARES:GET:OK:" + n);
				if(n < 1 && ActMain._coordinatorLayout != null)
				{
					if(_this._viewPager.getCurrentItem() == Util.LUGARES && _apf[Util.LUGARES].isAdded())
						Snackbar.make(ActMain._coordinatorLayout, getString(R.string.lista_vacia), Snackbar.LENGTH_SHORT).show();
				}
				int i = 0;
				Lugar[] aLugares = new Lugar[(int)n];
				for(DataSnapshot o : lugares.getChildren())
				{
					aLugares[i++] = o.getValue(Lugar.class);
				}
				_listView.setAdapter(new LugarArrayAdapter(_rootView.getContext(), aLugares, PlaceholderFragment.this));//.toArray(new Lugar[0])));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("---------LUGARES:GET:ERROR:" + err);
			}
		};*/

		//__________________________________________________________________________________________
		public void refreshRutas()
		{
System.err.println("ActMain:refreshRutas()");
			if(_aFiltro[_sectionNumber].isOn())
			{
				checkFechas();
				Ruta.getLista(_acRuta, _aFiltro[_sectionNumber]);
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
				System.err.println("---------RUTAS2:GET:OK:" + n);
				if(n < 1)
				{
					//TODO: por que se llama a ActMain:refreshRutas() tantas veces?
					//TODO:
					//if(_this._viewPager.getCurrentItem() == Util.LUGARES && _apf[Util.LUGARES].isAdded())
					//	Snackbar.make(ActMain._coordinatorLayout, getString(R.string.lista_vacia), Snackbar.LENGTH_SHORT).show();
				}
				else
				_listView.setAdapter(new RutaArrayAdapter(_rootView.getContext(), aRutas, PlaceholderFragment.this));//.toArray(new Lugar[0])));
			}
			@Override
			public void onError(String err)
			{
				System.err.println("---------RUTAS2:GET:ERROR:" + err);
			}
		};/*
		private ValueEventListener _acRuta = new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot rutas)
			{
				long n = rutas.getChildrenCount();
				System.err.println("---------RUTAS:GET:OK:" + n);//TODO: Usar text to speach y opcion de config para habilitarlo...
				if(n < 1)
				{
					if(_this._viewPager.getCurrentItem() == Util.RUTAS && _apf[Util.RUTAS].isAdded())
						Snackbar.make(ActMain._coordinatorLayout, getString(R.string.lista_vacia), Snackbar.LENGTH_SHORT).show();
				}
				int i = 0;
				Ruta[] aRutas = new Ruta[(int)n];
				for(DataSnapshot o : rutas.getChildren())
				{
					try
					{
						aRutas[i] = o.getValue(Ruta.class);
						i++;
					}
					catch(Exception e)
					{
						System.err.println("---------RUTAS:ValueEventListener:e:"+e+" : "+o);
					}
				}
				RutaArrayAdapter r = new RutaArrayAdapter(_rootView.getContext(), aRutas, PlaceholderFragment.this);
				_listView.setAdapter(r);
				r.notifyDataSetChanged();
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("---------RUTAS:GET:ERROR:" + err);
			}
		};*/

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
				System.err.println("---------AVISOS2:GET:OK:" + n);
				if(n < 1)
				{
					if(_this._viewPager.getCurrentItem() == Util.AVISOS && _apf[Util.AVISOS].isAdded())
						Snackbar.make(ActMain._coordinatorLayout, getString(R.string.lista_vacia), Snackbar.LENGTH_SHORT).show();
				}
				_listView.setAdapter(new AvisoArrayAdapter(_rootView.getContext(), aAvisos, PlaceholderFragment.this));
			}
			@Override
			public void onError(String err)
			{
				System.err.println("---------AVISOS2:GET:ERROR:" + err);
			}
		};
		/*private ValueEventListener _acAviso = new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot avisos)
			{
				long n = avisos.getChildrenCount();
				System.err.println("---------LUGARES:GET:OK:" + n);
				if(n < 1)
				{
					if(_this._viewPager.getCurrentItem() == Util.AVISOS && _apf[Util.AVISOS].isAdded())
						Snackbar.make(ActMain._coordinatorLayout, getString(R.string.lista_vacia), Snackbar.LENGTH_SHORT).show();
				}
				int i = 0;
				Aviso[] aAviso = new Aviso[(int)n];
				for(DataSnapshot o : avisos.getChildren())
				{
					aAviso[i++] = o.getValue(Aviso.class);
				}
				_listView.setAdapter(new AvisoArrayAdapter(_rootView.getContext(), aAviso, PlaceholderFragment.this));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("---------AVISOS:GET:ERROR:" + err);
			}
		};*/

		//__________________________________________________________________________________________
		//TODO: a otro lugar
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