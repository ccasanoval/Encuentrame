package com.cesoft.encuentrame3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cesoft.encuentrame3.adapters.AvisoArrayAdapter;
import com.cesoft.encuentrame3.adapters.IListaItemClick;
import com.cesoft.encuentrame3.adapters.ShadowItemDecorator;
import com.cesoft.encuentrame3.adapters.SpaceItemDecorator;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
	private static final String LIST_SCROLL_STATE = "LIST_SCROLL_STATE";

	private Filtro filtro;
		Filtro getFiltro(){return filtro;}

	@Inject	Util util;

	private View rootView;
	private RecyclerView.LayoutManager layoutManager;
	private RecyclerView listView;		//Lista de Lugares, Rutas, Avisos
	private Parcelable scrollState = null;	//Recuerda posicion del scroll de la lista
	private MainIterface main;
	private int sectionNumber = Constantes.LUGARES;
	int getSectionNumber() { return sectionNumber; }

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
	public void onSaveInstanceState(@NonNull Bundle state) {
		super.onSaveInstanceState(state);
		if(layoutManager != null) {
			scrollState = layoutManager.onSaveInstanceState();
			state.putParcelable(LIST_SCROLL_STATE, scrollState);
		}
		else
			Log.e(TAG, "onSaveInstanceState:e:--------*********************-------------layoutManager == null");
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if(App.getComponent() != null)
			App.getComponent().inject(this);

		Bundle args = getArguments();
		if(args != null)
			sectionNumber = args.getInt(ARG_SECTION_NUMBER);

		filtro = new Filtro(sectionNumber);

		rootView = inflater.inflate(R.layout.act_main_frag, container, false);
		listView = rootView.findViewById(R.id.listView);
		layoutManager = new LinearLayoutManager(getContext());
		listView.setLayoutManager(layoutManager);
		// Decorators
		int verticalSpacing = 20;
		SpaceItemDecorator itemDecorator = new SpaceItemDecorator(verticalSpacing);
		listView.addItemDecoration(itemDecorator);
		ShadowItemDecorator shadowItemDecorator = new ShadowItemDecorator(getContext(), R.drawable.drop_shadow);
		listView.addItemDecoration(shadowItemDecorator);

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
				case Constantes.LUGARES:	main.onLugar(false);	break;
				case Constantes.RUTAS:		main.onRuta(false);	break;
				case Constantes.AVISOS:		main.onAviso(false);	break;
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
		//listView.addHeaderView(textView);//TODO

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
		scrollState = layoutManager.onSaveInstanceState();
		if(sectionNumber == Constantes.RUTAS && getContext() != null)
			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(messageReceiver);
	}


	//----------------------------------------------------------------------------------------------
	interface MainIterface
	{
		void gotoLogin();

		void onLugar(boolean isVoiceCommand);
		void onAviso(boolean isVoiceCommand);
		void onRuta(boolean isVoiceCommand);

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
	public void onAttach(@NonNull Context context)
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
					scrollState = layoutManager.onSaveInstanceState();
					layoutManager.onRestoreInstanceState(scrollState);
					listView.setAdapter(new LugarArrayAdapter(aLugares, FrgMain.this));
					listView.setContentDescription(getString(R.string.lugares));//Para Espresso
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
					scrollState = layoutManager.onSaveInstanceState();
					layoutManager.onRestoreInstanceState(scrollState);
					RutaArrayAdapter r = new RutaArrayAdapter(aRutas,FrgMain.this);
					listView.setAdapter(r);
					listView.setContentDescription(getString(R.string.rutas));//Para Espresso
					r.notifyDataSetChanged();

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
					scrollState = layoutManager.onSaveInstanceState();
					layoutManager.onRestoreInstanceState(scrollState);
					listView.setAdapter(new AvisoArrayAdapter(aAvisos, FrgMain.this));
					listView.setContentDescription(getString(R.string.avisos));//Para Espresso
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
	private void refreshLugares()
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
	private void refreshRutas()
	{
		if(filtro.isOn())
		{
			checkFechas();
			Ruta.getLista(lisRuta, filtro, util.getIdTrackingRoute());
		}
		else
			Ruta.getLista(lisRuta);
	}

	//__________________________________________________________________________________________
	private void refreshAvisos()
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
	private static final String MENSAJE = "mensaje";
	private static final String DIRTY = "dirty";

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
