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

import androidx.fragment.app.FragmentTransaction;

import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.presenters.PreRuta;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActRuta extends VistaBase implements PreRuta.IVistaRuta
{
	@Inject Util util;
	@Inject PreRuta presenter;

	@Override public void moveCamara(LatLng pos) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, mapZoom));
	}

	//______________________________________________________________________________________________
	private boolean oncePideActivarGPS = true;
	private boolean oncePideActivarBateria = true;
	private boolean oncePideActivarBateria2 = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent(getApplicationContext()).inject(this);
		super.ini(presenter, util, new Ruta(), R.layout.act_ruta);
		super.onCreate(savedInstanceState);

		//-----------------------------------
		initStartButton();
		initStopButton();
		//TODO: Hacer tambien funcion pausa

		//-----------------------------------
		initUI();
	}

	private void initStartButton() {
		final ImageButton btnStart = findViewById(R.id.btnStart);
		if(btnStart != null)
		{
			btnStart.setEnabled(true);
			btnStart.setOnClickListener(v -> startButtonListener(btnStart));
		}
	}
	private void startButtonListener(ImageButton btnStart) {
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
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			presenter.guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			presenter.onEliminar();
		else if(item.getItemId() == R.id.menu_estadisticas)
			presenter.estadisticas();
		return super.onOptionsItemSelected(item);
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

}
