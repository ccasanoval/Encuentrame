package com.cesoft.encuentrame3.presenters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.maps.GoogleMap;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 09/05/2017.
// TODO: Use Android Components
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
	protected Objeto o;
		public String getNombre(){return o.getNombre();}
		public String getDescripcion(){return o.getDescripcion();}
		//
		public double getLatitud(){return o.getLatitud();}
		public double getLongitud(){return o.getLongitud();}
		public void setLatLon(double lat, double lon){
			o.setLatLon(lat, lon);}

	boolean onBackPressed = false;
		public void onBackPressed(){ onBackPressed=true; }
	boolean isSucio = false;
		public void setSucio(){ isSucio=true; }
	private boolean isNuevo = false;
		public boolean isNuevo(){return isNuevo;}
	private boolean isVoiceCommand = false;
		public boolean isVoiceCommand(){return isVoiceCommand;}
	boolean bDesdeNotificacion = false;
	////////////////////////////////////////////////////
	protected final Application app;
	PresenterBase(Application app) { this.app = app; }

	//----------------------------------------------------------------------------------------------
	public void ini(IVista view)
	{
		this.view = view;
		isSucio = false;
	}
	public void subscribe(IVista view)
	{
		this.view = view;
		if( !isEliminar)
		{
			isEliminar =true;
			onEliminar();
		}
	}
	public void unsubscribe()
	{
		//Como dlg tienen referencia a view, debemos destruir referencia para evitar MemoryLeak!
		if(dlgEliminar != null)
		{
			boolean b = isEliminar;
			dlgEliminar.dismiss();
			dlgEliminar = null;
			isEliminar = b;//Para recordar si estabamos mostrarndo dlg, porque dismiss borra flag //TODO: Cutre
		}
		//
		if(dlgSucio != null) dlgSucio.dismiss();
		dlgSucio = null;
		//
		view = null;
	}

	private static final String SUCIO = "sucio";
	private static final String ELIMINAR = "eliminar";
	public void loadSavedInstanceState(Bundle savedInstanceState)
	{
		if(savedInstanceState != null)
		{
			isSucio = savedInstanceState.getBoolean(SUCIO);
			isEliminar = savedInstanceState.getBoolean(ELIMINAR);
		}
	}
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean(SUCIO, isSucio);
		outState.putBoolean(ELIMINAR, isEliminar);
	}

	//______________________________________________________________________________________________
	public void loadObjeto(Objeto objDefault)
	{

		Intent intent = view.getAct().getIntent();
		o = intent.getParcelableExtra(Objeto.NOMBRE);
		if(o == null) {
			isNuevo = true;
			o = objDefault;
			isVoiceCommand = intent.getBooleanExtra(Voice.NAME, false);
		}
		else {
			isNuevo = false;
			isVoiceCommand = false;
		}
	}

	//______________________________________________________________________________________________
	private AlertDialog dlgSucio = null;
	public void onSalir() { onSalir(false); }
	public void onSalir(boolean force)
	{
		if(!force && isSucio)
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
	boolean isEliminar = true;
	public void onEliminar()
	{
		if(!isEliminar)return;
		isEliminar = false;

		AlertDialog.Builder dialog = new AlertDialog.Builder(view.getAct());
		dialog.setNegativeButton(app.getString(R.string.cancelar), (dlg, which) -> isEliminar = true);
		dialog.setPositiveButton(app.getString(R.string.eliminar), (dialog1, which) -> eliminar());
		dlgEliminar = dialog.create();
		dlgEliminar.setTitle(o.getNombre());
		dlgEliminar.setMessage(app.getString(R.string.seguro_eliminar));
		dlgEliminar.setOnDismissListener(dlg -> isEliminar = dlgEliminar ==null || !dlgEliminar.isShowing());//bEliminar=true no funcionaria, se llama con retraso
		dlgEliminar.show();
	}
	protected abstract void eliminar();

	private static class CesTextWatcher implements TextWatcher
	{
		private final TextView tv;
		private final String str;
		private final PresenterBase presenter;
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
