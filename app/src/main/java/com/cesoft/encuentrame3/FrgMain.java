package com.cesoft.encuentrame3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.adapters.AvisoArrayAdapter;
import com.cesoft.encuentrame3.adapters.IListaItemClick;
import com.cesoft.encuentrame3.adapters.LugarArrayAdapter;
import com.cesoft.encuentrame3.adapters.RutaArrayAdapter;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import java.util.Date;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the {@link MainIterface} interface to handle interaction events.
 * Use the {@link FrgMain#newInstance} factory method to create an instance of this fragment.
 */
public class FrgMain extends Fragment implements IListaItemClick
{
	private static final String TAG = FrgMain.class.getSimpleName();
	private static final String ARG_SECTION_NUMBER = "section_number";

	private Filtro filtro;
		public Filtro getFiltro(){return filtro;}

	@Inject	Util util;

	private View rootView;
	private ListView listView;			//Lista de Lugares, Rutas, Avisos
	private Parcelable scroll = null;	//Recuerda posicion del scroll de la lista
	private MainIterface main;
	private int sectionNumber = Constantes.LUGARES;
	public int getSectionNumber() { return sectionNumber; }

	//----------------------------------------------------------------------------------------------
	public FrgMain() {
		// Required empty public constructor
	}
	public static FrgMain newInstance(int sectionNumber) {
		FrgMain fragment = new FrgMain();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		fragment.setRetainInstance(true);
		return fragment;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(App.getComponent(getContext()) != null)
			App.getComponent(getContext()).inject(this);

		Bundle args = getArguments();
		if(args != null)
			sectionNumber = args.getInt(ARG_SECTION_NUMBER);

		filtro = new Filtro(sectionNumber);

		rootView = inflater.inflate(R.layout.act_main_frag, container, false);
		listView = rootView.findViewById(R.id.listView);

		if(sectionNumber < 0)
		{
			Log.e(TAG, "onCreateView:-----------------------------------------sectionNumber="+ sectionNumber);
			main.gotoLogin();
			return null;
		}

		FloatingActionButton fab = rootView.findViewById(R.id.fabNuevo);
		fab.setOnClickListener(view ->
		{
			switch(sectionNumber)
			{
				case Constantes.LUGARES:	main.onLugar();	break;
				case Constantes.RUTAS:		main.onRuta();	break;
				case Constantes.AVISOS:		main.onAviso();	break;
				default:break;
			}
		});

		TextView textView = new TextView(rootView.getContext());
		switch(sectionNumber)
		{
			case Constantes.LUGARES:
				textView.setText(getString(R.string.lugares));
				break;
			case Constantes.RUTAS:
				textView.setText(getString(R.string.rutas));
				break;
			case Constantes.AVISOS:
				textView.setText(getString(R.string.avisos));
				break;
			default:break;
		}
		listView.addHeaderView(textView);

		return rootView;
	}
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		ViewGroup view = ((ViewGroup) rootView.getParent());
		if(view != null) view.removeView(rootView);
		rootView = null;
		listView = null;
	}

	//______________________________________________________________________________________________
	@Override
	public void onStart()
	{
		super.onStart();
		newListeners();

		switch(sectionNumber)
		{
			case Constantes.LUGARES:refreshLugares();	break;
			case Constantes.RUTAS:	refreshRutas();		break;
			case Constantes.AVISOS:	refreshAvisos();	break;
			default:break;
		}

		/// Actualizar lista de rutas
		if(sectionNumber == Constantes.RUTAS)
		{
			util.setRefreshCallback(this);
			messageReceiver = new BroadcastReceiver() {
				@Override public void onReceive(Context context, Intent intent) {
					refreshRutas();
				}
			};
		}
	}
	//______________________________________________________________________________________________
	@Override
	public void onStop()
	{
		super.onStop();
		messageReceiver = null;
		if(sectionNumber == Constantes.RUTAS)
			util.setRefreshCallback(null);
		delListeners();
	}
	//______________________________________________________________________________________________
	@Override
	public void onResume()
	{
		super.onResume();
		if(sectionNumber == Constantes.RUTAS)
		{
			if(getContext() != null)
				LocalBroadcastManager
						.getInstance(getContext())
						.registerReceiver(messageReceiver, new IntentFilter(RUTA_REFRESH));
			refreshRutas();
		}
	}
	@Override
	public void onPause()
	{
		super.onPause();
		scroll = listView.onSaveInstanceState();
		if(sectionNumber == Constantes.RUTAS && getContext() != null)
			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageReceiver);
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

		void buscar(FrgMain f);
		int getCurrentItem();
	}
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		if(context instanceof MainIterface) {
			main = (MainIterface)context;
		}
		else {
			throw new RuntimeException(context.toString() + " must implement MainIterface");
		}
	}
	@Override
	public void onDetach()
	{
		super.onDetach();
		main = null;
	}


	//// implements IListaItemClick
	//______________________________________________________________________________________________
	@Override
	public void onItemEdit(int tipo, Objeto obj)
	{
		switch(tipo)
		{
			case Constantes.LUGARES:main.goLugar(obj);	break;
			case Constantes.AVISOS:	main.goAviso(obj);	break;
			case Constantes.RUTAS:	main.goRuta(obj);	break;
			default:break;
		}
	}
	@Override
	public void onItemMap(int tipo, Objeto obj)
	{
		switch(tipo)
		{
			case Constantes.LUGARES:main.goLugarMap(obj);	break;
			case Constantes.AVISOS:	main.goAvisoMap(obj);	break;
			case Constantes.RUTAS:	main.goRutaMap(obj);	break;
			default:break;
		}
	}

	// 4 IListaItemClick
	private BroadcastReceiver messageReceiver;
	private static final String RUTA_REFRESH = "ces";

	public void onRefreshListaRutas() {
		if(getContext() != null)
			LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(RUTA_REFRESH));
	}


	//______________________________________________________________________________________________
	private Fire.DatosListener<Lugar> lisLugar;
	private Fire.DatosListener<Aviso> lisAviso;
	private Fire.DatosListener<Ruta> lisRuta;
	//----------------------------------------------------------------------------------------------
	private void delListeners()
	{
		if(lisLugar !=null) lisLugar.setListener(null);
		if(lisAviso !=null) lisAviso.setListener(null);
		if(lisRuta !=null) lisRuta.setListener(null);
	}

	///(@StringRes int idString) {
	private void showMsgListaVacia() {
		try{Toast.makeText(getContext(), R.string.lista_vacia, Toast.LENGTH_SHORT).show();}
		catch(Exception e){Log.e(TAG, "showToast:e:%s", e);}
	}
	private void setLugaresListener() {
		lisLugar = new Fire.DatosListener<Lugar>()
		{
			@Override
			public void onDatos(Lugar[] aLugares)
			{
				long n = aLugares.length;
				if(n < 1)
				{
					try { if(main.getCurrentItem() == Constantes.LUGARES) showMsgListaVacia(); }
					catch(Exception e){Log.e(TAG, String.format("_acLugar:%s",e), e);}
				}
				if(listView != null && rootView != null)
				{
					scroll = listView.onSaveInstanceState();
					listView.setAdapter(new LugarArrayAdapter(rootView.getContext(), aLugares, FrgMain.this));
					listView.setContentDescription(getString(R.string.lugares));//Para Espresso
					listView.onRestoreInstanceState(scroll);
				}
			}
			@Override
			public void onError(String err)//FirebaseError err)
			{
				Log.e(TAG, "LUGARES2:GET:e:---------------------------------------------------------"+err);
			}
		};
	}
	private void setRutasListener() {
		lisRuta = new Fire.DatosListener<Ruta>()
		{
			@Override
			public void onDatos(Ruta[] aRutas)
			{
				long n = aRutas.length;
				if(n < 1)
				{
					try { if(main.getCurrentItem() == Constantes.RUTAS) showMsgListaVacia(); }
					catch(Exception e){Log.e(TAG, "_acRuta:e:------------------------------------------", e);}
				}
				if(rootView != null)
				{
					scroll = listView.onSaveInstanceState();
					RutaArrayAdapter r = new RutaArrayAdapter(rootView.getContext(), aRutas, FrgMain.this);
					listView.setAdapter(r);
					listView.setContentDescription(getString(R.string.rutas));//Para Espresso
					r.notifyDataSetChanged();
					listView.onRestoreInstanceState(scroll);
				}
			}
			@Override public void onError(String err) {
				Log.e(TAG, "RUTAS2:GET:e:-----------------------------------------------------------"+err);
			}
		};
	}
	private void setAvisoListener() {
		lisAviso = new Fire.DatosListener<Aviso>()
		{
			@Override
			public void onDatos(Aviso[] aAvisos)
			{
				long n = aAvisos.length;
				if(n < 1 && main.getCurrentItem() == Constantes.AVISOS)
				{
					showMsgListaVacia();
				}
				if(listView != null && rootView != null)
				{
					scroll = listView.onSaveInstanceState();
					listView.setAdapter(new AvisoArrayAdapter(rootView.getContext(), aAvisos, FrgMain.this));
					listView.setContentDescription(getString(R.string.avisos));//Para Espresso
					listView.onRestoreInstanceState(scroll);
				}
			}
			@Override public void onError(String err) { Log.e(TAG, "AVISOS2:GET:e:------------------"+err); }
		};
	}
	private void newListeners()
	{
		delListeners();
		//--- LUGARES
		setLugaresListener();
		//--- RUTAS
		setRutasListener();
		//--- AVISO
		setAvisoListener();
	}

	//______________________________________________________________________________________________
	public void refreshLugares()
	{
		if(filtro == null)
		{
			Log.e(TAG, "refreshLugares:------------------------- FILTRO = NULL   "+ sectionNumber);
			filtro = new Filtro(sectionNumber);
		}
		if(filtro.isOn())
		{
			checkFechas();
			Lugar.getLista(lisLugar, filtro);
		}
		else
			Lugar.getLista(lisLugar);
	}

	//__________________________________________________________________________________________
	public void refreshRutas()
	{
		if(filtro.isOn())
		{
			checkFechas();
			Ruta.getLista(lisRuta, filtro, util.getTrackingRoute());
		}
		else
			Ruta.getLista(lisRuta);
	}

	//__________________________________________________________________________________________
	public void refreshAvisos()
	{
		if(filtro.isOn())
		{
			checkFechas();
			Aviso.getLista(lisAviso, filtro);
		}
		else
			Aviso.getLista(lisAviso);
	}

	//__________________________________________________________________________________________
	private void checkFechas()
	{
		Date ini = filtro.getFechaIni();
		Date fin = filtro.getFechaFin();
		if(ini!=null && fin!=null && ini.getTime() > fin.getTime())
		{
			filtro.setFechaIni(fin);
			filtro.setFechaFin(ini);
		}
	}


	//----------------------------------------------------------------------------------------------
	// Recoge el resultado de startActivityForResult : buscar
	public static final String MENSAJE = "mensaje";
	public static final String DIRTY = "dirty";

	private void processDataResult(Intent data) {
		if(data != null)
		{
			try
			{
				String sMensaje = data.getStringExtra(MENSAJE);
				if(sMensaje != null && !sMensaje.isEmpty())
					Toast.makeText(getContext(), sMensaje, Toast.LENGTH_LONG).show();
			}
			catch(Exception e){Log.e(TAG, "processDataResult:e:--------------------------------------",e);}
			if( ! data.getBooleanExtra(DIRTY, true))return;
			Filtro filtro0 = data.getParcelableExtra(Filtro.FILTRO);

			if(filtro != null && filtro.getTipo() != Constantes.NADA)
			{
				this.filtro = filtro0;
				if( ! filtro.isOn())
					Toast.makeText(getContext(), getString(R.string.sin_filtro), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode != RESULT_OK)return;

		processDataResult(data);

		if(requestCode == Constantes.BUSCAR && filtro != null)requestCode = filtro.getTipo();
		switch(requestCode)
		{
			case Constantes.LUGARES:refreshLugares(); break;
			case Constantes.RUTAS:	refreshRutas(); break;
			case Constantes.AVISOS:	refreshAvisos(); break;
			default:break;
		}
	}

}
