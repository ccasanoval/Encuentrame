package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.presenters.PresenterBase;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

import java.util.List;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 10/05/2017.
//
public abstract class VistaBase
		extends AppCompatActivity
		implements PresenterBase.IVista, OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
	private static final String TAG = VistaBase.class.getSimpleName();
	private static final int DELAY_LOCATION = 30 * 1000;

	private PresenterBase presenter;
	private Util util;
	private Voice voice;

    protected MenuItem vozMenuItem;
	protected EditText txtNombre;
	protected EditText txtDescripcion;

	protected GoogleApiClient googleApiClient;
	protected LocationRequest locationRequest;
	protected GoogleMap map;
	protected int idLayout;

	protected FusedLocationProviderClient fusedLocationClient;

	//----------------------------------------------------------------------------------------------
	void ini(PresenterBase presenter, Util util, Voice voice, Objeto objDefecto, int idLayout) {
		this.util = util;
		this.voice = voice;
		this.presenter = presenter;
		this.presenter.ini(this);
		this.presenter.loadObjeto(objDefecto);
		this.idLayout = idLayout;
	}

	//----------------------------------------------------------------------------------------------
	protected synchronized void buildGoogleApiClient() {
		googleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		googleApiClient.connect();
	}

	protected boolean checkPlayServices() {
		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);
		if (result != ConnectionResult.SUCCESS) {
			Log.e(TAG, "checkPlayServices:e:--------------------------------------------------" + result);
			return false;
		}
		return true;
	}

	protected void buildLocationRequest() {
		locationRequest = new LocationRequest();
		locationRequest.setInterval(DELAY_LOCATION);
		locationRequest.setFastestInterval(DELAY_LOCATION);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	protected void clean() {
		stopTracking();
		locationRequest = null;
		googleApiClient.unregisterConnectionCallbacks(this);
		googleApiClient.unregisterConnectionFailedListener(this);
		googleApiClient.disconnect();
		googleApiClient = null;
		if (map != null) {
			map.clear();
			map = null;
		}
		if (progressDialog != null) progressDialog.dismiss();
		progressDialog = null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ACTIVITY
	//----------------------------------------------------------------------------------------------
	@Override
	public void onBackPressed() {
		presenter.onSalir();
	}

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(idLayout);

		//----------------------------
		try { setSupportActionBar(findViewById(R.id.toolbar)); } catch(Exception ignore){}//ActMaps no tiene toolbar
		//
		FloatingActionButton fab = findViewById(R.id.fabVolver);
		if(fab != null) fab.setOnClickListener(view -> presenter.onSalir());
		//
		fab = findViewById(R.id.fabBuscar);
		if(fab != null) fab.setOnClickListener(view -> util.onBuscar(this, map, mapZoom));

		//----------------------------
		try {
			txtNombre = findViewById(R.id.txtNombre);
			txtDescripcion = findViewById(R.id.txtDescripcion);
			txtNombre.setText(presenter.getNombre());
			txtDescripcion.setText(presenter.getDescripcion());
			presenter.setOnTextChange(txtNombre, txtDescripcion);
		} catch(Exception ignore){}//ActMaps no tiene campos

		//----------------------------
		if (savedInstanceState != null) {
			mapZoom = savedInstanceState.getFloat(MAP_ZOOM, 15);
			presenter.loadSavedInstanceState(savedInstanceState);
		}

		if(presenter.isVoiceCommand())
			txtNombre.setText(R.string.voice_generated);

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
	}

	//----------------------------------------------------------------------------------------------
	private static final String MAP_ZOOM = "mapzoom";
	protected float mapZoom = 15;

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putFloat(MAP_ZOOM, mapZoom);
		presenter.onSaveInstanceState(outState);
	}

	//______________________________________________________________________________________________
	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
		presenter.subscribe(this);
		if(checkPlayServices()) buildGoogleApiClient();
		buildLocationRequest();
		SupportMapFragment smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		if(smf != null)
			smf.getMapAsync(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		presenter.unsubscribe();
		clean();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume----------------------------------------");
		startTracking();
		if(voice.isListening()) {
			voice.startListening();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopTracking();
		voice.stopListening();
	}


	private void startTracking() {
        if (googleApiClient == null || !googleApiClient.isConnected())return;
		if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		    return;
		locationCallback = createLocationCallback();
		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
	}

	private void stopTracking() {
        if (googleApiClient == null || !googleApiClient.isConnected())return;
		if(locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback);
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
	private ProgressDialog progressDialog = null;
	@Override public void iniEspera() {
		progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
	}
	@Override public void finEspera() {
		if(progressDialog !=null) progressDialog.dismiss();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// OnConnectionFailedListener : https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult
	//----------------------------------------------------------------------------------------------
	@Override public void onConnectionFailed(@NonNull ConnectionResult result) {
		Log.e(TAG, "onConnectionFailed:e:*****************************************************"+result.getErrorCode());
		toast(R.string.err_conn_google, result.getErrorMessage());
	}
	@Override public void onConnected(Bundle arg0) {
		Log.w(TAG, "------------------------------onConnected---------------------------------");
	}
	@Override public void onConnectionSuspended(int arg0) {
		if(googleApiClient != null)
			googleApiClient.connect();
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	// ResultCallback
	//----------------------------------------------------------------------------------------------
	@Override public void onResult(@NonNull Status status) {
		Log.w(TAG, "---------------------------------onResult---------------------------------"+status);
	}

	private LocationCallback locationCallback = null;
	private LocationCallback createLocationCallback() {
		return new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				List<Location> locationList = locationResult.getLocations();
				if( ! locationList.isEmpty()) {
					Location location = locationList.get(locationList.size() - 1);
					util.setLocation(location);
					if(presenter.getLatitud() == 0 && presenter.getLongitud() == 0)
						setPosLugar(location.getLatitude(), location.getLongitude());
				}
			}
		};
	}
	protected void setPosLugar(double lat, double lon) { presenter.setLatLon(lat, lon); }

	////////////////////////////////////////////////////////////////////////////////////////////////
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		map = googleMap;
		try { map.setMyLocationEnabled(true);} catch(SecurityException ignored){}
		//https://developers.google.com/maps/documentation/android-api/map?hl=es-419
		//map.setMapType(GoogleMap.MAP_TYPE_NORMAL y GoogleMap.MAP_TYPE_SATELLITE
		map.setOnCameraMoveListener(() -> {
			if(map != null)
				mapZoom = map.getCameraPosition().zoom;
		});
		map.animateCamera(CameraUpdateFactory.zoomTo(mapZoom));
		if(presenter.getLatitud() == 0 && presenter.getLongitud() == 0)
		{
			Location loc = util.getLocation();
			if(loc != null) presenter.setLatLon(loc.getLatitude(), loc.getLongitude());
		}
		setPosLugar(presenter.getLatitud(), presenter.getLongitud());
	}

    protected void refreshVoiceIcon() {
        if(vozMenuItem != null)
            vozMenuItem.setIcon(voice.isListening() ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_white_24dp);
    }

	@Subscribe(sticky = true, threadMode = ThreadMode.POSTING)
	public void onVoiceEvent(Voice.VoiceEvent event)
	{
		refreshVoiceIcon();
	}
}
