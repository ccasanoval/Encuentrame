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
	private final String TAG = PreLugar.class.getSimpleName();

	private String _imgURLnew = null;

	private Util _util;
	@Inject PreLugar(Application app, Util util)
	{
		super(app);
		_util = util;
	}

	//______________________________________________________________________________________________
	@Override
	public synchronized void guardar()
	{
		if(!_bGuardar)return;
		_bGuardar = false;
		_view.iniEspera();

		if(_o.getLatitud() == 0 && _o.getLongitud() == 0)
		{
			//O escondes el teclado o el snackbar no se ve.....
			//Snackbar.make(_coordinatorLayout, getString(R.string.sin_lugar), Snackbar.LENGTH_LONG).show();
			_bGuardar = true;
			_view.toast(R.string.sin_lugar);
			_view.finEspera();
			return;
		}
		if(_view.getTextNombre().isEmpty())
		{
			_bGuardar = true;
			_view.toast(R.string.sin_nombre);
			_view.requestFocusNombre();
			_view.finEspera();
			return;
		}
		_o.setNombre(_view.getTextNombre());
		_o.setDescripcion(_view.getTextDescripcion());
		//if(_imgURLnew != null)_l.setImagen(_imgURLnew);
		((Lugar)_o).guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				Log.w(TAG, "guardar-----------------------------------------------------------------"+ _imgURLnew);
				_bGuardar = true;
				if(_imgURLnew != null)((Lugar)_o).uploadImg(_imgURLnew);
				_imgURLnew = null;
				_util.return2Main(_view.getAct(), true, _app.getString(R.string.ok_guardar_lugar));
				_view.finEspera();
			}
			@Override
			protected void onError(String err, int code)
			{
				_bGuardar = true;
				_view.finEspera();
				_view.toast(R.string.error_guardar, err);
				Log.e(TAG, "guardar:handleFault:e:--------------------------------------------------"+err);
			}
		});
	}

	//______________________________________________________________________________________________
	@Override
	protected synchronized void eliminar()
	{
		_view.iniEspera();
		((Lugar)_o).eliminar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				_bEliminar = true;
				_view.finEspera();
				_util.return2Main(_view.getAct(), true, _app.getString(R.string.ok_eliminar_lugar));
			}
			@Override
			protected void onError(String err, int code)
			{
				_bEliminar = true;
				_view.finEspera();
				_view.toast(R.string.error_eliminar, err);
				Log.e(TAG, "eliminar:handleFault:e:-------------------------------------------------"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	public void setImg(Intent data)
	{
		_imgURLnew = data.getStringExtra(ActImagen.PARAM_IMG_PATH);
		_bSucio = true;
	}
	public void imagen()
	{
		Intent i = new Intent(_view.getAct(), ActImagen.class);
		Log.e(TAG, "onActivityResult-----------------LUGAR---2---------------------- "+ _imgURLnew);
		i.putExtra(ActImagen.PARAM_IMG_PATH, _imgURLnew);
		i.putExtra(ActImagen.PARAM_LUGAR, _o);
		_view.getAct().startActivityForResult(i, ActImagen.IMAGE_CAPTURE);
	}
}
