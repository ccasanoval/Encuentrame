package com.cesoft.encuentrame3.views;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import java.util.Locale;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.presenters.PreAviso;
import com.cesoft.encuentrame3.util.Log;

import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova
public class ActAviso extends VistaBase implements PreAviso.IVistaAviso
{
	private static final String TAG = ActAviso.class.getSimpleName();

	private TextView lblPosicion = null;
		private void setPosLabel(double lat, double lon) {
			if(lblPosicion != null)
				lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));
		}
	private Switch swtActivo = null;
		public boolean isActivo() {
			if(swtActivo == null) return false;
			return swtActivo.isChecked();
		}
	private TextView lblRadio = null;
		private void setRadioLabel(int radio) {
			if(lblRadio != null)
				lblRadio.setText(getString(R.string.radio_m, radio));
		}
	private SeekBar seekBarRadio;
	private void changeRadio(int radio) {
		seekBarRadio.setProgress((int)Math.sqrt(radio));
		setRadioLabel(radio);
		setMarker();
	}

	private Circle circle;
	private Marker marker;

	@Inject PreAviso presenter;

	//------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent().inject(this);
		super.ini(presenter, new Aviso(), R.layout.act_aviso);
		super.onCreate(savedInstanceState);

		//------------------------------------
		ImageButton btnActPos = findViewById(R.id.btnActPos);
		btnActPos.setOnClickListener(v ->
		{
			Location loc = util.getLocation();
			if(loc != null)
				setPosicion(loc.getLatitude(), loc.getLongitude(), true);
		});

		//------------------------------------
		lblPosicion = findViewById(R.id.lblPosicion);
		setPosLabel(presenter.getLatitud(), presenter.getLongitud());

		swtActivo = findViewById(R.id.bActivo);
		swtActivo.setChecked(presenter.isActivo());
		swtActivo.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.setActivo(isChecked));

		//https://abhiandroid.com/ui/seekbar
		lblRadio = findViewById(R.id.lblRadio);
		seekBarRadio = findViewById(R.id.seekBar);
		seekBarRadio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				int radio = seekBar.getProgress();
				radio = radio*radio;
				if(radio < Aviso.MIN_RADIO)
					radio=Aviso.MIN_RADIO;
				presenter.setRadio(radio);
				changeRadio(radio);
			}
		});
		int radio = (int)presenter.getRadio();
		changeRadio(radio);

		//------------------------------------
		if(presenter.isNuevo())
			setTitle(getString(R.string.nuevo_aviso));
		else
			setTitle(getString(R.string.editar_aviso));
	}

	//______________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_aviso, menu);
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	//----------------------------------------------------------------------------------------------
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap map)
	{
		super.onMapReady(map);
		map.getUiSettings().setZoomControlsEnabled(true);
		map.setOnMapClickListener(latLng -> setPosicion(latLng.latitude, latLng.longitude, true));

		Location loc = util.getLocation();
		if(loc != null && presenter.isNuevo()) {
			setPosicion(loc.getLatitude(), loc.getLongitude(), false);
		}
		else {
			setPosicion(presenter.getLatitud(), presenter.getLongitud(), false);
		}
	}

	//----------------------------------------------------------------------------------------------
	private void setPosicion(double lat, double lon, boolean sucio)
	{
		presenter.setLatLon(lat, lon);
		if(sucio) presenter.setSucio();
		setPosLabel(lat, lon);
		setMarker();
	}

	//----------------------------------------------------------------------------------------------
	private void setMarker()
	{
		if(map == null)return;
		try
		{
			if(marker != null) marker.remove();
			LatLng pos = new LatLng(presenter.getLatitud(), presenter.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(presenter.getNombre()).snippet(presenter.getDescripcion());
			marker = map.addMarker(mo);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, mapZoom));

			if(circle != null) circle.remove();
			circle = map.addCircle(new CircleOptions()
					.center(pos)
					.radius(presenter.getRadio())
					.strokeColor(Color.TRANSPARENT)
					.fillColor(0x55AA0000));//Color.BLUE
		}
		catch(Exception e) {
			Log.e(TAG, "setMarker:e:-----------------------------------------------------------", e);
		}
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onCommandEvent(Voice.CommandEvent event)
	{
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
