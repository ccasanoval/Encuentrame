package com.cesoft.encuentrame3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;

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
import com.google.android.gms.maps.model.PolylineOptions;

import com.cesoft.encuentrame3.models.Fire;
import com.cesoft.encuentrame3.util.Log;
import com.cesoft.encuentrame3.util.Util;
import com.cesoft.encuentrame3.models.Aviso;
import com.cesoft.encuentrame3.models.Lugar;
import com.cesoft.encuentrame3.models.Ruta;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMaps extends FragmentActivity implements OnMapReadyCallback
{
	private static final String TAG = ActMaps.class.getSimpleName();

	private Util _util;
	private GoogleMap _Map;
	private Location _loc;
	private Marker _marker;
	private Circle _circle;

	private Lugar _l;
	private Aviso _a;
	private Ruta _r;

	private int _iTipo = Constantes.NADA;

	CoordinatorLayout _coordinatorLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_maps);

		_util = ((App)getApplication()).getGlobalComponent().util();

		//------------------------------------------------------------------------------------------
		try{_l = getIntent().getParcelableExtra(Lugar.NOMBRE);}catch(Exception e){_l=null;}
		try{_r = getIntent().getParcelableExtra(Ruta.NOMBRE);}catch(Exception e){_r=null;}
		try{_a = getIntent().getParcelableExtra(Aviso.NOMBRE);}catch(Exception e){_a=null;}
		try{_iTipo = getIntent().getIntExtra(Util.TIPO, Constantes.NADA);}catch(Exception e){_iTipo=Constantes.NADA;}
		//------------------------------------------------------------------------------------------

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.btnGuardar);
		FloatingActionButton fabVolver = (FloatingActionButton)findViewById(R.id.btnVolver);
		fabVolver.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					_util.return2Main(ActMaps.this, false, "");
				}
			});
		if(_iTipo != Constantes.NADA || _r != null)fab.setVisibility(View.GONE);

		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(_l != null)
				{
					_l.guardar(new Fire.CompletadoListener()
					{
						@Override
						protected void onDatos(String id)
						{
							Toast.makeText(ActMaps.this, getString(R.string.ok_guardar_lugar), Toast.LENGTH_LONG).show();
							Intent data = new Intent();
							data.putExtra(Lugar.NOMBRE, _l);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						@Override
						protected void onError(String err, int code)
						{
							Log.e(TAG, "guardar:handleFault:f:" + err);
							Toast.makeText(ActMaps.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
						}
					});
				}
				if(_a != null)
				{
					_a.guardar(new Fire.CompletadoListener()
					{
						@Override
						protected void onDatos(String id)
						{
							Toast.makeText(ActMaps.this, getString(R.string.ok_guardar_aviso), Toast.LENGTH_LONG).show();
							Intent data = new Intent();
							data.putExtra(Aviso.NOMBRE, _a);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						@Override
						protected void onError(String err, int code)
						{
							Log.e(TAG, "guardar:handleFault:f:" + err);
							Toast.makeText(ActMaps.this, String.format(getString(R.string.error_guardar), err), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
	}
	//______________________________________________________________________________________________
	@Override
	protected void onStart()
	{
		super.onStart();
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		newListeners();
	}
	@Override
	protected void onStop()
	{
		super.onStop();
		if(_Map != null)
		{
			_Map.clear();
			_Map = null;
		}
		delListeners();
		/*if(_GoogleApiClient != null)
		{
			_GoogleApiClient.unregisterConnectionCallbacks(this);
			_GoogleApiClient.unregisterConnectionFailedListener(this);
			_GoogleApiClient.disconnect();
			_GoogleApiClient = null;
		}
		_LocationRequest = null;*/
	}

	// This callback is triggered when the map is ready to be used. This is where we can add markers or lines, add listeners or move the camera.
	// If Google Play services is not installed on the device, the user will be prompted to install it inside the SupportMapFragment.
	// This method will only be triggered once the user has installed Google Play services and returned to the app.
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException se){Log.e(TAG, String.format("ActAviso:onMapReady:e:%s",se));}
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

		if(_l != null)			/// LUGAR
		{
			setPosLugar(_l.getLatitud(), _l.getLongitud());
			showLugar(_l);
		}
		else if(_a != null)		/// AVISO
		{
			setPosLugar(_a.getLatitud(), _a.getLongitud());
			showAviso(_a);
		}
		else if(_r != null)		/// RUTA
		{
			showRuta(_r);
		}
		else
		switch(_iTipo)
		{
		case Constantes.LUGARES:	showLugares();break;
		case Constantes.AVISOS:	showAvisos();break;
		case Constantes.RUTAS:	showRutas();break;
		}

		_Map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng latLng)
			{
				setPosLugar(latLng.latitude, latLng.longitude);
			}
		});
	}

	//______________________________________________________________________________________________
	private void setPosLugar(double lat, double lon)
	{
		if(_loc == null)_loc = new Location("dummyprovider");
		_loc.setLatitude(lat);
		_loc.setLongitude(lon);
		if(_l != null)
		{
			setMarker(_l.getNombre(), _l.getDescripcion());
		}
		else if(_a != null)
		{
			setMarker(_a.getNombre(), _a.getDescripcion());
			setMarkerRadius();
		}
		else if(_r != null)
		{
			_Map.animateCamera(CameraUpdateFactory.zoomTo(15));
		}
	}
	private void setMarker(String sTitulo, String sDescripcion)
	{
		try
		{
			LatLng pos = new LatLng(_loc.getLatitude(), _loc.getLongitude());
			if(_marker != null)_marker.remove();
			_Map.clear();
			MarkerOptions mo = new MarkerOptions()
					.position(pos)
					.title(sTitulo)
					.snippet(sDescripcion);
			_marker = _Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
			_Map.animateCamera(CameraUpdateFactory.zoomTo(15));
		}
		catch(Exception e){Log.e(TAG, "setMarker:e:"+e, e);}
	}
	private void setMarkerRadius()
	{
		LatLng pos = new LatLng(_loc.getLatitude(), _loc.getLongitude());
		if(_circle != null)_circle.remove();
		_circle = _Map.addCircle(new CircleOptions()
				.center(pos)
				.radius(_a.getRadio())
				.strokeColor(Color.TRANSPARENT)
				.fillColor(0x55AA0000));//Color.BLUE
	}


	//______________________________________________________________________________________________
	private int _iColor = Color.LTGRAY;
	private BitmapDescriptor getNextIcon()
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

	private void showLugar(Lugar l)
	{
		LatLng pos = new LatLng(l.getLatitud(), l.getLongitud());
		MarkerOptions mo = new MarkerOptions()
				.position(pos)
				.icon(getNextIcon())
				.title(l.getNombre())
				.snippet(l.getDescripcion());
		_marker = _Map.addMarker(mo);
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
	}
	private void showAviso(Aviso a)
	{
		//BitmapDescriptor bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
		LatLng pos = new LatLng(a.getLatitud(), a.getLongitud());
		MarkerOptions mo = new MarkerOptions()
				.position(pos)
				.icon(getNextIcon())
				.title(a.getNombre())
				.snippet(a.getDescripcion());
		_marker = _Map.addMarker(mo);
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
		_circle = _Map.addCircle(new CircleOptions()
				.center(pos)
				.radius(a.getRadio())
				.strokeColor(Color.TRANSPARENT)
				.fillColor(0x55000000 + _iColor));
				//.fillColor(0x55AA0000));//Color.BLUE
		//}catch(Exception e){System.err.println("ActMapas:showAviso:e:"+e);}
	}

	//----------------------------------------------------------------------------------------------
	private synchronized void showRuta(final Ruta r)
	{
		if(r == null)
		{
			Log.e(TAG, "showRuta:e:----------------------------------------------------------------- r == NULL");
			return;
		}
		Ruta.RutaPunto.getLista(r.getId(), new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(Ruta.RutaPunto[] aData)
			{
//if(r.getPuntosCount() != aData.length)Log.e(TAG, "showRuta:--------------------------------------------------------"+r.getPuntosCount()+"---<>--- "+aData.length);
				showRutaHelper(r, aData);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showRuta:e:-----------------------------------------------%s",err));
			}
		});
	}
	private void showRutaHelper(Ruta r, Ruta.RutaPunto[] aPts)
	{
//Log.e(TAG, "---------------------------------------------------------------------showRutaHelper:::"+r.getId()+" ::: "+aPts.length);
		try{
		if(aPts.length < 1)return;
		float distancia = 0;
		Ruta.RutaPunto ptoAnt = null;

		String INI = getString(R.string.ini);
		String FIN = getString(R.string.fin);
		PolylineOptions po = new PolylineOptions();
		BitmapDescriptor bm = getNextIcon();

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
				//if(pto.distanciaReal(gpIni) > 5 && pto.distanciaReal(gpFin) > 5)
				{
					mo.icon(bm);
					_Map.addMarker(mo.position(pos));
				}
			}
			po.add(pos);
		}
		po.width(5).color(_iColor);
		_Map.addPolyline(po);//Polyline line =
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()), 15));
		}catch(Exception e){Log.e(TAG, String.format("showRutaHelper:e:%s",e), e);}
	}


	//______________________________________________________________________________________________
	private Fire.DatosListener<Lugar> _lisLugar;
	private Fire.DatosListener<Aviso> _lisAviso;
	private Fire.DatosListener<Ruta> _lisRuta;
	//----------------------------------------------------------------------------------------------
	private void showLugares()
	{
		Lugar.getLista(_lisLugar);
	}
	//---
	private void showAvisos()
	{
		Aviso.getLista(_lisAviso);
	}
	//---
	private void showRutas()
	{
		Ruta.getLista(_lisRuta);
	}
	//----------------------------------------------------------------------------------------------
	private void delListeners()
	{
		if(_lisLugar!=null)_lisLugar.setListener(null);
		if(_lisAviso!=null)_lisAviso.setListener(null);
		if(_lisRuta!=null)_lisRuta.setListener(null);
	}
	private void newListeners()
	{
		delListeners();
		_lisLugar = new Fire.DatosListener<Lugar>()
		{
			@Override
			public void onDatos(Lugar[] aData)
			{
				for(Lugar o : aData)showLugar(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showLugares:e:--------------------------------------------LUGARES:GET:ERROR:%s",err));
			}
		};
		_lisAviso = new Fire.DatosListener<Aviso>()
		{
			@Override
			public void onDatos(Aviso[] aData)
			{
				for(Aviso o : aData)showAviso(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showAvisos:e:---------------------------------------------AVISOS:GET:ERROR:%s",err));
			}
		};
		_lisRuta = new Fire.DatosListener<Ruta>()
		{
			@Override
			public void onDatos(Ruta[] aData)
			{
				Log.e(TAG, "------------------------------------------------------------------------getLista : "+aData.length);
				for(Ruta o : aData)
					showRuta(o);
			}
			@Override
			public void onError(String err)
			{
				Log.e(TAG, String.format("showRutas:e:----------------------------------------------RUTAS:GET:ERROR:%s",err));
			}
		};

	}

}
