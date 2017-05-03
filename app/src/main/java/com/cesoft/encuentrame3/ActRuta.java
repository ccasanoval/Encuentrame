package com.cesoft.encuentrame3;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
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

import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActRuta extends AppCompatActivity implements OnMapReadyCallback, LocationListener,
		GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private static final String TAG = ActRuta.class.getSimpleName();
	private static final int DELAY_LOCATION = 60000;

	@Inject Util _util;
	@Inject PreRuta _presenter;

	private EditText _txtNombre;
		String getNombre(){return _txtNombre.getText().toString();}
		void requestFocusNombre(){_txtNombre.requestFocus();}
	private EditText _txtDescripcion;
		String getDescripcion(){return _txtDescripcion.getText().toString();}

	private LocationRequest _LocationRequest;
	private GoogleApiClient _GoogleApiClient;
	private GoogleMap _Map;
		GoogleMap getMap(){return _Map;}
		void moveCamara(LatLng pos){_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));}


	@Override
	public void onBackPressed()
	{
		_presenter.onSalir();
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
		setContentView(R.layout.act_ruta);

		App.getComponent(getApplicationContext()).inject(this);
		_presenter.ini(this);

		//------------------------------------------------------------------------------------------
		_txtNombre = (EditText)findViewById(R.id.txtNombre);
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		//-----------
		final ImageButton btnStart = (ImageButton)findViewById(R.id.btnStart);
		if(btnStart != null)
		{
			btnStart.setEnabled(true);
			btnStart.setOnClickListener(v ->
			{
				//http://mobisoftinfotech.com/resources/blog/android/3-ways-to-implement-efficient-location-tracking-in-android-applications/
				btnStart.setEnabled(false);
				btnStart.setAlpha(0.5f);
				_presenter.startTrackingRecord();
			});
		}
		final ImageButton btnStop = (ImageButton)findViewById(R.id.btnStop);
		if(btnStop != null)
		{
			btnStop.setEnabled(true);
			btnStop.setOnClickListener(v ->
			{
				btnStop.setEnabled(false);
				btnStop.setAlpha(0.5f);
				_presenter.stopTrackingRecord();
			});
		}
		//-----------
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		//
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)fab.setOnClickListener(view -> _presenter.onSalir());

		_presenter.loadObjeto();
		_txtNombre.setText(_presenter.getNombre());
		_txtDescripcion.setText(_presenter.getDescripcion());
		_txtNombre.addTextChangedListener(new CesTextWatcher(_txtNombre, _presenter.getNombre()));
		_txtDescripcion.addTextChangedListener(new CesTextWatcher(_txtDescripcion, _presenter.getDescripcion()));

		//------------------------------------------------------------------------------------------
		if(_presenter.isNuevo())
		{
			setTitle(getString(R.string.nueva_ruta));
			if(btnStop!=null)btnStop.setVisibility(View.GONE);
			try
			{	//Oculta el mapa, no hay puntos que enseñar en el
				SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
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
			if( ! sId.equals(_presenter.getId()))
			{
				if(layStartStop!=null)layStartStop.setVisibility(View.GONE);
			}
			else
				_txtNombre.setTextColor(Color.RED);
		}

		if(savedInstanceState != null)
		{
			_fMapZoom = savedInstanceState.getFloat(MAP_ZOOM, 15);
		}
Log.e(TAG, "-------------------------------- CREATE");
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
		finEspera();
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
		if(_presenter.isNuevo())
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
			_presenter.guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			_presenter.eliminar();
		else if(item.getItemId() == R.id.menu_estadisticas)
			_presenter.estadisticas();
		return super.onOptionsItemSelected(item);
	}

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
	@Override public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}
	@Override public void onConnected(Bundle arg0){}
	@Override public void onConnectionSuspended(int arg0)
	{
		if(_GoogleApiClient != null)
			_GoogleApiClient.connect();
	}


	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override public void onLocationChanged(Location location) { _util.setLocation(location); }

	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		Log.e(TAG, "----------------------------------------------------------------- MAP READY");
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException ignored){}

		if(_presenter.isNuevo())
		{
			_Map.moveCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		}
		else// if(_r.getPuntos().size() > 0)
		{
			_presenter.showRuta();
		}

		_Map.setOnCameraMoveListener(() ->
		{ if(_Map!=null)_fMapZoom = _Map.getCameraPosition().zoom; });
		//_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));

		//MARCADOR MULTILINEA --------------------------------------------
		_Map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
		{
			@Override public View getInfoWindow(Marker arg0){return null;}
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

	//----------------------------------------------------------------------------------------------
	private ProgressDialog _progressDialog;
	public void iniEspera()
	{
		_progressDialog = ProgressDialog.show(this, "", getString(R.string.cargando), true, true);
	}
	public void finEspera()
	{
		if(_progressDialog!=null)_progressDialog.dismiss();
	}


}

