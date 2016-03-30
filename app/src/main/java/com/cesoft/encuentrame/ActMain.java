package com.cesoft.encuentrame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcel;
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

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Lugar;
import com.cesoft.encuentrame.models.Objeto;
import com.cesoft.encuentrame.models.Ruta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//TODO: Dejar ingles como lengua por defecto: mover ingles a carpeta default y crear carpeta español...
//TODO: mostrar fecha de creacion y modificacion en vistas...
//TODO: CONFIF: hacer vista de configuracion : usr/pwd de backendless, start at boot, dont ask for password->save login and password, delay to tracking routes, geofence radius?...
//TODO: icono app : android con gorro de wally?
//TODO: Main menu => refresh listas, or inside config: refresh data...
//TODO: widget para ruta start/stop... widget para guardar punto...
//TODO: Add photo to lugar & alerta n save it in backendless...
//TODO: Menu para ir al inicio, asi cuando abres aviso puedes volver y no cerrar directamente
//TODO: main window=> Number or routes, places and geofences...
//TODO: CATEGORIA: hacer vista de lista y CRUD
//TODO: Develop a web app for points management : connect to backendless by REST API...
//MOCK LOCATIONS ON DEVICE : http://stackoverflow.com/questions/2531317/android-mock-location-on-device
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity
{
	private ViewPager _viewPager;
	public static final String PAGINA = "pagina", MENSAJE = "mensaje", DIRTY = "dirty";
	private static ActMain _this;
	private static CoordinatorLayout _coordinatorLayout;

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

		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		_viewPager = (ViewPager)findViewById(R.id.container);
		_viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(_viewPager);

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

//cargaDatosDebug();//TODO:Debug
//Util.refreshListaRutas();
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
		// Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
		Intent i;
		int id = item.getItemId();
		switch(id)
		{
		case R.id.action_config:
			startActivity(new Intent(this, ActConfig.class));
			return true;
		case R.id.action_mapa:
			i = new Intent(this, ActMaps.class);
			i.putExtra(Util.TIPO, _viewPager.getCurrentItem());
			startActivity(i);
			return true;
		case R.id.action_buscar:
			i = new Intent(this, ActBuscar.class);
			i.putExtra(Util.TIPO, _viewPager.getCurrentItem());
			startActivityForResult(i, Util.LUGARES);//TODO
			//startActivity(i);
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
	public static class PlaceholderFragment extends Fragment implements CesIntLista
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		private static PlaceholderFragment[] _apf = new PlaceholderFragment[3];

		public PlaceholderFragment(){}

		// Returns a new instance of this fragment for the given section number.
		public static PlaceholderFragment newInstance(final int sectionNumber)
		{
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			_apf[sectionNumber] = fragment;
			return fragment;
		}

		private View _rootView;
		private ListView _listView;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			Bundle args = getArguments();
			final int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
			_rootView = inflater.inflate(R.layout.act_main_frag, container, false);
			_listView = (ListView)_rootView.findViewById(R.id.listView);
			final TextView textView = new TextView(_rootView.getContext());

			FloatingActionButton fab = (FloatingActionButton)_rootView.findViewById(R.id.fabNuevo);
			fab.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					//Snackbar.make(view, "Replace with your own action: " + _viewPager.getCurrentItem(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
					Intent i;
					//switch(_viewPager.getCurrentItem())
					switch(sectionNumber)
					{
					case Util.LUGARES:
						i = new Intent(ActMain._this, ActLugar.class);
						startActivityForResult(i, Util.LUGARES);
						break;
					case Util.RUTAS:
						i = new Intent(ActMain._this, ActRuta.class);
						startActivityForResult(i, Util.RUTAS);
						break;
					case Util.AVISOS:
						i = new Intent(ActMain._this, ActAviso.class);
						startActivityForResult(i, Util.AVISOS);
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

		//// implements CesIntLista
		//______________________________________________________________________________________________
		@Override
		public void onItemEdit(tipoLista tipo, Objeto obj)
		{
			Intent i;
			switch(tipo)
			{
			case LUGAR:
				i = new Intent(ActMain._this, ActLugar.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, Util.LUGARES);
				break;
			case RUTA:
				i = new Intent(ActMain._this, ActRuta.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, Util.RUTAS);
				break;
			case AVISO:
				i = new Intent(ActMain._this, ActAviso.class);
				i.putExtra(Aviso.NOMBRE, obj);
				startActivityForResult(i, Util.AVISOS);
				break;
			}
		}
		@Override
		public void onItemMap(tipoLista tipo, Objeto obj)
		{
			Intent i;
			switch(tipo)
			{
			case LUGAR:
				i = new Intent(ActMain._this, ActMaps.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, Util.LUGARES);
				break;
			case RUTA:
				i = new Intent(ActMain._this, ActMaps.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, Util.RUTAS);
				break;
			case AVISO:
				i = new Intent(ActMain._this, ActMaps.class);
				i.putExtra(Aviso.NOMBRE, obj);
				startActivityForResult(i, Util.AVISOS);
				break;
			}
		}
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data)
		{
System.err.println("---------ActMain:onActivityResult:0:");
			if(data != null)
			{
				String sMensaje = data.getStringExtra(MENSAJE);
				if(sMensaje != null && !sMensaje.isEmpty())
					Snackbar.make(ActMain._coordinatorLayout, getString(R.string.ok_guardar), Snackbar.LENGTH_LONG).show();
				if( ! data.getBooleanExtra(DIRTY, true))return;
			}
System.err.println("---------ActMain:onActivityResult:1:");
			if(resultCode != RESULT_OK)return;
System.err.println("---------ActMain:onActivityResult:2:"+requestCode);
			switch(requestCode)
			{
			case Util.LUGARES:	refreshLugares(); break;
			case Util.RUTAS:	refreshRutas(); break;
			case Util.AVISOS:	refreshAvisos(); break;
			}
		}
		// 4 CesIntLista
		public void onRefreshListaRutas()
		{
System.err.println("---------ActMain:onRefreshListaRutas():");
			//ActMain._this.runOnUiThread(new Runnable(){public void run(){refreshRutas();}});//No funciona, ha de hacerse mediante broadcast...
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent("ces"));
		}
		@Override
		public void onResume()
		{
  			super.onResume();
			if(_MessageReceiver != null)
			//if(_apf[ActMain.RUTAS] != null && _apf[ActMain.RUTAS].getContext() !=null && LocalBroadcastManager.getInstance(_apf[ActMain.RUTAS].getContext()) != null)
  			LocalBroadcastManager.getInstance(_apf[Util.RUTAS].getContext()).registerReceiver(_MessageReceiver, new IntentFilter("ces"));
		}
		@Override
		public void onPause()
		{
			if(_MessageReceiver != null)
			//if(_apf[ActMain.RUTAS] != null && _apf[ActMain.RUTAS].getContext() !=null && LocalBroadcastManager.getInstance(_apf[ActMain.RUTAS].getContext()) != null)
			LocalBroadcastManager.getInstance(_apf[Util.RUTAS].getContext()).unregisterReceiver(_MessageReceiver);
  			super.onPause();
		}
		private BroadcastReceiver _MessageReceiver;


		//__________________________________________________________________________________________
		public void refreshLugares()//TODO: Hacer filtro...
		{
			Lugar.getLista(new AsyncCallback<BackendlessCollection<Lugar>>()
			{
				@Override
				public void handleResponse(BackendlessCollection<Lugar> lugares)
				{
					int n = lugares.getTotalObjects();
					System.err.println("---------LUGARES:GET:OK:" + n);
					if(n < 1)
						return;
					Iterator<Lugar> iterator = lugares.getCurrentPage().iterator();
					Lugar[] listaAL = new Lugar[n];
					int i = 0;
					while(iterator.hasNext())
						listaAL[i++] = iterator.next();
					_listView.setAdapter(new LugarArrayAdapter(_rootView.getContext(), listaAL, PlaceholderFragment.this));//.toArray(new Lugar[0])));
				}
				@Override
				public void handleFault(BackendlessFault backendlessFault)
				{
					System.err.println("---------LUGARES:GET:ERROR:" + backendlessFault);//LUGARES:GET:ERROR:BackendlessFault{ code: '1009', message: 'Unable to retrieve data - unknown entity' }
				}
			});
		}
		//__________________________________________________________________________________________
		public void refreshRutas()
		{
System.err.println("ActMain:refreshRutas()");
			Ruta.getLista(new AsyncCallback<BackendlessCollection<Ruta>>()
			{
				@Override
				public void handleResponse(BackendlessCollection<Ruta> rutas)
				{
					int n = rutas.getTotalObjects();
					System.err.println("---------RUTAS:GET:OK:" + n);
					if(n < 1)
						return;
					Iterator<Ruta> iterator = rutas.getCurrentPage().iterator();
					Ruta[] listaAL = new Ruta[n];
					int i = 0;
					while(iterator.hasNext())
						listaAL[i++] = iterator.next();
					RutaArrayAdapter r = new RutaArrayAdapter(_rootView.getContext(), listaAL, PlaceholderFragment.this);
					_listView.setAdapter(r);
					r.notifyDataSetChanged();
				}
				@Override
				public void handleFault(BackendlessFault backendlessFault)
				{
					System.err.println("---------RUTAS:GET:ERROR:" + backendlessFault);
				}
			});
		}

		//__________________________________________________________________________________________
		public void refreshAvisos()
		{
			Aviso.getLista(new AsyncCallback<BackendlessCollection<Aviso>>()
			{
				@Override
				public void handleResponse(BackendlessCollection<Aviso> avisos)
				{
					int n = avisos.getTotalObjects();
					System.err.println("---------AVISOS:GET:OK:" + n);
					if(n < 1)
						return;
					Iterator<Aviso> iterator = avisos.getCurrentPage().iterator();
					Aviso[] listaAL = new Aviso[n];
					int i = 0;
					while(iterator.hasNext())
						listaAL[i++] = iterator.next();
					_listView.setAdapter(new AvisoArrayAdapter(_rootView.getContext(), listaAL, PlaceholderFragment.this));
				}
				@Override
				public void handleFault(BackendlessFault backendlessFault)
				{
					System.err.println("---------AVISOS:GET:ERROR:" + backendlessFault);
				}
			});
		}

	}



//TODO:DEBUG
	private void cargaDatosDebug()
	{
		//-------- LUGAR --------
		/*
		Lugar l = new Lugar();
		l.setNombre("Lugar 3");
		l.setDescripcion("Lug Desc 3");
		l.setLugar(new GeoPoint(40.4676353, -3.5608333));
		l.guardar(new AsyncCallback<Lugar>()
		{
			@Override
			public void handleResponse(Lugar lugar){System.err.println("************* L3-----------" + lugar);}
			@Override
			public void handleFault(BackendlessFault backendlessFault){System.err.println("*********** FAIL:L3-----------"+backendlessFault);}
		});

		/**/

		/*
		//-------- AVISO --------
		Aviso a = new Aviso();
		a.setNombre("Aviso 1");
		a.setDescripcion("Aviso 1: Recoge la nota del buzon muerto1");
		GeoPoint l = new GeoPoint(40.4676111, -3.5608111);
		a.setLugar(l, 2000);//l.setDistance(1000);	//l.setRa(Aviso.RADIO, 1000);
		a.guardar(new AsyncCallback<Aviso>()
		{
			@Override
			public void handleResponse(Aviso aviso){System.err.println("************* A5-----------" + aviso);}
			@Override
			public void handleFault(BackendlessFault backendlessFault){System.err.println("*********** FAIL:A5-----------"+backendlessFault);}
		});
		/**/


		//-------- RUTA --------
		Ruta r = new Ruta();
		r.setNombre("Ruta 1");
		r.setDescripcion("Ruta 1 desc");

		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try
		{
			r.addPunto(new GeoPoint(40.4610001, -3.5611001), format.parse("2016-03-30 8:0:0"));
			r.addPunto(new GeoPoint(40.4752002, -3.5644002), format.parse("2016-03-30 8:5:0"));
			r.addPunto(new GeoPoint(40.4893003, -3.5677003), format.parse("2016-03-30 8:10:0"));
			r.addPunto(new GeoPoint(40.4940004, -3.5711004), format.parse("2016-03-30 8:15:0"));
			r.addPunto(new GeoPoint(40.5050005, -3.5744005), format.parse("2016-03-30 8:20:0"));
		} catch (Exception e) {   e.printStackTrace();}
		System.err.println("************* ruta-----------"+r);
		r.guardar(new AsyncCallback<Ruta>()
		{
			@Override
			public void handleResponse(Ruta ruta)
			{
				System.err.println("************* OK-----------"+ruta);
				Ruta.getLista(new AsyncCallback<BackendlessCollection<Ruta>>()
				{
					@Override
					public void handleResponse(BackendlessCollection<Ruta> rutaBackendlessCollection)
					{
						System.err.println("************* OK OK-----------"+rutaBackendlessCollection.getCurrentPage().size()+" : "+rutaBackendlessCollection.getCurrentPage().get(0));
					}
					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						System.err.println("*********** OK FAIL-----------" + backendlessFault);
					}
				});
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("*********** FAIL-----------" + backendlessFault);
			}
		});
				/*System.err.println("************* R1-----------" + ruta);
				Map<String, Object> meta = new HashMap<>();
				meta.put(Ruta.NOMBRE, ruta);
				try
				{
					Backendless.Geo.savePoint(40.4676555, -3.5608555, meta, new AsyncCallback<GeoPoint>()
					{
						@Override
						public void handleResponse(GeoPoint geoPoint)
						{
							System.out.println("*************  pto1-----------" + geoPoint.getObjectId());
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							System.out.println("*********** FAIL: pto1-----------" + backendlessFault);
						}
					});
					Backendless.Geo.savePoint(40.4676599, -3.5608599, meta, new AsyncCallback<GeoPoint>()
					{
						@Override
						public void handleResponse(GeoPoint geoPoint)
						{
							System.out.println("************* pto2-----------" + geoPoint.getObjectId());
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							System.out.println("*********** FAIL: pto2-----------" + backendlessFault);
						}
					});
					Backendless.Geo.savePoint(40.4676699, -3.5608699, meta, new AsyncCallback<GeoPoint>()
					{
						@Override
						public void handleResponse(GeoPoint geoPoint)
						{
							System.out.println("*************  pto3-----------" + geoPoint.getObjectId());
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							System.out.println("*********** FAIL: pto3-----------" + backendlessFault);
						}
					});
					Backendless.Geo.savePoint(40.4676799, -3.5608799, meta, new AsyncCallback<GeoPoint>()
					{
						@Override
						public void handleResponse(GeoPoint geoPoint)
						{
							System.out.println("************* pto4-----------" + geoPoint.getObjectId());
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							System.out.println("*********** FAIL: pto4-----------" + backendlessFault);
						}
					});
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("*********** FAIL:R1-----------" + backendlessFault);
			}
		});
		/**/

	}
}

