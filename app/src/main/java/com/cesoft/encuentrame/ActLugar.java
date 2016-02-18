package com.cesoft.encuentrame;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.models.Lugar;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class ActLugar extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private static final int ACC_MAPA = 1;

	private Lugar _l;
	private TextView _lblPosicion;
	private EditText _txtNombre;
	private EditText _txtDescripcion;
	private Location _locLast;
	private GoogleApiClient _GoogleApiClient;
	private ConnectionResult result;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_lugar);

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
				getAndDisplayLocation();
			}
		});

		ImageButton btnEliminar = (ImageButton)findViewById(R.id.btnEliminar);
		if(_l==null)btnEliminar.setVisibility(View.GONE);
		else btnEliminar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				eliminar();
			}
		});

		ImageButton btnGuardar = (ImageButton) findViewById(R.id.btnGuardar);
		btnGuardar.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				guardar();
			}
		});

		ImageButton btnMapa = (ImageButton) findViewById(R.id.btnMapa);
		btnMapa.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(getBaseContext(), ActMaps.class);
				i.putExtra("objeto", _l);
				startActivityForResult(i, ACC_MAPA);//TODO: si es guardado, borrado => refresca la vista, si no nada
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
			System.err.println("ActLugar:onCreate:ERROR:"+e);
			this.finish();
		}
		//------------------------------------------------------------------------------------------

		/*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			}
		});
		*/

		if(_l==null)
			setTitle(getString(R.string.nuevo_lugar));
		else
			setTitle(getString(R.string.editar_lugar));

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_OK)return;
		if(requestCode == ACC_MAPA)
		{
			_l = data.getParcelableExtra("lugar");
			System.err.println("ActLugar:onActivityResult----------:" + _l);
		}
	}

	//______________________________________________________________________________________________
	private void setPosAct(String s){_lblPosicion.setText(s);}
	private void setPosAct(double lat, double lon){_lblPosicion.setText(lat + "/" + lon);}
	private void setValores()
	{
		_txtNombre.setText(_l.getNombre());
		_txtDescripcion.setText(_l.getDescripcion());
		//_locLast		//GeoPoint p = _l.getLugar();
		if(_l.getLugar() != null)
		{
			if(_locLast == null)_locLast = new Location("dummyprovider");
			_locLast.setLatitude(_l.getLugar().getLatitude());
			_locLast.setLongitude(_l.getLugar().getLongitude());
			setPosAct(_l.getLugar().getLatitude(), _l.getLugar().getLongitude());
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
		if(_l == null)	// CREAR
			_l = new Lugar();

		//else			// EDITAR
		_l.setNombre(_txtNombre.getText().toString());
		_l.setDescripcion(_txtDescripcion.getText().toString());
		_l.setLugar(new GeoPoint(_locLast.getLatitude(), _locLast.getLongitude()));

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
				_l.eliminar(new AsyncCallback<Long>()
				{
					@Override
					public void handleResponse(Long lugar)
					{
						Snackbar.make(null, R.string.eliminar, Snackbar.LENGTH_LONG).setAction("Action", null).show();
						ActLugar.this.finish();
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
