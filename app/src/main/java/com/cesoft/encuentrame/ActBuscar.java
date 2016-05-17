package com.cesoft.encuentrame;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.cesoft.encuentrame.models.Filtro;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//TODO: cuando radio es Util.NADA => borrar punto
////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActBuscar extends AppCompatActivity implements OnMapReadyCallback, LocationListener
{
	//private static final int DELAY_LOCATION = 60000;

	private Filtro _filtro;
	private EditText _txtNombre;

	private EditText _txtFechaIni, _txtFechaFin;
	private DatePickerDialog _datePickerDialogIni, _datePickerDialogFin;

	private String[] _asRadio = {"-",		 "10 m", "50 m", "100 m", "200 m", "300 m", "400 m", "500 m", "750 m", "1 Km", "2 Km", "3 Km", "4 Km", "5 Km", "7.5 Km", "10 Km"};
	private int[]    _adRadio = { Util.NADA,  10,     50,     100,     200,     300,     400,     500,     750,     1000,   2000,   3000,   4000,   5000,   7500,     10000};

	//private GoogleApiClient _GoogleApiClient;
	//private LocationRequest _LocationRequest;
	private GoogleMap _Map;
	private Marker _marker;
	private Circle _circle;

	//private CoordinatorLayout _coordinatorLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_buscar);

		//_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fabVolver);
		if(fab != null)fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				ActBuscar.this.finish();
			}
		});

		//------------------------------------------------------------------------------------------
		_txtNombre = (EditText)findViewById(R.id.txtNombre);//txtLogin.requestFocus();

		ArrayAdapter<String> adapter;
		//_swtActivo = (Switch)findViewById(R.id.bActivo);_swtActivo.setChecked(true);
		Spinner _spnActivo = (Spinner)findViewById(R.id.spnActivo);
		if(_spnActivo!=null)
		{
			adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"-", getString(R.string.activos), getString(R.string.inactivos)});
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			_spnActivo.setAdapter(adapter);
			_spnActivo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{
					switch(position)
					{
					case 1:		_filtro.setActivo(Filtro.ACTIVO);		break;
					case 2:		_filtro.setActivo(Filtro.INACTIVO);		break;
					default:	_filtro.setActivo(Util.NADA);			break;
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent)
				{
					_filtro.setActivo(Util.NADA);
				}
			});
		}
		//
		Spinner _spnRadio = (Spinner)findViewById(R.id.spnRadio);
		if(_spnRadio != null)
		{
			adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, _asRadio);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			_spnRadio.setAdapter(adapter);
			_spnRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{
					_filtro.setRadio(_adRadio[position]);
					if(_adRadio[position] == Util.NADA)//if(position==0)
						delPosLugar();
					else
						setRadio();
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent)
				{
					//_filtro.setRadio(Util.NADA);
					delPosLugar();
				}
			});
			_spnRadio.setSelection(3);
		}

		//------------------------------------------------------------------------------------------
		/// FECHAS
		Calendar newCalendar = Calendar.getInstance();
		final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", getResources().getConfiguration().locale);
		_datePickerDialogIni = new DatePickerDialog(this,
			new DatePickerDialog.OnDateSetListener()
			{
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
				{
					Calendar newDate = Calendar.getInstance();
					newDate.set(year, monthOfYear, dayOfMonth, 0, 0);
					_filtro.setFechaIni(newDate.getTime());
					_txtFechaIni.setText(dateFormatter.format(newDate.getTime()));
				}
			},
			newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

		_datePickerDialogFin = new DatePickerDialog(this,
			new DatePickerDialog.OnDateSetListener()
			{
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
				{
					Calendar newDate = Calendar.getInstance();
					newDate.set(year, monthOfYear, dayOfMonth, 23, 59);
					_filtro.setFechaFin(newDate.getTime());
					_txtFechaFin.setText(dateFormatter.format(newDate.getTime()));
				}
			},
			newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

		_txtFechaIni = (EditText)findViewById(R.id.txtFechaIni);
		_txtFechaFin = (EditText)findViewById(R.id.txtFechaFin);
		ImageButton ib = (ImageButton)findViewById(R.id.btnFechaIni);
		if(ib != null)
		ib.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_datePickerDialogIni.show();
				InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				//noinspection ConstantConditions
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				//_txtFechaIni.clearFocus();_txtFechaFin.clearFocus();
			}
		});
		ib = (ImageButton)findViewById(R.id.btnFechaFin);
		if(ib != null)
		ib.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				_datePickerDialogFin.show();
				InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				//noinspection ConstantConditions
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});

		//------------------------------------------------------------------------------------------
		/*_GoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
		_GoogleApiClient.connect();
		_LocationRequest = new LocationRequest();
		_LocationRequest.setInterval(DELAY_LOCATION);
		_LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		//mLocationRequestBalancedPowerAccuracy  || LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
		pideGPS();*/
		try
		{
			_filtro = getIntent().getParcelableExtra(Filtro.FILTRO);
			//-----
			//_iTipo = getIntent().getIntExtra(Util.TIPO, Util.NADA);
			_txtNombre.setText(_filtro.getNombre());
			//-----
			//_swtActivo.setChecked(_filtro.getActivo() == Filtro.ACTIVO);
			if(_spnActivo != null)
			switch(_filtro.getActivo())
			{
			case Filtro.ACTIVO:		_spnActivo.setSelection(1);break;
			case Filtro.INACTIVO:	_spnActivo.setSelection(2);break;
			default:				_spnActivo.setSelection(0);break;
			}
			//-----
			Calendar cal = Calendar.getInstance();
			Date dt = _filtro.getFechaIni();
			if(dt != null)
			{
				cal.setTime(dt);
				_txtFechaIni.setText(dateFormatter.format(dt));
				_datePickerDialogIni.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			}
			//
			dt = _filtro.getFechaFin();
			if(dt != null)
			{
				cal.setTime(dt);
				_txtFechaFin.setText(dateFormatter.format(dt));
				_datePickerDialogFin.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			}
			//-----
			for(int i=0; i < _adRadio.length; i++)
			{
				if(_adRadio[i] == _filtro.getRadio())
				{
					if(_spnRadio!=null)_spnRadio.setSelection(i);
					break;
				}
			}
		}
		catch(Exception e)
		{
			System.err.println("ActBuscar:onCreate:e:"+e);
			Util.return2Main(this, null);
		}
		switch(_filtro.getTipo())
		{
		case Util.LUGARES:
			setTitle(String.format("%s %s", getString(R.string.buscar), getString(R.string.lugares)));
			//_spnActivo.setVisibility(View.GONE);
			View v = findViewById(R.id.layActivo);
			if(v!=null)v.setVisibility(View.GONE);
			break;
		case Util.RUTAS:
			setTitle(String.format("%s %s", getString(R.string.buscar), getString(R.string.rutas)));
			//_spnActivo.setVisibility(View.VISIBLE);
			//findViewById(R.id.layActivo).setVisibility(View.VISIBLE);
			break;
		case Util.AVISOS:
			setTitle(String.format("%s %s", getString(R.string.buscar), getString(R.string.avisos)));
			break;
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException se){System.err.println("ActBuscar:onMapReady:setMyLocationEnabled:e:"+se);}
		Location loc = Util.getLocation();
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 15));
		_Map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng pos)
			{
				setPosLugar(pos);
			}
		});
		setMarker();
	}
	//______________________________________________________________________________________________
	private void setPosLugar(LatLng pos)
	{
		//_pos = pos;
		_filtro.setPunto(pos);
		setMarker();
		setRadio();
	}
	private void delPosLugar()
	{
		if(_marker != null)_marker.remove();
		if(_circle != null)_circle.remove();
		_filtro.setPunto(null);
		_filtro.setRadio(Util.NADA);
	}
	private void setMarker()
	{
		try
		{
			if(_marker != null)_marker.remove();
			if(_filtro.getPunto() == null || (_filtro.getPunto().latitude == 0 && _filtro.getPunto().longitude == 0))return;
			//LatLng pos = new LatLng(_loc.getLatitude(), _loc.getLongitude());
			MarkerOptions mo = new MarkerOptions()
					.position(_filtro.getPunto())
					.title(getString(R.string.buscar));
			_marker = _Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(_filtro.getPunto(), 15));
		}
		catch(Exception e){System.err.println("ActBuscar:setMarker:e:"+e);}
	}
	private void setRadio()
	{
		if(_circle != null)_circle.remove();
		if(_filtro.getPunto() == null || _filtro.getRadio() < 1)return;
		_circle = _Map.addCircle(new CircleOptions().center(_filtro.getPunto())
				.radius(_filtro.getRadio()).strokeColor(Color.TRANSPARENT)
				.fillColor(0x55AA0000));//Color.BLUE
	}

	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override
	public void onLocationChanged(Location location)
	{
		Util.setLocation(location);
	}

	//______________________________________________________________________________________________
	/*private void pideGPS()
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
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
					System.err.println("LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
					// Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.
					break;
				}
			}
		});
	}*/

		//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_buscar, menu);
		if(!_filtro.isOn())menu.findItem(R.id.menu_eliminar).setVisible(false);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_buscar)
			buscar();
		else if(item.getItemId() == R.id.menu_eliminar)
			eliminar();
		return super.onOptionsItemSelected(item);
	}
	private void eliminar()
	{
		_filtro.turnOff();
		Util.return2Main(this, _filtro);
		//Util.return2Main(this, true, getString(R.string.sin_filtro));
	}
	private void buscar()
	{
		_filtro.setNombre(_txtNombre.getText().toString());
		if(_filtro.isValid())
		{
			checkFechas();
			_filtro.turnOn();
System.err.println("ActBuscar:buscar:filtro:---------------------------------------------- " + _filtro);
			Util.return2Main(this, _filtro);
		}
		else
		{
			eliminar();
		}
	}
	//__________________________________________________________________________________________
	private void checkFechas()
	{
		Date ini = _filtro.getFechaIni();
		Date fin = _filtro.getFechaFin();
		if(ini!=null && fin!=null && ini.getTime() > fin.getTime())
		{
			_filtro.setFechaIni(fin);
			_filtro.setFechaFin(ini);
		}
	}
}
