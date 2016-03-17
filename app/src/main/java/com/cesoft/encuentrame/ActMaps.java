package com.cesoft.encuentrame;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;
import android.view.View;

import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Lugar;
import com.cesoft.encuentrame.models.Ruta;


//TODO:Quitar boton guardar cuando es ruta...
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
		//------------------------------------------------------------------------------------------

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		_coordinatorLayout = (CoordinatorLayout)findViewById(R.id.coordinatorLayout);
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		//ImageButton btnSave = (ImageButton) findViewById(R.id.btnGuardar);btnSave.setOnClickListener(new View.OnClickListener()
		FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.btnGuardar);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent data = new Intent();
				if(_l != null)
				{
					_l.guardar(new AsyncCallback<Lugar>()
					{
						@Override
						public void handleResponse(Lugar l)
						{
							Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar), Snackbar.LENGTH_LONG).show();
							Intent data = new Intent();
							data.putExtra(Lugar.NOMBRE, _l);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							Snackbar.make(_coordinatorLayout, getString(R.string.error_guardar), Snackbar.LENGTH_LONG).show();
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
							Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar), Snackbar.LENGTH_LONG).show();
							Intent data = new Intent();
							data.putExtra(Aviso.NOMBRE, _a);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							Snackbar.make(_coordinatorLayout, getString(R.string.error_guardar), Snackbar.LENGTH_LONG).show();
						}
					});
				}
				if(_r != null)
				{
					_r.guardar(new AsyncCallback<Ruta>()
					{
						@Override
						public void handleResponse(Ruta a)
						{
							Snackbar.make(_coordinatorLayout, getString(R.string.ok_guardar), Snackbar.LENGTH_LONG).show();
							Intent data = new Intent();
							data.putExtra(Ruta.NOMBRE, _r);
							setResult(Activity.RESULT_OK, data);
							finish();
						}
						@Override
						public void handleFault(BackendlessFault backendlessFault)
						{
							Snackbar.make(_coordinatorLayout, getString(R.string.error_guardar), Snackbar.LENGTH_LONG).show();
						}
					});
				}
			}
		});
	}

	/**
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera.
	 * If Google Play services is not installed on the device, the user will be prompted to install it inside the SupportMapFragment.
	 * This method will only be triggered once the user has installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		_Map = googleMap;
		try{_Map.setMyLocationEnabled(true);}catch(SecurityException se){}

		if(_l != null)			/// LUGAR
		{
			setPosLugar(_l.getLatitud(), _l.getLongitud());
		}
		else if(_a != null)		/// AVISO
		{
			setPosLugar(_a.getLatitud(), _a.getLongitud());
		}
		else if(_r != null)		/// RUTA
		{
			//http://javapapers.com/android/draw-path-on-google-maps-android-api/
			//TODO: Draw a route...
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
				/** Traversing all routes */
				for (int i = 0; i < jRoutes.length(); i++)
				{
					jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
					List<HashMap<String, String>> path = new ArrayList<>();

					/** Traversing all legs */
					for (int j = 0; j < jLegs.length(); j++)
					{
						jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

						/** Traversing all steps */
						for (int k = 0; k < jSteps.length(); k++)
						{
							String polyline = (String)((JSONObject) ((JSONObject)jSteps.get(k)).get("polyline")).get("points");
							List<LatLng> list = decodePoly(polyline);

							/** Traversing all points */
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

		/**
		 * Method Courtesy :
		 * jeffreysambells.com/2010/05/27
		 * /decoding-polylines-from-google-maps-direction-api-with-java
		 * */
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
}
