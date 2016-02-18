package com.cesoft.encuentrame;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

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
}
