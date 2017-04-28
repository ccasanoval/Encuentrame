package com.cesoft.encuentrame3;

import android.app.ProgressDialog;
import android.os.Build;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
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
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.svc.CesService;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.models.Aviso;

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

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActAviso extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status>
{
	private static final String TAG = ActAviso.class.getSimpleName();
	private static final int DELAY_LOCATION = 60000;

	private boolean _bDesdeNotificacion = false;
	private boolean _bSucio = false;
	private boolean _bNuevo = false;
	private Aviso _a;
	private TextView _lblPosicion;
	private EditText _txtNombre;
	private EditText _txtDescripcion;
	private Switch  _swtActivo;

	private static final String[] _asRadio = {"10 m", "50 m", "100 m", "200 m", "300 m", "400 m", "500 m", "750 m", "1 Km", "2 Km", "3 Km", "4 Km", "5 Km", "7.5 Km", "10 Km"};
	private static final int[]    _adRadio = { 10,     50,     100,     200,     300,     400,     500,     750,     1000,   2000,   3000,   4000,   5000,   7500,     10000};
	private Spinner _spnRadio;

	private GoogleApiClient _GoogleApiClient;
	private LocationRequest _LocationRequest;
	private GoogleMap _Map;
	private Marker _marker;
	private Circle _circle;

	@Inject CesService _servicio;
	@Inject Util _util;

	//______________________________________________________________________________________________
	private void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(_a.getNombre());
			dialog.setMessage(getString(R.string.seguro_salir));
			dialog.setPositiveButton(getString(R.string.guardar), (dialog1, which) -> guardar());
			dialog.setCancelable(true);
			dialog.setNegativeButton(getString(R.string.salir), (dialog2, which) -> finish());
			dialog.create().show();
		}
		else
			finish();
	}
	@Override
	public void onBackPressed()
	{
		onSalir();
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
			if(_str == null && _tv.getText().length() > 0)_bSucio=true;
			if(_str != null && _tv.getText().toString().compareTo(_str) != 0)_bSucio=true;
		}
	}
	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_aviso);

		App.getComponent(getApplicationContext()).inject(this);

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
		fab.setOnClickListener(view -> onSalir());
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
				_bSucio = _a.getRadio() != _adRadio[position];
				//Log.e(TAG, "********************* ------- ---- ---- _spnRadio : "+position+"     :   "+_bSucio);
				_a.setRadio(_adRadio[position]);
				setMarker();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				_a.setRadio(100);//TODO:radio por defecto en settings
			}
		});

		//------------------------------------------------------------------------------------------
		try
		{
			_a = getIntent().getParcelableExtra(Aviso.NOMBRE);
			_bDesdeNotificacion = getIntent().getBooleanExtra("notificacion", false);
			setValores();
			_txtNombre.addTextChangedListener(new ActAviso.CesTextWatcher(_txtNombre, _a.getNombre()));
			_txtDescripcion.addTextChangedListener(new ActAviso.CesTextWatcher(_txtDescripcion, _a.getDescripcion()));
			_swtActivo.setOnCheckedChangeListener((buttonView, isChecked) -> _bSucio = isChecked != _a.isActivo());
		}
		catch(Exception e)
		{
			_bNuevo = true;
			_a = new Aviso();
			Location loc = _util.getLocation();
			if(loc != null)setPosLugar(loc);
			_txtNombre.addTextChangedListener(new ActAviso.CesTextWatcher(_txtNombre, _a.getNombre()));
			_txtDescripcion.addTextChangedListener(new ActAviso.CesTextWatcher(_txtDescripcion, _a.getDescripcion()));
		}
		//------------------------------------------------------------------------------------------
		if(_bNuevo)
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
		if(checkPlayServices())buildGoogleApiClient();
		buildLocationRequest();
		((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
		Util.pideGPS(this, _GoogleApiClient, _LocationRequest);
	}
	@Override
	public void onStop()
	{
		super.onStop();
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
		if(_bNuevo)
			menu.findItem(R.id.menu_eliminar).setVisible(false);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			eliminar();
		return super.onOptionsItemSelected(item);
	}

	//______________________________________________________________________________________________
	//______________________________________________________________________________________________
	private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}
	private void setValores()
	{
		_txtNombre.setText(_a.getNombre());
		_txtDescripcion.setText(_a.getDescripcion());
		_swtActivo.setChecked(_a.isActivo());
		setPosLabel(_a.getLatitud(), _a.getLongitud());
		for(int i=0; i < _adRadio.length; i++)
		{
			if(_a.getRadio() == _adRadio[i])
			{
				_spnRadio.setSelection(i);
				break;
			}
		}
	}


	//______________________________________________________________________________________________
	// GET BACK TO MAIN
	public void openMain(boolean bDirty, String sMensaje)
	{
		if(_bDesdeNotificacion)
			_util.openMain(ActAviso.this, bDirty, sMensaje, Constantes.AVISOS);
		else
			_util.return2Main(ActAviso.this, bDirty, sMensaje);
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
	public void iniEspera()
	{
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
	}
	public void finEspera()
	{
		if(_progressDialog!=null)_progressDialog.dismiss();
	}
	//______________________________________________________________________________________________
	private boolean _bGuardar = true;
	private synchronized void guardar()
	{
		if(!_bGuardar)return;
		_bGuardar = false;
		iniEspera();

		if(_a.getLatitud()==0 && _a.getLongitud()==0)
		{
			Toast.makeText(ActAviso.this, getString(R.string.sin_lugar), Toast.LENGTH_LONG).show();
			_bGuardar = true;
			finEspera();
			return;
		}
		if(_txtNombre.getText().toString().isEmpty())
		{
			Toast.makeText(ActAviso.this, getString(R.string.sin_nombre), Toast.LENGTH_LONG).show();
			_txtNombre.requestFocus();
			_bGuardar = true;
			finEspera();
			return;
		}
		_a.setNombre(_txtNombre.getText().toString());
		_a.setDescripcion(_txtDescripcion.getText().toString());
		_a.setActivo(_swtActivo.isChecked());
		//_a.reactivarPorHoy();
		//_a.setLugar(new GeoPoint(_loc.getLatitude(), _loc.getLongitude()), _radio);
		_a.guardar(new Fire.CompletadoListener()
		{
			@Override
			protected void onDatos(String id)
			{
				_servicio.cargarListaGeoAvisos();//System.err.println("ActAviso:guardar:handleResponse:" + a);
				openMain(true, getString(R.string.ok_guardar_aviso));//return2Main(true, getString(R.string.ok_guardar));
				_bGuardar = true;
				finEspera();
			}
			@Override
			protected void onError(String err, int code)
			{
				Log.e(TAG, "guardar:handleFault:f:" + err);

				//*****************************************************************************
				try{Thread.sleep(500);}catch(InterruptedException ignored){}
				_a.guardar(new Fire.CompletadoListener()
				{
					@Override
					protected void onDatos(String id)
					{
						finEspera();
						_bGuardar = true;
						_servicio.cargarListaGeoAvisos();
						openMain(true, getString(R.string.ok_guardar_aviso));
					}
					@Override
					protected void onError(String err, int code)
					{
						finEspera();
						_bGuardar = true;
						Log.e(TAG, "guardar:handleFault2:f:" + err);
						Toast.makeText(ActAviso.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}

	//______________________________________________________________________________________________
	private boolean _bEliminar = true;
	private synchronized void eliminar()
	{
		if(!_bEliminar)return;
		_bEliminar=false;
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(_a.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(getString(R.string.seguro_eliminar));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			dialog.setOnDismissListener(dlg -> _bEliminar = true);
		}
		dialog.setNegativeButton(getString(R.string.cancelar), (dlg, which) -> _bEliminar = true);
		dialog.setPositiveButton(getString(R.string.eliminar), (dlg, which) ->
		{
			iniEspera();
			_a.eliminar(new Fire.CompletadoListener() {
				@Override
				protected void onDatos(String id)
				{
					finEspera();
					_bEliminar=true;
					openMain(true, getString(R.string.ok_eliminar_aviso));
				}
				@Override
				protected void onError(String err, int code)
				{
					finEspera();
					_bEliminar=true;
					Log.e(TAG, String.format("eliminar:handleFault:f:%s",err));
					Toast.makeText(ActAviso.this, String.format(getString(R.string.error_eliminar), err), Toast.LENGTH_LONG).show();
				}
			});
		});
		dialog.create().show();
	}


	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override
	public void onLocationChanged(Location location)
	{
		_util.setLocation(location);
		if(_a.getLatitud() == 0 && _a.getLongitud() == 0)
			setPosLugar(location);
	}

	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException se){Log.e(TAG, "onMapReady:e:"+se, se);}
		_Map.setOnMapClickListener(latLng ->
		{
			_bSucio = true;
			setPosLugar(latLng.latitude, latLng.longitude);
		});
		_Map.setOnCameraMoveListener(() ->	{ if(_Map!=null)_fMapZoom = _Map.getCameraPosition().zoom; });
		//_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		setPosLugar(_a.getLatitud(), _a.getLongitud());
	}

	//______________________________________________________________________________________________
	//// 4 ResultCallback
	@Override
	public void onResult(@NonNull Status status){}

	//______________________________________________________________________________________________
	private void setPosLugar(Location loc)
	{
		setPosLugar(loc.getLatitude(), loc.getLongitude());
	}
	private void setPosLugar(double lat, double lon)
	{
		_a.setLatitud(lat);_a.setLongitud(lon);
		_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", _a.getLatitud(), _a.getLongitud()));
		setMarker();
	}
	private void setMarker()
	{
		if(_Map == null)return;
		try
		{
			if(_marker != null)_marker.remove();
			LatLng pos = new LatLng(_a.getLatitud(), _a.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(_a.getNombre()).snippet(_a.getDescripcion());
			_marker = _Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));

			if(_circle != null)_circle.remove();
			_circle = _Map.addCircle(new CircleOptions()
					.center(pos)
					.radius(_a.getRadio())
					.strokeColor(Color.TRANSPARENT)
					.fillColor(0x55AA0000));//Color.BLUE
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}

	/*//______________________________________________________________________________________________
	private void pideGPS()//TODO: standarizar?
	{
		//https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(_LocationRequest)
				.setAlwaysShow(true)//so it ask for GPS activation like google maps
				//.addLocationRequest()
				;
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(_GoogleApiClient, builder.build());
		result.setResultCallback(res ->
		{
			final Status status = res.getStatus();
			//final LocationSettingsStates le = result.getLocationSettingsStates();
			switch(status.getStatusCode())
			{
			case LocationSettingsStatusCodes.SUCCESS:
				Log.w(TAG, "LocationSettingsStatusCodes.SUCCESS");
				// All location settings are satisfied. The client can initialize location requests here.
				break;
			case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
				try
				{
					status.startResolutionForResult(ActAviso.this, 1000);
				}
				catch(android.content.IntentSender.SendIntentException e){Log.e(TAG, String.format("LocationSettingsStatusCodes.RESOLUTION_REQUIRED:e:%s",e), e);}
				break;
			case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
				Log.e(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
				// Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
				break;
			}
		});
	}*/
}
