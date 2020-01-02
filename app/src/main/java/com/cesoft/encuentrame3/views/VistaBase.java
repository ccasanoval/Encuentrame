package com.cesoft.encuentrame3.views;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.presenters.PresenterBase;
import com.cesoft.encuentrame3.util.GpsLocationCallback;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 10/05/2017.
//
public abstract class VistaBase
		extends AppCompatActivity
		implements PresenterBase.IVista, OnMapReadyCallback,
		ResultCallback<Status> {
	private static final String TAG = VistaBase.class.getSimpleName();
	private static final int DELAY_LOCATION = 30 * 1000;
	private static final String MAP_ZOOM = "mapzoom";

    protected MenuItem vozMenuItem;
	protected EditText txtNombre;
	protected EditText txtDescripcion;
	protected ProgressBar progressBar;

	protected LocationRequest locationRequest;
	protected GoogleMap map;
	protected int idLayout;
	protected float mapZoom = 20;

	protected FusedLocationProviderClient fusedLocationClient;
	protected PresenterBase presenterBase;

	@Inject Voice voice;
	@Inject	Util util;
	//@Inject
	GpsLocationCallback locationCallback = null;

	//----------------------------------------------------------------------------------------------
	void ini(PresenterBase presenter, Objeto objDefecto, int idLayout) {
		this.presenterBase = presenter;
		this.presenterBase.ini(this);
		this.presenterBase.loadObjeto(objDefecto);
		this.idLayout = idLayout;
	}

	/* TODO: meter en utiles y llamar al inicio para comprobar que tiene servicios...
	protected boolean checkPlayServices() {
		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);
		if (result != ConnectionResult.SUCCESS) {
			Log.e(TAG, "checkPlayServices:e:--------------------------------------------------" + result);
			return false;
		}
		return true;
	}*/

	protected void buildLocationRequest() {
		locationRequest = new LocationRequest();
		locationRequest.setInterval(DELAY_LOCATION);
		locationRequest.setFastestInterval(DELAY_LOCATION);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	protected void clean() {
		stopTracking();
		locationRequest = null;
		if (map != null) {
			map.clear();
			map = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ACTIVITY
	//----------------------------------------------------------------------------------------------

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(idLayout);
		App.getComponent().inject(this);

		//----------------------------
		try {
			Toolbar toolbar = findViewById(R.id.toolbar);
			if(toolbar != null) {
				toolbar.setTitleTextAppearance(this, R.style.toolbarTextStyle);
				setSupportActionBar(toolbar);
				ActionBar ab = getSupportActionBar();
				if(ab != null) ab.setDisplayHomeAsUpEnabled(true);
			}
		}
		catch(Exception e) {
			Log.e(TAG, "onCreate--------------------------------------------------------------",e);
		}
		//
		FloatingActionButton fab;
		fab = findViewById(R.id.fabBuscar);
		if(fab != null) fab.setOnClickListener(view -> util.onBuscar(this, map, mapZoom));

		//----------------------------
		try {
			txtNombre = findViewById(R.id.txtNombre);
			txtDescripcion = findViewById(R.id.txtDescripcion);
			txtNombre.setText(presenterBase.getNombre());
			txtDescripcion.setText(presenterBase.getDescripcion());
			presenterBase.setOnTextChange(txtNombre, txtDescripcion);
			progressBar = findViewById(R.id.progressBar);
		} catch(Exception ignore){}//ActMaps no tiene campos

		//----------------------------
		if(savedInstanceState != null) {
			mapZoom = savedInstanceState.getFloat(MAP_ZOOM, mapZoom);
			presenterBase.loadSavedInstanceState(savedInstanceState);
		}

		if(presenterBase.isVoiceCommand())
			txtNombre.setText(R.string.voice_generated);

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
	}

	//----------------------------------------------------------------------------------------------

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putFloat(MAP_ZOOM, mapZoom);
		presenterBase.onSaveInstanceState(outState);
	}

	//______________________________________________________________________________________________
	@Override
	public boolean onSupportNavigateUp() {
		if(getSupportFragmentManager().popBackStackImmediate())
			return true;
		finish();
		return super.onSupportNavigateUp();
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
		presenterBase.subscribe(this);
		buildLocationRequest();
		SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		if(smf != null)
			smf.getMapAsync(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		presenterBase.unsubscribe();
		clean();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		startTracking();
		if(voice.isListening()) {
			voice.startListening();
		}
		refreshVoiceIcon();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopTracking();
		voice.stopListening();
		presenterBase.onPause();
	}


	private void startTracking() {
		locationCallback = new GpsLocationCallback();
		if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
		&& ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		    return;
		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
	}

	private void stopTracking() {
		if(locationCallback != null) {
			fusedLocationClient.removeLocationUpdates(locationCallback);
		}
		locationCallback = null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// PRESENTER VISTA INTERFACE
	//----------------------------------------------------------------------------------------------
	@Override public Activity getAct() { return this; }
	@Override public GoogleMap getMap() { return map; }
	@Override public String getTextNombre() { return txtNombre.getText().toString(); }
	@Override public String getTextDescripcion() { return txtDescripcion.getText().toString(); }
	@Override public void requestFocusNombre() { txtNombre.requestFocus(); }
	@Override public void toast(int msg) {
		Toast.makeText(this, getString(msg), Toast.LENGTH_LONG).show();
	}
	@Override public void toast(int msg, String err) {
		Toast.makeText(this, String.format(getString(msg), err), Toast.LENGTH_LONG).show();
	}

	//----------------------------------------------------------------------------------------------
	@Override public void iniEspera() {
		if(progressBar != null)
			progressBar.setVisibility(View.VISIBLE);
		//getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}
	@Override public void finEspera() {
		if(progressBar != null)
			progressBar.setVisibility(View.GONE);
		//getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ResultCallback
	//----------------------------------------------------------------------------------------------
	@Override public void onResult(@NonNull Status status) {
		Log.e(TAG, "---------------------------------onResult---------------------------------"+status);
	}

	protected void setPosLugar(double lat, double lon)
	{
		presenterBase.setLatLon(lat, lon);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		map = googleMap;
		map.getUiSettings().setZoomControlsEnabled(true);
		try { map.setMyLocationEnabled(true);} catch(SecurityException ignored){}
		//https://developers.google.com/maps/documentation/android-api/map?hl=es-419
		//map.setMapType(GoogleMap.MAP_TYPE_NORMAL y GoogleMap.MAP_TYPE_SATELLITE
		map.setOnCameraIdleListener(() -> {
			if(map != null)
				mapZoom = map.getCameraPosition().zoom;
		});
		if(presenterBase.getLatitud() == 0 && presenterBase.getLongitud() == 0 && presenterBase.isNuevo())
		{
			Location loc = util.getLocation();
			if(loc != null)
				presenterBase.setLatLon(loc.getLatitude(), loc.getLongitude());
		}
		setPosLugar(presenterBase.getLatitud(), presenterBase.getLongitud());
		map.animateCamera(CameraUpdateFactory.zoomTo(mapZoom));
	}

    protected void refreshVoiceIcon() {
        if(vozMenuItem != null)
            vozMenuItem.setIcon(voice.isListening() ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_white_24dp);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		vozMenuItem = menu.findItem(R.id.action_voz);
		refreshVoiceIcon();
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			presenterBase.onBackPressed();
		}
		if(item.getItemId() == R.id.action_voz) {
			voice.checkPermissions(this);
			voice.toggleListening();
			refreshVoiceIcon();
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		presenterBase.onBackPressed();
	}


	@Subscribe(sticky = true, threadMode = ThreadMode.POSTING)
	public void onVoiceEvent(Voice.VoiceStatusEvent event) {
		refreshVoiceIcon();
	}

	@Subscribe
	public void onGpsLocation(Location location) {
		setPosLugar(location.getLatitude(), location.getLongitude());
		util.setLocation(location);
	}
}
