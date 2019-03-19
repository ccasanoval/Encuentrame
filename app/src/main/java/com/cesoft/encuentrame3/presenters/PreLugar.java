package com.cesoft.encuentrame3.presenters;

import android.app.Application;
import android.content.Intent;

import com.cesoft.encuentrame3.ActImagen;
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

	private Util util;
	public @Inject PreLugar(Application app, Util util)
	{
		super(app);
		this.util = util;
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
		((Lugar) o).guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				Log.w(TAG, "guardar-----------------------------------------------------------------"+ imgURLnew);
				bGuardar = true;
				if(imgURLnew != null)((Lugar) o).uploadImg(imgURLnew);
				imgURLnew = null;
				util.return2Main(view.getAct(), true, app.getString(R.string.ok_guardar_lugar));
				view.finEspera();
			}
			@Override
			protected void onError(String err, int code)
			{
				bGuardar = true;
				view.finEspera();
				view.toast(R.string.error_guardar, err);
				Log.e(TAG, "guardar:handleFault:e:--------------------------------------------------"+err);
			}
		});
	}

	//______________________________________________________________________________________________
	@Override
	protected synchronized void eliminar()
	{
		view.iniEspera();
		((Lugar) o).eliminar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				bEliminar = true;
				view.finEspera();
				util.return2Main(view.getAct(), true, app.getString(R.string.ok_eliminar_lugar));
			}
			@Override
			protected void onError(String err, int code)
			{
				bEliminar = true;
				view.finEspera();
				view.toast(R.string.error_eliminar, err);
				Log.e(TAG, "eliminar:handleFault:e:-------------------------------------------------"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	public void setImg(Intent data)
	{
		imgURLnew = data.getStringExtra(ActImagen.PARAM_IMG_PATH);
		bSucio = true;
	}
	public void imagen()
	{
		Intent i = new Intent(view.getAct(), ActImagen.class);
		Log.e(TAG, "onActivityResult-----------------LUGAR---2---------------------- "+ imgURLnew);
		i.putExtra(ActImagen.PARAM_IMG_PATH, imgURLnew);
		i.putExtra(ActImagen.PARAM_LUGAR, o);
		view.getAct().startActivityForResult(i, ActImagen.IMAGE_CAPTURE);
	}
}
