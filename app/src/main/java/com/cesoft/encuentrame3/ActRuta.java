package com.cesoft.encuentrame3;

import android.app.AlertDialog;
import android.graphics.Typeface;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.presenters.PreRuta;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActRuta extends VistaBase implements PreRuta.IVistaRuta
{
	private static final String TAG = ActRuta.class.getSimpleName();

	@Inject	Voice voice;
	@Inject Util util;
	@Inject PreRuta presenter;

	@Override public void moveCamara(LatLng pos) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, mapZoom));
	}

	//______________________________________________________________________________________________
	private boolean oncePideActivarGPS = true;
	private boolean oncePideActivarBateria = true;
	private boolean oncePideActivarBateria2 = true;

	private ImageButton btnStart;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent(getApplicationContext()).inject(this);
		super.ini(presenter, util, voice, new Ruta(), R.layout.act_ruta);
		super.onCreate(savedInstanceState);

		voice.setActivity(this);
		btnStart = findViewById(R.id.btnStart);

		//-----------------------------------
		initStartButton();
		initStopButton();
		//TODO: Hacer tambien funcion pausa

		//-----------------------------------
		initUI();
	}

	private void initStartButton() {
		if(btnStart != null)
		{
			btnStart.setEnabled(true);
			btnStart.setOnClickListener(v -> startButtonListener());
		}
	}
	private void startButtonListener() {
		if(oncePideActivarBateria && util.pideBateria(ActRuta.this)) {
			oncePideActivarBateria = false;
			return;
		}
		if(oncePideActivarBateria2 && util.pideBateriaDeNuevoSiEsNecesario(ActRuta.this)) {
			oncePideActivarBateria2 = false;
			return;
		}
		if(oncePideActivarGPS && util.pideActivarGPS(ActRuta.this)) {
			oncePideActivarGPS = false;
			return;
		}
		if(presenter.startTrackingRecord()) {
			btnStart.setEnabled(false);
			btnStart.setAlpha(0.5f);
		}
	}

	private void initStopButton() {
		final ImageButton btnStop = findViewById(R.id.btnStop);
		if(btnStop != null)
		{
			btnStop.setEnabled(true);
			btnStop.setOnClickListener(v -> stopButtonListener(btnStop));
		}
	}
	private void stopButtonListener(ImageButton btnStop) {
		btnStop.setEnabled(false);
		btnStop.setAlpha(0.5f);
		presenter.stopTrackingRecord();
	}

	private void initUI() {
		if(presenter.isNuevo())
			initUINewRoute();
		else
			initUIOldRoute();
	}
	private void initUINewRoute() {
		final ImageButton btnStop = findViewById(R.id.btnStop);
		setTitle(getString(R.string.nueva_ruta));
		if(btnStop!=null)btnStop.setVisibility(View.GONE);
		try
		{	//Oculta el mapa, no hay puntos que enseñar en el
			SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
			if(mapFragment != null && mapFragment.getFragmentManager() != null) {
				FragmentTransaction ft = mapFragment.getFragmentManager().beginTransaction();
				ft.hide(mapFragment);
				ft.commit();
			}
		}
		catch(Exception e){
			Log.e(TAG, "onCreate:e:--------------------------------------------------------",e);}
	}
	private void initUIOldRoute() {
		final ImageButton btnStart = findViewById(R.id.btnStart);
		setTitle(getString(R.string.editar_ruta));
		if(btnStart!=null)btnStart.setVisibility(View.GONE);
		//si está isActivo muestra btnStop
		String sId = util.getTrackingRoute();
		View layStartStop = findViewById(R.id.layStartStop);
		if( ! sId.equals(presenter.getId()))
		{
			if(layStartStop!=null)layStartStop.setVisibility(View.GONE);
		}
		else
			txtNombre.setTextColor(Color.RED);
	}

	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_ruta, menu);
		if(presenter.isNuevo())
		{
			menu.findItem(R.id.menu_guardar).setVisible(false);
			menu.findItem(R.id.menu_eliminar).setVisible(false);
			menu.findItem(R.id.menu_estadisticas).setVisible(false);
		}
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
			case R.id.menu_estadisticas:
				presenter.estadisticas();
				return true;
			case R.id.action_voz:
				voice.toggleStatus();
				voice.toggleListening();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	//______________________________________________________________________________________________

	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap map)
	{
		super.onMapReady(map);

		if(presenter.isNuevo())
			map.moveCamera(CameraUpdateFactory.zoomTo(mapZoom));
		else
			presenter.showRuta();

		//MARCADOR MULTILINEA --------------------------------------------
		map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
		{
			@Override
			public View getInfoWindow(Marker arg0){return null;}
			@Override
			public View getInfoContents(Marker marker)
			{
Log.e(TAG, "---------------------------- getInfoContents "+marker.getSnippet()+"-----------------------------------------------");
				LinearLayout info = new LinearLayout(ActRuta.this);
				info.setOrientation(LinearLayout.VERTICAL);

				TextView title = new TextView(ActRuta.this);
				title.setTextColor(Color.BLACK);
				title.setGravity(Gravity.CENTER);
				title.setTypeface(null, Typeface.BOLD);
				title.setText(marker.getTitle());

				TextView snippet = new TextView(ActRuta.this);
				snippet.setTextColor(Color.GRAY);
				snippet.setText(marker.getSnippet());

				ImageButton button = new ImageButton(ActRuta.this);
				button.setImageDrawable(getDrawable(android.R.drawable.ic_menu_delete));

				info.addView(title);
				info.addView(snippet);
				info.addView(button);
				return info;
			}
		});
		map.setOnInfoWindowClickListener(this::askToDelete);
	}

	private void askToDelete(Marker marker) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ActRuta.this);
		dialogBuilder.setNegativeButton(getString(R.string.cancelar), (dlg, which) -> {});
		dialogBuilder.setPositiveButton(getString(R.string.eliminar), (dialog1, which) -> {
            String id = (String)marker.getTag();
			presenter.eliminarPto(id);
		});
		final AlertDialog dlgEliminar = dialogBuilder.create();
		dlgEliminar.setTitle(marker.getTitle());
		dlgEliminar.setMessage(getString(R.string.seguro_eliminar));
		dlgEliminar.show();
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
				//presenter.guardar();
				startButtonListener();
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
