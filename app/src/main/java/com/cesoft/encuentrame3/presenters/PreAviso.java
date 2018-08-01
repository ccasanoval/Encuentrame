package com.cesoft.encuentrame3.presenters;

import android.app.Application;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.svc.LoadGeofenceJobService;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 03/05/2017.
// PRESENTER AVISO
@Singleton
public class PreAviso extends PresenterBase
{
	private static final String TAG = PreAviso.class.getSimpleName();

	////////////////////////////////////////////////////
	public interface IVistaAviso extends IVista
	{
		boolean isActivo();
	}
	/*@SuppressWarnings("WeakerAccess")
	protected IVistaAviso _view;
	@Override void ini(IVista view)
	{
		super.ini(view);
		_view = (IVistaAviso)view;
	}*/

	private Util _util;
	//private CesService _servicio;
	private LoadGeofenceJobService _servicio;
	@Inject PreAviso(Application app, Util util, LoadGeofenceJobService servicio)
	{
		super(app);
		_util = util;
		_servicio = servicio;
	}

	@Override public void setLatLon(double lat, double lon)
	{
		_o.setLatLon(lat, lon);
	}

	//----------------------------------------------------------------------------------------------
	public boolean isActivo() { return ((Aviso)_o).isActivo(); }
	public double getRadio() { return ((Aviso)_o).getRadio(); }
	public void setRadio(int radio)
	{
		_bSucio = ((Aviso)_o).getRadio() != radio;
		((Aviso)_o).setRadio(radio);
	}
	public void setActivo(boolean isChecked)
	{
		_bSucio = isChecked != ((Aviso)_o).isActivo();
	}
	@Override
	public void loadObjeto(Objeto objDefault)
	{
		super.loadObjeto(objDefault);
		try { _bDesdeNotificacion = _view.getAct().getIntent().getBooleanExtra(Constantes.NOTIF, false); }
		catch(Exception e){ _bDesdeNotificacion=false; }
	}

	//______________________________________________________________________________________________
	// GET BACK TO MAIN
	private void openMain(String sMensaje)
	{
		if(_bDesdeNotificacion)
			_util.openMain(_view.getAct(), true, sMensaje, Constantes.AVISOS);
		else
			_util.return2Main(_view.getAct(), true, sMensaje);
	}

	//______________________________________________________________________________________________
	private boolean _bGuardar = true;
	public synchronized void guardar()
	{
		//Log.e(TAG, "+++++++++++++++++++++ "+_view+" ++++++++++++++++ "+super._view);

		if(_o.getLatitud()==0 && _o.getLongitud()==0)
		{
			_view.toast(R.string.sin_lugar);
			return;
		}
		if(_view.getTextNombre().isEmpty())
		{
			_view.requestFocusNombre();
			_view.toast(R.string.sin_nombre);
			return;
		}
		if(!_bGuardar)return;
		_bGuardar = false;
		_view.iniEspera();

		_o.setNombre(_view.getTextNombre());
		_o.setDescripcion(_view.getTextDescripcion());
		((Aviso)_o).setActivo(((IVistaAviso)_view).isActivo());
		//_a.reactivarPorHoy();
		//_a.setLugar(new GeoPoint(_loc.getLatitude(), _loc.getLongitude()), _radio);
		((Aviso)_o).guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				_view.finEspera();
				_bGuardar = true;
				_servicio.cargarListaGeoAvisos();//System.err.println("ActAviso:guardar:handleResponse:" + a);
				openMain(_app.getString(R.string.ok_guardar_aviso));//return2Main(true, getString(R.string.ok_guardar));
			}
			@Override
			protected void onError(String err, int code)
			{
				_view.finEspera();
				_bGuardar = true;
				Log.e(TAG, "guardar:handleFault:e:--------------------------------------------------"+err);
				_view.toast(R.string.error_guardar, err);
			}
		});
	}

	//______________________________________________________________________________________________
	@Override
	public synchronized void eliminar()
	{
		_view.iniEspera();
		((Aviso)_o).eliminar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				_view.finEspera();
				_bEliminar=true;
				openMain(_app.getString(R.string.ok_eliminar_aviso));
			}
			@Override
			protected void onError(String err, int code)
			{
				_view.finEspera();
				_bEliminar=true;
				Log.e(TAG, "eliminar:handleFault:e:-------------------------------------------------"+err);
				_view.toast(R.string.error_eliminar, err);
			}
		});
	}
}
