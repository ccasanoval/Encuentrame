package com.cesoft.encuentrame3;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import java.util.Locale;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.presenters.PreAviso;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

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
public class ActAviso extends VistaBase implements PreAviso.IVistaAviso
{
	private static final String TAG = ActAviso.class.getSimpleName();

	private TextView lblPosicion;
		private void setPosLabel(double lat, double lon){
			lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}
	private Switch swtActivo;
		public boolean isActivo() { return swtActivo.isChecked(); }

	private static final String[] _asRadio = {"10 m", "50 m", "100 m", "200 m", "300 m", "400 m", "500 m", "750 m", "1 Km", "2 Km", "3 Km", "4 Km", "5 Km", "7.5 Km", "10 Km"};
	private static final int[]    _adRadio = { 10,     50,     100,     200,     300,     400,     500,     750,     1000,   2000,   3000,   4000,   5000,   7500,     10000};

	private Circle circle;
	private Marker marker;

	@Inject	Voice voice;
	@Inject Util util;
	@Inject PreAviso presenter;

	//------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent(getApplicationContext()).inject(this);
		super.ini(presenter, util, voice, new Aviso(), R.layout.act_aviso);
		super.onCreate(savedInstanceState);

		voice.setActivity(this);

		//------------------------------------
		ImageButton btnActPos = findViewById(R.id.btnActPos);
		btnActPos.setOnClickListener(v ->
		{
			Location loc = util.getLocation();
			if(loc != null) setPosicion(loc.getLatitude(), loc.getLongitude());
		});

		//------------------------------------
		lblPosicion = findViewById(R.id.lblPosicion);
		setPosLabel(presenter.getLatitud(), presenter.getLongitud());

		swtActivo = findViewById(R.id.bActivo);
		swtActivo.setChecked(presenter.isActivo());
		swtActivo.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.setActivo(isChecked));

		Spinner spnRadio = findViewById(R.id.spnRadio);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, _asRadio);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnRadio.setAdapter(adapter);
		spnRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				presenter.setRadio(_adRadio[position]);
				setMarker();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				presenter.setRadio(100);
			}
		});
		for(int i=0; i < _adRadio.length; i++)
		{
			if(presenter.getRadio() == _adRadio[i])
			{
				spnRadio.setSelection(i);
				break;
			}
		}

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
		vozMenuItem = menu.findItem(R.id.action_voz);
		refreshVoiceIcon();
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
			case R.id.action_voz:
				voice.toggleListening();
				refreshVoiceIcon();
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
		map.setOnMapClickListener(latLng -> setPosicion(latLng.latitude, latLng.longitude));

		Location loc = util.getLocation();
		if(loc != null) setPosicion(loc.getLatitude(), loc.getLongitude());
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
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
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
