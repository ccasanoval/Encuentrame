package com.cesoft.encuentrame;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.graphics.Color;
import android.view.View;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Build;
import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoLocation;
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

import java.text.DateFormat;
import java.util.Date;

import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Lugar;
import com.cesoft.encuentrame.models.Ruta;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMaps extends FragmentActivity implements OnMapReadyCallback
{
	private GoogleMap _Map;
	private Location _loc;
	private Marker _marker;
	private Circle _circle;

	private Lugar _l;
	private Aviso _a;
	private Ruta _r;

	private int _iTipo = Util.NADA;

	CoordinatorLayout _coordinatorLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_maps);

		//------------------------------------------------------------------------------------------
		try{_l = getIntent().getParcelableExtra(Lugar.NOMBRE);}catch(Exception e){_l=null;}
		try{_r = getIntent().getParcelableExtra(Ruta.NOMBRE);}catch(Exception e){_r=null;}
		try{_a = getIntent().getParcelableExtra(Aviso.NOMBRE);}catch(Exception e){_a=null;}
		try{_iTipo = getIntent().getIntExtra(Util.TIPO, Util.NADA);}catch(Exception e){_iTipo=Util.NADA;}
System.err.println("**********************_iTipo="+_iTipo);
		//------------------------------------------------------------------------------------------

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//Volver
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.btnVolver);
		fab.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					Util.return2Main(ActMaps.this, false, "");
				}
			});
		//Guardar
		fab = (FloatingActionButton)findViewById(R.id.btnGuardar);
		if(_iTipo != Util.NADA || _r != null)
		{
			/*fab.setImageResource(getResources().getIdentifier("@android:drawable/ic_menu_revert", null, null));
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				fab.setForegroundGravity(android.view.Gravity.RIGHT + android.view.Gravity.BOTTOM);
			fab.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					Util.return2Main(ActMaps.this, false, "");
				}
			});*/
		}
		else
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(_l != null)
				{
					_l.guardar(new Firebase.CompletionListener()
					{
						@Override
						public void onComplete(FirebaseError err, Firebase firebase)
						{
							if(err != null)
							{
								Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar_lugar), Snackbar.LENGTH_LONG).show();
								Intent data = new Intent();
								data.putExtra(Lugar.NOMBRE, _l);
								setResult(Activity.RESULT_OK, data);
								finish();
							}
							else
							{
								Snackbar.make(_coordinatorLayout, getString(R.string.error_guardar), Snackbar.LENGTH_LONG).show();
							}
						}
					});
				}
				if(_a != null)
				{
					_a.guardar(new Firebase.CompletionListener()
					{
						@Override
						public void onComplete(FirebaseError err, Firebase firebase)
						{
							if(err != null)
							{
								Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar_aviso), Snackbar.LENGTH_LONG).show();
								Intent data = new Intent();
								data.putExtra(Aviso.NOMBRE, _a);
								setResult(Activity.RESULT_OK, data);
								finish();
							}
							else
							{
								Snackbar.make(_coordinatorLayout, getString(R.string.error_guardar), Snackbar.LENGTH_LONG).show();
							}
						}
					});
				}
			}
		});

	}

	// This callback is triggered when the map is ready to be used. This is where we can add markers or lines, add listeners or move the camera.
	// If Google Play services is not installed on the device, the user will be prompted to install it inside the SupportMapFragment.
	// This method will only be triggered once the user has installed Google Play services and returned to the app.
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException se){System.err.println("ActAviso:onMapReady:e:"+se);}

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
		case Util.LUGARES:	showLugares();break;
		case Util.AVISOS:	showAvisos();break;
		case Util.RUTAS:	showRutas();break;
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
			_l.setLatitud(lat);
			_l.setLongitud(lon);
			setMarker(_l.getNombre(), _l.getDescripcion());
		}
		else if(_a != null)
		{
			_a.setLatitud(lat);
			_a.setLongitud(lon);
			//_a.setLatitud(lat);
			//_a.setLongitud(lon);
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
		catch(Exception e){System.err.println("ActMaps:setMarker:e:"+e);}
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
		//try{
		LatLng pos = new LatLng(l.getLatitud(), l.getLongitud());
		MarkerOptions mo = new MarkerOptions()
				.position(pos)
				.icon(getNextIcon())
				.title(l.getNombre())
				.snippet(l.getDescripcion());
		_marker = _Map.addMarker(mo);
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
		//}catch(Exception e){System.err.println("ActLugar:setMarker:e:"+e);}
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
	private void showRuta(final Ruta r)
	{
System.err.println("----------showRuta:"+r);
		r.getPuntos(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot ds)
			{
System.err.println("----------showRuta:"+ds);
				int i = 0;
				Ruta.RutaPunto[] aPts = new Ruta.RutaPunto[(int)ds.getChildrenCount()];
				for(DataSnapshot o : ds.getChildren())
				{
					aPts[i++] = o.getValue(Ruta.RutaPunto.class);//TODO:go to map pos
System.err.println("----------showRuta:2:"+aPts[i-1]);
				}
				showRutaHelper(r, aPts);
			}
			@Override
			public void onCancelled(FirebaseError firebaseError)
			{
				//Snackbar.make(_coordinatorLayout, getString(R.string.error_load_rute_pts), Snackbar.LENGTH_LONG).show();
				Snackbar.make(_coordinatorLayout, "Error al obtener los puntos de la ruta", Snackbar.LENGTH_LONG).show();//TODO:
			}
		});
	}
	private void showRutaHelper(Ruta r, Ruta.RutaPunto[] aPts)
	{
		if(aPts.length < 1)return;
System.err.println("----------showRutaHelper:1:"+aPts.length);
		DateFormat df = java.text.DateFormat.getDateTimeInstance();

		String INI = getString(R.string.ini);
		String FIN = getString(R.string.fin);
		PolylineOptions po = new PolylineOptions();
		BitmapDescriptor bm = getNextIcon();

		Ruta.RutaPunto gpIni = aPts[0];
		Ruta.RutaPunto gpFin = aPts[aPts.length -1];
		for(int i=0; i < aPts.length; i++)
		{
			Ruta.RutaPunto pto = aPts[i];
System.err.println("----------showRutaHelper:2:"+pto);
			MarkerOptions mo = new MarkerOptions();
			mo.title(r.getNombre());
			Date date = pto.getFecha();
			if(date != null)
				mo.snippet(df.format(date));

			LatLng pos = new LatLng(pto.getLatitud(), pto.getLongitud());
System.err.println("showRuta: " + pos);
			if(pto.equalTo(gpIni))//if(pto.getLatitud() == gpIni.getLatitud() && pto.getLongitud() == gpIni.getLongitud())//It's not possible to establish the z order for the marker...
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				mo.snippet(INI + df.format(date));
				mo.rotation(135);
				_Map.addMarker(mo.position(pos));
			}
			else if(pto.equalTo(gpFin))// getLatitud() == gpFin.getLatitud() && pto.getLongitud() == gpFin.getLongitud())
			{
				mo.snippet(FIN + df.format(date));
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
				//double disIni = (pt.getLatitude() - gpIni.getLatitude())*(pt.getLatitude() - gpIni.getLatitude()) + (pt.getLongitude() - gpIni.getLongitude())*(pt.getLongitude() - gpIni.getLongitude());
				//if(disIni < 0.00000001)pos = new LatLng(pos.latitude + 0.00001, pos.longitude + 0.00001);
				mo.rotation(45);
				_Map.addMarker(mo.position(pos));
			}
			else
			{
				if(pto.distancia2(gpIni) > 0.00000001 || pto.distancia2(gpFin) > 0.00000001)
				{
					//System.err.println("------- MID " + r.getNombre() + " : " + pos + " : " + df.format(date) + "            " + (pt.getLatitude() - gpIni.getLatitude()));
					mo.icon(bm);
					_Map.addMarker(mo.position(pos));
				}
				//Punto igual a ini o fin
				//else System.err.println("------- NO " + r.getNombre() + " : " + pos + " : " + df.format(date)+":::::::::::::::"+disIni+"-"+disFin+"----"+(pt.getLatitude() - gpIni.getLatitude())+"...."+(pt.getLatitude() - gpIni.getLatitude())*(pt.getLatitude() - gpIni.getLatitude()));
			}
			po.add(pos);
		}
		po.width(5).color(_iColor);
		_Map.addPolyline(po);//Polyline line =
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpIni.getLatitud(), gpIni.getLongitud()), 15));
	}


	//______________________________________________________________________________________________

	private void showLugares()
	{
		Lugar.getLista(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot lugares)
			{
				long n = lugares.getChildrenCount();
				System.err.println("---------LUGARES:GET:OK:" + n);
				if(n < 1)return;
				for(DataSnapshot lugar : lugares.getChildren())
					showLugar(lugar.getValue(Lugar.class));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("---------LUGARES:GET:ERROR:" + err);//LUGARES:GET:ERROR:BackendlessFault{ code: '1009', message: 'Unable to retrieve data - unknown entity' }
			}
		});
	}
	private void showAvisos()
	{
		Aviso.getLista(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot avisos)
			{
				long n = avisos.getChildrenCount();
				System.err.println("---------AVISOS:GET:OK:" + n);
				if(n < 1)return;
				for(DataSnapshot aviso : avisos.getChildren())
					showAviso(aviso.getValue(Aviso.class));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("---------AVISOS:GET:ERROR:" + err);//LUGARES:GET:ERROR:BackendlessFault{ code: '1009', message: 'Unable to retrieve data - unknown entity' }
			}
		});
	}
	private void showRutas()
	{
		Ruta.getLista(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot rutas)
			{
				long n = rutas.getChildrenCount();
				System.err.println("---------RUTAS:GET:OK:" + n);
				if(n < 1)return;
				for(DataSnapshot ruta : rutas.getChildren())
					showRuta(ruta.getValue(Ruta.class));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("---------RUTAS:GET:ERROR:" + err);//LUGARES:GET:ERROR:BackendlessFault{ code: '1009', message: 'Unable to retrieve data - unknown entity' }
			}
		});
	}
}
