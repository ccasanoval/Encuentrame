package com.cesoft.encuentrame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import com.cesoft.encuentrame.models.Lugar;

////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActLugar extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status>
	//GoogleMap.OnCameraChangeListener,
{
	private static final int DELAY_LOCATION = 60000;

	private boolean _bNuevo = false;
	private Lugar _l;
	private TextView _lblPosicion;
	private EditText _txtNombre;
	private EditText _txtDescripcion;

	private GoogleApiClient _GoogleApiClient;
	private LocationRequest _LocationRequest;
	private GoogleMap _Map;
	private Marker _marker;

	CoordinatorLayout _coordinatorLayout;


	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_lugar);

		_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//------------------------------------------------------------------------------------------
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ActLugar.this.finish();
			}
		});

		//------------------------------------------------------------------------------------------
		_lblPosicion = (TextView)findViewById(R.id.lblPosicion);
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		btnActPos.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Location loc = Util.getLocation();
				if(loc != null)setPosLugar(loc);
			}
		});

		//------------------------------------------------------------------------------------------
		try
		{
			_l = this.getIntent().getParcelableExtra(Lugar.NOMBRE);
System.err.println("*************"+_l);
			setValores();
		}
		catch(Exception e)
		{
			_bNuevo = true;
			_l = new Lugar();
		}

		//------------------------------------------------------------------------------------------
		if(_bNuevo)
			setTitle(getString(R.string.nuevo_lugar));
		else
			setTitle(getString(R.string.editar_lugar));
		//------------------------------------------------------------------------------------------

		//------------------------------------------------------------------------------------------
		_GoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		_GoogleApiClient.connect();
		_LocationRequest = new LocationRequest();
		_LocationRequest.setInterval(DELAY_LOCATION);
		_LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		//mLocationRequestBalancedPowerAccuracy  || LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
		pideGPS();
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
		/*Action viewAction = Action.newAction(Action.TYPE_VIEW, // choose an action type.
				"ActLugar Page", // Define a title for the content shown.
				// If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct. Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.cesoft.encuentrame/http/host/path"));
		AppIndex.AppIndexApi.start(_GoogleApiClient, viewAction);*/
	}
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
			//if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)return;
			try
			{
				LocationServices.FusedLocationApi.requestLocationUpdates(_GoogleApiClient, _LocationRequest, this);
			}catch(SecurityException se){}
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
		return super.onOptionsItemSelected(item);
	}

	//______________________________________________________________________________________________
	private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format("%.5f/%.5f", lat, lon));}
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
			System.err.println("ActLugar:checkPlayServices:e:" + result);
			//Snackbar.make(_coordinatorLayout, "Play Services Error:"+result, Snackbar.LENGTH_LONG).show();
	        return false;
	    }
	    return true;
	}

	//______________________________________________________________________________________________
	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		System.err.println("Connection failed: ConnectionResult.getErrorCode() = "+ result.getErrorCode());
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
	private void guardar()
	{
		if(_l.getLatitud() == 0 && _l.getLongitud() == 0)
		{
			Snackbar.make(_coordinatorLayout, getString(R.string.sin_lugar), Snackbar.LENGTH_LONG).show();
			return;
		}
		if(_txtNombre.getText().toString().isEmpty())
		{
			Snackbar.make(_coordinatorLayout, getString(R.string.sin_nombre), Snackbar.LENGTH_LONG).show();
			_txtNombre.requestFocus();
			return;
		}
		_l.setNombre(_txtNombre.getText().toString());
		_l.setDescripcion(_txtDescripcion.getText().toString());
		_l.guardar(new AsyncCallback<Lugar>()
		{
			@Override
			public void handleResponse(Lugar l)
			{
				Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar), Snackbar.LENGTH_LONG).show();//TODO: no lo muestra?
				Intent data = new Intent();
				data.putExtra("dirty", true);//si es guardado, editado, borrado => refresca la vista, si no nada
				ActLugar.this.setResult(android.app.Activity.RESULT_OK, data);
				ActLugar.this.finish();
			}

			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				Snackbar.make(_coordinatorLayout, getString(R.string.error_guardar), Snackbar.LENGTH_LONG).show();
			}
		});
	}

	//______________________________________________________________________________________________
	private void eliminar()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(_l.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(getString(R.string.seguro_eliminar));
		dialog.setPositiveButton(getString(R.string.eliminar), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_l.eliminar(new AsyncCallback<Long>()
				{
					@Override
					public void handleResponse(Long lugar)
					{
						Snackbar.make(_coordinatorLayout, getString(R.string.ok_eliminar), Snackbar.LENGTH_LONG).show();
						Intent data = new Intent();
						data.putExtra("dirty", true);
						ActLugar.this.setResult(android.app.Activity.RESULT_OK, data);
						ActLugar.this.finish();
					}

					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						Snackbar.make(_coordinatorLayout, String.format(getString(R.string.error_eliminar), backendlessFault.getCode()), Snackbar.LENGTH_LONG).show();
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
		Util.setLocation(location);
		if(_l.getLatitud() == 0 && _l.getLongitud() == 0)
			setPosLugar(location);
	}

	//______________________________________________________________________________________________
	// 4 GoogleMap.OnCameraChangeListener
	//@Override public void onCameraChange(CameraPosition cameraPosition){}
	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		//if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)return;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException se){}
		_Map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng latLng)
			{
				setPosLugar(latLng.latitude, latLng.longitude);
			}
		});
		_Map.animateCamera(CameraUpdateFactory.zoomTo(15));
		if(_l.getLatitud() == 0 && _l.getLongitud() == 0)
		{
			Location loc = Util.getLocation();
			_l.setLatLon(loc.getLatitude(), loc.getLongitude());
		}
		setPosLugar(_l.getLatitud(), _l.getLongitud());
	}

	//______________________________________________________________________________________________
	//// 4 ResultCallback
	@Override
	public void onResult(@NonNull Status status)
	{
		System.err.println("----------ActLugar:onResult:"+status);
	}


	//______________________________________________________________________________________________
	private void setPosLugar(Location loc)
	{
		setPosLugar(loc.getLatitude(), loc.getLongitude());
	}
	private void setPosLugar(double lat, double lon)
	{
		_l.setLatLon(lat, lon);
		_lblPosicion.setText(String.format("%.5f/%.5f", _l.getLatitud(), _l.getLongitud()));
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
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
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
     		public void onResult(@NonNull LocationSettingsResult result)
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
