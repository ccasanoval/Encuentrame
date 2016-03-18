package com.cesoft.encuentrame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.backendless.Backendless;
import com.cesoft.encuentrame.models.RutaPto;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;

import com.cesoft.encuentrame.models.Ruta;

//TODO: Cuando es nuevo borrar boton menu guardar... porque lo unico que toca es grabar nueva ruta...
//TODO: por que no refresca lista de rutas cuando empieza a grabar...
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActRuta extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
//, ResultCallback<Status>, GoogleMap.OnCameraChangeListener,
{
	private static final int DELAY_LOCATION = 60000;

	private boolean _bNuevo = false;
	private Ruta _r;
	private EditText _txtNombre;
	private EditText _txtDescripcion;
	private Spinner _spnTrackingDelay;
	private String[] _asDelay = {"2 min", "5 min", "10 min", "15 min", "20 min", "25 min", "30 min", "45 min", "1 h", "2 h", "3 h", "4 h", "5 h", "6 h", "12 h"};
	private int[]    _aiDelay = { 2,       5,       10,       15,       20,       25,       30,       45,       60,    2*60,  3*60,  4*60,  5*60,  6*60,  12*60 };//*60*1000

	private GoogleMap _Map;
	private LocationRequest _LocationRequest;
	private GoogleApiClient _GoogleApiClient;

	CoordinatorLayout _coordinatorLayout;


	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_ruta);

		_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//------------------------------------------------------------------------------------------
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		_spnTrackingDelay = (Spinner)findViewById(R.id.spnTrackingDelay);
//TODO: si se hace tracking mediante geofence no necesito esto... cambiarlo por radio de feofence...
((LinearLayout)findViewById(R.id.layPeriodo)).setVisibility(View.GONE);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, _asDelay);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spnTrackingDelay.setAdapter(adapter);
		_spnTrackingDelay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				_r.setPeriodo(_aiDelay[position] * 60 * 1000);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				_r.setPeriodo(2 * 60 * 1000);//TODO:radio por defecto en settings
			}
		});
		//-----------
		ImageButton btnStart = (ImageButton)findViewById(R.id.btnStart);
		btnStart.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//TODO: Save object and start background gps tracking proces
				//TODO: Show stratus in list (recording...)
				//http://mobisoftinfotech.com/resources/blog/android/3-ways-to-implement-efficient-location-tracking-in-android-applications/
				startTrackingRecord();
			}
		});
		ImageButton btnStop = (ImageButton)findViewById(R.id.btnStop);
		btnStop.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//TODO: Stop process...
				stopTrackingRecord();
			}
		});
		//-----------
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ActRuta.this.finish();
			}
		});

		//------------------------------------------------------------------------------------------
		try
		{
			_r = this.getIntent().getParcelableExtra(Ruta.NOMBRE);
System.err.println("ActRuta:onCreate:++++++++++++++++"+_r);
			setValores();
		}
		catch(Exception e)
		{
			_bNuevo = true;
			_r = new Ruta();
		}
		//------------------------------------------------------------------------------------------
		if(_bNuevo)
		{
			setTitle(getString(R.string.nueva_ruta));
			btnStop.setVisibility(View.GONE);
			//findViewById(R.id.layStartStop).setVisibility(View.VISIBLE);
			try
			{
				FragmentTransaction ft = mapFragment.getFragmentManager().beginTransaction();
				ft.hide(mapFragment);
				ft.commit();
			}
			catch(Exception e){e.printStackTrace();}
		}
		else
		{
			setTitle(getString(R.string.editar_ruta));
			findViewById(R.id.layPeriodo).setVisibility(View.GONE);
			//si está activo muestra btnStop
			String sId = Util.getTrackingRoute();
			if( ! sId.equals(_r.getObjectId()))
				findViewById(R.id.layStartStop).setVisibility(View.GONE);
			else
				_txtNombre.setTextColor(Color.RED);
		}

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
	/*private void return2Main(boolean bDirty, String sMensaje)
	{
		/*Intent data = new Intent();
		data.putExtra("dirty", bDirty);
		data.putExtra("mensaje", sMensaje);
		setResult(android.app.Activity.RESULT_OK, data);
		finish();
		Util.return2Main(this, bDirty, sMensaje);
	}*/

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
			catch(SecurityException e)
			{
				System.err.println("ActRuta:startTracking:e:"+e);
				Snackbar.make(_coordinatorLayout, "ActRuta:startTracking:e:"+e, Snackbar.LENGTH_LONG).show();
			}
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
		{
			menu.findItem(R.id.menu_guardar).setVisible(false);
			menu.findItem(R.id.menu_eliminar).setVisible(false);
		}
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
	private void setValores()
	{
		_txtNombre.setText(_r.getNombre());
		_txtDescripcion.setText(_r.getDescripcion());
		for(int i=0; i < _aiDelay.length; i++)
		{
			if(_r.getPeriodo() == _aiDelay[i])
			{
				_spnTrackingDelay.setSelection(i);
				break;
			}
		}
	}

	//______________________________________________________________________________________________
	@Override
	public void onStart()
	{
		super.onStart();
		if(checkPlayServices())buildGoogleApiClient();
		if(_GoogleApiClient != null)_GoogleApiClient.connect();
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
			System.err.println("ActRuta:checkPlayServices:e:" + result);
	        return false;
	    }
	    return true;
	}

	//______________________________________________________________________________________________
	//// 4 OnConnectionFailedListener
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		System.err.println("ActRuta:onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
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
	private void guardar()
	{
		guardar(
			new AsyncCallback<Ruta>()
			{
				@Override
				public void handleResponse(Ruta r)
				{
					System.err.println("ActRuta:guardar:" + r);
					//return2Main(true, getString(R.string.ok_guardar));
					Util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar));
				}
				@Override
				public void handleFault(BackendlessFault backendlessFault)
				{
					System.err.println("ActRuta:guardar:handleFault:f:" + backendlessFault);
					Snackbar.make(_coordinatorLayout, String.format(getString(R.string.error_guardar), backendlessFault), Snackbar.LENGTH_LONG).show();
				}
			});
	}
	private void guardar(AsyncCallback<Ruta> res)
	{
		if(_txtNombre.getText().toString().isEmpty())
		{
			Snackbar.make(_coordinatorLayout, getString(R.string.sin_nombre), Snackbar.LENGTH_LONG).show();
			_txtNombre.requestFocus();
			return;
		}
		_r.setNombre(_txtNombre.getText().toString());
		_r.setDescripcion(_txtDescripcion.getText().toString());
		_r.guardar(res);
	}

	//______________________________________________________________________________________________
	private void eliminar()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(_r.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(getString(R.string.seguro_eliminar));
		dialog.setPositiveButton(getString(R.string.eliminar), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_r.eliminar(new AsyncCallback<Long>()
				{
					@Override
					public void handleResponse(Long ruta)
					{
						Util.return2Main(ActRuta.this, true, getString(R.string.ok_eliminar));
						//ActRuta.this.runOnUiThread(new Runnable(){public void run(){Snackbar.make(_coordinatorLayout, , Snackbar.LENGTH_LONG).show();}});
					}
					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						System.err.println("ActRuta:eliminar:handleFault:f:" + backendlessFault);
						//ActRuta.this.runOnUiThread(new Runnable(){public void run(){
						Snackbar.make(_coordinatorLayout, String.format(getString(R.string.error_eliminar), backendlessFault), Snackbar.LENGTH_LONG).show();
						//}});
					}
				});
			}
		});
		dialog.create().show();
	}


	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override public void onLocationChanged(Location location)
	{
		Util.setLocation(location);
	}
	//______________________________________________________________________________________________
	//// 4 ResultCallback
	/*@Override public void onResult(@NonNull Status status)
	{
		System.err.println("----------ActRuta:onResult:"+status);
	}*/

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

		if(_bNuevo)
		{
			_Map.moveCamera(CameraUpdateFactory.zoomTo(15));
		}
		else if(_r.getPuntos().size() > 0)
		{
			showMarkers();
			GeoPoint pos = _r.getPuntos().get(0);
			LatLng pos2 = new LatLng(pos.getLatitude(), pos.getLongitude());
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos2, 15));
		}
	}
	private void showMarkers()
	{
		PolylineOptions po = new PolylineOptions();
		for(GeoPoint pt : _r.getPuntos())
		{
			LatLng pos = new LatLng(pt.getLatitude(), pt.getLongitude());
System.err.println("showMarkers: "+pos);
			_Map.addMarker(new MarkerOptions().position(pos));//.title("")//TODO: Show INI, FIN, y add fecha to each point
			po.add(pos);
		}
		po.width(5).color(Color.RED);
		Polyline line = _Map.addPolyline(po);
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


	//______________________________________________________________________________________________
	private void startTrackingRecord()
	{
		guardar(new AsyncCallback<Ruta>()
		{
			@Override
			public void handleResponse(Ruta r)
			{
				/// Si hay una ruta activa, se cierra ¿Avisar?
				//TODO: Guardar varias rutas al mismo tiempo ???
				String sId = Util.getTrackingRoute();
				if( ! sId.isEmpty())//No hace falta borrar punto anterior ...
				{
/*System.err.println("ActRuta:startTrackingRecord:Borrar ruta antigua: getTrackingRoute="+sId);
					RutaPto.getTrackingPto(new AsyncCallback<RutaPto>()
					{
						@Override public void handleResponse(RutaPto rutaPto)
						{
							rutaPto.removeTrackingPto(new AsyncCallback<Long>()
							{
								@Override public void handleResponse(Long l)
								{
									System.err.println("ActRuta:startTrackingRecord:RutaPto.removeTrackingPto:OK:"+l);
								}
								@Override public void handleFault(BackendlessFault backendlessFault)
								{
									System.err.println("ActRuta:startTrackingRecord:RutaPto.removeTrackingPto:f:"+backendlessFault);
								}
							});
						}
						@Override public void handleFault(BackendlessFault backendlessFault)
						{
							System.err.println("ActRuta:startTrackingRecord:RutaPto.getTrackingPto:f:"+backendlessFault);
						}
					});
					/*try{
						GeoPoint geoPoint = Backendless.Persistence.of(GeoPoint.class).findFirst();
						Backendless.Persistence.of(GeoPoint.class).remove(geoPoint);
					}catch(Exception e){System.err.println("ActRuta:startTrackingRecord:Borrar geo antigua:e:"+e);}*/
				}
				/// Activar tracking, guardar ruta activa
				Util.setTrackingRoute(r.getObjectId());
				/// Obtener posicion y guardar primer punto
				Location loc = Util.getLocation();
				r.addPunto(new GeoPoint(loc.getLatitude(), loc.getLongitude()));//TODO: Add date...
				r.guardar(new AsyncCallback<Ruta>()
				{
					@Override
					public void handleResponse(Ruta ruta)
					{
						System.err.println("ActRuta:startTrackingRecord:Guardar ruta:"+ruta);
					}
					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						System.err.println("ActRuta:startTrackingRecord:Guardar ruta:handleFault:"+backendlessFault);
					}
				});
System.err.println("ActRuta:startTrackingRecord-----------6:" + r);

				/// Crear geofence con pos actual
				RutaPto rp = new RutaPto();
				rp.setIdRuta(r.getObjectId());
				rp.setLatLon(loc.getLatitude(), loc.getLongitude());
				rp.saveTrackingPto(new AsyncCallback<RutaPto>()
				{
					@Override public void handleResponse(RutaPto rutaPto)
					{
System.err.println("ActRuta:startTrackingRecord-----------8:" + rutaPto);
						CesService.cargarGeoTracking();
System.err.println("ActRuta:startTrackingRecord-----------9:");
						Util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar));
					}
					@Override public void handleFault(BackendlessFault backendlessFault)
					{
						Snackbar.make(_coordinatorLayout, String.format(getString(R.string.error_guardar), backendlessFault), Snackbar.LENGTH_LONG).show();
						System.err.println("ActRuta:startTrackingRecord:handleFault:"+backendlessFault);
					}
				});
/*
				GeoPoint gp = new GeoPoint(loc.getLatitude(), loc.getLongitude());
				gp.addCategory("tracking");//https://backendless.com/feature-32-saving-a-geo-point-with-api/
				//gp.addMetadata("fecha", java.util.Date());
				Backendless.Geo.savePoint(gp, new AsyncCallback<GeoPoint>()
				{
					@Override
					public void handleResponse(GeoPoint geoPoint)
					{
						Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar), Snackbar.LENGTH_LONG).show();
System.err.println("ActRuta:startTrackingRecord-----------8:" + geoPoint);
						CesService.cargarGeoTracking();
						Intent data = new Intent();
						data.putExtra("dirty", true);//si es guardado, editado, borrado => refresca la vista, si no nada
						ActRuta.this.setResult(android.app.Activity.RESULT_OK, data);///TODO : por q ostias no va a Main?
System.err.println("ActRuta:startTrackingRecord-----------9:");
						ActRuta.this.finish();
					}
					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						System.err.println("ActRuta:startTrackingRecord:Backendless.Geo.savePoint:handleFault:"+backendlessFault);
					}
				});*/
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
System.err.println("ActRuta:startTrackingRecord:handleFault:" + backendlessFault);
				Snackbar.make(_coordinatorLayout, String.format(getString(R.string.error_guardar), backendlessFault), Snackbar.LENGTH_LONG).show();
			}
		});
	}
	//______________________________________________________________________________________________
	private void stopTrackingRecord()
	{
System.err.println("ActRuta:stopTrackingRecord:handleFault-----------0:");
		Util.setTrackingRoute("");
	}
}
