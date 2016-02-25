package com.cesoft.encuentrame;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Lugar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class ActAviso extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private static final int ACC_MAPA = 1;

	private Aviso _a;
	private TextView _lblPosicion;
	private EditText _txtNombre;
	private EditText _txtDescripcion;
	private Location _locLast;
	private GoogleApiClient _GoogleApiClient;
	private ConnectionResult result;

	//private ArrayAdapter<String> _adapter;
	private String[] _asRadio = {"10 m", "50 m", "100 m", "200 m", "300 m", "400 m", "500 m", "750 m", "1 Km", "2 Km", "3 Km", "4 Km", "5 Km", "7.5 Km", "10 Km"};
	private double[] _adRadio = { 10,     50,     100,     200,     300,     400,     500,     750,     1000,   2000,   3000,   4000,   5000,   7500,     10000};
	private Spinner _spnRadio;
	private double _radio;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_aviso);

		//-----------
		_lblPosicion = (TextView)findViewById(R.id.lblPosicion);
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();
		_txtDescripcion = (EditText)findViewById(R.id.txtDescripcion);
		_spnRadio = (Spinner)findViewById(R.id.spnRadio);
System.err.println("------------------------------------"+_spnRadio);
		como que es null???
		//ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.array_radio_tit, android.R.layout.simple_spinner_item);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, _asRadio);
System.err.println("------------------------------------"+_spnRadio+" : "+adapter);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spnRadio.setAdapter(adapter);
		_spnRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				_radio = _adRadio[position];
				/*String sRadio = parent.getItemAtPosition(position).toString();
				int i = sRadio.indexOf(" Km");
				if(i > 0)		_radio = Integer.parseInt(sRadio.substring(0,i))*1000;
				else			_radio = Integer.parseInt(sRadio);
				*/
System.err.println("------------------------------------_radio="+_radio);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				_radio = 1000;//TODO:radio por defecto en settings
			}
		});

		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		btnActPos.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getAndDisplayLocation();
			}
		});

		ImageButton btnEliminar = (ImageButton)findViewById(R.id.btnEliminar);
		if(_a==null)btnEliminar.setVisibility(View.GONE);
		else btnEliminar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				eliminar();
			}
		});

		///// GUARDAR
		ImageButton btnGuardar = (ImageButton) findViewById(R.id.btnGuardar);
		btnGuardar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				guardar();
			}
		});

		///// MAPA
		ImageButton btnMapa = (ImageButton) findViewById(R.id.btnMapa);
		btnMapa.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), ActMaps.class);
				i.putExtra("objeto", _a);
				startActivityForResult(i, ACC_MAPA);//TODO: si es guardado, borrado => refresca la vista, si no nada
			}
		});

		//------------------------------------------------------------------------------------------
		try
		{
			_a = this.getIntent().getParcelableExtra(Aviso.NOMBRE);
System.err.println("ActAviso:onCreate:++++++++++++++++"+_a);
			setValores();
		}
		catch(Exception e)
		{
			System.err.println("ActAviso:onCreate:ERROR:"+e);
			this.finish();
		}
		//------------------------------------------------------------------------------------------

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view){ActAviso.this.finish();}
		});

		if(_a==null)
			setTitle(getString(R.string.nuevo_aviso));
		else
			setTitle(getString(R.string.editar_aviso));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_OK)return;
		if(requestCode == ACC_MAPA)
		{
			_a = data.getParcelableExtra("aviso");
			System.err.println("ActAviso:onActivityResult----------:" + _a);
		}
	}

	//______________________________________________________________________________________________
	private void setPosAct(String s){_lblPosicion.setText(s);}
	private void setPosAct(double lat, double lon){_lblPosicion.setText(lat + "/" + lon);}
	private void setValores()
	{
		_txtNombre.setText(_a.getNombre());
		_txtDescripcion.setText(_a.getDescripcion());
		//_locLast		//GeoPoint p = _l.getLugar();
		if(_a.getLugar() != null)
		{
			if(_locLast == null)_locLast = new Location("dummyprovider");
			_locLast.setLatitude(_a.getLugar().getLatitude());
			_locLast.setLongitude(_a.getLugar().getLongitude());
			setPosAct(_a.getLugar().getLatitude(), _a.getLugar().getLongitude());

			_radio = _a.getLugar().getDistance();
			//int spinnerPosition = _adapter.getPosition("10 Km");
			for(int i=0; i < _adRadio.length; i++)
			{
				if(_radio == _adRadio[i])
				{
					_spnRadio.setSelection(i);
					break;
				}
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

	@Override
	protected void onResume()
	{
		super.onResume();
		//checkPlayServices();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		/*Action viewAction = Action.newAction(Action.TYPE_VIEW, // TODO: choose an action type.
				"ActLugar Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct. Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.cesoft.encuentrame/http/host/path"));
		AppIndex.AppIndexApi.end(_GoogleApiClient, viewAction);
		_GoogleApiClient.disconnect();*/
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
			System.err.println("ActLugar:checkPlayServices:ERROR:-------------"+result);
			//Snackbar.make(null, R.string.eliminar, Snackbar.LENGTH_LONG).setAction("Action", null).show();
	        return false;
	    }
	    return true;
	}
	private void getAndDisplayLocation()
	{
		if(_GoogleApiClient == null)return;
		if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)return;
		_locLast = LocationServices.FusedLocationApi.getLastLocation(_GoogleApiClient);
		if(_locLast != null)
			setPosAct(_locLast.getLatitude(), _locLast.getLongitude());
		else
			setPosAct(getString(R.string.sin_posicion));
	}



	//______________________________________________________________________________________________
	/**
	 * Google api callback methods
	 */
	@Override
	public void onConnectionFailed(@NonNull ConnectionResult result)
	{
		this.result = result;
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
		if(_a == null)	// CREAR
			_a = new Aviso();

		//else			// EDITAR
		_a.setNombre(_txtNombre.getText().toString());
		_a.setDescripcion(_txtDescripcion.getText().toString());
		GeoPoint l = new GeoPoint(_locLast.getLatitude(), _locLast.getLongitude());
		l.setDistance(_radio);
		_a.setLugar(l);

		//TODO:
		/*Intent data = new Intent();
		data.putExtra("aviso", _a);
		setResult(Activity.RESULT_OK, data);*/
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
				_a.eliminar(new AsyncCallback<Long>()
				{
					@Override
					public void handleResponse(Long lugar)
					{
						Snackbar.make(null, R.string.eliminar, Snackbar.LENGTH_LONG).setAction("Action", null).show();
						ActAviso.this.finish();
					}

					@Override
					public void handleFault(BackendlessFault backendlessFault)
					{
						// an error has occurred, the error code can be retrieved with
						Snackbar.make(null, "Error:"+backendlessFault.getCode(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
}
