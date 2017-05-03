package com.cesoft.encuentrame3;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cesoft.encuentrame3.models.Ruta;
import com.cesoft.encuentrame3.util.Constantes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMaps extends FragmentActivity implements OnMapReadyCallback, PreMaps.MapsView
{
	private static final String TAG = ActMaps.class.getSimpleName();

	private GoogleMap _Map;
	private Marker _marker;
	private Circle _circle;

	@Inject Util _util;
	@Inject PreMaps _presenter;

	//----------------------------------------------------------------------------------------------
	@Override public void onBackPressed() { _presenter.onSalir(); }
	//----------------------------------------------------------------------------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_maps);

		//_util = ((App)getApplication()).getGlobalComponent().util();
		((App)getApplication()).getGlobalComponent().inject(this);
		_presenter.ini(this);
		_presenter.loadObjeto();

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		//_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		FloatingActionButton fabVolver = (FloatingActionButton)findViewById(R.id.btnVolver);
		fabVolver.setOnClickListener(view -> _presenter.onSalir()); //_util.return2Main(ActMaps.this, false, ""));

		FloatingActionButton fabGuardar = (FloatingActionButton)findViewById(R.id.btnGuardar);
		if(_presenter.isRuta())fabGuardar.setVisibility(View.GONE);
		fabGuardar.setOnClickListener(view -> _presenter.guardar());

		FloatingActionButton fabBuscar = (FloatingActionButton)findViewById(R.id.fabBuscar);
		if(fabBuscar != null)fabBuscar.setOnClickListener(view -> _util.onBuscar(this, _Map, _fMapZoom));

		if(savedInstanceState != null)
			_fMapZoom = savedInstanceState.getFloat(MAP_ZOOM, 15);
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
	protected void onStart()
	{
		super.onStart();
		_presenter.subscribe(this);
		((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
	}
	@Override
	protected void onStop()
	{
		super.onStop();
		_presenter.unsubscribe();
		if(_Map != null)
		{
			_Map.clear();
			_Map = null;
		}
	}

	// This callback is triggered when the map is ready to be used. This is where we can add markers or lines, add listeners or move the camera.
	// If Google Play services is not installed on the device, the user will be prompted to install it inside the SupportMapFragment.
	// This method will only be triggered once the user has installed Google Play services and returned to the app.
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}
		catch(SecurityException se){Log.e(TAG, "ActAviso:onMapReady:e:------------------------------",se);}
		_Map.setOnCameraMoveListener(() -> { if(_Map!=null)_fMapZoom = _Map.getCameraPosition().zoom; });
		//MARCADOR MULTILINEA --------------------------------------------
		_Map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
		{
			@Override
			public View getInfoWindow(Marker arg0){return null;}
			@Override
			public View getInfoContents(Marker marker)
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

		if(_presenter.onMapReady())
		switch(_presenter.getTipo())
		{
		case Constantes.LUGARES:_presenter.showLugares();break;
		case Constantes.AVISOS:	_presenter.showAvisos();break;
		case Constantes.RUTAS:	_presenter.showRutas();break;
		}

		_Map.setOnMapClickListener(latLng -> _presenter.setPosLugar(latLng.latitude, latLng.longitude));
	}

	//______________________________________________________________________________________________
	@Override public void setMarker(String sTitulo, String sDescripcion, LatLng pos)
	{
		try
		{
			if(_marker != null)_marker.remove();
			_Map.clear();
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(sTitulo)
					.snippet(sDescripcion);
			_marker = _Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:-------------------------------------------------", e);}
	}
	@Override public void setMarkerRadius(LatLng pos)
	{
		if(_circle != null)_circle.remove();
		_circle = _Map.addCircle(new CircleOptions()
				.center(pos)
				.radius(_presenter.getRadioAviso())
				.strokeColor(Color.TRANSPARENT)
				.fillColor(0x55AA0000));//Color.BLUE
	}


	//______________________________________________________________________________________________
	private int _iColor = Color.LTGRAY;
	public BitmapDescriptor getNextIcon()
	{
		BitmapDescriptor bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
		switch(_iColor)
		{
		case Color.MAGENTA:
			_iColor = Color.BLUE;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
			break;
		case Color.BLUE:
			_iColor = Color.BLACK;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
			break;
		case Color.BLACK:
			_iColor = Color.DKGRAY;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
			break;
		case Color.DKGRAY:
			_iColor = Color.YELLOW;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
			break;
		case Color.YELLOW:
			_iColor = Color.CYAN;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
			break;
		case Color.CYAN:
			_iColor = Color.LTGRAY;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
			break;
		case Color.LTGRAY:
			_iColor = Color.MAGENTA;
			bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
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
		_marker = _Map.addMarker(mo);
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));
	}
	//----------------------------------------------------------------------------------------------
	public void showAviso(Aviso a)
	{
		//BitmapDescriptor bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
		LatLng pos = new LatLng(a.getLatitud(), a.getLongitud());
		MarkerOptions mo = new MarkerOptions()
				.position(pos)
				.icon(getNextIcon())
				.title(a.getNombre())
				.snippet(a.getDescripcion());
		_marker = _Map.addMarker(mo);
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));
		_circle = _Map.addCircle(new CircleOptions()
				.center(pos)
				.radius(a.getRadio())
				.strokeColor(Color.TRANSPARENT)
				.fillColor(0x55000000 + _iColor));
				//.fillColor(0x55AA0000));//Color.BLUE
		//}catch(Exception e){System.err.println("ActMapas:showAviso:e:"+e);}
	}
	@Override public void showRutaHelper(Ruta r, Ruta.RutaPunto[] aPts)
	{
//Log.e(TAG, "---------------------------------------------------------------------showRutaHelper:::"+r.getId()+" ::: "+aPts.length);
		try
		{
			if(aPts.length < 1)return;
			float distancia = 0;
			Ruta.RutaPunto ptoAnt = null;
			BitmapDescriptor bm = getNextIcon();

			String INI = getString(R.string.ini);
			String FIN = getString(R.string.fin);
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
				Date date = pto.getFecha();

				String sDist;
				if(distancia > 3000)	sDist = String.format(Locale.ENGLISH, getString(R.string.info_dist2), distancia/1000);
				else					sDist = String.format(getString(R.string.info_dist), distancia);

				LatLng pos = new LatLng(pto.getLatitud(), pto.getLongitud());
				if(pto == gpIni)
				{
					mo.snippet(String.format(Locale.ENGLISH, "%s %s", INI, _util.formatFechaTiempo(date)));
					mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					mo.rotation(45);
					_Map.addMarker(mo.position(pos));
				}
				else if(pto == gpFin)
				{
					mo.snippet(String.format(Locale.ENGLISH, "%s %s %s", FIN, _util.formatFechaTiempo(date), sDist));
					mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
					mo.rotation(-45);
					_Map.addMarker(mo.position(pos));
				}
				else
				{
					mo.snippet(String.format(Locale.ENGLISH, "%s %s %s", getString(R.string.info_time), _util.formatFechaTiempo(date), sDist));
					mo.icon(bm);
					_Map.addMarker(mo.position(pos));
				}
				po.add(pos);
			}

			po.width(5).color(_iColor);
			_Map.addPolyline(po);//Polyline line =
			_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()), _fMapZoom));
			//addLinea(po);
			//moveCamera(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()));
		}catch(Exception e){Log.e(TAG, "showRutaHelper:e:-------------------------------------------", e);}
	}


	@Override public Activity getAct() { return this; }
	//@Override public void iniEspera() { }
	//@Override public void finEspera() { }
	@Override public void toast(int msg)
	{
		Toast.makeText(this, getString(msg), Toast.LENGTH_LONG).show();
	}
	@Override public void toast(int msg, String err)
	{
		Toast.makeText(this, String.format(getString(msg), err), Toast.LENGTH_LONG).show();
	}
	@Override public void animateCamera()
	{
		_Map.animateCamera(CameraUpdateFactory.zoomTo(_fMapZoom));
	}
	/*@Override public void moveCamera(LatLng pos)
	{
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, _fMapZoom));
	}
	@Override public void addLinea(PolylineOptions po)
	{
		po.width(5).color(_iColor);
		_Map.addPolyline(po);//Polyline line =
	}
	@Override public void addMarcador(MarkerOptions mo)
	{
		_Map.addMarker(mo);
	}*/

}
