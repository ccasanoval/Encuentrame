package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 03/05/2017.
// PRESENTER MAPS
class PreMaps
{
	private final String TAG = PreMaps.class.getSimpleName();

	////////////////////////////////////////////////////
	interface MapsView
	{
		Activity getAct();
		void finish(Intent i);
		void toast(int msg);
		void toast(int msg, String err);
		void showLugar(Lugar l);
		void showAviso(Aviso a);
		void setMarkerRadius(LatLng pos);
		void setMarker(String sTitulo, String sDescripcion, LatLng pos);
		void animateCamera();
		void showRutaHelper(Ruta r, Ruta.RutaPunto[] aPts);
	}
	private MapsView _view;
	////////////////////////////////////////////////////

	private boolean _bSucio = false;
	private Lugar _l;
	private Aviso _a;
	private Ruta _r;
	private int _iTipo = Constantes.NADA;

	private Application _app;
	@Inject PreMaps(Application app) { _app = app; }

	void ini(MapsView view)
	{
		_bSucio = false; _view = view;
	}
	void subscribe(MapsView view)
	{
		_view = view;
		newListeners();
	}
	void unsubscribe()
	{
		_view = null;
		delListeners();
		_dlgSalir.dismiss();//Para evitar MemLeak puesto que dlg guarda ref a _view
	}
	int getTipo(){return _iTipo;}

	private static final String SUCIO = "sucio";
	void loadSavedInstanceState(Bundle savedInstanceState)
	{
		if(savedInstanceState != null)_bSucio = savedInstanceState.getBoolean(SUCIO);
		//Log.e(TAG, "++++++++++++++++++++ LOAD "+_bSucio);
	}
	void onSaveInstanceState(Bundle outState)
	{
		//Log.e(TAG, "++++++++++++++++++++ SAVE "+_bSucio);
		outState.putBoolean(SUCIO, _bSucio);
	}

	//______________________________________________________________________________________________
	private AlertDialog _dlgSalir;
	void onSalir()
	{
		Log.e(TAG, "onSalir----------------------------------------------"+_bSucio);
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(_view.getAct());
			dialog.setTitle( _l != null ? _l.getNombre() : _a.getNombre());
			dialog.setMessage(_app.getString(R.string.seguro_salir));
			dialog.setPositiveButton(_app.getString(R.string.guardar), (dialog1, which) -> guardar());
			dialog.setCancelable(true);
			dialog.setNegativeButton(_app.getString(R.string.salir), (dialog2, which) -> _view.finish(null));
			_dlgSalir = dialog.create();
			_dlgSalir.show();
		}
		else
			_view.finish(null);
	}

	//______________________________________________________________________________________________
	void loadObjeto()
	{
		//------------------------------------------------------------------------------------------
		try{_l = _view.getAct().getIntent().getParcelableExtra(Lugar.NOMBRE);}catch(Exception e){_l=null;}
		try{_r = _view.getAct().getIntent().getParcelableExtra(Ruta.NOMBRE);}catch(Exception e){_r=null;}
		try{_a = _view.getAct().getIntent().getParcelableExtra(Aviso.NOMBRE);}catch(Exception e){_a=null;}
		try{_iTipo = _view.getAct().getIntent().getIntExtra(Util.TIPO, Constantes.NADA);}catch(Exception e){_iTipo=Constantes.NADA;}
		//------------------------------------------------------------------------------------------
	}
	boolean isRuta(){return _iTipo != Constantes.NADA || _r != null;}


	void guardar()
	{
		if(_l != null)
		{
			_l.setLatitud(_loc.getLatitude());
			_l.setLongitud(_loc.getLongitude());
			_l.guardar(new Fire.CompletadoListener()
			{
				@Override
				protected void onDatos(String id)
				{
					_view.toast(R.string.ok_guardar_lugar);
					Intent data = new Intent();
					data.putExtra(Lugar.NOMBRE, _l);
					//_view.getAct().setResult(Activity.RESULT_OK, data);//finish(data)..
					_view.finish(data);
				}
				@Override
				protected void onError(String err, int code)
				{
					Log.e(TAG, "guardar:handleFault:e:----------------------------------------------"+err);
					_view.toast(R.string.error_guardar, err);
				}
			});
		}
		if(_a != null)
		{
			_a.setLatitud(_loc.getLatitude());
			_a.setLongitud(_loc.getLongitude());
			_a.guardar(new Fire.CompletadoListener()
			{
				@Override
				protected void onDatos(String id)
				{
					_view.toast(R.string.ok_guardar_aviso);
					Intent data = new Intent();
					data.putExtra(Aviso.NOMBRE, _a);
					//_view.getAct().setResult(Activity.RESULT_OK, data);
					_view.finish(data);
				}
				@Override
				protected void onError(String err, int code)
				{
					Log.e(TAG, "guardar:handleFault:e:----------------------------------------------"+err);
					_view.toast(R.string.error_guardar, err);
				}
			});
		}
	}


	//______________________________________________________________________________________________
	private Fire.DatosListener<Lugar> _lisLugar;
	private Fire.DatosListener<Aviso> _lisAviso;
	private Fire.DatosListener<Ruta> _lisRuta;
	//----------------------------------------------------------------------------------------------
	private void delListeners()
	{
		if(_lisLugar!=null)_lisLugar.setListener(null);
		if(_lisAviso!=null)_lisAviso.setListener(null);
		if(_lisRuta!=null)_lisRuta.setListener(null);
	}
	private void newListeners()
	{
		delListeners();
		_lisLugar = new Fire.DatosListener<Lugar>()
		{
			@Override public void onDatos(Lugar[] aData)
			{
				for(Lugar o : aData)_view.showLugar(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showLugares:e:--------------------------------------------LUGARES:GET:ERROR:%s",err));
			}
		};
		_lisAviso = new Fire.DatosListener<Aviso>()
		{
			@Override public void onDatos(Aviso[] aData)
			{
				for(Aviso o : aData)_view.showAviso(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showAvisos:e:---------------------------------------------AVISOS:GET:ERROR:%s",err));
			}
		};
		_lisRuta = new Fire.DatosListener<Ruta>()
		{
			@Override public void onDatos(Ruta[] aData)
			{
				Log.e(TAG, "------------------------------------------------------------------------getLista : "+aData.length);
				for(Ruta o : aData)
					showRuta(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showRutas:e:----------------------------------------------RUTAS:GET:ERROR:%s",err));
			}
		};
	}

	//----------------------------------------------------------------------------------------------
	void showLugares()
	{
		Lugar.getLista(_lisLugar);
	}
	//---
	void showAvisos()
	{
		Aviso.getLista(_lisAviso);
	}
	//---
	void showRutas()
	{
		Ruta.getLista(_lisRuta);
	}

	//----------------------------------------------------------------------------------------------
	private void showRuta()
	{
		showRuta(_r);
	}
	private synchronized void showRuta(Ruta r)
	{
		if(r == null)
		{
			Log.e(TAG, "showRuta:e:----------------------------------------------------------------- r == NULL");
			return;
		}
		Ruta.RutaPunto.getLista(r.getId(), new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
//if(r.getPuntosCount() != aData.length)Log.e(TAG, "showRuta:--------------------------------------------------------"+r.getPuntosCount()+"---<>--- "+aData.length);
				_view.showRutaHelper(r, aData);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, "showRuta:e:-------------------------------------------------------------"+err);
			}
		});
	}

	double getRadioAviso(){return _a.getRadio();}

	private Location _loc;
	void setPosLugar(double lat, double lon)
	{
		if(_loc == null)_loc = new Location("dummyprovider");
		_loc.setLatitude(lat);
		_loc.setLongitude(lon);
		if(_l != null)
		{
			if(_l.getLatitud() != _loc.getLatitude() || _l.getLongitud() != _loc.getLongitude())_bSucio = true;
			//Log.e(TAG, "setPosLugar Lugar != NULL----------------------------------------------"+_bSucio);
			_view.setMarker(_l.getNombre(), _l.getDescripcion(), new LatLng(_loc.getLatitude(), _loc.getLongitude()));
		}
		else if(_a != null)
		{
			if(_a.getLatitud() != _loc.getLatitude() || _a.getLongitud() != _loc.getLongitude())_bSucio = true;
			//Log.e(TAG, "setPosLugar Aviso != NULL----------------------------------------------"+_bSucio);
			LatLng pos = new LatLng(_loc.getLatitude(), _loc.getLongitude());
			_view.setMarker(_a.getNombre(), _a.getDescripcion(), pos);
			_view.setMarkerRadius(pos);
		}
		else if(_r != null)
		{
			_view.animateCamera();
			//_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		}
	}
	boolean onMapReady()
	{
		if(_l != null)			/// LUGAR
		{
			setPosLugar(_l.getLatitud(), _l.getLongitud());
			_view.showLugar(_l);
			return false;
		}
		else if(_a != null)		/// AVISO
		{
			setPosLugar(_a.getLatitud(), _a.getLongitud());
			_view.showAviso(_a);
			return false;
		}
		else if(_r != null)		/// RUTA
		{
			showRuta();
			return false;
		}
		else
			return true;
	}
}
