package com.cesoft.encuentrame3;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cesoft.encuentrame3.models.Filtro;
import com.cesoft.encuentrame3.util.Constantes;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActBuscar extends AppCompatActivity implements OnMapReadyCallback, LocationListener
{
	private static final String TAG = ActBuscar.class.getSimpleName();

	@Inject	Util util;

	private Filtro filtro;
	private EditText txtNombre;

	private EditText txtFechaIni;
	private EditText txtFechaFin;

	private final String[] asRadio = {"-",			"10 m", "50 m", "100 m", "200 m", "300 m", "400 m", "500 m", "750 m", "1 Km", "2 Km", "3 Km", "4 Km", "5 Km", "7.5 Km", "10 Km"};
	private final int[]	adRadio = { Constantes.NADA, 10,     50,     100,     200,     300,     400,     500,     750,     1000,   2000,   3000,   4000,   5000,   7500,     10000};

	private GoogleMap map;
	private Marker marker;
	private Circle circle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_buscar);

		App.getComponent().inject(this);

		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		if(mapFragment != null)
			mapFragment.getMapAsync(this);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
//		FloatingActionButton fab = findViewById(R.id.fabVolver);
//		if(fab != null)
//			fab.setOnClickListener(view -> finish());

		//------------------------------------------------------------------------------------------
		txtNombre = findViewById(R.id.txtNombre);


		//------------------------------------------------------------------------------------------
		/// FECHAS
		Calendar newCalendar = Calendar.getInstance();
		DatePickerDialog datePickerDialogIni = new DatePickerDialog(this,
				(view, year, monthOfYear, dayOfMonth) ->
				{
					Calendar newDate = Calendar.getInstance();
					newDate.set(year, monthOfYear, dayOfMonth, 0, 0);
					filtro.setFechaIni(newDate.getTime());
					txtFechaIni.setText(util.formatFecha(newDate.getTime()));
				},
			newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

		DatePickerDialog datePickerDialogFin = new DatePickerDialog(this,
				(view, year, monthOfYear, dayOfMonth) ->
				{
					Calendar newDate = Calendar.getInstance();
					newDate.set(year, monthOfYear, dayOfMonth, 23, 59);
					filtro.setFechaFin(newDate.getTime());
					txtFechaFin.setText(util.formatFecha(newDate.getTime()));
				},
			newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

		txtFechaIni = findViewById(R.id.txtFechaIni);
		txtFechaFin = findViewById(R.id.txtFechaFin);
		ImageButton ib = findViewById(R.id.btnFechaIni);
		if(ib != null)
			ib.setOnClickListener(v ->
			{
				datePickerDialogIni.show();
				InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				//noinspection ConstantConditions
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			});
		ib = findViewById(R.id.btnFechaFin);
		if(ib != null)
			ib.setOnClickListener(v ->
			{
				datePickerDialogFin.show();
				InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				//noinspection ConstantConditions
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			});

		//------------------------------------------------------------------------------------------
		initActivo();
		initRadio();
		initFiltro(datePickerDialogIni, datePickerDialogFin);
		initLayoutActivo();
	}

	private void initActivo() {
		Spinner spnActivo = findViewById(R.id.spnActivo);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
				new String[]{"-", getString(R.string.activos), getString(R.string.inactivos)});
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if(spnActivo != null)
		{
			spnActivo.setAdapter(adapter);
			spnActivo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{
					switch(position)
					{
						case 1:		filtro.setActivo(Filtro.ACTIVO);	break;
						case 2:		filtro.setActivo(Filtro.INACTIVO);	break;
						default:	filtro.setActivo(Constantes.NADA);		break;
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent)
				{
					filtro.setRadio(Constantes.NADA);
				}
			});
		}
	}
	private void initRadio() {
		Spinner spnRadio = findViewById(R.id.spnRadio);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, asRadio);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		if(spnRadio != null)
		{
			spnRadio.setAdapter(adapter);
			spnRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
				{
					filtro.setRadio(adRadio[position]);
					setRadio();
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent)
				{
					filtro.setRadio(Constantes.NADA);
				}
			});
			spnRadio.setSelection(3);
		}
	}
	private void initFiltro(DatePickerDialog datePickerDialogIni, DatePickerDialog datePickerDialogFin) {
		try
		{
			Spinner spnRadio = findViewById(R.id.spnRadio);
			Spinner spnActivo = findViewById(R.id.spnActivo);

			filtro = getIntent().getParcelableExtra(Filtro.FILTRO);
			//-----
			txtNombre.setText(filtro.getNombre());
			//-----
			if(spnActivo != null)
				switch(filtro.getActivo())
				{
					case Filtro.ACTIVO:		spnActivo.setSelection(1);break;
					case Filtro.INACTIVO:	spnActivo.setSelection(2);break;
					default:				spnActivo.setSelection(0);break;
				}
			//-----
			Calendar cal = Calendar.getInstance();
			Date dt = filtro.getFechaIni();
			if(dt != null)
			{
				cal.setTime(dt);
				txtFechaIni.setText(util.formatFecha(dt));
				datePickerDialogIni.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			}
			//
			dt = filtro.getFechaFin();
			if(dt != null)
			{
				cal.setTime(dt);
				txtFechaFin.setText(util.formatFecha(dt));
				datePickerDialogFin.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			}
			//-----
			for(int i = 0; i < adRadio.length; i++)
			{
				if(adRadio[i] == filtro.getRadio() && spnRadio != null)
				{
					spnRadio.setSelection(i);
					break;
				}
			}
		}
		catch(Exception e)
		{
			Log.e(TAG, String.format("ActBuscar:onCreate:e:%s",e));
			util.return2Main(this, null);
		}
	}
	private void initLayoutActivo() {
		View viewActivo = findViewById(R.id.layActivo);
		String patron = "%s %s";
		switch(filtro.getTipo())
		{
			case Constantes.LUGARES:
				setTitle(String.format(patron, getString(R.string.buscar), getString(R.string.lugares)));
				viewActivo.setVisibility(View.GONE);
				break;
			case Constantes.RUTAS:
				setTitle(String.format(patron, getString(R.string.buscar), getString(R.string.rutas)));
				viewActivo.setVisibility(View.VISIBLE);
				break;
			case Constantes.AVISOS:
				setTitle(String.format(patron, getString(R.string.buscar), getString(R.string.avisos)));
				viewActivo.setVisibility(View.VISIBLE);
				break;
			default:break;
		}
	}


	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		map = googleMap;
		map.getUiSettings().setZoomControlsEnabled(true);
		try{ map.setMyLocationEnabled(true); }
		catch(SecurityException se){Log.e(TAG, String.format("onMapReady:setMyLocationEnabled:e:%s",se), se);}
		Location loc = util.getLocation();
		if(loc == null)return;
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 15));
		map.setOnMapClickListener(this::setPosLugar);
		setMarker();
	}
	//______________________________________________________________________________________________
	private void setPosLugar(LatLng pos)
	{
		filtro.setPunto(pos);
		setMarker();
		setRadio();
	}
	private void setMarker()
	{
		try
		{
			if(marker != null) marker.remove();
			if(filtro.getPunto() == null || (filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0))return;
			MarkerOptions mo = new MarkerOptions()
					.position(filtro.getPunto())
					.title(getString(R.string.buscar));
			marker = map.addMarker(mo);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(filtro.getPunto(), 15));
		}
		catch(Exception e){Log.e(TAG, String.format("ActBuscar:setMarker:e:%s",e), e);}
	}
	private void setRadio()
	{
		if(circle != null) circle.remove();
		if(filtro == null || filtro.getPunto() == null || filtro.getRadio() < 1)return;
		circle = map.addCircle(new CircleOptions().center(filtro.getPunto())
				.radius(filtro.getRadio()).strokeColor(Color.TRANSPARENT)
				.fillColor(0x55AA0000));//Color.BLUE
	}

	//______________________________________________________________________________________________
	//// 4 LocationListener
	@Override
	public void onLocationChanged(Location location)
	{
		util.setLocation(location);
	}


	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_buscar, menu);
		if(!filtro.isOn())menu.findItem(R.id.menu_eliminar).setVisible(false);
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
		filtro.turnOff();
		util.return2Main(this, filtro);
	}
	private void buscar()
	{
		filtro.setNombre(txtNombre.getText().toString());
		if(filtro.isValid())
		{
			filtro.turnOn();
			util.return2Main(this, filtro);
		}
		else
		{
			eliminar();
		}
	}
}
