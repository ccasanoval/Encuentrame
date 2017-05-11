package com.cesoft.encuentrame3;

import android.graphics.Typeface;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.presenters.PreRuta;
import com.cesoft.encuentrame3.util.Util;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActRuta extends VistaBase implements PreRuta.IVistaRuta
{
	//private static final String TAG = ActRuta.class.getSimpleName();

	@Inject Util _util;
	@Inject PreRuta _presenter;

	@Override public void moveCamara(LatLng pos){_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));}

	//______________________________________________________________________________________________
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent(getApplicationContext()).inject(this);
		super.ini(_presenter, _util, new Ruta(), R.layout.act_ruta);
		super.onCreate(savedInstanceState);

		//-----------------------------------
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

		//-----------------------------------
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
			_presenter.onEliminar();
		else if(item.getItemId() == R.id.menu_estadisticas)
			_presenter.estadisticas();
		return super.onOptionsItemSelected(item);
	}
	//______________________________________________________________________________________________

	//______________________________________________________________________________________________
	// 4 OnMapReadyCallback
	@Override
	public void onMapReady(GoogleMap Map)
	{
		super.onMapReady(Map);

		if(_presenter.isNuevo())
		{
			_Map.moveCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		}
		else// if(_r.getPuntos().size() > 0)
		{
			_presenter.showRuta();
		}

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
}
