package com.cesoft.encuentrame3.presenters;

import android.app.Application;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.svc.GeofenceStore;
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

	private final GeofenceStore geofenceStoreAvisos;
	@Inject PreAviso(Application app, Util util, GeofenceStore geofenceStoreAvisos)
	{
		super(app, util);
		this.geofenceStoreAvisos = geofenceStoreAvisos;
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
		boolean changed = ((Aviso)o).setRadio(radio);
		isSucio = isSucio || changed;
	}
	public void setActivo(boolean isChecked)
	{
		boolean changed = isChecked != ((Aviso)o).isActivo();
		isSucio = isSucio || changed;
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
		currentCompletadoListener = new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				bGuardar = true;
				geofenceStoreAvisos.cargarListaGeoAvisos();
				if(view != null) {
					view.finEspera();
					if( ! isBackPressed)
						openMain(app.getString(R.string.ok_guardar_aviso));
				}
			}
			@Override
			protected void onError(String err, int code)
			{
				Log.e(TAG, "guardar:onError:e: ------------------------------------------------"+err);
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.error_guardar, err);
				}
			}
			@Override
			protected void onTimeout()
			{
				if( ! isWorking)return;
				Log.e(TAG, "guardar:onTimeout--------------------------------------------------");
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.on_timeout);
					if( ! isBackPressed)
						openMain(app.getString(R.string.on_timeout));
				}
			}
		};
		((Aviso) o).guardar(currentCompletadoListener);
	}

	//______________________________________________________________________________________________
	@Override
	public synchronized void eliminar()
	{
		view.iniEspera();
		currentCompletadoListener = new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				isEliminar = true;
				if(view != null) view.finEspera();
				if( ! isBackPressed)
					openMain(app.getString(R.string.ok_eliminar_aviso));
			}
			@Override
			protected void onError(String err, int code)
			{
				Log.e(TAG, "eliminar:handleFault:e: -------------------------------------------"+err);
				isEliminar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.error_eliminar, err);
				}
			}
			@Override
			protected void onTimeout()
			{
				if( ! isWorking)return;
				Log.e(TAG, "eliminar:timeout---------------------------------------------------");
				isEliminar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.on_timeout);
					if( ! isBackPressed)
						openMain(app.getString(R.string.on_timeout));
				}
			}
		};
		((Aviso) o).eliminar(currentCompletadoListener);
	}
}
