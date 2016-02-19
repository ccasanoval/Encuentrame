package com.cesoft.encuentrame;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.backendless.geo.GeoPoint;
import com.cesoft.encuentrame.models.Aviso;
import com.cesoft.encuentrame.models.Lugar;
import com.cesoft.encuentrame.models.Objeto;
import com.cesoft.encuentrame.models.Ruta;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.graphics.Color;



////////////////////////////////////////////////////////////////////////////////////////////////////
public class ActMaps extends FragmentActivity implements OnMapReadyCallback
{
	private GoogleMap _Map;

	private Lugar _l;
	private Aviso _a;
	private Ruta _r;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_maps);

		//------------------------------------------------------------------------------------------
		try
		{
			_l = null; _a = null; _r = null;
			Objeto o = this.getIntent().getParcelableExtra(Objeto.NOMBRE);
			if(o.getClass().isInstance(Lugar.class))_l=(Lugar)o;
			else if(o.getClass().isInstance(Aviso.class))_a=(Aviso)o;
			else if(o.getClass().isInstance(Ruta.class))_r=(Ruta)o;
System.err.println("ActAviso:onCreate:++++++++++++++++" + _a);
			//setValores();
		}
		catch(Exception e)
		{
			System.err.println("ActAviso:onCreate:ERROR:"+e);
			this.finish();
		}
		//------------------------------------------------------------------------------------------

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
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

		if(_l != null)			/// LUGAR
		{
			LatLng pos = new LatLng(_l.getLugar().getLatitude(), _l.getLugar().getLongitude());
			MarkerOptions mo = new MarkerOptions().position(pos).title(_l.getNombre()).snippet(_l.getDescripcion());//TODO: Add texto de descripcion...
			_Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
		}
		else if(_a != null)		/// AVISO
		{
			LatLng pos = new LatLng(_a.getLugar().getLatitude(), _a.getLugar().getLongitude());
			MarkerOptions mo = new MarkerOptions().position(pos).title(_a.getNombre()).snippet(_a.getDescripcion());//TODO: Add texto de descripcion...
			_Map.addMarker(mo);
			_Map.moveCamera(CameraUpdateFactory.newLatLng(pos));
		}
		else if(_r != null)		/// RUTA
		{
			//http://javapapers.com/android/draw-path-on-google-maps-android-api/
			//TODO: Draw a route...
		}
	}





	private String getMapsApiDirectionsUrl(GeoPoint[] pts)
	{
		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?waypoints=optimize:true|");
		for(int i=0; i < pts.length; i++)
		{
			sb.append(pts[i].getLatitude());
			sb.append(",");
			sb.append(pts[i].getLongitude());
			sb.append("|");
		}
		sb.append("&sensor=false");
		return sb.toString();
	}

	private void addMarkers(GeoPoint[] pts)
	{
		if(_Map != null)
		{
			for(int i=0; i < pts.length; i++)
				_Map.addMarker(new MarkerOptions().position(new LatLng(pts[i].getLatitude(), pts[i].getLongitude())));//.title("")
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
						sb.append(line+"\n");
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
				if(c != null)try{c.disconnect();}catch(Exception ex){}
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
			ArrayList<LatLng> points = null;
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
			List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
			JSONArray jRoutes = null;
			JSONArray jLegs = null;
			JSONArray jSteps = null;
			try {
				jRoutes = jObject.getJSONArray("routes");
				/** Traversing all routes */
				for (int i = 0; i < jRoutes.length(); i++) {
					jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
					List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

					/** Traversing all legs */
					for (int j = 0; j < jLegs.length(); j++) {
						jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

						/** Traversing all steps */
						for (int k = 0; k < jSteps.length(); k++) {
							String polyline = "";
							polyline = (String) ((JSONObject) ((JSONObject) jSteps
									.get(k)).get("polyline")).get("points");
							List<LatLng> list = decodePoly(polyline);

							/** Traversing all points */
							for (int l = 0; l < list.size(); l++) {
								HashMap<String, String> hm = new HashMap<String, String>();
								hm.put("lat",
										Double.toString(((LatLng) list.get(l)).latitude));
								hm.put("lng",
										Double.toString(((LatLng) list.get(l)).longitude));
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
			List<LatLng> poly = new ArrayList<LatLng>();
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
