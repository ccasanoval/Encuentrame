package com.cesoft.encuentrame3;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import java.util.Locale;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.presenters.PreAviso;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActAviso extends VistaBase implements PreAviso.IVistaAviso
{
	private static final String TAG = ActAviso.class.getSimpleName();

	private TextView _lblPosicion;
		private void setPosLabel(double lat, double lon){_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", lat, lon));}
	private Switch  _swtActivo;
		public boolean isActivo() { return _swtActivo.isChecked(); }

	private static final String[] _asRadio = {"10 m", "50 m", "100 m", "200 m", "300 m", "400 m", "500 m", "750 m", "1 Km", "2 Km", "3 Km", "4 Km", "5 Km", "7.5 Km", "10 Km"};
	private static final int[]    _adRadio = { 10,     50,     100,     200,     300,     400,     500,     750,     1000,   2000,   3000,   4000,   5000,   7500,     10000};

	private Circle _circle;
	private Marker _marker;

	@Inject Util _util;
	@Inject PreAviso _presenter;

	//------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent(getApplicationContext()).inject(this);
		super.ini(_presenter, _util, new Aviso(), R.layout.act_aviso);
		super.onCreate(savedInstanceState);

		//------------------------------------
		ImageButton btnActPos = (ImageButton)findViewById(R.id.btnActPos);
		if(btnActPos != null)
		btnActPos.setOnClickListener(v ->
		{
			Location loc = _util.getLocation();
			if(loc != null)setPosLugar(loc);
		});

		//------------------------------------
		_lblPosicion = (TextView)findViewById(R.id.lblPosicion);
		setPosLabel(_presenter.getLatitud(), _presenter.getLongitud());

		_swtActivo = (Switch)findViewById(R.id.bActivo);
		_swtActivo.setChecked(_presenter.isActivo());
		_swtActivo.setOnCheckedChangeListener((buttonView, isChecked) -> _presenter.setActivo(isChecked));

		Spinner _spnRadio = (Spinner)findViewById(R.id.spnRadio);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, _asRadio);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		_spnRadio.setAdapter(adapter);
		_spnRadio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				_presenter.setRadio(_adRadio[position]);
				setMarker();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				_presenter.setRadio(100);//TODO:radio por defecto en settings
			}
		});

		for(int i=0; i < _adRadio.length; i++)
		{
			if(_presenter.getRadio() == _adRadio[i])
			{
				_spnRadio.setSelection(i);
				break;
			}
		}

		//------------------------------------
		if(_presenter.isNuevo())
			setTitle(getString(R.string.nuevo_aviso));
		else
			setTitle(getString(R.string.editar_aviso));

	}

	//____________________________________________________________________________________________________________________________________________________
	/// MENU
	//______________________________________________________________________________________________
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_aviso, menu);
		if(_presenter.isNuevo())
			menu.findItem(R.id.menu_eliminar).setVisible(false);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.menu_guardar)
			_presenter.guardar();
		else if(item.getItemId() == R.id.menu_eliminar)
			_presenter.eliminar();
		return super.onOptionsItemSelected(item);
	}


	//______________________________________________________________________________________________
	// OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap Map)
	{
		super.onMapReady(Map);
		_Map.setOnMapClickListener(latLng ->
		{
			_presenter.setSucio();
			setPosLugar(latLng.latitude, latLng.longitude);
		});
	}

	//______________________________________________________________________________________________
	@Override
	protected void setPosLugar(double lat, double lon)
	{
		_presenter.setLatitud(lat);_presenter.setLongitud(lon);
		_lblPosicion.setText(String.format(Locale.ENGLISH, "%.5f/%.5f", _presenter.getLatitud(), _presenter.getLongitud()));
		setMarker();
	}
	private void setMarker()
	{
		if(_Map == null)return;
		try
		{
			if(_marker != null)_marker.remove();
			LatLng pos = new LatLng(_presenter.getLatitud(), _presenter.getLongitud());
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(_presenter.getNombre()).snippet(_presenter.getDescripcion());
			_marker = _Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));

			if(_circle != null)_circle.remove();
			_circle = _Map.addCircle(new CircleOptions()
					.center(pos)
					.radius(_presenter.getRadio())
					.strokeColor(Color.TRANSPARENT)
					.fillColor(0x55AA0000));//Color.BLUE
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}

}
