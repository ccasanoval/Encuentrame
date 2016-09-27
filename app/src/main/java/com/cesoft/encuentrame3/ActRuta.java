package com.cesoft.encuentrame3;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import com.cesoft.encuentrame3.models.Ruta;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActRuta extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
	private static final String TAG = "CESoft:ActRuta:";
	private static final int DELAY_LOCATION = 60000;

	private boolean _bNuevo = false;
	private Ruta _r;
	private EditText _txtNombre;
	private EditText _txtDescripcion;
	//private Spinner _spnTrackingDelay;
	//private static final String[] _asDelay = {"2 min", "5 min", "10 min", "15 min", "20 min", "25 min", "30 min", "45 min", "1 h", "2 h", "3 h", "4 h", "5 h", "6 h", "12 h"};
	//private static final int[]    _aiDelay = { 2,       5,       10,       15,       20,       25,       30,       45,       60,    2*60,  3*60,  4*60,  5*60,  6*60,  12*60 };//*60*1000

	private GoogleMap _Map;
	private LocationRequest _LocationRequest;
	private GoogleApiClient _GoogleApiClient;
	//private CoordinatorLayout _coordinatorLayout;//TODO: eliminar de layout


	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_ruta);

		//_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//------------------------------------------------------------------------------------------
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		//_spnTrackingDelay = (Spinner)findViewById(R.id.spnTrackingDelay);
//TODO: si se hace tracking mediante geofence no necesito esto... cambiarlo por radio de feofence...
		/*View layPeriodo = findViewById(R.id.layPeriodo);
		if(layPeriodo != null)layPeriodo.setVisibility(View.GONE);
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
		});*/
		//-----------
		final ImageButton btnStart = (ImageButton)findViewById(R.id.btnStart);
		if(btnStart != null)
		{
			btnStart.setEnabled(true);
			btnStart.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					//http://mobisoftinfotech.com/resources/blog/android/3-ways-to-implement-efficient-location-tracking-in-android-applications/
					btnStart.setEnabled(false);
					startTrackingRecord();
				}
			});
		}
		final ImageButton btnStop = (ImageButton)findViewById(R.id.btnStop);
		if(btnStop != null)
		{
			btnStop.setEnabled(true);
			btnStop.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					btnStop.setEnabled(false);
					stopTrackingRecord();
				}
			});
		}
		//-----------
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)
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
			setValores();
		}
		catch(Exception e)
		{
			Log.e(TAG, "onCreate:Nueva ruta o error al desempaquetar:"+e);
			_bNuevo = true;
			_r = new Ruta();
		}
		//------------------------------------------------------------------------------------------
		if(_bNuevo)
		{
			setTitle(getString(R.string.nueva_ruta));
			if(btnStop!=null)btnStop.setVisibility(View.GONE);
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
			if(btnStart!=null)btnStart.setVisibility(View.GONE);
			//if(layPeriodo != null)layPeriodo.setVisibility(View.GONE);
			//si está activo muestra btnStop
			String sId = Util.getTrackingRoute(ActRuta.this);
			View layStartStop = findViewById(R.id.layStartStop);
			if( ! sId.equals(_r.getId()))
			{
				if(layStartStop!=null)layStartStop.setVisibility(View.GONE);
			}
			else
				_txtNombre.setTextColor(Color.RED);
		}

		//------------------------------------------------------------------------------------------
		_GoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		_GoogleApiClient.connect();
		_LocationRequest = new LocationRequest();
		_LocationRequest.setInterval(DELAY_LOCATION);
		_LocationRequest.setFastestInterval(DELAY_LOCATION);
		_LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		//mLocationRequestBalancedPowerAccuracy  || LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
		pideGPS();
	}
	//______________________________________________________________________________________________
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		stopTracking();
		_GoogleApiClient.unregisterConnectionCallbacks(this);
		_GoogleApiClient.unregisterConnectionFailedListener(this);
		_GoogleApiClient.disconnect();
		_GoogleApiClient = null;
		_LocationRequest = null;
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
			catch(SecurityException e)
			{
				Log.e(TAG, "startTracking:e:"+e, e);
				Toast.makeText(this, "ActRuta:startTracking:e:"+e, Toast.LENGTH_LONG).show();
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
		//
		TextView lblFecha = (TextView)findViewById(R.id.lblFecha);
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
		Date date = _r.getFecha();//_r.getUpdated()!=null ? _r.getUpdated() : _r.getCreated();
		if(date != null && lblFecha!= null)lblFecha.setText(dateFormat.format(date));
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
		Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
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
	private synchronized void guardar()
	{
		guardar(new DatabaseReference.CompletionListener()
		{
			@Override
			public void onComplete(DatabaseError err, DatabaseReference data)
			{
				if(err == null)
				{
					//Log.w(TAG, "guardar:"+data);
					Util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar_ruta));
				}
				else
				{
					Log.e(TAG, "guardar:handleFault:f:" + err);
					Toast.makeText(ActRuta.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
				}
			}
		});

	}
	private void guardar(DatabaseReference.CompletionListener res)
	{
		if(_txtNombre.getText().toString().isEmpty())
		{
			Toast.makeText(ActRuta.this, getString(R.string.sin_nombre), Toast.LENGTH_LONG).show();
			_txtNombre.requestFocus();
			return;
		}
		_r.setNombre(_txtNombre.getText().toString());
		_r.setDescripcion(_txtDescripcion.getText().toString());
		_r.guardar(res);
	}

	//______________________________________________________________________________________________
	private synchronized void eliminar()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(_r.getNombre());//getString(R.string.eliminar));
		dialog.setMessage(getString(R.string.seguro_eliminar));
		dialog.setPositiveButton(getString(R.string.eliminar), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_r.eliminar(new DatabaseReference.CompletionListener()
				{
					@Override
					public void onComplete(DatabaseError err, DatabaseReference data)
					{
						if(err == null)
						{
							//Log.w(TAG, "eliminar:handleResponse:"+data);
							Util.return2Main(ActRuta.this, true, getString(R.string.ok_eliminar_ruta));
						}
						else
						{
							Log.e(TAG, "eliminar:handleFault:f:" + err);
							Toast.makeText(ActRuta.this, String.format(getString(R.string.error_eliminar), err), Toast.LENGTH_LONG).show();
						}
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
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException ignored){}

		if(_bNuevo)
		{
			_Map.moveCamera(CameraUpdateFactory.zoomTo(15));
		}
		else// if(_r.getPuntos().size() > 0)
		{
			showRuta();//showMarkers();
			//GeoPoint pos = _r.getPuntos().get(0);
			//LatLng pos2 = new LatLng(pos.getLatitude(), pos.getLongitude());
			//_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos2, 15));
		}

		//MARCADOR MULTILINEA --------------------------------------------
		_Map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
		{
			@Override
			public View getInfoWindow(Marker arg0){return null;}
			@Override
			public View getInfoContents(Marker marker)
			{
				LinearLayout info = new LinearLayout(ActRuta.this);
				info.setOrientation(LinearLayout.VERTICAL);

				TextView title = new TextView(ActRuta.this);
				title.setTextColor(Color.BLACK);
				title.setGravity(Gravity.CENTER);
				title.setTypeface(null, Typeface.BOLD);
				title.setText(marker.getTitle());

				TextView snippet = new TextView(ActRuta.this);
				snippet.setTextColor(Color.GRAY);
				snippet.setText(marker.getSnippet());

				info.addView(title);
				info.addView(snippet);
				return info;
			}
		});
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
					try{status.startResolutionForResult(ActRuta.this, 1000);}catch(android.content.IntentSender.SendIntentException ignored){}
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
					Log.w(TAG, "LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
					// Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
					break;
				}
			}
		});
	}


	//______________________________________________________________________________________________
	private void startTrackingRecord()
	{
		guardar(new DatabaseReference.CompletionListener()
		{
			@Override
			public void onComplete(DatabaseError err, DatabaseReference data)
			{
				if(err == null)
				{
					Util.setTrackingRoute(ActRuta.this, _r.getId());
					Util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar_ruta));
				}
				else
				{
					Log.e(TAG, "startTrackingRecord:handleFault:" + err);
					//*****************************************************************************
					try{Thread.sleep(500);}catch(InterruptedException ignored){}
					//TODO: Repetia la op por Backendless, en Firebase quizá podría eliminar este repetitivo
					_r.guardar(new DatabaseReference.CompletionListener()
					{
						@Override
						public void onComplete(DatabaseError err, DatabaseReference data)
						{
							if(err == null)
							{
								Util.setTrackingRoute(ActRuta.this, _r.getId());
								Util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar_ruta));
							}
							else
							{
								Log.e(TAG, "startTrackingRecord:handleFault2:" + err);
								Toast.makeText(ActRuta.this, String.format(getString(R.string.error_guardar),err), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			}
		});
	}
	//______________________________________________________________________________________________
	private void stopTrackingRecord()
	{
		Util.setTrackingRoute(ActRuta.this, "");
		Util.return2Main(ActRuta.this, true, getString(R.string.ok_stop_tracking));
	}

	private void showRuta()
	{
		_r.getPuntos(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot ds)
			{
				int i = 0;
				Ruta.RutaPunto[] aPts = new Ruta.RutaPunto[(int)ds.getChildrenCount()];
				for(DataSnapshot o : ds.getChildren())
				{
					aPts[i++] = o.getValue(Ruta.RutaPunto.class);//TODO:go to map pos
				}
				showRutaHelper(aPts);
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, "showRuta:onCancelled:-----------:"+err);
				//Toast.makeText(this, "Error al obtener los puntos de la ruta", Toast.LENGTH_LONG).show();
			}
		});
	}


	private void showRutaHelper(Ruta.RutaPunto[] aPts)
	{
		if(aPts.length < 1)return;
		_Map.clear();

		DateFormat df = java.text.DateFormat.getDateTimeInstance();//TODO: set 24h

		String INI = getString(R.string.ini);
		String FIN = getString(R.string.fin);
		PolylineOptions po = new PolylineOptions();

		Ruta.RutaPunto gpAnt = null;
		Ruta.RutaPunto gpIni = aPts[0];
		Ruta.RutaPunto gpFin = aPts[aPts.length -1];
		for(Ruta.RutaPunto pto : aPts)
		{
			LatLng pos = new LatLng(pto.getLatitud(), pto.getLongitud());
			MarkerOptions mo = new MarkerOptions();
			mo.title(_r.getNombre());

			String snippet;
			if(pto == gpIni)snippet = INI;
			else if(pto == gpFin)snippet = FIN;
			else snippet = getString(R.string.info_time);

			Date date = pto.getFecha();
			if(date != null)snippet += df.format(date);
			snippet += String.format(Locale.ENGLISH, getString(R.string.info_prec), pto.getPrecision());
			if(gpAnt != null)
			{
				float d = pto.distanciaReal(gpAnt);
				String sDist;
				if(d > 3000)	sDist = String.format(Locale.ENGLISH, getString(R.string.info_dist2), d/1000);
				else			sDist = String.format(Locale.ENGLISH, getString(R.string.info_dist), d);
				snippet += sDist;
				//snippet += String.format(Locale.ENGLISH, getString(R.string.info_dist), pto.distanciaReal(gpAnt));
			}
			if(pto.getVelocidad() > 3)
				snippet += String.format(Locale.ENGLISH, getString(R.string.info_speed2), pto.getVelocidad()*3600/1000);
			else if(pto.getVelocidad() > 0)
				snippet += String.format(Locale.ENGLISH, getString(R.string.info_speed), pto.getVelocidad());
			if(pto.getDireccion() > 0)snippet += String.format(Locale.ENGLISH, getString(R.string.info_nor), pto.getDireccion());
			if(pto.getAltura() > 0)snippet += String.format(Locale.ENGLISH, getString(R.string.info_alt), pto.getAltura());
			mo.snippet(snippet);

			if(pto == gpIni)//if(pto.equalTo(gpIni)) //getLat() == gpIni.getLat() && pto.getLon() == gpIni.getLon())//It's not possible to establish the z order for the marker...
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				mo.rotation(45);
				_Map.addMarker(mo.position(pos));
			}
			else if(pto == gpFin)//else if(pto.equalTo(gpFin))//(pto.getLat() == gpFin.getLat() && pto.getLon() == gpFin.getLon())
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				mo.rotation(-45);
				_Map.addMarker(mo.position(pos));
			}
			//if(pto.distanciaReal(gpIni) > 5 && pto.distanciaReal(gpFin) > 5)//0.000000005 || pto.distancia2(gpFin) > 0.000000005)
			else if(gpAnt != null && pto.distanciaReal(gpAnt) > 5)
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
				_Map.addMarker(mo.position(pos));
			}
			gpAnt = pto;
			po.add(pos);
		}
		po.width(5).color(Color.BLUE);
		_Map.addPolyline(po);
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()), 15));
	}
}

/*
//TODO: try geofence for tracking again????
				/// Crear geofence con pos actual
				RutaPto rp = new RutaPto();
				rp.setIdRuta(r.getObjectId());
				rp.setLatLon(loc.getLatitude(), loc.getLongitude());
				rp.saveTrackingPto(new AsyncCallback<RutaPto>()
				{
					@Override public void handleResponse(RutaPto rutaPto)
					{
System.err.println("ActRuta:startTrackingRecord-----------8:" + rutaPto);
						//CesService.cargarGeoTracking();
						Util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar));
					}
					@Override public void handleFault(BackendlessFault backendlessFault)
					{
						Snackbar.make(_coordinatorLayout, String.format(getString(R.string.error_guardar), backendlessFault), Snackbar.LENGTH_LONG).show();
						System.err.println("ActRuta:startTrackingRecord:handleFault:"+backendlessFault);
					}
				});
 */