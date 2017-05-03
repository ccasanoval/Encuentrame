package com.cesoft.encuentrame3;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import java.util.Locale;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.annotation.NonNull;
import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActAviso extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status>
{
	private static final String TAG = ActAviso.class.getSimpleName();
	private static final int DELAY_LOCATION = 60000;

	private TextView _lblPosicion;
	private EditText _txtNombre;		public String getNombre(){return _txtNombre.getText().toString();}
	private EditText _txtDescripcion;	public String getDescripcion(){return _txtDescripcion.getText().toString();}
	private Switch  _swtActivo;			public boolean isActivo() { return _swtActivo.isChecked(); }

	private static final String[] _asRadio = {"10 m", "50 m", "100 m", "200 m", "300 m", "400 m", "500 m", "750 m", "1 Km", "2 Km", "3 Km", "4 Km", "5 Km", "7.5 Km", "10 Km"};
	private static final int[]    _adRadio = { 10,     50,     100,     200,     300,     400,     500,     750,     1000,   2000,   3000,   4000,   5000,   7500,     10000};
	private Spinner _spnRadio;

	private GoogleApiClient _GoogleApiClient;
	private LocationRequest _LocationRequest;
	private GoogleMap _Map;
	private Marker _marker;
	private Circle _circle;


	@Inject Util _util;
	@Inject PreAviso _presenter;


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
			if(_str == null && _tv.getText().length() > 0)_presenter.setSucio(true);
			if(_str != null && _tv.getText().toString().compareTo(_str) != 0)_presenter.setSucio(true);
		}
	}
	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_aviso);

		App.getComponent(getApplicationContext()).inject(this);
		_presenter.ini(this);

		//------------------------------------------------------------------------------------------
		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		if(btnActPos != null)
		btnActPos.setOnClickListener(v ->
		{
			Location loc = _util.getLocation();
			if(loc != null)setPosLugar(loc);
		});

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)
		fab.setOnClickListener(view -> _presenter.onSalir());
		//
		fab = (FloatingActionButton)findViewById(R.id.fabBuscar);
		if(fab != null)fab.setOnClickListener(view -> _util.onBuscar(ActAviso.this, _Map, _fMapZoom));

		//-----------
		_lblPosicion = (TextView)findViewById(R.id.lblPosicion);
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		_swtActivo = (Switch)findViewById(R.id.bActivo);if(_swtActivo!=null)_swtActivo.setChecked(true);
		_spnRadio = (Spinner)findViewById(R.id.spnRadio);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, _asRadio);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spnRadio.setAdapter(adapter);
		_spnRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				_presenter.setRadio(_adRadio[position]);
				setMarker();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				_presenter.setRadio(100);//TODO:radio por defecto en settings
			}
		});

		//------------------------------------------------------------------------------------------
		_presenter.loadObject();
		setValores();
		_txtNombre.addTextChangedListener(new ActAviso.CesTextWatcher(_txtNombre, _presenter.getNombre()));
		_txtDescripcion.addTextChangedListener(new ActAviso.CesTextWatcher(_txtDescripcion, _presenter.getDescripcion()));
		_swtActivo.setOnCheckedChangeListener((buttonView, isChecked) -> _presenter.setActivo(isChecked));
		//------------------------------------------------------------------------------------------
		if(_presenter.isNuevo())
			setTitle(getString(R.string.nuevo_aviso));
		else
			setTitle(getString(R.string.editar_aviso));

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
	}
	@Override
	public void onStop()
	{
		super.onStop();
		_presenter.unsubscribe();
		clean();
	}
	//______________________________________________________________________________________________
	protected void buildGoogleApiClient()
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
		if(result != ConnectionResult.SUCCESS)Log.e(TAG, "checkPlayServices:e:--------------------------------------------------------"+result);
		return result == ConnectionResult.SUCCESS;
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
			catch(SecurityException se){Log.e(TAG, "startTracking:e:--------------------------------"+se);}
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
		getMenuInflater().inflate(R.menu.menu_aviso, menu);
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
		return super.onOptionsItemSelected(item);
	}

	//______________________________________________________________________________________________
	//______________________________________________________________________________________________
	private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}
	private void setValores()
	{
		_txtNombre.setText(_presenter.getNombre());
		_txtDescripcion.setText(_presenter.getDescripcion());
		_swtActivo.setChecked(_presenter.isActivo());
		setPosLabel(_presenter.getLatitud(), _presenter.getLongitud());
		for(int i=0; i < _adRadio.length; i++)
		{
			if(_presenter.getRadio() == _adRadio[i])
			{
				_spnRadio.setSelection(i);
				break;
			}
		}
	}

	//______________________________________________________________________________________________
	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}
	@Override
	public void onConnected(Bundle arg0)
	{
	}
	@Override
	public void onConnectionSuspended(int arg0)
	{
		if(_GoogleApiClient != null)
			_GoogleApiClient.connect();
	}


	//______________________________________________________________________________________________
	private ProgressDialog _progressDialog;
	public void iniEspera() { _progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true); }
	public void finEspera() { if(_progressDialog!=null)_progressDialog.dismiss(); }
	public void requestFocusNombre() { _txtNombre.requestFocus(); }

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
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}
		catch(SecurityException se){Log.e(TAG, "onMapReady:e:---------------------------------------", se);}
		_Map.setOnMapClickListener(latLng ->
		{
			_presenter.setSucio(true);
			setPosLugar(latLng.latitude, latLng.longitude);
		});
		_Map.setOnCameraMoveListener(() ->	{ if(_Map!=null)_fMapZoom = _Map.getCameraPosition().zoom; });
		//_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		setPosLugar(_presenter.getLatitud(), _presenter.getLongitud());
	}

	//______________________________________________________________________________________________
	//// 4 ResultCallback
	@Override public void onResult(@NonNull Status status){}

	//______________________________________________________________________________________________
	public void setPosLugar(Location loc)
	{
		setPosLugar(loc.getLatitude(), loc.getLongitude());
	}
	private void setPosLugar(double lat, double lon)
	{
		_presenter.setLatitud(lat);_presenter.setLongitud(lon);
		_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", _presenter.getLatitud(), _presenter.getLongitud()));
		setMarker();
	}
	private void setMarker()
	{
		if(_Map == null)return;
		try
		{
			if(_marker != null)_marker.remove();
			LatLng pos = new LatLng(_presenter.getLatitud(), _presenter.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(_presenter.getNombre()).snippet(_presenter.getDescripcion());
			_marker = _Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));

			if(_circle != null)_circle.remove();
			_circle = _Map.addCircle(new CircleOptions()
					.center(pos)
					.radius(_presenter.getRadio())
					.strokeColor(Color.TRANSPARENT)
					.fillColor(0x55AA0000));//Color.BLUE
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}

}
