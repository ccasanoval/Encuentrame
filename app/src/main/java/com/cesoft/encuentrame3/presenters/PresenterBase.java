package com.cesoft.encuentrame3.presenters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.util.Log;
import com.google.android.gms.maps.GoogleMap;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 09/05/2017.
//
public abstract class PresenterBase
{
	private static final String TAG = PresenterBase.class.getSimpleName();

	////////////////////////////////////////////////////
	public interface IVista
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
		GoogleMap getMap();
	}
	IVista _view;
	////////////////////////////////////////////////////
	Objeto _o;
		public String getNombre(){return _o.getNombre();}
		public String getDescripcion(){return _o.getDescripcion();}
		//
		public double getLatitud(){return _o.getLatitud();}
		public double getLongitud(){return _o.getLongitud();}
		public void setLatLon(double lat, double lon){_o.setLatLon(lat, lon);}
		//public void setLatitud(double v){_o.setLatitud(v);}
		//public void setLongitud(double v){_o.setLongitud(v);}

	boolean _bSucio = false;
		public void setSucio(){_bSucio=true;}
	private boolean _bNuevo = false;
		public boolean isNuevo(){return _bNuevo;}
	boolean _bDesdeNotificacion = false;
	////////////////////////////////////////////////////
	Application _app;
	PresenterBase(Application app) { _app = app; }

	//----------------------------------------------------------------------------------------------
	public void ini(IVista view)
	{
		_view = view;
		_bSucio = false;
	}
	public void subscribe(IVista view)
	{
		_view = view;
		if( ! _bEliminar)
		{
			_bEliminar=true;
			onEliminar();
		}
	}
	public void unsubscribe()
	{
		Log.e(TAG, "-------------------------------unsubscribe------1--------------------------------");
		//Como dlg tienen referencia a _view, debemos destruir referencia para evitar MemoryLeak!
		if(_dlgEliminar != null)
		{
			boolean b = _bEliminar;
			_dlgEliminar.dismiss();
			_dlgEliminar = null;
			_bEliminar = b;//Para recordar si estabamos mostrarndo dlg, porque dismiss borra flag
		}
		//
		if(_dlgSucio != null)_dlgSucio.dismiss();
		_dlgSucio = null;
		//
		_view = null;
		Log.e(TAG, "-------------------------------unsubscribe------2--------------------------------");
	}

	private static final String SUCIO = "sucio";
	private static final String ELIMINAR = "eliminar";
	public void loadSavedInstanceState(Bundle savedInstanceState)
	{
		if(savedInstanceState != null)
		{
			_bSucio = savedInstanceState.getBoolean(SUCIO);
			_bEliminar = savedInstanceState.getBoolean(ELIMINAR);
		}
	}
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean(SUCIO, _bSucio);
		outState.putBoolean(ELIMINAR, _bEliminar);
	}

	//______________________________________________________________________________________________
	public void loadObjeto(Objeto objDefault)
	{
		try
		{
			_o = _view.getAct().getIntent().getParcelableExtra(Objeto.NOMBRE);
			if(_o == null)throw new Exception();
            _bNuevo = false;
			Log.e(TAG, "loadObjeto:-----------------------------------------------------------"+_o);
		}
		catch(Exception e)
		{
			Log.e(TAG, "loadObjeto:e:---------------------------------------------------------",e);
			_bNuevo = true;
			_o = objDefault;
		}
	}

	//______________________________________________________________________________________________
	private AlertDialog _dlgSucio = null;
	public void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(_view.getAct());
			dialog.setPositiveButton(_app.getString(R.string.guardar), (dlg, which) -> guardar());
			dialog.setNegativeButton(_app.getString(R.string.salir), (dlg, which) -> _view.finish());
			_dlgSucio = dialog.create();
			_dlgSucio.setCancelable(true);
			_dlgSucio.setTitle(_o.getNombre());
			_dlgSucio.setMessage(_app.getString(R.string.seguro_salir));
			_dlgSucio.show();
		}
		else
			_view.finish();
	}
	boolean _bGuardar = true;
	protected abstract void guardar();

	//______________________________________________________________________________________________
	private AlertDialog _dlgEliminar = null;
	boolean _bEliminar = true;
	public void onEliminar()
	{
		if(!_bEliminar)return;
		_bEliminar=false;

		AlertDialog.Builder dialog = new AlertDialog.Builder(_view.getAct());
		//dialog.setTitle(_o.getNombre());//getString(R.string.eliminar));
		//dialog.setMessage(_app.getString(R.string.seguro_eliminar));
		//if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)dialog.setOnDismissListener(dlg -> _bEliminar = true);
		dialog.setNegativeButton(_app.getString(R.string.cancelar), (dlg, which) -> _bEliminar = true);
		dialog.setPositiveButton(_app.getString(R.string.eliminar), (dialog1, which) -> eliminar());
		_dlgEliminar = dialog.create();
		_dlgEliminar.setTitle(_o.getNombre());//getString(R.string.eliminar));
		_dlgEliminar.setMessage(_app.getString(R.string.seguro_eliminar));
		_dlgEliminar.setOnDismissListener(dlg -> _bEliminar = _dlgEliminar==null || !_dlgEliminar.isShowing());//_bEliminar=true no funcionaria, se llama con retraso
		_dlgEliminar.show();
	}
	protected abstract void eliminar();

	// TODO : to vistaBase ?
	private static class CesTextWatcher implements TextWatcher
	{
		private TextView _tv;
		private String _str;
		private PresenterBase _presenter;
		CesTextWatcher(TextView tv, String str, PresenterBase presenter){_tv = tv; _str = str; _presenter = presenter;}
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}
		@Override
		public void afterTextChanged(Editable editable)
		{
			if(_str == null && _tv.getText().length() > 0)_presenter.setSucio();
			if(_str != null && _tv.getText().toString().compareTo(_str) != 0)_presenter.setSucio();
		}
	}
	public void setOnTextChange(EditText nom, EditText desc)
	{
		nom.addTextChangedListener(new PresenterBase.CesTextWatcher(nom, getNombre(), this));
		desc.addTextChangedListener(new PresenterBase.CesTextWatcher(desc,  getDescripcion(), this));
	}
}
