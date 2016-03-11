package com.cesoft.encuentrame;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.models.Lugar;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.NotNull;

////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActLugar extends AppCompatActivity implements GoogleMap.OnCameraChangeListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status>
{
	private static final int DELAY_LOCATION = 60000;
	private static final int ACC_MAPA = 1;

	private boolean _bNuevo = false;
	private Lugar _l;
	private TextView _lblPosicion;
	private EditText _txtNombre;
	private EditText _txtDescripcion;
	private GoogleApiClient _GoogleApiClient;

	private Location _loc, _locLast;
	private LocationRequest _LocationRequest;
	private GoogleMap _Map;
	private Marker _marker;


	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_lugar);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//------------------------------------------------------------------------------------------
		_GoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		_GoogleApiClient.connect();
		_LocationRequest = new LocationRequest();
		_LocationRequest.setInterval(DELAY_LOCATION);
		_LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		//mLocationRequestBalancedPowerAccuracy  || LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
		pideGPS();
		//------------------------------------------------------------------------------------------

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ActLugar.this.finish();
			}
		});

		//-----------
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		//_GoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

		//-----------
		_lblPosicion = (TextView) findViewById(R.id.lblPosicion);
		_txtNombre = (EditText) findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText) findViewById(R.id.txtDescripcion);

		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		btnActPos.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(_locLast != null)
					setPosLugar(_locLast);
			}
		});

		//------------------------------------------------------------------------------------------
		try
		{
			_l = this.getIntent().getParcelableExtra(Lugar.NOMBRE);
System.err.println("ActLugar:onCreate:++++++++++++++++"+_l);
			setValores();
		}
		catch(Exception e)
		{
			System.err.println("ActLugar:onCreate:e:"+e);
			_bNuevo = true;
			_l = new Lugar();
		}
		//------------------------------------------------------------------------------------------

		//------------------------------------------------------------------------------------------
		if(_bNuevo)
			setTitle(getString(R.string.nuevo_lugar));
		else
			setTitle(getString(R.string.editar_lugar));
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
			if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)return;
				LocationServices.FusedLocationApi.requestLocationUpdates(_GoogleApiClient, _LocationRequest, this);
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
		{
			guardar();
			System.err.println("R.id.menu_guardar");
		}
		else if(item.getItemId() == R.id.menu_eliminar)
		{
			eliminar();
			System.err.println("R.id.menu_eliminar");
		}
		///saveValores();
		return super.onOptionsItemSelected(item);
	}

	//______________________________________________________________________________________________
	private void setPosLabel(String s){_lblPosicion.setText(s);}
	private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format("%.5f/%.5f", lat, lon));}
	private void setValores()
	{
		_txtNombre.setText(_l.getNombre());
		_txtDescripcion.setText(_l.getDescripcion());
		if(_l.getLugar() != null)
		{
			if(_loc == null)_loc = new Location("dummyprovider");
			_loc.setLatitude(_l.getLugar().getLatitude());
			_loc.setLongitude(_l.getLugar().getLongitude());
			setPosLabel(_l.getLugar().getLatitude(), _l.getLugar().getLongitude());
System.err.println("INI----------"+_l.getLugar().getLatitude()+"/"+_l.getLugar().getLongitude());
		}
	}

	//______________________________________________________________________________________________
	@Override
	public void onStart()
	{
		super.onStart();
		if(checkPlayServices())buildGoogleApiClient();
		if(_GoogleApiClient != null)_GoogleApiClient.connect();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		/*Action viewAction = Action.newAction(Action.TYPE_VIEW, // TODO: choose an action type.
				"ActLugar Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct. Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.cesoft.encuentrame/http/host/path"));
		AppIndex.AppIndexApi.start(_GoogleApiClient, viewAction);*/
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
			System.err.println("ActLugar:checkPlayServices:e:"+result);
			//Snackbar.make(null, R.string.eliminar, Snackbar.LENGTH_LONG).setAction("Action", null).show();
	        return false;
	    }
	    return true;
	}
	/*private void getAndDisplayLocation()
	{
		if(_GoogleApiClient == null)return;
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)return;
		_locLast = LocationServices.FusedLocationApi.getLastLocation(_GoogleApiClient);
		if(_locLast != null)
			setPosLabel(_locLast.getLatitude(), _locLast.getLongitude());
		else
			setPosLabel(getString(R.string.sin_posicion));
	}*/



	//______________________________________________________________________________________________
	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		//this.result = result;
		//Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "+ result.getErrorCode());
		System.err.println("Connection failed: ConnectionResult.getErrorCode() = "+ result.getErrorCode());
	}
	@Override
	public void onConnected(Bundle arg0)
	{
		//displayLocation();
	}
	@Override
	public void onConnectionSuspended(int arg0)
	{
		if(_GoogleApiClient != null)
			_GoogleApiClient.connect();
	}


	//______________________________________________________________________________________________
	private void guardar()
	{
		if(_loc == null)
		{
			Snackbar.make(null, "El lugar no tiene posición...", Snackbar.LENGTH_LONG).show();
			return;
		}
		if(_txtNombre.getText().toString().isEmpty())
		{
			Snackbar.make(null, "El lugar no tienen nombre...", Snackbar.LENGTH_LONG).show();
			_txtNombre.requestFocus();
			return;
		}
		_l.setNombre(_txtNombre.getText().toString());
		_l.setDescripcion(_txtDescripcion.getText().toString());
		_l.setLugar(new GeoPoint(_loc.getLatitude(), _loc.getLongitude()));

		_l.guardar(new AsyncCallback<Lugar>()
		{
			@Override
			public void handleResponse(Lugar l)
			{
				System.out.println(l.getObjectId());
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
			}
		});
System.err.println("INI----------" + _l.getLugar().getLatitude() + "/" + _l.getLugar().getLongitude());
		finish();
	}

	//______________________________________________________________________________________________
	private void eliminar()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(getString(R.string.eliminar));
		dialog.setMessage(getString(R.string.seguro_eliminar));
		dialog.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_l.eliminar(new AsyncCallback<Long>()
				{
					@Override
					public void handleResponse(Long lugar)
					{
						Snackbar.make(null, R.string.eliminar, Snackbar.LENGTH_LONG).show();
						ActLugar.this.finish();
					}
					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						// an error has occurred, the error code can be retrieved with
						Snackbar.make(null, "Error:" + backendlessFault.getCode(), Snackbar.LENGTH_LONG).show();
					}
				});
			}
		});
		/*dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which){}
		});*/
		dialog.create().show();
	}




	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override
	public void onLocationChanged(Location location)
	{
		_locLast = location;
		if(_loc.getLatitude() == 0 && _loc.getLongitude() == 0)
			setPosLugar(_locLast);
	}

	//______________________________________________________________________________________________
	// 4 GoogleMap.OnCameraChangeListener
	@Override
	public void onCameraChange(CameraPosition cameraPosition)
	{
		//if(_l != null && (_l.getLugar().getLatitude() != 0 || _l.getLugar().getLongitude() != 0))
	}
	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		if(_l != null && (_l.getLugar().getLatitude() != 0 || _l.getLugar().getLongitude() != 0))
		{
			LatLng latLng = new LatLng(_l.getLugar().getLatitude(), _l.getLugar().getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
			_Map.animateCamera(cameraUpdate);
		}
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)return;
		_Map.setMyLocationEnabled(true);
		_Map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng latLng)
			{
				setPosLugar(latLng.latitude, latLng.longitude);
			}
		});
		setPosLugar(_l.getLugar().getLatitude(), _l.getLugar().getLongitude());
	}

	//______________________________________________________________________________________________
	//// 4 ResultCallback
	@Override
	public void onResult(@NonNull Status status)
	{
	}


	//______________________________________________________________________________________________
	private void setPosLugar(Location loc)
	{
		setPosLugar(loc.getLatitude(), loc.getLongitude());
	}
	private void setPosLugar(double lat, double lon)
	{
		if(_loc == null)_loc = new Location("dummyprovider");
		_loc.setLatitude(lat);
		_loc.setLongitude(lon);
		_lblPosicion.setText(String.format("%.5f/%.5f", _loc.getLatitude(), _loc.getLongitude()));
		setMarker();
	}
	private void setMarker()
	{
		try
		{
			if(_marker != null)_marker.remove();
			LatLng pos = new LatLng(_loc.getLatitude(), _loc.getLongitude());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(_l.getNombre())//getString(R.string.aviso)
					.snippet(_l.getDescripcion());
			_marker = _Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			_Map.animateCamera(CameraUpdateFactory.zoomTo(15));
		}
		catch(Exception e){System.err.println("ActLugar:setMarker:e:"+e);}
	}

	//______________________________________________________________________________________________
	private void pideGPS()
	{
		//https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(_LocationRequest)
				//.addLocationRequest()
				;
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(_GoogleApiClient, builder.build());

		result.setResultCallback(new ResultCallback<LocationSettingsResult>()
		{
     		@Override
     		public void onResult(@NotNull LocationSettingsResult result)
			{
         		final Status status = result.getStatus();
         		final LocationSettingsStates le = result.getLocationSettingsStates();
         		switch(status.getStatusCode())
				{
             	case LocationSettingsStatusCodes.SUCCESS:
					System.err.println("LocationSettingsStatusCodes.SUCCESS");
					// All location settings are satisfied. The client can initialize location requests here.
					break;
				case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
					System.err.println("LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
					// Location settings are not satisfied. But could be fixed by showing the user a dialog.
					/*try
					{
						// Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
						//status.startResolutionForResult(OuterClass.this, REQUEST_CHECK_SETTINGS);
					}
					catch(IntentSender.SendIntentException e){}*/
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
					System.err.println("LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
					// Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
					break;
				}
			}
		});
	}
}
