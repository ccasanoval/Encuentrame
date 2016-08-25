package com.cesoft.encuentrame2;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.graphics.Color;
import android.view.View;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.backendless.BackendlessCollection;
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

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame2.models.Aviso;
import com.cesoft.encuentrame2.models.Lugar;
import com.cesoft.encuentrame2.models.Ruta;


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

		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.btnGuardar);
		FloatingActionButton fabVolver = (FloatingActionButton)findViewById(R.id.btnVolver);
		fabVolver.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					Util.return2Main(ActMaps.this, false, "");
				}
			});
		if(_iTipo != Util.NADA || _r != null)fab.setVisibility(View.GONE);
		/*if(_iTipo != Util.NADA || _r != null)
		{
			fab.setImageResource(getResources().getIdentifier("@android:drawable/ic_menu_revert", null, null));
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				fab.setForegroundGravity(android.view.Gravity.RIGHT + android.view.Gravity.BOTTOM);
			fab.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					Util.return2Main(ActMaps.this, false, "");
				}
			});
		}
		else*/
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(_l != null)
				{
					_l.guardar(new AsyncCallback<Lugar>()
					{
						@Override
						public void handleResponse(Lugar l)
						{
							Toast.makeText(ActMaps.this, getString(R.string.ok_guardar_lugar), Toast.LENGTH_LONG).show();
							//Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar_lugar), Snackbar.LENGTH_LONG).show();
							Intent data = new Intent();
							data.putExtra(Lugar.NOMBRE, _l);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							System.err.println("ActMaps:guardar:handleFault:f:" + backendlessFault);
							Toast.makeText(ActMaps.this, String.format(getString(R.string.error_guardar), backendlessFault), Toast.LENGTH_LONG).show();
							//Snackbar.make(_coordinatorLayout, String.format(getString(R.string.error_guardar), backendlessFault), Snackbar.LENGTH_LONG).show();
						}
					});
				}
				if(_a != null)
				{
					_a.guardar(new AsyncCallback<Aviso>()
					{
						@Override
						public void handleResponse(Aviso a)
						{
							Toast.makeText(ActMaps.this, getString(R.string.ok_guardar_aviso), Toast.LENGTH_LONG).show();
							//Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar_aviso), Snackbar.LENGTH_LONG).show();
							Intent data = new Intent();
							data.putExtra(Aviso.NOMBRE, _a);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							//Snackbar.make(_coordinatorLayout, getString(R.string.error_guardar), Snackbar.LENGTH_LONG).show();
							Toast.makeText(ActMaps.this, String.format(getString(R.string.error_guardar), backendlessFault), Toast.LENGTH_LONG).show();
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
			if(_r.getPuntos().size() > 0)
				setPosLugar(_r.getPuntos().get(0).getLatitude(), _r.getPuntos().get(0).getLongitude());
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
			_l.setLatLon(lat, lon);
			setMarker(_l.getNombre(), _l.getDescripcion());
		}
		else if(_a != null)
		{
			_a.setLatLon(lat, lon);
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
	private void showRuta(Ruta r)
	{
		if(r.getPuntos().size() < 1)return;

		//DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
		//DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
		DateFormat df = java.text.DateFormat.getDateTimeInstance();

		String INI = getString(R.string.ini);
		String FIN = getString(R.string.fin);
		PolylineOptions po = new PolylineOptions();
		BitmapDescriptor bm = getNextIcon();

		GeoPoint gpIni = r.getPuntos().get(0);
		GeoPoint gpFin = r.getPuntos().get(r.getPuntos().size() - 1);
		for(GeoPoint pt : r.getPuntos())
		{
			MarkerOptions mo = new MarkerOptions();
			mo.title(r.getNombre());
			Date date = r.getFechaPunto(pt);
			if(date != null)
				mo.snippet(df.format(date));//mo.snippet(dateFormat.format(date) + " " + timeFormat.format(date));

			LatLng pos = new LatLng(pt.getLatitude(), pt.getLongitude());
System.err.println("showRuta: " + pos);
			if(pt == gpIni)//It's not possible to establish the z order for the marker...
			{
				mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
				mo.snippet(INI + df.format(date));
				mo.rotation(135);
				_Map.addMarker(mo.position(pos));
			}
			else if(pt == gpFin)
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
				//else if((pt.getLatitude() != gpIni.getLatitude() || pt.getLongitude() != gpIni.getLongitude()) && (pt.getLatitude() != gpFin.getLatitude() || pt.getLongitude() != gpFin.getLongitude()))
				//else if(Location.distanceBetween(pt.getLatitude(), pt.getLongitude(), gpIni.getLatitude(), gpFin.getLongitude()))
				double disIni = (pt.getLatitude() - gpIni.getLatitude())*(pt.getLatitude() - gpIni.getLatitude()) + (pt.getLongitude() - gpIni.getLongitude())*(pt.getLongitude() - gpIni.getLongitude());
				double disFin = (pt.getLatitude() - gpFin.getLatitude())*(pt.getLatitude() - gpFin.getLatitude()) + (pt.getLongitude() - gpFin.getLongitude())*(pt.getLongitude() - gpFin.getLongitude());
				if(disIni > 0.00000001 || disFin > 0.00000001)
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
		_Map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gpIni.getLatitude(), gpIni.getLongitude()), 15));
	}


	//______________________________________________________________________________________________

	private void showLugares()
	{
		Lugar.getLista(new AsyncCallback<BackendlessCollection<Lugar>>()
		{
			@Override
			public void handleResponse(BackendlessCollection<Lugar> lugares)
			{
				int n = lugares.getTotalObjects();
				System.err.println("---------LUGARES:GET:OK:" + n);
				if(n < 1)return;
				for(Lugar lugar : lugares.getCurrentPage())
					showLugar(lugar);
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("---------LUGARES:GET:ERROR:" + backendlessFault);//LUGARES:GET:ERROR:BackendlessFault{ code: '1009', message: 'Unable to retrieve data - unknown entity' }
			}
		});
	}
	private void showAvisos()
	{
		Aviso.getLista(new AsyncCallback<BackendlessCollection<Aviso>>()
		{
			@Override
			public void handleResponse(BackendlessCollection<Aviso> avisos)
			{
				int n = avisos.getTotalObjects();
				System.err.println("---------AVISOS:GET:OK:" + n);
				if(n < 1)return;
				for(Aviso aviso : avisos.getCurrentPage())
					showAviso(aviso);
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("---------AVISOS:GET:ERROR:" + backendlessFault);
			}
		});
	}
	private void showRutas()
	{
		Ruta.getLista(new AsyncCallback<BackendlessCollection<Ruta>>()
		{
			@Override
			public void handleResponse(BackendlessCollection<Ruta> rutas)
			{
				int n = rutas.getTotalObjects();
				System.err.println("---------RUTAS:GET:OK:" + n);
				if(n < 1)return;
				for(Ruta ruta : rutas.getCurrentPage())
					showRuta(ruta);
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("---------RUTAS:GET:ERROR:" + backendlessFault);
			}
		});
	}


/*
	//______________________________________________________________________________________________
	private String getMapsApiDirectionsUrl(GeoPoint[] pts)
	{
		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?waypoints=optimize:true|");
		for(GeoPoint pt : pts)//for(int i=0; i < pts.length; i++)
		{
			sb.append(pt.getLatitude());
			sb.append(",");
			sb.append(pt.getLongitude());
			sb.append("|");
		}
		sb.append("&sensor=false");
		return sb.toString();
	}

	private void addMarkers(GeoPoint[] pts)
	{
		if(_Map != null)
		{
			for(GeoPoint pt : pts)
				_Map.addMarker(new MarkerOptions().position(new LatLng(pt.getLatitude(), pt.getLongitude())));//.title("")
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	private class ReadTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... sURL)
		{
			String data = "";
			HttpURLConnection c = null;
			try
			{
				URL u = new URL(sURL[0]);
				c = (HttpURLConnection) u.openConnection();
				c.setRequestMethod("GET");
				c.setRequestProperty("Content-length", "0");
				c.setUseCaches(false);
				c.setAllowUserInteraction(false);
				//c.setConnectTimeout(1000);
				//c.setReadTimeout(1000);
				c.connect();
				int status = c.getResponseCode();

				switch (status)
				{
				case 200:
				case 201:
					BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while((line = br.readLine()) != null)
					{
						sb.append(line);
						sb.append("\n");
					}
					br.close();
					return sb.toString();
				}
			}
			catch(Exception e)
			{
				System.err.println("");
			}
			finally
			{
				if(c != null)try{c.disconnect();}catch(Exception e){System.err.println("ReadTask:doInBackground:e:"+e);}
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////
	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>
	{
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData)
		{
			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;
			try
			{
				jObject = new JSONObject(jsonData[0]);
				PathJSONParser parser = new PathJSONParser();
				routes = parser.parse(jObject);
			}
			catch(Exception e){e.printStackTrace();}
			return routes;
		}

		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> routes)
		{
			ArrayList<LatLng> points;
			PolylineOptions polyLineOptions = null;

			// traversing through routes
			for(int i = 0; i < routes.size(); i++)
			{
				points = new ArrayList<>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = routes.get(i);

				for(int j=0; j < path.size(); j++)
				{
					HashMap<String, String> point = path.get(j);
					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);
					points.add(position);
				}

				polyLineOptions.addAll(points);
				polyLineOptions.width(2);
				polyLineOptions.color(Color.BLUE);
			}

			_Map.addPolyline(polyLineOptions);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	class PathJSONParser
	{
		public List<List<HashMap<String, String>>> parse(JSONObject jObject)
		{
			List<List<HashMap<String, String>>> routes = new ArrayList<>();
			JSONArray jRoutes, jLegs, jSteps;
			try
			{
				jRoutes = jObject.getJSONArray("routes");
				// Traversing all routes
				for (int i = 0; i < jRoutes.length(); i++)
				{
					jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
					List<HashMap<String, String>> path = new ArrayList<>();

					// Traversing all legs
					for (int j = 0; j < jLegs.length(); j++)
					{
						jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

						// Traversing all steps
						for (int k = 0; k < jSteps.length(); k++)
						{
							String polyline = (String)((JSONObject) ((JSONObject)jSteps.get(k)).get("polyline")).get("points");
							List<LatLng> list = decodePoly(polyline);

							// Traversing all points
							for (int l = 0; l < list.size(); l++)
							{
								HashMap<String, String> hm = new HashMap<>();
								hm.put("lat", Double.toString((list.get(l)).latitude));
								hm.put("lng", Double.toString((list.get(l)).longitude));
								path.add(hm);
							}
						}
						routes.add(path);
					}
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return routes;
		}

//
//		 * Method Courtesy :
//		 * jeffreysambells.com/2010/05/27
//		 * /decoding-polylines-from-google-maps-direction-api-with-java
//		 *
		private List<LatLng> decodePoly(String encoded)
		{
			List<LatLng> poly = new ArrayList<>();
			int index = 0, len = encoded.length();
			int lat = 0, lng = 0;

			while (index < len)
			{
				int b, shift = 0, result = 0;
				do
				{
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				}
				while (b >= 0x20);
				int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lat += dlat;

				shift = 0;
				result = 0;
				do
				{
					b = encoded.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				}
				while (b >= 0x20);
				int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
				lng += dlng;

				LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
				poly.add(p);
			}
			return poly;
		}
	}
	*/
}
