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

	@Inject	Util util;
	@Inject	PreLugar presenter;

	private Marker marker;
	private TextView lblPosicion;
	private void setPosLabel(double lat, double lon){
		lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}

	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		((App)getApplication()).getGlobalComponent().inject(this);
		super.ini(presenter, util, new Lugar(), R.layout.act_lugar);
		super.onCreate(savedInstanceState);
		//------------------------------------
		ImageButton btnActPos = findViewById(R.id.btnActPos);
		btnActPos.setOnClickListener(v -> setCurrentLocation());

		//------------------------------------
		lblPosicion = findViewById(R.id.lblPosicion);
		setPosLabel(presenter.getLatitud(), presenter.getLongitud());

		//------------------------------------
		if(presenter.isNuevo()) {
			setTitle(getString(R.string.nuevo_lugar));
			setCurrentLocation();
		}
		else {
			setTitle(getString(R.string.editar_lugar));
		}
	}

	private void setCurrentLocation() {
		Location loc = util.getLocation();
		if(loc != null)
			setPosicion(loc.getLatitude(), loc.getLongitude());
	}


	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_lugar, menu);
		if(presenter.isNuevo())
			menu.findItem(R.id.menu_eliminar).setVisible(false);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			presenter.guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			presenter.onEliminar();
		else if(item.getItemId() == R.id.menu_img)
			presenter.imagen();
		return super.onOptionsItemSelected(item);
	}
	//______________________________________________________________________________________________


	//----------------------------------------------------------------------------------------------
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap map)
	{
		super.onMapReady(map);
		map.setOnMapClickListener(latLng -> setPosicion(latLng.latitude, latLng.longitude));
		setMarker();
	}
	//----------------------------------------------------------------------------------------------
	protected void setPosicion(double lat, double lon)
	{
		presenter.setLatLon(lat, lon);
		presenter.setSucio();
		lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));
		setMarker();
	}

	//----------------------------------------------------------------------------------------------
	private void setMarker()
	{
		try
		{
			if(marker != null) marker.remove();
			LatLng pos = new LatLng(presenter.getLatitud(), presenter.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(presenter.getNombre())
					.snippet(presenter.getDescripcion());
			if(map != null) {
				marker = map.addMarker(mo);
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, mapZoom));
			}
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}

	//______________________________________________________________________________________________
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ActImagen.IMAGE_CAPTURE && resultCode == RESULT_OK) {
			presenter.setImg(data);
			Log.e(TAG, "onActivityResult-----------------LUGAR----------- ");
		}
		else
			Log.e(TAG, "onActivityResult-----------------LUGAR ERROR----------- ");
	}
}
