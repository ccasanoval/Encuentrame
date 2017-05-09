package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.os.Build;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Objeto;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by booster-bikes on 09/05/2017.
//
public class PresenterBase
{
	////////////////////////////////////////////////////
	public interface Vista
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
	protected Vista _view;
	////////////////////////////////////////////////////
	protected Objeto _o;
	private boolean _bSucio = false;
	private boolean _bNuevo = false;
	////////////////////////////////////////////////////
	protected Application _app;
	public PresenterBase(Application app)
	{
		_app = app;
	}

	//----------------------------------------------------------------------------------------------
	void ini(Vista view)
	{
		_view = view;
		_bSucio = false;
	}
	void subscribe(Vista view)
	{
		_view = view;
		//newListeners();
		_bEliminar=false;
	}
	void unsubscribe()
	{
		_view = null;
		//delListeners();
		//Como dlg tienen referencia a _view, debemos destruir referencia para evitar MemoryLeak!
		if(_dlgEliminar != null)_dlgEliminar.dismiss();
		if(_dlgSucio != null)_dlgSucio.dismiss();
	}

	//______________________________________________________________________________________________
	private boolean checkCampos()
	{
		if(_view.getTextDescripcion().isEmpty())
		{
			Toast.makeText(_view.getAct(), _app.getString(R.string.sin_nombre), Toast.LENGTH_LONG).show();
			_view.requestFocusNombre();
			return false;
		}
		return true;
	}

	//______________________________________________________________________________________________
	private AlertDialog _dlgSucio = null;
	void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(_view.getAct());
			dialog.setTitle(_o.getNombre());
			dialog.setMessage(_app.getString(R.string.seguro_salir));
			dialog.setPositiveButton(_app.getString(R.string.guardar), (dlg, which) -> guardar());
			dialog.setNegativeButton(_app.getString(R.string.salir), (dlg, which) -> _view.finish());
			dialog.setCancelable(true);
			_dlgSucio = dialog.create();
			_dlgSucio.show();
		}
		else
			_view.finish();
	}
	protected void guardar(){}

	//______________________________________________________________________________________________
	protected boolean _bEliminar = true;
	protected AlertDialog _dlgEliminar = null;
	public void onEliminar()
	{
		if(!_bEliminar)return;
		_bEliminar=false;

		AlertDialog.Builder dialog = new AlertDialog.Builder(_view.getAct());
		dialog.setTitle(_o.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(_app.getString(R.string.seguro_eliminar));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			dialog.setOnDismissListener(dlg -> _bEliminar = true);
		}
		dialog.setNegativeButton(_app.getString(R.string.cancelar), (dlg, which) -> _bEliminar = true);
		dialog.setPositiveButton(_app.getString(R.string.eliminar), (dialog1, which) -> eliminar());
		_dlgEliminar = dialog.create();
		_dlgEliminar.show();
	}
	protected void eliminar()
	{
		_bEliminar = true;
	}

	//----------------------------------------------------------------------------------------------
	protected ProgressDialog _progressDialog;
	public void iniEspera()
	{
		_progressDialog = ProgressDialog.show(_view.getAct(), "", _app.getString(R.string.cargando), true, true);
	}
	public void finEspera()
	{
		if(_progressDialog!=null)_progressDialog.dismiss();
	}
}
