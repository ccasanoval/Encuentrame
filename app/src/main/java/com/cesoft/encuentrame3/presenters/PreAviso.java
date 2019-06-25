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

	private Util util;
	private LoadGeofenceJobService servicio;
	@Inject PreAviso(Application app, Util util, LoadGeofenceJobService servicio)
	{
		super(app);
		this.util = util;
		this.servicio = servicio;
	}

	@Override public void setLatLon(double lat, double lon)
	{
		o.setLatLon(lat, lon);
	}

	//----------------------------------------------------------------------------------------------
	public boolean isActivo() { return ((Aviso) o).isActivo(); }
	public double getRadio() { return ((Aviso) o).getRadio(); }
	public void setRadio(int radio)
	{
		bSucio = ((Aviso) o).getRadio() != radio;
		((Aviso) o).setRadio(radio);
	}
	public void setActivo(boolean isChecked)
	{
		bSucio = isChecked != ((Aviso) o).isActivo();
	}
	@Override
	public void loadObjeto(Objeto objDefault)
	{
		super.loadObjeto(objDefault);
		try { bDesdeNotificacion = view.getAct().getIntent().getBooleanExtra(Constantes.NOTIF, false); }
		catch(Exception e){ bDesdeNotificacion =false; }
	}

	//______________________________________________________________________________________________
	// GET BACK TO MAIN
	private void openMain(String sMensaje)
	{
		if (bDesdeNotificacion)
			util.openMain(view.getAct(), true, sMensaje, Constantes.AVISOS);
		else
			util.return2Main(view.getAct(), true, sMensaje);
	}

	//______________________________________________________________________________________________
	public synchronized void guardar()
	{
		if(o.getLatitud()==0 && o.getLongitud()==0)
		{
			view.toast(R.string.sin_lugar);
			return;
		}
		if(view.getTextNombre().isEmpty())
		{
			view.requestFocusNombre();
			view.toast(R.string.sin_nombre);
			return;
		}
		if(!bGuardar)return;
		bGuardar = false;
		view.iniEspera();

		o.setNombre(view.getTextNombre());
		o.setDescripcion(view.getTextDescripcion());
		((Aviso) o).setActivo(((IVistaAviso) view).isActivo());
		((Aviso) o).guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				bGuardar = true;
				servicio.cargarListaGeoAvisos();
				if(view != null) {
					view.finEspera();
					openMain(app.getString(R.string.ok_guardar_aviso));
				}
			}
			@Override
			protected void onError(String err, int code)
			{
				Log.e(TAG, "guardar:handleFault:e:--------------------------------------------- "+err);
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.error_guardar, err);
				}
			}
			@Override
			protected void onTimeout()
			{
				Log.e(TAG, "eliminar:timeout");
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.on_timeout);
					openMain(app.getString(R.string.on_timeout));
				}
			}
		});
	}

	//______________________________________________________________________________________________
	@Override
	public synchronized void eliminar()
	{
		view.iniEspera();
		((Aviso) o).eliminar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				bEliminar =true;
				if(view != null) view.finEspera();
				openMain(app.getString(R.string.ok_eliminar_aviso));
			}
			@Override
			protected void onError(String err, int code)
			{
				Log.e(TAG, "eliminar:handleFault:e:-------------------------------------------- "+err);
				bEliminar =true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.error_eliminar, err);
				}
			}
			@Override
			protected void onTimeout()
			{
				Log.e(TAG, "eliminar:timeout");
				bEliminar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.on_timeout);
					openMain(app.getString(R.string.on_timeout));
				}
			}
		});
	}
}
