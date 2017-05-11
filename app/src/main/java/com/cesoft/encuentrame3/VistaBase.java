package com.cesoft.encuentrame3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Objeto;
import com.cesoft.encuentrame3.presenters.PresenterBase;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.common.api.Status;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar Casanova on 10/05/2017.
//
public abstract class VistaBase
		extends AppCompatActivity
		implements PresenterBase.IVista, LocationListener, OnMapReadyCallback,
			GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>
{
	protected static final String TAG = VistaBase.class.getSimpleName();
	private static final int DELAY_LOCATION = 30*1000;

	private PresenterBase _presenter;
	protected Util _util;

	protected EditText _txtNombre;
	protected EditText _txtDescripcion;

	protected GoogleApiClient _GoogleApiClient;
	protected LocationRequest _LocationRequest;
	protected GoogleMap _Map;
	protected int _id_layout;

	//----------------------------------------------------------------------------------------------
	void ini(PresenterBase presenter, Util util, Objeto objDefecto, int id_layout)
	{
		_util = util;
		_presenter=presenter;
		_presenter.ini(this);
		_presenter.loadObjeto(objDefecto);
		_id_layout = id_layout;
	}

	//----------------------------------------------------------------------------------------------
	protected synchronized void buildGoogleApiClient()
	{
		_GoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		_GoogleApiClient.connect();
	}
	protected boolean checkPlayServices()
	{
		GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
		int result = googleAPI.isGooglePlayServicesAvailable(this);
		if(result != ConnectionResult.SUCCESS)
		{
			Log.e(TAG, "checkPlayServices:e:--------------------------------------------------------"+result);
			return false;
		}
		return true;
	}
	protected void buildLocationRequest()
	{
		_LocationRequest = new LocationRequest();
		_LocationRequest.setInterval(DELAY_LOCATION);
		_LocationRequest.setFastestInterval(DELAY_LOCATION-100);
		_LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}
	protected void clean()
	{
		try{ LocationServices.FusedLocationApi.removeLocationUpdates(_GoogleApiClient, this); }catch(Exception ignore){}
		_LocationRequest = null;
		_GoogleApiClient.unregisterConnectionCallbacks(this);
		_GoogleApiClient.unregisterConnectionFailedListener(this);
		_GoogleApiClient.disconnect();
		_GoogleApiClient = null;
		if(_Map != null)
		{
			_Map.clear();
			_Map = null;
		}
		if(_progressDialog != null)_progressDialog.dismiss();
		_progressDialog = null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ACTIVITY
	//----------------------------------------------------------------------------------------------
	@Override
	public void onBackPressed()
	{
		_presenter.onSalir();
		//super.onBackPressed();
	}
	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(_id_layout);

		//----------------------------
		try{ setSupportActionBar((Toolbar)findViewById(R.id.toolbar)); }
		catch(Exception ignore){}//ActMaps no tiene toolbar
		//
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)fab.setOnClickListener(view -> _presenter.onSalir());
		//
		fab = (FloatingActionButton)findViewById(R.id.fabBuscar);
		if(fab != null)fab.setOnClickListener(view -> _util.onBuscar(this, _Map, _fMapZoom));

		//----------------------------
		try{
		_txtNombre = (EditText)findViewById(R.id.txtNombre);
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		_txtNombre.setText(_presenter.getNombre());
		_txtDescripcion.setText(_presenter.getDescripcion());
		_presenter.setOnTextChange(_txtNombre, _txtDescripcion);//Todo, aqui, no en presenter...?
		}catch(Exception ignore){}//ActMaps no tiene campos

		//----------------------------
		if(savedInstanceState != null)
		{
			_fMapZoom = savedInstanceState.getFloat(MAP_ZOOM, 15);
			_presenter.loadSavedInstanceState(savedInstanceState);
		}
	}
	//----------------------------------------------------------------------------------------------
	private static final String MAP_ZOOM = "mapzoom";
	protected float _fMapZoom = 15;
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putFloat(MAP_ZOOM, _fMapZoom);
		_presenter.onSaveInstanceState(outState);
	}

	//______________________________________________________________________________________________
	@Override
	public void onStart()
	{
		super.onStart();
		_presenter.subscribe(this);
		if(checkPlayServices())buildGoogleApiClient();
		buildLocationRequest();
		((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
		Util.pideGPS(this, _GoogleApiClient, _LocationRequest);
		Log.e(TAG, "-------------------- ON START");
	}
	@Override
	public void onStop()
	{
		super.onStop();
		_presenter.unsubscribe();
		clean();
		Log.e(TAG, "-------------------- ON STOP");
	}

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onPause()
	{
		super.onPause();
		stopTracking();
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		startTracking();
	}
	private void startTracking()
	{
		if(_GoogleApiClient != null && _GoogleApiClient.isConnected())
		{
			try{ LocationServices.FusedLocationApi.requestLocationUpdates(_GoogleApiClient, _LocationRequest, this); }
			catch(SecurityException ignored){}
		}
	}
	private void stopTracking()
	{
		if(_GoogleApiClient != null && _GoogleApiClient.isConnected())
			LocationServices.FusedLocationApi.removeLocationUpdates(_GoogleApiClient, this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// PRESENTER VISTA INTERFACE
	//----------------------------------------------------------------------------------------------
	//@Override public void finish() { }
	@Override public Activity getAct() { return this; }
	@Override public GoogleMap getMap() { return _Map; }
	@Override public String getTextNombre() { return _txtNombre.getText().toString(); }
	@Override public String getTextDescripcion() { return _txtDescripcion.getText().toString(); }
	@Override public void requestFocusNombre() { _txtNombre.requestFocus(); }
	@Override public void toast(int msg)
	{
		Toast.makeText(this, getString(msg), Toast.LENGTH_LONG).show();
	}
	@Override public void toast(int msg, String err)
	{
		Toast.makeText(this, String.format(getString(msg), err), Toast.LENGTH_LONG).show();
	}

	//----------------------------------------------------------------------------------------------
	private ProgressDialog _progressDialog = null;
	@Override public void iniEspera()
	{
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
	}
	@Override public void finEspera()
	{
		if(_progressDialog!=null)_progressDialog.dismiss();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	// OnConnectionFailedListener  :  https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult
	//----------------------------------------------------------------------------------------------
	@Override public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		Log.e(TAG, "onConnectionFailed:e:***********************************************************"+result.getErrorCode());
		toast(R.string.err_conn_google, result.getErrorMessage());
	}
	@Override public void onConnected(Bundle arg0)
	{
		Log.w(TAG, "------------------------------------onConnected---------------------------------");
	}
	@Override public void onConnectionSuspended(int arg0)
	{
		if(_GoogleApiClient != null)
			_GoogleApiClient.connect();
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	// ResultCallback
	//----------------------------------------------------------------------------------------------
	@Override public void onResult(@NonNull Status status)
	{
		Log.w(TAG, "---------------------------------------onResult---------------------------------"+status);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// LocationListener
	//----------------------------------------------------------------------------------------------
	@Override
	public void onLocationChanged(Location location)
	{
		_util.setLocation(location);
		if(_presenter.getLatitud() == 0 && _presenter.getLongitud() == 0)
			setPosLugar(location.getLatitude(), location.getLongitude());
	}
	//______________________________________________________________________________________________
	//protected void setPosLugar(Location loc) { setPosLugar(loc.getLatitude(), loc.getLongitude()); }
	protected void setPosLugar(double lat, double lon) { _presenter.setLatLon(lat, lon); }

	////////////////////////////////////////////////////////////////////////////////////////////////
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException ignored){}
		//https://developers.google.com/maps/documentation/android-api/map?hl=es-419
		//_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL y GoogleMap.MAP_TYPE_SATELLITE
		_Map.setOnCameraMoveListener(() -> _fMapZoom = _Map.getCameraPosition().zoom);
		_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		if(_presenter.getLatitud() == 0 && _presenter.getLongitud() == 0)
		{
			Location loc = _util.getLocation();
			if(loc != null)_presenter.setLatLon(loc.getLatitude(), loc.getLongitude());
		}
		setPosLugar(_presenter.getLatitud(), _presenter.getLongitud());
	}

}
