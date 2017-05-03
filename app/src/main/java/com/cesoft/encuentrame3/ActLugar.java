package com.cesoft.encuentrame3;

import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActLugar extends AppCompatActivity implements PreLugar.LugarView, LocationListener, OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>
{
	private static final String TAG = ActLugar.class.getSimpleName();
	private static final int DELAY_LOCATION = 60000;

	@Inject	Util _util;
	@Inject PreLugar _presenter;

	private TextView _lblPosicion;
	private EditText _txtNombre;
	private EditText _txtDescripcion;

	private GoogleApiClient _GoogleApiClient;
	private LocationRequest _LocationRequest;
	private GoogleMap _Map;
	private Marker _marker;


	@Override
	public void onBackPressed()
	{
		_presenter.onSalir();
		//super.onBackPressed();
	}
	private class CesTextWatcher implements TextWatcher
	{
		private TextView _tv;
		private String _str;
		CesTextWatcher(TextView tv, String str){_tv = tv; _str = str;}
		@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
		@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){}
		@Override
		public void afterTextChanged(Editable editable)
		{
			if(_str == null && _tv.getText().length() > 0)_presenter.setSucio();
			if(_str != null && _tv.getText().toString().compareTo(_str) != 0)_presenter.setSucio();
		}
	}
	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_lugar);

		//_util = ((App)getApplication()).getGlobalComponent().util();
		//_util = DaggerGlobalComponent.create();
		((App)getApplication()).getGlobalComponent().inject(this);
		_presenter.ini(this);

		//------------------------------------------------------------------------------------------
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)fab.setOnClickListener(view -> _presenter.onSalir());
		//
		fab = (FloatingActionButton)findViewById(R.id.fabBuscar);
		if(fab != null)fab.setOnClickListener(view -> _util.onBuscar(ActLugar.this, _Map, _fMapZoom));

		//------------------------------------------------------------------------------------------
		_lblPosicion = (TextView)findViewById(R.id.lblPosicion);
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		if(btnActPos != null)
		btnActPos.setOnClickListener(v ->
		{
			Location loc = _util.getLocation();
			if(loc != null)setPosLugar(loc);
		});

		//------------------------------------------------------------------------------------------
		_presenter.loadObjeto();
		_txtNombre.setText(_presenter.getNombre());
		_txtDescripcion.setText(_presenter.getDescripcion());
		setPosLabel(_presenter.getLatitud(), _presenter.getLongitud());
		_txtNombre.addTextChangedListener(new CesTextWatcher(_txtNombre, _presenter.getNombre()));
		_txtDescripcion.addTextChangedListener(new CesTextWatcher(_txtDescripcion, _presenter.getDescripcion()));

		//------------------------------------------------------------------------------------------
		if(_presenter.isNuevo())
			setTitle(getString(R.string.nuevo_lugar));
		else
			setTitle(getString(R.string.editar_lugar));
		//------------------------------------------------------------------------------------------

		if(savedInstanceState != null)
		{
			_fMapZoom = savedInstanceState.getFloat(MAP_ZOOM, 15);
		}
	}
	//----------------------------------------------------------------------------------------------
	private static final String MAP_ZOOM = "mapzoom";
	private float _fMapZoom = 15;
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putFloat(MAP_ZOOM, _fMapZoom);
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
		//Log.e(TAG, "-------------------- ON START");
	}
	@Override
	public void onStop()
	{
		super.onStop();
		_presenter.unsubscribe();
		clean();
		//Log.e(TAG, "-------------------- ON STOP");
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
			try
			{
				LocationServices.FusedLocationApi.requestLocationUpdates(_GoogleApiClient, _LocationRequest, this);
			}
			catch(SecurityException ignored){}
		}
	}
	private void stopTracking()
	{
		if(_GoogleApiClient != null && _GoogleApiClient.isConnected())
		LocationServices.FusedLocationApi.removeLocationUpdates(_GoogleApiClient, this);
	}

	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_lugar, menu);
		if(_presenter.isNuevo())
			menu.findItem(R.id.menu_eliminar).setVisible(false);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			_presenter.guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			_presenter.eliminar();
		else if(item.getItemId() == R.id.menu_img)
			_presenter.imagen();
		return super.onOptionsItemSelected(item);
	}

	//______________________________________________________________________________________________
	private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}


	//______________________________________________________________________________________________
	protected synchronized void buildGoogleApiClient()
	{
		_GoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		_GoogleApiClient.connect();
	}
	private boolean checkPlayServices()
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
	private void buildLocationRequest()
	{
		_LocationRequest = new LocationRequest();
		_LocationRequest.setInterval(DELAY_LOCATION);
		_LocationRequest.setFastestInterval(DELAY_LOCATION);
		_LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}
	private void clean()
	{
		LocationServices.FusedLocationApi.removeLocationUpdates(_GoogleApiClient, this);
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
	}

	//______________________________________________________________________________________________
	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = "+ result.getErrorCode());
	}
	@Override
	public void onConnected(Bundle arg0){}
	@Override
	public void onConnectionSuspended(int arg0)
	{
		if(_GoogleApiClient != null)
			_GoogleApiClient.connect();
	}

	//______________________________________________________________________________________________
	private ProgressDialog _progressDialog;
	@Override public void iniEspera()
	{
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
	}
	@Override public void finEspera()
	{
		if(_progressDialog!=null)_progressDialog.dismiss();
	}
	@Override public void toast(int msg)
	{
		Toast.makeText(this, getString(msg), Toast.LENGTH_LONG).show();
	}
	@Override public void toast(int msg, String err)
	{
		Toast.makeText(this, String.format(getString(msg), err), Toast.LENGTH_LONG).show();
	}
	@Override public String getTextNombre(){return _txtNombre.getText().toString();}
	@Override public String getTextDescripcion(){return _txtDescripcion.getText().toString();}
	@Override public void requestFocusNombre(){_txtNombre.requestFocus();}
	@Override public Activity getAct(){return this;}


	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override
	public void onLocationChanged(Location location)
	{
		_util.setLocation(location);
		if(_presenter.getLatitud() == 0 && _presenter.getLongitud() == 0)
			setPosLugar(location);
	}

	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		Log.e(TAG, "-------------------- ON MAP READY");
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException ignored){}
		//https://developers.google.com/maps/documentation/android-api/map?hl=es-419
		//_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL y GoogleMap.MAP_TYPE_SATELLITE
		_Map.setOnMapClickListener(latLng ->
		{
			_presenter.setSucio();
			setPosLugar(latLng.latitude, latLng.longitude);
		});
		_Map.setOnCameraMoveListener(() -> _fMapZoom = _Map.getCameraPosition().zoom);
		_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		if(_presenter.getLatitud() == 0 && _presenter.getLongitud() == 0)
		{
			Location loc = _util.getLocation();
			if(loc != null)
			{
				_presenter.setLatitud(loc.getLatitude());
				_presenter.setLongitud(loc.getLongitude());
			}
		}
		setPosLugar(_presenter.getLatitud(), _presenter.getLongitud());
	}

	//______________________________________________________________________________________________
	//// 4 ResultCallback
	@Override
	public void onResult(@NonNull Status status)
	{
		Log.w(TAG, "----------:onResult:"+status);
	}

	//______________________________________________________________________________________________
	private void setPosLugar(Location loc)
	{
		setPosLugar(loc.getLatitude(), loc.getLongitude());
	}
	private void setPosLugar(double lat, double lon)
	{
		_presenter.setLatitud(lat);
		_presenter.setLongitud(lon);
		_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));
		setMarker();
	}
	private void setMarker()
	{
		try
		{
			if(_marker != null)_marker.remove();
			LatLng pos = new LatLng(_presenter.getLatitud(), _presenter.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(_presenter.getNombre())//getString(R.string.aviso)
					.snippet(_presenter.getDescripcion());
			_marker = _Map.addMarker(mo);
			//_Map.animateCamera(CameraUpdateFactory.zoomTo(15));
			//_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}

	//TODO: añadir altura, velocidad, etc en punto guardado y en aviso?
	//______________________________________________________________________________________________
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.e(TAG, "onActivityResult-------------"+requestCode+" --------------- "+resultCode);
		if(requestCode == ActImagen.IMAGE_CAPTURE && resultCode == RESULT_OK)
		{
			_presenter.setImg(data);
			Log.e(TAG, "onActivityResult-----------------LUGAR----------- ");
		}
		else
			Log.e(TAG, "onActivityResult-----------------LUGAR ERROR----------- ");
	}
}
