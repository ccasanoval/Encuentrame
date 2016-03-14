package com.cesoft.encuentrame;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

//TODO:Añadir margin top a todos incluso login
//TODO:guardar usr/pwd de backendless (luego en settings)
//TODO: CONFIF: hacer vista de configuracion : start at boot, dont ask for password->save login and password, delay to tracking routes, geofence radius?...
//TODO:icono app
//TODO: main window=> Number or routes, places and geofences...
//TODO: CATEGORIA: hacer vista de lista y CRUD
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity
{
	private ViewPager _viewPager;
	private static final int LUGARES=0, RUTAS=1, AVISOS=2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three primary sections of the activity.
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		_viewPager = (ViewPager)findViewById(R.id.container);
		_viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(_viewPager);

		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Snackbar.make(view, "Replace with your own action: " + _viewPager.getCurrentItem(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
				System.err.println("QAZ---------------------" + _viewPager.getCurrentItem());
				switch(_viewPager.getCurrentItem())
				{
				case LUGARES:
					//startActivity(new Intent(getBaseContext(), ActLugar.class));
					//i.putExtra("aviso", _o.getAviso());
					//startActivityForResult(i, AVISO);//TODO: si es guardado, borrado => refresca la vista, si no nada
					System.err.println("-------------***LUG");
					break;
				case RUTAS:
					break;
				case AVISOS:
					break;
				}
			}
		});
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
		int id = item.getItemId();
		//noinspection SimplifiableIfStatement
		switch(id)
		{
		case R.id.action_config:
			startActivity(new Intent(getBaseContext(), ActConfig.class));
			return true;
		case R.id.action_mapa:
			startActivity(new Intent(getBaseContext(), ActMaps.class));
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
			case LUGARES:
				return getString(R.string.lugares);
			case RUTAS:
				return getString(R.string.rutas);
			case AVISOS:
				return getString(R.string.avisos);
			}
			return null;
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	public static class PlaceholderFragment extends Fragment implements CesIntLista
	{
		private static final String ARG_SECTION_NUMBER = "section_number";
		//private ListView _ListView;

		public PlaceholderFragment(){}

		// Returns a new instance of this fragment for the given section number.
		public static PlaceholderFragment newInstance(int sectionNumber)
		{
System.err.println(sectionNumber+"--------------newInstance");
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		private View _rootView;
		private ListView _listView;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			Bundle args = getArguments();
System.err.println(args.getInt(ARG_SECTION_NUMBER)+"--------------args.getInt(ARG_SECTION_NUMBER)");
			final int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
			_rootView = inflater.inflate(R.layout.act_main_frag, container, false);
			_listView = (ListView)_rootView.findViewById(R.id.listView);
			final TextView textView = new TextView(_rootView.getContext());

			switch(sectionNumber)
			{
			case LUGARES://-------------------------------------------------------------------------
				textView.setText(getString(R.string.lugares));
				refreshLugares();
				break;

			case RUTAS://---------------------------------------------------------------------------
				textView.setText(getString(R.string.rutas));
				refreshRutas();
				break;

			case AVISOS://--------------------------------------------------------------------------
				textView.setText(getString(R.string.avisos));
				refreshAvisos();
				break;
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
				i = new Intent(getContext(), ActLugar.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, LUGARES);
				break;
			case RUTA:
				i = new Intent(getContext(), ActRuta.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, RUTAS);
				break;
			case AVISO:
				i = new Intent(getContext(), ActAviso.class);
				i.putExtra(Aviso.NOMBRE, obj);
				startActivityForResult(i, AVISOS);
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
				i = new Intent(getContext(), ActMaps.class);
				i.putExtra(Lugar.NOMBRE, obj);
				startActivityForResult(i, LUGARES);
				break;
			case RUTA:
				i = new Intent(getContext(), ActMaps.class);
				i.putExtra(Ruta.NOMBRE, obj);
				startActivityForResult(i, RUTAS);
				break;
			case AVISO:
				i = new Intent(getContext(), ActMaps.class);
				i.putExtra(Aviso.NOMBRE, obj);
				startActivityForResult(i, AVISOS);
				break;
			}
		}
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data)
		{
			super.onActivityResult(requestCode, resultCode, data);
			if(resultCode != RESULT_OK)return;
			if(requestCode == LUGARES)
			{
				//Snackbar.make(null, "Se guardaron los datos del lugar.", Snackbar.LENGTH_LONG).show();
				refreshLugares();
			}
			else if(requestCode == RUTAS)
			{
				//Snackbar.make(null, "Se guardaron los datos de la ruta.", Snackbar.LENGTH_LONG).show();
				refreshRutas();
			}
			else if(requestCode == AVISOS)
			{
				//Snackbar.make(null, "Se guardaron los datos del aviso.", Snackbar.LENGTH_LONG).show();
				refreshAvisos();
			}
		}


		//__________________________________________________________________________________________
		public void refreshLugares()
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
			Ruta.getLista(new AsyncCallback<BackendlessCollection<Ruta>>()
			{
				@Override
				public void handleResponse(BackendlessCollection<Ruta> rutas)
				{
					int n = rutas.getTotalObjects();
System.err.println("---------RUTAS:GET:OK:" + n);
					if(n < 1)return;
					Iterator<Ruta> iterator = rutas.getCurrentPage().iterator();
					Ruta[] listaAL = new Ruta[n];
					int i=0;
					while(iterator.hasNext())
						listaAL[i++] = iterator.next();
					_listView.setAdapter(new RutaArrayAdapter(_rootView.getContext(), listaAL, PlaceholderFragment.this));
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
				public void handleResponse(BackendlessCollection<Aviso> aviso)
				{
					int n = aviso.getTotalObjects();
System.err.println("---------AVISOS:GET:OK:" + n);
					if(n < 1)return;
					Iterator<Aviso> iterator = aviso.getCurrentPage().iterator();
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

		l = new Lugar();
		l.setNombre("Lugar 4");
		l.setDescripcion("Lug Desc 4");
		l.setLugar(new GeoPoint(40.4690714,-3.5721634));
		l.guardar(new AsyncCallback<Lugar>()
			{
				@Override
				public void handleResponse(Lugar lugar){System.err.println("************* L4-----------" + lugar);}
				@Override
				public void handleFault(BackendlessFault backendlessFault){System.err.println("*********** FAIL:L4-----------"+backendlessFault);}
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

		/*
		//-------- RUTA --------
		Ruta r = new Ruta();
		r.setNombre("Ruta 1");
		r.setDescripcion("Ruta 1 desc");
		r.guardar(new AsyncCallback<Ruta>()
		{
			@Override
			public void handleResponse(Ruta ruta)
			{
				System.err.println("************* R1-----------" + ruta);
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

