package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.os.Build;

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
class PreLugar
{
	private final String TAG = PreLugar.class.getSimpleName();

	////////////////////////////////////////////////////
	interface LugarView
	{
		Activity getAct();
		void finish();
		void iniEspera();
		void finEspera();
		void toast(int msg);
		void toast(int msg, String err);
		String getTextNombre();
		String getTextDescripcion();
		void requestFocusNombre();
	}
	private LugarView _view;
	////////////////////////////////////////////////////

	private String _imgURLnew =null;
	private boolean _bSucio = false;
	private boolean _bNuevo = false;
	private Lugar _l = new Lugar();

	private Application _app;
	private Util _util;
	@Inject PreLugar(Application app, Util util)
	{
		_app = app;
		_util = util;
	}

	void ini(LugarView view) { _bSucio = false; _view = view; }
	void subscribe(LugarView view) { _view = view; }
	void unsubscribe() { _view = null; }

	String getNombre(){return _l.getNombre();}
	String getDescripcion(){return _l.getDescripcion();}
	double getLatitud(){return _l.getLatitud();}
	double getLongitud(){return _l.getLongitud();}
	boolean isNuevo(){return _bNuevo;}
	//boolean isSucio(){return _bSucio;}
	//
	void setSucio(){_bSucio=true;}
	void setLatitud(double v){_l.setLatitud(v);}
	void setLongitud(double v){_l.setLongitud(v);}

	//______________________________________________________________________________________________
	void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(_view.getAct());
			dialog.setTitle(_l.getNombre());
			dialog.setMessage(_app.getString(R.string.seguro_salir));
			dialog.setPositiveButton(_app.getString(R.string.guardar), (dialog1, which) -> guardar());
			dialog.setCancelable(true);
			dialog.setNegativeButton(_app.getString(R.string.salir), (dialog2, which) -> _view.finish());
			dialog.create().show();
		}
		else
			_view.finish();
	}
	//______________________________________________________________________________________________
	void loadObjeto()
	{
		try
		{
			_l = _view.getAct().getIntent().getParcelableExtra(Lugar.NOMBRE);
			if(_l == null)
			{
				_bNuevo = true;
				_l = new Lugar();
			}
		}
		catch(Exception e)
		{
			_bNuevo = true;
			_l = new Lugar();
		}
	}

	//______________________________________________________________________________________________
	private boolean _bGuardar = true;
	synchronized void guardar()
	{
		if(!_bGuardar)return;
		_bGuardar = false;
		_view.iniEspera();

		if(_l.getLatitud() == 0 && _l.getLongitud() == 0)
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
		_l.setNombre(_view.getTextNombre());
		_l.setDescripcion(_view.getTextDescripcion());
		//if(_imgURLnew != null)_l.setImagen(_imgURLnew);
		_l.guardar(new Fire.CompletadoListener() {
			@Override
			protected void onDatos(String id)
			{
				_bGuardar = true;
				if(_imgURLnew != null)_l.uploadImg(_imgURLnew);
				_util.return2Main(_view.getAct(), true, _app.getString(R.string.ok_guardar_lugar));
				_view.finEspera();
				Log.e(TAG, "guardar--------------------------------"+ _imgURLnew);
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
	private boolean _bEliminar = true;
	synchronized void eliminar()
	{
		if(!_bEliminar)return;
		_bEliminar=false;
		AlertDialog.Builder dialog = new AlertDialog.Builder(_view.getAct());
		dialog.setTitle(_l.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(_app.getString(R.string.seguro_eliminar));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			dialog.setOnDismissListener(dlg -> _bEliminar = true);
		}
		dialog.setNegativeButton(_app.getString(R.string.cancelar), (dialog13, which) -> _bEliminar = true);
		dialog.setPositiveButton(_app.getString(R.string.eliminar), (dialog12, which) ->
		{
			_view.iniEspera();
			_l.eliminar(new Fire.CompletadoListener()
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
					Log.e(TAG, "eliminar:handleFault:e:---------------------------------------------"+err);
				}
			});
		});
		dialog.create().show();
	}


	void setImg(Intent data)
	{
		_imgURLnew = data.getStringExtra(ActImagen.PARAM_IMG_PATH);
		_bSucio = true;
	}
	void imagen()
	{
		Intent i = new Intent(_view.getAct(), ActImagen.class);
		Log.e(TAG, "onActivityResult-----------------LUGAR---2---------------------- "+ _imgURLnew);
		i.putExtra(ActImagen.PARAM_IMG_PATH, _imgURLnew);
		i.putExtra(ActImagen.PARAM_LUGAR, _l);
		_view.getAct().startActivityForResult(i, ActImagen.IMAGE_CAPTURE);
	}
}
