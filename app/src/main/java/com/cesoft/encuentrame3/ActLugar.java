package com.cesoft.encuentrame3;

import java.util.Locale;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Fire;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActLugar extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status>
{
	private static final String TAG = ActLugar.class.getSimpleName();
	private static final int DELAY_LOCATION = 60000;

	//@Inject
	private Util _util;

	private boolean _bSucio = false;
	private boolean _bNuevo = false;
	private Lugar _l = new Lugar();
	private TextView _lblPosicion;
	private EditText _txtNombre;
	private EditText _txtDescripcion;

	private GoogleApiClient _GoogleApiClient;
	private LocationRequest _LocationRequest;
	private GoogleMap _Map;
	private Marker _marker;
	//CoordinatorLayout _coordinatorLayout;
	private String _imgURLnew =null;


	//______________________________________________________________________________________________
	private void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(_l.getNombre());
			dialog.setMessage(getString(R.string.seguro_salir));
			dialog.setPositiveButton(getString(R.string.guardar), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which) { guardar(); }
			});
			dialog.setCancelable(true);
			dialog.setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which) { ActLugar.this.finish(); }
			});
			dialog.create().show();
		}
		else
		ActLugar.this.finish();
	}
	@Override
	public void onBackPressed()
	{
		onSalir();
		//super.onBackPressed();
	}
	private class CesTextWatcher implements TextWatcher
	{
		//TODO: private boolean _bSucio;...
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
		setContentView(R.layout.act_lugar);

		_util = ((App)getApplication()).getGlobalComponent().util();
		//_util = DaggerGlobalComponent.create();
		//((App)getApplication()).getGlobalComponent().inject(this);

		//------------------------------------------------------------------------------------------
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View view) { onSalir(); }
		});

		//------------------------------------------------------------------------------------------
		_lblPosicion = (TextView)findViewById(R.id.lblPosicion);
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		if(btnActPos != null)
		btnActPos.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Location loc = _util.getLocation();
				if(loc != null)setPosLugar(loc);
			}
		});

		//------------------------------------------------------------------------------------------
		try
		{
			_l = getIntent().getParcelableExtra(Lugar.NOMBRE);
			setValores();
			_txtNombre.addTextChangedListener(new CesTextWatcher(_txtNombre, _l.getNombre()));
			_txtDescripcion.addTextChangedListener(new CesTextWatcher(_txtDescripcion, _l.getDescripcion()));
		}
		catch(Exception e)
		{
			_bNuevo = true;
			_l = new Lugar();
			_txtNombre.addTextChangedListener(new CesTextWatcher(_txtNombre, null));
			_txtDescripcion.addTextChangedListener(new CesTextWatcher(_txtDescripcion, null));
		}

		//------------------------------------------------------------------------------------------
		if(_bNuevo)
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
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		if(checkPlayServices())buildGoogleApiClient();
		if(_GoogleApiClient != null)
		{
			_GoogleApiClient.connect();
			pideGPS();
		}
		_LocationRequest = new LocationRequest();
		_LocationRequest.setInterval(DELAY_LOCATION);
		_LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		Log.e(TAG, "-------------------- ON START");
	}
	@Override
	public void onStop()
	{
		super.onStop();
		if(_Map != null)
		{
			_Map.clear();
			_Map = null;
		}
		if(_GoogleApiClient != null)
		{
			_GoogleApiClient.unregisterConnectionCallbacks(this);
			_GoogleApiClient.unregisterConnectionFailedListener(this);
			_GoogleApiClient.disconnect();
			_GoogleApiClient = null;
		}
		_LocationRequest = null;
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
		else if(item.getItemId() == R.id.menu_img)
			imagen();
		return super.onOptionsItemSelected(item);
	}

	//______________________________________________________________________________________________
	private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}
	private void setValores()
	{
		_txtNombre.setText(_l.getNombre());
		_txtDescripcion.setText(_l.getDescripcion());
		setPosLabel(_l.getLatitud(), _l.getLongitud());
	}


	//______________________________________________________________________________________________
	protected synchronized void buildGoogleApiClient()
	{
		_GoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
	}
	private boolean checkPlayServices()
	{
    	GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
    	int result = googleAPI.isGooglePlayServicesAvailable(this);
    	if(result != ConnectionResult.SUCCESS)
		{
			/*int PLAY_SERVICES_RESOLUTION_REQUEST = 6969;
        	if(googleAPI.isUserResolvableError(result))googleAPI.getErrorDialog(this.getParent(), result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        	*/
			Log.e(TAG, "checkPlayServices:e:" + result);
	        return false;
	    }
	    return true;
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

		if(_l.getLatitud() == 0 && _l.getLongitud() == 0)
		{
			//O escondes el teclado o el snackbar no se ve.....
			//Snackbar.make(_coordinatorLayout, getString(R.string.sin_lugar), Snackbar.LENGTH_LONG).show();
			Toast.makeText(this, getString(R.string.sin_lugar), Toast.LENGTH_LONG).show();
			_bGuardar = true;
			finEspera();
			return;
		}
		if(_txtNombre.getText().toString().isEmpty())
		{
			Toast.makeText(this, getString(R.string.sin_nombre), Toast.LENGTH_LONG).show();
			_txtNombre.requestFocus();
			_bGuardar = true;
			finEspera();
			return;
		}
		_l.setNombre(_txtNombre.getText().toString());
		_l.setDescripcion(_txtDescripcion.getText().toString());
		//if(_imgURLnew != null)_l.setImagen(_imgURLnew);
		_l.guardar(new Fire.CompletadoListener() {
			@Override
			protected void onDatos(String id)
			{
				Log.e(TAG, "guardar--------------------------------"+ _imgURLnew);
				if(_imgURLnew != null)_l.uploadImg(_imgURLnew);

				_util.return2Main(ActLugar.this, true, getString(R.string.ok_guardar_lugar));
				_bGuardar = true;
				finEspera();
			}
			@Override
			protected void onError(String err, int code)
			{
				finEspera();
				_bGuardar = true;
				Log.e(TAG, String.format("guardar:handleFault:f:%s",err));
				Toast.makeText(ActLugar.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
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
		dialog.setTitle(_l.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(getString(R.string.seguro_eliminar));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
		{
			dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
			{
				@Override public void onDismiss(DialogInterface dialog){_bEliminar = true;}
			});
		}
		dialog.setNegativeButton(getString(R.string.cancelar), new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which){_bEliminar = true;}
		});
		dialog.setPositiveButton(getString(R.string.eliminar), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				iniEspera();
				_l.eliminar(new Fire.CompletadoListener()
				{
					@Override
					protected void onDatos(String id)
					{
						finEspera();
						_bEliminar = true;
						_util.return2Main(ActLugar.this, true, getString(R.string.ok_eliminar_lugar));
					}
					@Override
					protected void onError(String err, int code)
					{
						finEspera();
						_bEliminar = true;
						Log.e(TAG, String.format("eliminar:handleFault:f:%s",err));
						Toast.makeText(ActLugar.this, String.format(getString(R.string.error_eliminar), err), Toast.LENGTH_LONG).show();
					}
				});
			}
		});
		dialog.create().show();
	}


	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override
	public void onLocationChanged(Location location)
	{
		_util.setLocation(location);
		if(_l.getLatitud() == 0 && _l.getLongitud() == 0)
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
		//TODO:
		//https://developers.google.com/maps/documentation/android-api/map?hl=es-419
		//_Map.setMapType(GoogleMap.MAP_TYPE_NORMAL y GoogleMap.MAP_TYPE_SATELLITE
		_Map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng latLng)
			{
				_bSucio = true;
				setPosLugar(latLng.latitude, latLng.longitude);
			}
		});
		_Map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener()
		{
			 @Override public void onCameraMove() { _fMapZoom = _Map.getCameraPosition().zoom; }
		});
		_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		if(_l.getLatitud() == 0 && _l.getLongitud() == 0)
		{
			Location loc = _util.getLocation();
			if(loc != null)
			{
				_l.setLatitud(loc.getLatitude());
				_l.setLongitud(loc.getLongitude());
			}
		}
		setPosLugar(_l.getLatitud(), _l.getLongitud());
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
		_l.setLatitud(lat);
		_l.setLongitud(lon);
		_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", _l.getLatitud(), _l.getLongitud()));
		setMarker();
	}
	private void setMarker()
	{
		try
		{
			if(_marker != null)_marker.remove();
			LatLng pos = new LatLng(_l.getLatitud(), _l.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(_l.getNombre())//getString(R.string.aviso)
					.snippet(_l.getDescripcion());
			_marker = _Map.addMarker(mo);
			//_Map.animateCamera(CameraUpdateFactory.zoomTo(15));
			//_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));
		}
		catch(Exception e){Log.e(TAG, String.format("setMarker:e:%s",e), e);}
	}

	//______________________________________________________________________________________________
	private void pideGPS()
	{
		//https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(_LocationRequest)
				.setAlwaysShow(true)//so it ask for GPS activation like google maps
				//.addLocationRequest()
				;
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(_GoogleApiClient, builder.build());

		result.setResultCallback(new ResultCallback<LocationSettingsResult>()
		{
     		@Override
     		public void onResult(@NonNull LocationSettingsResult result)
			{
         		final Status status = result.getStatus();
         		//final LocationSettingsStates le = result.getLocationSettingsStates();
         		switch(status.getStatusCode())
				{
             	case LocationSettingsStatusCodes.SUCCESS:
					Log.w(TAG, "LocationSettingsStatusCodes.SUCCESS");
					// All location settings are satisfied. The client can initialize location requests here.
					break;
				case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
					try{status.startResolutionForResult(ActLugar.this, 1000);}
					catch(android.content.IntentSender.SendIntentException e){Log.e(TAG, String.format("RESOLUTION_REQUIRED:e:%s",e), e);}
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
					Log.e(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
					// Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
					break;
				}
			}
		});
	}

	//TODO: avisar a usuario de que no hay gps?????????????????????????!!!!!!!!!!!!!!!
	//TODO: añadir altura, velocidad, etc en punto guardado y en aviso?
	//______________________________________________________________________________________________
	private void imagen()
	{
		Intent i = new Intent(this, ActImagen.class);
Log.e(TAG, "onActivityResult-----------------LUGAR---2-------- "+ _imgURLnew);
		i.putExtra(ActImagen.PARAM_IMG_PATH, _imgURLnew);
		i.putExtra(ActImagen.PARAM_LUGAR, _l);
		startActivityForResult(i, ActImagen.IMAGE_CAPTURE);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.e(TAG, "onActivityResult-------------"+requestCode+" --------------- "+resultCode);
		if(requestCode == ActImagen.IMAGE_CAPTURE && resultCode == RESULT_OK)
		{
			_imgURLnew = data.getStringExtra(ActImagen.PARAM_IMG_PATH);
			_bSucio = true;
			Log.e(TAG, "onActivityResult-----------------LUGAR----------- "+ _imgURLnew);
		}
		else
			Log.e(TAG, "onActivityResult-----------------LUGAR ERROR----------- ");
	}

}
