package com.cesoft.encuentrame3;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Build;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.svc.CesService;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 03/05/2017.
// PRESENTER AVISO
@Singleton
class PreAviso
{
	private static final String TAG = PreAviso.class.getSimpleName();

	private ActAviso _view;
	private Aviso _a = new Aviso();

	private boolean _bDesdeNotificacion = false;
	private boolean _bSucio = false;
	private boolean _bNuevo = false;

	private Util _util;
	private CesService _servicio;
	@Inject PreAviso(Util util, CesService servicio)
	{
		_util = util;
		_servicio = servicio;
	}

	void ini(ActAviso view) { _view = view; _bSucio = false; }
	void subscribe(ActAviso view) { _view = view; }
	void unsubscribe() { _view = null; }

	//----------------------------------------------------------------------------------------------
	public String getNombre() { return _a.getNombre(); }
	public String getDescripcion() { return _a.getDescripcion(); }
	public boolean isActivo() { return _a.isActivo(); }
	double getLatitud() { return _a.getLatitud(); }
	double getLongitud() { return _a.getLongitud(); }
	double getRadio() { return _a.getRadio(); }
	boolean isNuevo() { return _bNuevo; }
	//boolean isNotificacion() { return _bDesdeNotificacion; }
	void setSucio(boolean bSucio) { _bSucio = bSucio; }
	void setLatitud(double latitud) { _a.setLatitud(latitud); }
	void setLongitud(double longitud) { _a.setLongitud(longitud); }
	public void setRadio(int radio)
	{
		_bSucio = _a.getRadio() != radio;
		_a.setRadio(radio);
	}
	public void setActivo(boolean isChecked)
	{
		_bSucio = isChecked != _a.isActivo();
	}
	void loadObject()
	{
		try
		{
			_a = _view.getIntent().getParcelableExtra(Aviso.NOMBRE);
			if(_a == null)
			{
				_bNuevo = true;
				_a = new Aviso();
				Location loc = _util.getLocation();
				if(loc != null)_view.setPosLugar(loc);
			}
		}
		catch(Exception e)
		{
			_bNuevo = true;
			_a = new Aviso();
			Location loc = _util.getLocation();
			if(loc != null)_view.setPosLugar(loc);
		}
		try
		{
			_bDesdeNotificacion = _view.getIntent().getBooleanExtra("notificacion", false);
		}
		catch(Exception e){_bDesdeNotificacion=false;}
	}
	//______________________________________________________________________________________________
	void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(_view);
			dialog.setTitle(_a.getNombre());
			dialog.setMessage(_view.getString(R.string.seguro_salir));
			dialog.setPositiveButton(_view.getString(R.string.guardar), (dialog1, which) -> guardar());
			dialog.setCancelable(true);
			dialog.setNegativeButton(_view.getString(R.string.salir), (dialog2, which) -> _view.finish());
			dialog.create().show();
		}
		else
			_view.finish();
	}
	//______________________________________________________________________________________________
	// GET BACK TO MAIN
	private void openMain(boolean bDirty, String sMensaje)
	{
		if(_bDesdeNotificacion)
			_util.openMain(_view, bDirty, sMensaje, Constantes.AVISOS);
		else
			_util.return2Main(_view, bDirty, sMensaje);
	}


	//______________________________________________________________________________________________
	private boolean _bGuardar = true;
	public synchronized void guardar()
	{
		if(!_bGuardar)return;
		_bGuardar = false;
		_view.iniEspera();

		if(_a.getLatitud()==0 && _a.getLongitud()==0)
		{
			_view.finEspera();
			_bGuardar = true;
			Toast.makeText(_view, _view.getString(R.string.sin_lugar), Toast.LENGTH_LONG).show();
			return;
		}
		if(_view.getNombre().isEmpty())
		{
			_bGuardar = true;
			_view.finEspera();
			_view.requestFocusNombre();
			Toast.makeText(_view, _view.getString(R.string.sin_nombre), Toast.LENGTH_LONG).show();
			return;
		}
		_a.setNombre(_view.getNombre());
		_a.setDescripcion(_view.getDescripcion());
		_a.setActivo(_view.isActivo());
		//_a.reactivarPorHoy();
		//_a.setLugar(new GeoPoint(_loc.getLatitude(), _loc.getLongitude()), _radio);
		_a.guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				_view.finEspera();
				_bGuardar = true;
				_servicio.cargarListaGeoAvisos();//System.err.println("ActAviso:guardar:handleResponse:" + a);
				openMain(true, _view.getString(R.string.ok_guardar_aviso));//return2Main(true, getString(R.string.ok_guardar));
			}
			@Override
			protected void onError(String err, int code)
			{
				Log.e(TAG, "guardar:handleFault:f:" + err);

				//*****************************************************************************
				try{Thread.sleep(500);}catch(InterruptedException ignored){}
				_a.guardar(new Fire.CompletadoListener()
				{
					@Override
					protected void onDatos(String id)
					{
						_view.finEspera();
						_bGuardar = true;
						_servicio.cargarListaGeoAvisos();
						openMain(true, _view.getString(R.string.ok_guardar_aviso));
					}
					@Override
					protected void onError(String err, int code)
					{
						_view.finEspera();
						_bGuardar = true;
						Log.e(TAG, "guardar:handleFault2:e:-----------------------------------------"+err);
						Toast.makeText(_view, String.format(_view.getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}

	//______________________________________________________________________________________________
	private boolean _bEliminar = true;
	public synchronized void eliminar()
	{
		if(!_bEliminar)return;
		_bEliminar=false;
		AlertDialog.Builder dialog = new AlertDialog.Builder(_view);
		dialog.setTitle(_a.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(_view.getString(R.string.seguro_eliminar));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			dialog.setOnDismissListener(dlg -> _bEliminar = true);
		dialog.setNegativeButton(_view.getString(R.string.cancelar), (dlg, which) -> _bEliminar = true);
		dialog.setPositiveButton(_view.getString(R.string.eliminar), (dlg, which) ->
		{
			_view.iniEspera();
			_a.eliminar(new Fire.CompletadoListener()
			{
				@Override
				protected void onDatos(String id)
				{
					_view.finEspera();
					_bEliminar=true;
					openMain(true, _view.getString(R.string.ok_eliminar_aviso));
				}
				@Override
				protected void onError(String err, int code)
				{
					_view.finEspera();
					_bEliminar=true;
					Log.e(TAG, "eliminar:handleFault:e:---------------------------------------------"+err);
					Toast.makeText(_view, String.format(_view.getString(R.string.error_eliminar), err), Toast.LENGTH_LONG).show();
				}
			});
		});
		dialog.create().show();
	}
}
