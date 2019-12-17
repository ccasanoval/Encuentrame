package com.cesoft.encuentrame3.presenters;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.cesoft.encuentrame3.views.ActImagen;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;
import javax.inject.Singleton;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 03/05/2017.
// PRESENTER LUGAR
@Singleton
public class PreLugar extends PresenterBase
{
	private static final String TAG = PreLugar.class.getSimpleName();

	private String imgURLnew = null;

	@Inject
	public PreLugar(Application app, Util util)
	{
		super(app, util);
	}

	//______________________________________________________________________________________________
	@Override
	public synchronized void guardar()
	{
		if(!bGuardar)return;
		bGuardar = false;
		view.iniEspera();

		if(o.getLatitud() == 0 && o.getLongitud() == 0)
		{
			bGuardar = true;
			view.toast(R.string.sin_lugar);
			view.finEspera();
			return;
		}
		if(view.getTextNombre().isEmpty())
		{
			bGuardar = true;
			view.toast(R.string.sin_nombre);
			view.requestFocusNombre();
			view.finEspera();
			return;
		}
		o.setNombre(view.getTextNombre());
		o.setDescripcion(view.getTextDescripcion());

		currentCompletadoListener = new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
Log.e(TAG, "GUARDAR : *****************************************"+imgURLnew);
				bGuardar = true;
				if(imgURLnew != null)((Lugar) o).uploadImg(imgURLnew);
				imgURLnew = null;
				if(view != null) {
					Activity act = view.getAct();
					if (act != null && ! isBackPressed)
						util.return2Main(act, true, app.getString(R.string.ok_guardar_lugar));
					view.finEspera();
				}
			}
			@Override
			protected void onError(String err, int code)
			{
				bGuardar = true;
				view.finEspera();
				view.toast(R.string.error_guardar, err);
				Log.e(TAG, "guardar:handleFault:e: "+err);
			}
			@Override
			protected void onTimeout()
			{
				if( ! isWorking)return;
				Log.e(TAG, "guardar:timeout");
				bGuardar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.on_timeout);
					if(! isBackPressed)
						util.return2Main(view.getAct(), true, app.getString(R.string.on_timeout));
				}
			}
		};
		((Lugar) o).guardar(currentCompletadoListener);
	}

	//______________________________________________________________________________________________
	@Override
	protected synchronized void eliminar()
	{
		view.iniEspera();
		currentCompletadoListener = new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				isEliminar = true;
				if(view != null) {
					view.finEspera();
					if(! isBackPressed)
						util.return2Main(view.getAct(), true, app.getString(R.string.ok_eliminar_lugar));
				}
			}
			@Override
			protected void onError(String err, int code)
			{
				Log.e(TAG, "eliminar:handleFault:e: "+err);
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
				Log.e(TAG, "eliminar:timeout");
				isEliminar = true;
				if(view != null) {
					view.finEspera();
					view.toast(R.string.on_timeout);
					if(! isBackPressed)
						util.return2Main(view.getAct(), true, app.getString(R.string.on_timeout));
				}
			}
		};
		((Lugar) o).eliminar(currentCompletadoListener);
	}

	//----------------------------------------------------------------------------------------------
	public void setImg(Intent data)
	{
		imgURLnew = data.getStringExtra(ActImagen.PARAM_IMG_PATH);
		isSucio = true;
	}
	public void imagen()
	{
		Intent i = new Intent(view.getAct(), ActImagen.class);
		i.putExtra(ActImagen.PARAM_IMG_PATH, imgURLnew);
		i.putExtra(ActImagen.PARAM_LUGAR, o);
		view.getAct().startActivityForResult(i, ActImagen.IMAGE_CAPTURE);
	}
}
