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
import com.cesoft.encuentrame.models.Lugar;

import java.util.ArrayList;
import java.util.Iterator;

//TODO:icono app
//TODO: main window=> Number or routes, places and geofences...
//TODO: ventana de mapa que muestre punto, ruta o geofence...
//TODO: CONFIF: hacer vista de configuracion : start at boot, dont ask for password->save login and password, delay to tracking routes, geofence radius?...
//TODO: CATEGORIA: hacer vista de lista y CRUD
//TODO: Mejorar aspecto boton de login...
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMain extends AppCompatActivity
{
	private SectionsPagerAdapter _SectionsPagerAdapter;
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
		_SectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		_viewPager = (ViewPager)findViewById(R.id.container);
		_viewPager.setAdapter(_SectionsPagerAdapter);
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
					startActivity(new Intent(getBaseContext(), ActLugar.class));
					//i.putExtra("aviso", _o.getAviso());
					//startActivityForResult(i, AVISO);//TODO: si es guardado, borrado => refresca la vista, si no nada
					break;
				case RUTAS:
					break;
				case AVISOS:
					break;
				}
			}
		});

		cargaDatosDebug();
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
	public static class PlaceholderFragment extends Fragment
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

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			Bundle args = getArguments();
System.err.println(args.getInt(ARG_SECTION_NUMBER)+"--------------args.getInt(ARG_SECTION_NUMBER)");
			final int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
			final View rootView = inflater.inflate(R.layout.act_main_frag, container, false);
			final ListView listView = (ListView)rootView.findViewById(R.id.listView);
			final TextView textView = new TextView(rootView.getContext());

			switch(sectionNumber)
			{
			case LUGARES://---------------------------------------------------------------------------
				textView.setText(getString(R.string.lugares));
				Lugar.getLista(new AsyncCallback<BackendlessCollection<Lugar>>()
						{
							@Override
							public void handleResponse(BackendlessCollection<Lugar> lugares)
							{
								int n = lugares.getTotalObjects();
								System.err.println("---------LUGARES:GET:OK:" + n);
								if(n < 1)return;//TODO:change to use Lugar[] directly

								Iterator<Lugar> iterator = lugares.getCurrentPage().iterator();
								Lugar[] listaAL = new Lugar[n];
								//for(int i=0; i < n; i++)
								int i=0;
								while(iterator.hasNext())
									listaAL[i++] = iterator.next();
								/*
								ArrayList<Lugar> listaAL = new ArrayList<>();
								while(iterator.hasNext())
									listaAL.add(iterator.next());
									*/
								listView.setAdapter(new LugarArrayAdapter(rootView.getContext(), listaAL));//.toArray(new Lugar[0])));
							}

							@Override
							public void handleFault(BackendlessFault backendlessFault)
							{
								System.err.println("---------LUGARES:GET:ERROR:" + backendlessFault);//LUGARES:GET:ERROR:BackendlessFault{ code: '1009', message: 'Unable to retrieve data - unknown entity' }
							}
						});
				//BackendlessCollection<Lugar> listaBE = Backendless.Data.of(Lugar.class).find();
				//BackendlessCollection<GeoPoint> points = Backendless.Geo.getPoints( geoQuery);
				//Iterator<GeoPoint> iterator=points.getCurrentPage().iterator();
				break;

			case RUTAS://------------------------------------------------------------------------
				textView.setText(getString(R.string.rutas));
				break;

			case AVISOS://-------------------------------------------------------------------------
				textView.setText(getString(R.string.avisos));
				break;
			}

			listView.addHeaderView(textView);
			return rootView;
		}

		//______________________________________________________________________________________________
		/*public void refrescarLista()
		{
			Iterator<Objeto> it = Objeto.findAll(Objeto.class);
			ArrayList<Objeto> lista = Objeto.conectarHijos(it);//TODO:Por que no funciona con la lista pasada????? Lo deja duplicado y el nuevo no es editable???
			ActEdit.setLista(lista);
			_expListView.setAdapter(new NivelUnoListAdapter(this.getApplicationContext(), _expListView, lista));
			_expListView.refreshDrawableState();
		}*/

	}


//DEBUG
	private void cargaDatosDebug()
	{
		Lugar l = new Lugar();
		l.setNombre("Lugar 1");
		l.setDescripcion("Lug Desc 1");
		l.setLugar(new GeoPoint(40.4676352, -3.5608339));
		l.guardar(new AsyncCallback<Lugar>()
		{
			@Override
			public void handleResponse(Lugar lugar)
			{
				System.err.println("L1-----------" + lugar);
			}

			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
			}
		});

		l = new Lugar();
		l.setNombre("Lugar 2");
		l.setDescripcion("Lug Desc 2");
		l.setLugar(new GeoPoint(40.4690717,-3.5721635));
		l.guardar(new AsyncCallback<Lugar>()
			{
				@Override
				public void handleResponse(Lugar lugar)
				{
					System.err.println("L2-----------" + lugar);
				}
				@Override
				public void handleFault(BackendlessFault backendlessFault){}
			});

	}

}

