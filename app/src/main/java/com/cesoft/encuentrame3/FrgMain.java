package com.cesoft.encuentrame3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Util;

import java.util.Date;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link MainIterface} interface to handle interaction events.
 * Use the {@link FrgMain#newInstance} factory method to create an instance of this fragment.
 */
public class FrgMain extends Fragment implements IListaItemClick
{
	private static final String TAG = "CESoft:FrgMain";
	private static final String ARG_SECTION_NUMBER = "section_number";

	private Filtro _filtro;
		public Filtro getFiltro(){return _filtro;}

	@Inject	Util _util;

	private int _sectionNumber = Util.LUGARES;//Util.NADA;
	private View _rootView;
	private ListView _listView;

	private MainIterface _main;

	//----------------------------------------------------------------------------------------------
	public FrgMain()
	{
		// Required empty public constructor
	}
	public static FrgMain newInstance(int sectionNumber)
	{
		FrgMain fragment = new FrgMain();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		fragment.setRetainInstance(true);
		fragment._sectionNumber = sectionNumber;
		fragment._filtro = new Filtro(sectionNumber);//TODO: constructor?
		App.getInstance().getGlobalComponent().inject(fragment);
		return fragment;
	}


	//----------------------------------------------------------------------------------------------
	/*@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//if(getArguments() != null)int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
	}*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//Bundle args = getArguments();
		//final int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
		_rootView = inflater.inflate(R.layout.act_main_frag, container, false);
		_listView = (ListView)_rootView.findViewById(R.id.listView);
		final TextView textView = new TextView(_rootView.getContext());

		if(_sectionNumber < 0)
		{
			Log.e(TAG, "onCreateView:---------------------------------------------------------------_sectionNumber="+_sectionNumber);
			_main.gotoLogin();
			return null;
		}

		FloatingActionButton fab = (FloatingActionButton) _rootView.findViewById(R.id.fabNuevo);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				switch(_sectionNumber)//switch(_viewPager.getCurrentItem())
				{
					case Util.LUGARES:	_main.onLugar();	break;
					case Util.RUTAS:	_main.onRuta();		break;
					case Util.AVISOS:	_main.onAviso();	break;
				}
			}
		});

		switch(_sectionNumber)
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
		if(_sectionNumber == Util.RUTAS)
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
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		_rootView = null;
		_listView = null;
	}


	//______________________________________________________________________________________________
	@Override
	public void onStop()
	{
		super.onStop();
		//
		//_acLugar.delListener();
		//
		//_rootView = null;
		//_listView = null;
		//_filtro = null;
		_MessageReceiver = null;
		if(_sectionNumber == Util.RUTAS)
			Util.setRefreshCallback(null);
	}
	//______________________________________________________________________________________________
	@Override
	public void onResume()
	{
		super.onResume();
		if(_sectionNumber == Util.RUTAS)
		{
			LocalBroadcastManager.getInstance(getContext()).registerReceiver(_MessageReceiver, new IntentFilter(RUTA_REFRESH));
			refreshRutas();
		}
	}
	@Override
	public void onPause()
	{
		super.onPause();
		if(_sectionNumber == Util.RUTAS)
			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(_MessageReceiver);
	}


	//----------------------------------------------------------------------------------------------
	interface MainIterface
	{
		void gotoLogin();

		void onLugar();
		void onAviso();
		void onRuta();

		void goLugar(Objeto obj);
		void goAviso(Objeto obj);
		void goRuta(Objeto obj);

		void goLugarMap(Objeto obj);
		void goAvisoMap(Objeto obj);
		void goRutaMap(Objeto obj);

		void buscar(Fragment f, Filtro filtro);
		int getCurrentItem();
	}
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		if(context instanceof MainIterface)
		{
			_main = (MainIterface)context;
		}
		else
		{
			throw new RuntimeException(context.toString() + " must implement MainIterface");
		}
	}
	@Override
	public void onDetach()
	{
		super.onDetach();
		_main = null;
	}



	//// implements IListaItemClick
	//______________________________________________________________________________________________
	@Override
	public void onItemEdit(int tipo, Objeto obj)
	{
		switch(tipo)
		{
			case Util.LUGARES:	_main.goLugar(obj);	break;
			case Util.AVISOS:	_main.goAviso(obj);	break;
			case Util.RUTAS:	_main.goRuta(obj);	break;
		}
	}
	@Override
	public void onItemMap(int tipo, Objeto obj)
	{
		switch(tipo)
		{
			case Util.LUGARES:	_main.goLugarMap(obj);	break;
			case Util.AVISOS:	_main.goAvisoMap(obj);	break;
			case Util.RUTAS:	_main.goRutaMap(obj);	break;
		}
	}
	public void buscar()
	{
		_main.buscar(this, _filtro);
	}

		// 4 IListaItemClick
	private BroadcastReceiver _MessageReceiver;
	private static final String RUTA_REFRESH = "ces";

	public void onRefreshListaRutas()
	{
		//ActMain._this.runOnUiThread(new Runnable(){public void run(){refreshRutas();}});//No funciona, ha de hacerse mediante broadcast...
		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(RUTA_REFRESH));
	}


	//______________________________________________________________________________________________
	public void refreshLugares()
	{
		if(_filtro.isOn())
		{
			checkFechas();
			Lugar.getLista(_lisLugar, _filtro);
		}
		else
			Lugar.getLista(_lisLugar);
	}
	private Fire.ObjetoListener<Lugar> _lisLugar = new Fire.ObjetoListener<Lugar>()
	{
		@Override
		public void onData(Lugar[] aLugares)
		{
			long n = aLugares.length;
			if(n < 1)
			{
				try
				{
					if(_main.getCurrentItem() == Util.LUGARES)
						try{Toast.makeText(getContext(), getString(R.string.lista_vacia), Toast.LENGTH_SHORT).show();}
						catch(Exception e){Log.e(TAG, String.format("LUGARES:handleResponse:e:%s",e),e);}//java.lang.IllegalStateException: Fragment PlaceholderFragment{41e3b090} not attached to Activity
				}
				catch(Exception e){Log.e(TAG, String.format("_acLugar:%s",e), e);}
			}
			_listView.setAdapter(new LugarArrayAdapter(_rootView.getContext(), aLugares, FrgMain.this));
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
		if(_filtro.isOn())
		{
			checkFechas();
			Ruta.getLista(_lisRuta, _filtro, _util.getTrackingRoute());
		}
		else
			Ruta.getLista(_lisRuta);
	}
	private Fire.ObjetoListener<Ruta> _lisRuta = new Fire.ObjetoListener<Ruta>()
	{
		@Override
		public void onData(Ruta[] aRutas)
		{
			long n = aRutas.length;
			if(n < 1)
			{
				try
				{
					if(_main.getCurrentItem() == Util.RUTAS)
						try{Toast.makeText(getContext(), getString(R.string.lista_vacia), Toast.LENGTH_SHORT).show();}
						catch(Exception e){Log.e(TAG, "RUTAS:handleResponse:e:----------------------", e);}
				}catch(Exception e){Log.e(TAG, "_acRuta:e:------------------------------------------", e);}
			}
			RutaArrayAdapter r = new RutaArrayAdapter(_rootView.getContext(), aRutas, FrgMain.this);
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
		if(_filtro.isOn())
		{
			checkFechas();
			Aviso.getLista(_lisAviso, _filtro);
		}
		else
			Aviso.getLista(_lisAviso);
	}
	private Fire.ObjetoListener<Aviso> _lisAviso = new Fire.ObjetoListener<Aviso>()
	{
		@Override
		public void onData(Aviso[] aAvisos)
		{
			long n = aAvisos.length;
			if(n < 1)
			{
				if(_main.getCurrentItem() == Util.AVISOS)//if(_sectionNumber == Util.AVISOS)
					try{Toast.makeText(getContext(), getString(R.string.lista_vacia), Toast.LENGTH_SHORT).show();}
					catch(Exception e){Log.e(TAG, String.format("AVISOS:handleResponse:e:%s",e), e);}
			}
			_listView.setAdapter(new AvisoArrayAdapter(_rootView.getContext(), aAvisos, FrgMain.this));
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
		Date ini = _filtro.getFechaIni();
		Date fin = _filtro.getFechaFin();
		if(ini!=null && fin!=null && ini.getTime() > fin.getTime())
		{
			_filtro.setFechaIni(fin);
			_filtro.setFechaFin(ini);
		}
	}



	//----------------------------------------------------------------------------------------------
	// Recoge el resultado de startActivityForResult : buscar
	public static final String MENSAJE = "mensaje", DIRTY = "dirty";//TODO?! mensaje tambien en main...
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != RESULT_OK)return;

		/*if(requestCode == Util.CONFIG)
		{
			_main.gotoLogin();
			return;
		}*/

		if(data != null)
		{
			try
			{
				String sMensaje = data.getStringExtra(MENSAJE);
				if(sMensaje != null && !sMensaje.isEmpty())
					Toast.makeText(getContext(), sMensaje, Toast.LENGTH_LONG).show();
			}
			catch(Exception e){Log.e(TAG, "onActivityResult:e:--------------------------------------",e);}
			if( ! data.getBooleanExtra(DIRTY, true))return;
			Filtro filtro = data.getParcelableExtra(Filtro.FILTRO);
Log.e(TAG, "--------------------------onActivityResult: A::"+filtro);
			if(filtro != null && filtro.getTipo() != Util.NADA)
			{
				_filtro = filtro;
				if( ! filtro.isOn())
					Toast.makeText(getContext(), getString(R.string.sin_filtro), Toast.LENGTH_SHORT).show();
			}
		}

		if(requestCode == Util.BUSCAR)requestCode = _filtro.getTipo();
		switch(requestCode)
		{
			case Util.LUGARES:	refreshLugares(); break;
			case Util.RUTAS:	refreshRutas(); break;
			case Util.AVISOS:	refreshAvisos(); break;
		}
	}

}
