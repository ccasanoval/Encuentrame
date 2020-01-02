package com.cesoft.encuentrame3.views;

import java.util.Locale;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.presenters.PreLugar;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.cesoft.encuentrame3.util.Log;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
// EDITAR LUGAR
public class ActLugar extends VistaBase
{
	protected static final String TAG = ActLugar.class.getSimpleName();

	private Marker marker;
	private TextView lblPosicion;
	private void setPosLabel(double lat, double lon){
		lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}

	@Inject	PreLugar presenter;

	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent().inject(this);
		super.ini(presenter, new Lugar(), R.layout.act_lugar);
		super.onCreate(savedInstanceState);

		//------------------------------------
		ImageButton btnActPos = findViewById(R.id.btnActPos);
		btnActPos.setOnClickListener(v -> setCurrentLocation(true));

		//------------------------------------
		lblPosicion = findViewById(R.id.lblPosicion);
		setPosLabel(presenter.getLatitud(), presenter.getLongitud());

		//------------------------------------
		if(presenter.isNuevo()) {
			setTitle(getString(R.string.nuevo_lugar));
			setCurrentLocation(false);
		}
		else {
			setTitle(getString(R.string.editar_lugar));
		}
	}

	private void setCurrentLocation(boolean sucio) {
		Location loc = util.getLocation();
		if(loc != null)
			setPosicion(loc.getLatitude(), loc.getLongitude(), sucio);
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
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.menu_guardar:
				presenter.guardar();
				return true;
			case R.id.menu_eliminar:
				presenter.onEliminar();
				return true;
			case R.id.menu_img:
				presenter.imagen();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	//______________________________________________________________________________________________


	//----------------------------------------------------------------------------------------------
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap map)
	{
		super.onMapReady(map);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.setOnMapClickListener(latLng -> setPosicion(latLng.latitude, latLng.longitude, true));
		setMarker();
	}
	//----------------------------------------------------------------------------------------------
	protected void setPosicion(double lat, double lon, boolean sucio)
	{
		presenter.setLatLon(lat, lon);
		if(sucio)presenter.setSucio();
		setPosLabel(lat, lon);
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
			Log.e(TAG, "onActivityResult-----------------LUGAR----------- "+data+" / "+data.getStringExtra(ActImagen.PARAM_IMG_PATH));
		}
		else
			Log.e(TAG, "onActivityResult-----------------LUGAR ERROR----------- ");
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onCommandEvent(Voice.CommandEvent event)
	{
		Log.e(TAG, "onCommandEvent--------------------------- "+event.getCommand()+" / "+event.getText());
		Toast.makeText(this, event.getText(), Toast.LENGTH_LONG).show();

		switch(event.getCommand()) {
			case R.string.voice_cancel:
				presenter.onSalir(true);
				voice.speak(event.getText());
				break;
			case R.string.voice_save:
			case R.string.voice_start:
				presenter.guardar();
				voice.speak(event.getText());
				break;
			case R.string.voice_stop_listening:
				voice.stopListening();
				voice.speak(event.getText());
				break;
			default:
				break;
		}
	}

}