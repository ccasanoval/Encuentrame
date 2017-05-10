package com.cesoft.encuentrame3;

import java.util.Locale;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.presenters.PreLugar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
// EDITAR LUGAR
public class ActLugar extends VistaBase
{
	protected static final String TAG = ActLugar.class.getSimpleName();

	@Inject	Util _util;
	@Inject	PreLugar _presenter;

	private Marker _marker;
	private TextView _lblPosicion;
	private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}


	@Override
	public void onBackPressed()
	{
		_presenter.onSalir();
		//super.onBackPressed();
	}

	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		((App)getApplication()).getGlobalComponent().inject(this);
		super.ini(_presenter, _util, new Lugar(), R.layout.act_lugar);
		super.onCreate(savedInstanceState);

		//------------------------------------
		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		if(btnActPos != null)
		btnActPos.setOnClickListener(v ->
		{
			Location loc = _util.getLocation();
			if(loc != null)setPosLugar(loc);
		});

		//------------------------------------
		_lblPosicion = (TextView)findViewById(R.id.lblPosicion);
		setPosLabel(_presenter.getLatitud(), _presenter.getLongitud());

		//------------------------------------
		if(_presenter.isNuevo())
			setTitle(getString(R.string.nuevo_lugar));
		else
			setTitle(getString(R.string.editar_lugar));
	}


	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_lugar, menu);
		if(_presenter.isNuevo())
			menu.findItem(R.id.menu_eliminar).setVisible(false);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			_presenter.guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			_presenter.onEliminar();
		else if(item.getItemId() == R.id.menu_img)
			_presenter.imagen();
		return super.onOptionsItemSelected(item);
	}
	//______________________________________________________________________________________________


	//______________________________________________________________________________________________
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap Map)
	{
		super.onMapReady(Map);
		_Map.setOnMapClickListener(latLng ->
		{
			_presenter.setSucio();
			setPosLugar(latLng.latitude, latLng.longitude);
		});
	}

	@Override
	protected void setPosLugar(double lat, double lon)
	{
		super.setPosLugar(lat, lon);
		_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));
		setMarker();
	}
	private void setMarker()
	{
		try
		{
			if(_marker != null)_marker.remove();
			LatLng pos = new LatLng(_presenter.getLatitud(), _presenter.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(_presenter.getNombre())//getString(R.string.aviso)
					.snippet(_presenter.getDescripcion());
			_marker = _Map.addMarker(mo);
			//_Map.animateCamera(CameraUpdateFactory.zoomTo(15));
			//_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}

	//TODO: añadir altura, velocidad, etc en punto guardado y en aviso?
	//______________________________________________________________________________________________
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.e(TAG, "onActivityResult-------------"+requestCode+" --------------- "+resultCode);
		if(requestCode == ActImagen.IMAGE_CAPTURE && resultCode == RESULT_OK)
		{
			_presenter.setImg(data);
			Log.e(TAG, "onActivityResult-----------------LUGAR----------- ");
		}
		else
			Log.e(TAG, "onActivityResult-----------------LUGAR ERROR----------- ");
	}
}
