package com.cesoft.encuentrame3;

import java.util.Locale;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.presenters.PreLugar;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
// EDITAR LUGAR
public class ActLugar extends VistaBase
{
	protected static final String TAG = ActLugar.class.getSimpleName();

	@Inject	Voice voice;
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
		super.ini(presenter, util, voice, new Lugar(), R.layout.act_lugar);
		super.onCreate(savedInstanceState);

		voice.setActivity(this);

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
		vozMenuItem = menu.findItem(R.id.action_voz);
		return true;
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
			case R.id.action_voz:
				voice.toggleListening();
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
