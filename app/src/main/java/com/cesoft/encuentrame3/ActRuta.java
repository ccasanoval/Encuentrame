package com.cesoft.encuentrame3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Build;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.svc.CesService;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

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

import javax.inject.Inject;


//TODO: cuando actualice recordar el zoom y la posición actual....
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActRuta extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private static final String TAG = ActRuta.class.getSimpleName();
	private static final int DELAY_LOCATION = 60000;

	@Inject CesService _servicio;
	@Inject Util _util;

	private boolean _bSucio = false;
	private boolean _bNuevo = false;
	private Ruta _r;
	private EditText _txtNombre;
	private EditText _txtDescripcion;

	private GoogleMap _Map;
	private LocationRequest _LocationRequest;
	private GoogleApiClient _GoogleApiClient;

	//______________________________________________________________________________________________
	private void onSalir()
	{
		if(_bSucio)
		{
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle(_r.getNombre());
			dialog.setMessage(getString(R.string.seguro_salir));
			dialog.setPositiveButton(getString(R.string.guardar), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which) { guardar(); }
			});
			dialog.setCancelable(true);
			dialog.setNegativeButton(getString(R.string.salir), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which) { ActRuta.this.finish(); }
			});
			dialog.create().show();
		}
		else
			ActRuta.this.finish();
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
		setContentView(R.layout.act_ruta);

		App.getComponent(getApplicationContext()).inject(this);

		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//------------------------------------------------------------------------------------------
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
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
					btnStart.setAlpha(0.5f);
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
					btnStop.setAlpha(0.5f);
					stopTrackingRecord();
				}
			});
		}
		//-----------
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)fab.setOnClickListener(new View.OnClickListener()
		{
			//@Override public void onClick(View view) { ActRuta.this.finish(); }
			@Override public void onClick(View view) { onSalir(); }
		});

		//------------------------------------------------------------------------------------------
		try
		{
			_r = this.getIntent().getParcelableExtra(Ruta.NOMBRE);
			setValores();
			_txtNombre.addTextChangedListener(new ActRuta.CesTextWatcher(_txtNombre, _r.getNombre()));
			_txtDescripcion.addTextChangedListener(new ActRuta.CesTextWatcher(_txtDescripcion, _r.getDescripcion()));
		}
		catch(Exception e)
		{
			Log.e(TAG, "onCreate:Nueva ruta o error al desempaquetar:"+e);
			_bNuevo = true;
			_r = new Ruta();
			_txtNombre.addTextChangedListener(new ActRuta.CesTextWatcher(_txtNombre, _r.getNombre()));
			_txtDescripcion.addTextChangedListener(new ActRuta.CesTextWatcher(_txtDescripcion, _r.getDescripcion()));
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
			String sId = _util.getTrackingRoute();
			View layStartStop = findViewById(R.id.layStartStop);
			if( ! sId.equals(_r.getId()))
			{
				if(layStartStop!=null)layStartStop.setVisibility(View.GONE);
			}
			else
				_txtNombre.setTextColor(Color.RED);
		}
	}
	//______________________________________________________________________________________________
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	//______________________________________________________________________________________________
	@Override
	public void onStart()
	{
		super.onStart();
		if(checkPlayServices())
			buildGoogleApiClient();
		buildLocationRequest();
		pideGPS();
		newListeners();
	}
	@Override
	public void onStop()
	{
		super.onStop();
		finEspera();
		delListeners();
		stopTracking();
		clean();
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
		getMenuInflater().inflate(R.menu.menu_ruta, menu);
		if(_bNuevo)
		{
			menu.findItem(R.id.menu_guardar).setVisible(false);
			menu.findItem(R.id.menu_eliminar).setVisible(false);
			menu.findItem(R.id.menu_estadisticas).setVisible(false);
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
		else if(item.getItemId() == R.id.menu_estadisticas)
			estadisticas();
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
	protected synchronized void buildGoogleApiClient()
	{
		_GoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
		_GoogleApiClient.connect();
	}
	private boolean checkPlayServices()
	{
    	GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
    	int result = googleAPI.isGooglePlayServicesAvailable(this);
    	if(result != ConnectionResult.SUCCESS)
		{
			Log.e(TAG, String.format(Locale.getDefault(), "checkPlayServices:e:------------------------------------------%d",result));
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
	private boolean _bGuardar = true;
	private synchronized void guardar()
	{
		if(!_bGuardar)return;
		_bGuardar = false;

		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
				_bGuardar = true;
				finEspera();
				//Log.w(TAG, "guardar:---(synchronized)-----------------------------------------------------------"+data);
				_util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar_ruta));
			}
			@Override
			public void onError(String err, int code)
			{
				_bGuardar = true;
				finEspera();
				Log.e(TAG, "guardar:handleFault:f:--------------------------------------------------"+err);
				Toast.makeText(ActRuta.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
			}
		});
	}
	private void guardar(Fire.CompletadoListener res)
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
		//Solo si es nuevo?
	}

	//______________________________________________________________________________________________
	private boolean _bEliminar = true;
	private void eliminar()
	{
		if(!_bEliminar)return;
		_bEliminar=false;

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(_r.getNombre());//getString(R.string.eliminar));
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
				synchronized(this)
				{
					if(_r.getId().equals(_util.getTrackingRoute()))
						_util.setTrackingRoute("");
					_r.eliminar(new Fire.CompletadoListener()
					{
						@Override
						protected void onDatos(String id)
						{
							_bEliminar = true;
							finEspera();
							_util.return2Main(ActRuta.this, true, getString(R.string.ok_eliminar_ruta));
						}
						@Override
						protected void onError(String err, int code)
						{
							_bEliminar = true;
							finEspera();
							Log.e(TAG, "eliminar:handleFault:f:" + err);
							Toast.makeText(ActRuta.this, String.format(getString(R.string.error_eliminar), err), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
		dialog.create().show();
	}


	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override public void onLocationChanged(Location location)
	{
		_util.setLocation(location);
	}

	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException ignored){}

		if(_bNuevo)
		{
			_Map.moveCamera(CameraUpdateFactory.zoomTo(15));
		}
		else// if(_r.getPuntos().size() > 0)
		{
			showRuta();
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
	private void pideGPS()//TODO Standarizar...
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
	private void startTrackingRecord()
	{
		iniEspera();
		guardar(new Fire.CompletadoListener()
		{
			@Override
			public void onDatos(String id)
			{
				finEspera();
				_util.setTrackingRoute(_r.getId());
				//
				_servicio._restartDelayRuta();
				_util.return2Main(ActRuta.this, true, getString(R.string.ok_guardar_ruta));
			}
			@Override
			public void onError(String err, int code)
			{
				finEspera();
				_util.setTrackingRoute("");
				//
				Log.e(TAG, "startTrackingRecord:onError:e:------------------------------------------"+err);
				Toast.makeText(ActRuta.this, String.format(getString(R.string.error_guardar),err), Toast.LENGTH_LONG).show();
			}
		});
	}
	//______________________________________________________________________________________________
	private void stopTrackingRecord()
	{
		_util.setTrackingRoute("");
		_util.return2Main(ActRuta.this, true, getString(R.string.ok_stop_tracking));
	}

	//----------------------------------------------------------------------------------------------
	private Fire.DatosListener<Ruta.RutaPunto> _lisRuta;
	private void delListeners()
	{
		if(_lisRuta != null)_lisRuta.setListener(null);
		_lisRuta = null;
	}
	private void newListeners()
	{
		delListeners();
		_lisRuta = new Fire.DatosListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
				showRutaHelper(aData);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showRuta:onCancelled:-----------:%s",err));
				//Toast.makeText(this, "Error al obtener los puntos de la ruta", Toast.LENGTH_LONG).show();
			}
		};
	}

	//----------------------------------------------------------------------------------------------
	private void showRuta()
	{
		Ruta.RutaPunto.getListaRep(_r.getId(), _lisRuta);
	}

	//----------------------------------------------------------------------------------------------
	private void showRutaHelper(Ruta.RutaPunto[] aPts)
	{
		if(aPts.length < 1)return;
		if(_Map==null)return;
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

	//----------------------------------------------------------------------------------------------
	private void estadisticas()
	{
		_r.getPuntos(new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
				double fMaxAlt = 0, fMinAlt = 99999;
				double fMaxVel = 0, fMinVel = 99999;
				double fDistancia = 0;

				for(int i=0; i < aData.length; i++)
				{
					Ruta.RutaPunto pto = aData[i];
					if(fMaxAlt < pto.getAltura())fMaxAlt = pto.getAltura();
					if(pto.getAltura() != 0.0 && fMinAlt > pto.getAltura())fMinAlt = pto.getAltura();
					if(fMaxVel < pto.getVelocidad())fMaxVel = pto.getVelocidad();
					if(pto.getVelocidad() != 0.0 && fMinVel > pto.getVelocidad())fMinVel = pto.getVelocidad();
					if(i>0)fDistancia += pto.distanciaReal(aData[i-1]);
				}
				Locale loc = Locale.getDefault();
				String sDistancia;
				if(fDistancia < 2000)   sDistancia = String.format(loc, "%.0f m", fDistancia);
				else					sDistancia = String.format(loc, "%.1f Km", fDistancia/1000);

				String sTiempo = "", sVelMed = "";
				if(aData.length > 0)
				{
					long t = aData[aData.length-1].getFecha().getTime() - aData[0].getFecha().getTime();
					sTiempo = formatTiempo(t);

					if(t > 0)
					{
						double d = fDistancia*1000/t;
						if(d > 3)
						{
							d = d*3600/1000;
							sVelMed = String.format(loc, "%.1f Km/h", d);
						}
						else
							sVelMed = String.format(loc, "%.1f m/s", d);
					}
					else sVelMed = "-";
				}

				String sAltMin;
				if(fMinAlt==99999)sAltMin = "-";
				else sAltMin = String.format(loc, "%.0f m",fMinAlt);

				String sAltMax;
				if(fMaxAlt==0)sAltMax = "-";
				else sAltMax = String.format(loc, "%.0f m",fMaxAlt);

				String sVelMin;
				if(fMinVel==99999)sVelMin = "-";
				else
				{
					if(fMinVel > 3)
					{
						fMinVel = fMinVel*3600/1000;
						sVelMin = String.format(loc, "%.1f Km/h", fMinVel);
					}
					else
						sVelMin = String.format(loc, "%.1f m/s", fMinVel);
				}

				String sVelMax;
				if(fMaxVel == 0)sVelMax = "-";
				else if(fMaxVel > 3)
				{
					fMaxVel = fMaxVel*3600/1000;
					sVelMax = String.format(loc, "%.1f Km/h", fMaxVel);
				}
				else
					sVelMax = String.format(loc, "%.1f m/s", fMaxVel);

				estadisticasShow(String.format(getString(R.string.estadisticas_format),
						sDistancia, sTiempo, sVelMed, sVelMin, sVelMax, sAltMin, sAltMax));
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("estadisticas:onCancelled:-----------:%s",err));
				//Toast.makeText(this, "Error al obtener los puntos de la ruta", Toast.LENGTH_LONG).show();
			}
		});
	}
	private void estadisticasShow(String s)
	{
		//Mostrar
		AlertDialog alertDialog = new AlertDialog.Builder(ActRuta.this).create();
		alertDialog.setTitle(getString(R.string.estadisticas));
		alertDialog.setMessage(s);
		alertDialog.show();
	}

	private static String formatTiempo(long t)//TODO: meter en util
	{
		Date d = new Date(t);
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()); // HH for 0-23
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(d);
	}
}

