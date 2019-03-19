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
	IVista view;
	////////////////////////////////////////////////////
	Objeto o;
		public String getNombre(){return o.getNombre();}
		public String getDescripcion(){return o.getDescripcion();}
		//
		public double getLatitud(){return o.getLatitud();}
		public double getLongitud(){return o.getLongitud();}
		public void setLatLon(double lat, double lon){
			o.setLatLon(lat, lon);}

	boolean bSucio = false;
		public void setSucio(){
			bSucio =true;}
	private boolean bNuevo = false;
		public boolean isNuevo(){return bNuevo;}
	boolean bDesdeNotificacion = false;
	////////////////////////////////////////////////////
	protected Application app;
	PresenterBase(Application app) { this.app = app; }

	//----------------------------------------------------------------------------------------------
	public void ini(IVista view)
	{
		this.view = view;
		bSucio = false;
	}
	public void subscribe(IVista view)
	{
		this.view = view;
		if( !bEliminar)
		{
			bEliminar =true;
			onEliminar();
		}
	}
	public void unsubscribe()
	{
		Log.e(TAG, "-------------------------------unsubscribe------1--------------------------------");
		//Como dlg tienen referencia a view, debemos destruir referencia para evitar MemoryLeak!
		if(dlgEliminar != null)
		{
			boolean b = bEliminar;
			dlgEliminar.dismiss();
			dlgEliminar = null;
			bEliminar = b;//Para recordar si estabamos mostrarndo dlg, porque dismiss borra flag
		}
		//
		if(dlgSucio != null) dlgSucio.dismiss();
		dlgSucio = null;
		//
		view = null;
		Log.e(TAG, "-------------------------------unsubscribe------2--------------------------------");
	}

	private static final String SUCIO = "sucio";
	private static final String ELIMINAR = "eliminar";
	public void loadSavedInstanceState(Bundle savedInstanceState)
	{
		if(savedInstanceState != null)
		{
			bSucio = savedInstanceState.getBoolean(SUCIO);
			bEliminar = savedInstanceState.getBoolean(ELIMINAR);
		}
	}
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean(SUCIO, bSucio);
		outState.putBoolean(ELIMINAR, bEliminar);
	}

	//______________________________________________________________________________________________
	public void loadObjeto(Objeto objDefault)
	{
		try
		{
			o = view.getAct().getIntent().getParcelableExtra(Objeto.NOMBRE);
			if(o == null)throw new Exception();
            bNuevo = false;
		}
		catch(Exception e)
		{
			bNuevo = true;
			o = objDefault;
		}
	}

	//______________________________________________________________________________________________
	private AlertDialog dlgSucio = null;
	public void onSalir()
	{
		if(bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(view.getAct());
			dialog.setPositiveButton(app.getString(R.string.guardar), (dlg, which) -> guardar());
			dialog.setNegativeButton(app.getString(R.string.salir), (dlg, which) -> view.finish());
			dlgSucio = dialog.create();
			dlgSucio.setCancelable(true);
			dlgSucio.setTitle(o.getNombre());
			dlgSucio.setMessage(app.getString(R.string.seguro_salir));
			dlgSucio.show();
		}
		else
			view.finish();
	}
	boolean bGuardar = true;
	protected abstract void guardar();

	//______________________________________________________________________________________________
	private AlertDialog dlgEliminar = null;
	boolean bEliminar = true;
	public void onEliminar()
	{
		if(!bEliminar)return;
		bEliminar =false;

		AlertDialog.Builder dialog = new AlertDialog.Builder(view.getAct());
		dialog.setNegativeButton(app.getString(R.string.cancelar), (dlg, which) -> bEliminar = true);
		dialog.setPositiveButton(app.getString(R.string.eliminar), (dialog1, which) -> eliminar());
		dlgEliminar = dialog.create();
		dlgEliminar.setTitle(o.getNombre());
		dlgEliminar.setMessage(app.getString(R.string.seguro_eliminar));
		dlgEliminar.setOnDismissListener(dlg -> bEliminar = dlgEliminar ==null || !dlgEliminar.isShowing());//bEliminar=true no funcionaria, se llama con retraso
		dlgEliminar.show();
	}
	protected abstract void eliminar();

	private static class CesTextWatcher implements TextWatcher
	{
		private TextView tv;
		private String str;
		private PresenterBase presenter;
		CesTextWatcher(TextView tv, String str, PresenterBase presenter){
			this.tv = tv; this.str = str; this.presenter = presenter;}
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}
		@Override
		public void afterTextChanged(Editable editable)
		{
			if(str == null && tv.getText().length() > 0) presenter.setSucio();
			if(str != null && tv.getText().toString().compareTo(str) != 0) presenter.setSucio();
		}
	}
	public void setOnTextChange(EditText nom, EditText desc)
	{
		nom.addTextChangedListener(new PresenterBase.CesTextWatcher(nom, getNombre(), this));
		desc.addTextChangedListener(new PresenterBase.CesTextWatcher(desc,  getDescripcion(), this));
	}
}
