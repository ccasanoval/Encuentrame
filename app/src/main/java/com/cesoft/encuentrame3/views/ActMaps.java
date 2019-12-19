package com.cesoft.encuentrame3.views;

import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.App;
import com.cesoft.encuentrame3.R;
import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.presenters.PreMaps;
import com.cesoft.encuentrame3.util.GpsLocationCallback;
import com.cesoft.encuentrame3.util.Voice;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Lugar;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
//
public class ActMaps extends VistaBase implements PreMaps.IMapsView
{
	private static final String TAG = ActMaps.class.getSimpleName();

	private Marker marker;
	private Circle circle;

	@Inject PreMaps presenter;

	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		App.getComponent().inject(this);
		super.ini(presenter, new Lugar(), R.layout.act_maps);
		super.onCreate(savedInstanceState);
		FloatingActionButton fabGuardar = findViewById(R.id.btnGuardar);
		if( ! presenter.isAviso() && ! presenter.isLugar())
			fabGuardar.hide();
		fabGuardar.setOnClickListener(view -> presenter.guardar());
	}

	//----------------------------------------------------------------------------------------------
	@Override public void animateCamera()
	{
		map.animateCamera(CameraUpdateFactory.zoomTo(mapZoom));
	}

	//----------------------------------------------------------------------------------------------
	// This callback is triggered when the map is ready to be used. This is where we can add markers or lines, add listeners or move the camera.
	// If Google Play services is not installed on the device, the user will be prompted to install it inside the SupportMapFragment.
	// This method will only be triggered once the user has installed Google Play services and returned to the app.
	@Override
	public void onMapReady(GoogleMap map)
	{
		super.onMapReady(map);
		map.getUiSettings().setZoomControlsEnabled(true);

		//MARCADOR MULTILINEA --------------------------------------------
		this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
		{
			@Override public View getInfoWindow(Marker arg0){return null;}
			@Override public View getInfoContents(Marker marker)
			{
				LinearLayout info = new LinearLayout(ActMaps.this);
				info.setOrientation(LinearLayout.VERTICAL);

				TextView title = new TextView(ActMaps.this);
				title.setTextColor(Color.BLACK);
				title.setGravity(Gravity.CENTER);
				title.setTypeface(null, Typeface.BOLD);
				title.setText(marker.getTitle());

				TextView snippet = new TextView(ActMaps.this);
				snippet.setTextColor(Color.GRAY);
				snippet.setText(marker.getSnippet());

				info.addView(title);
				info.addView(snippet);
				return info;
			}
		});

		this.map.setOnMapClickListener(latLng -> presenter.setPosicion(latLng.latitude, latLng.longitude));

		presenter.dibujar();
	}

	//----------------------------------------------------------------------------------------------
	@Override public void setMarker(String sTitulo, String sDescripcion, LatLng pos)
	{
		try
		{
			if(marker != null) marker.remove();
			map.clear();
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(sTitulo)
					.snippet(sDescripcion);
			marker = map.addMarker(mo);
			map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			map.animateCamera(CameraUpdateFactory.zoomTo(mapZoom));
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}
	@Override public void setMarkerRadius(LatLng pos)
	{
		if(circle != null) circle.remove();
		circle = map.addCircle(new CircleOptions()
				.center(pos)
				.radius(presenter.getRadioAviso())
				.strokeColor(Color.TRANSPARENT)
				.fillColor(0x55AA0000));//Color.BLUE
	}

	//----------------------------------------------------------------------------------------------
	private int iColor = Color.BLUE;
	public BitmapDescriptor getNextIcon()
	{
		BitmapDescriptor bm;
		switch(iColor)
		{
		case Color.BLUE:
			iColor = Color.BLACK;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
			break;
		case Color.BLACK:
			iColor = Color.DKGRAY;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
			break;
		case Color.DKGRAY:
			iColor = Color.YELLOW;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
			break;
		case Color.YELLOW:
			iColor = Color.CYAN;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
			break;
		case Color.CYAN:
			iColor = Color.MAGENTA;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
			break;
		default:
		case Color.MAGENTA:
			iColor = Color.BLUE;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
			break;
		}
		return bm;
	}

	//----------------------------------------------------------------------------------------------
	public void showLugar(Lugar l)
	{
		LatLng pos = new LatLng(l.getLatitud(), l.getLongitud());
		MarkerOptions mo = new MarkerOptions()
				.position(pos)
				.icon(getNextIcon())
				.title(l.getNombre())
				.snippet(l.getDescripcion());
		marker = map.addMarker(mo);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, mapZoom));
	}
	//----------------------------------------------------------------------------------------------
	public void showAviso(Aviso a)
	{
		LatLng pos = new LatLng(a.getLatitud(), a.getLongitud());
		MarkerOptions mo = new MarkerOptions()
				.position(pos)
				.icon(getNextIcon())
				.title(a.getNombre())
				.snippet(a.getDescripcion());
		marker = map.addMarker(mo);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, mapZoom));
		circle = map.addCircle(new CircleOptions()
				.center(pos)
				.radius(a.getRadio())
				.strokeColor(Color.TRANSPARENT)
				.fillColor(0x55000000 + iColor));
	}
	@Override public void showRutaHelper(Ruta r, Ruta.RutaPunto[] aPts)
	{
		try
		{
			if(aPts.length < 1)return;
			float distancia = 0;
			Ruta.RutaPunto ptoAnt = null;

			String ini = getString(R.string.ini);
			String fin = getString(R.string.fin);
			PolylineOptions po = new PolylineOptions();

			Ruta.RutaPunto gpIni = aPts[0];
			Ruta.RutaPunto gpFin = aPts[aPts.length -1];
			for(Ruta.RutaPunto pto : aPts)
			{
				if(ptoAnt != null)
					distancia += pto.distanciaReal(ptoAnt);
				ptoAnt = pto;

				MarkerOptions mo = new MarkerOptions();
				mo.title(r.getNombre());
				Date date = new Date(pto.getFecha());

				String sDist;
				if(distancia > 3000)	sDist = String.format(Locale.ENGLISH, getString(R.string.info_dist2), distancia/1000);
				else					sDist = String.format(getString(R.string.info_dist), distancia);

				LatLng pos = new LatLng(pto.getLatitud(), pto.getLongitud());
				if(pto == gpIni)
				{
					mo.snippet(String.format(Locale.ENGLISH, "%s %s", ini, util.formatFechaTiempo(date)));
					mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					mo.rotation(45);
					map.addMarker(mo.position(pos));
				}
				else if(pto == gpFin)
				{
					mo.snippet(String.format(Locale.ENGLISH, "%s %s %s", fin, util.formatFechaTiempo(date), sDist));
					mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					mo.rotation(-45);
					map.addMarker(mo.position(pos));
				}
				po.add(pos);
			}

			po.width(5).color(iColor);
			map.addPolyline(po);//Polyline line =
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()), mapZoom));
		}
		catch(Exception e)
		{
			Log.e(TAG, "showRutaHelper:e:-------------------------------------------", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_mapa, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Subscribe(threadMode = ThreadMode.POSTING)
	public void onCommandEvent(Voice.CommandEvent event)
	{
		//Log.e(TAG, "onCommandEvent--------------------------- "+event.getCommand()+" / "+event.getText());
		Toast.makeText(this, event.getText(), Toast.LENGTH_LONG).show();

		switch(event.getCommand()) {
			case R.string.voice_cancel:
				presenter.onSalir(true);
				voice.speak(event.getText());
				break;
			case R.string.voice_stop_listening:
				voice.stopListening();
				voice.speak(event.getText());
				break;
			default:
				break;
		}
	}
}
