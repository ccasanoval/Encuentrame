package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.cesoft.encuentrame.Util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.client.snapshot.IndexedNode;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.ArrayList;
import java.util.Locale;
//TODO:
//Robert Kiyosaki
//Raimon Samsó / El código del dinero

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 10/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
@JsonIgnoreProperties({"_datos", "_datGeo"})
public class Lugar extends Objeto implements Parcelable
{
	public static final String NOMBRE = "lugar";

	private Firebase _datos;
	private static Firebase newFirebase(){return new Firebase(FIREBASE).child(NOMBRE);}

	//______________________________________________________________________________________________
	private double latitud, longitud;
		public double getLatitud(){return latitud;}
		public double getLongitud(){return longitud;}
		public void setLatitud(double v){latitud=v;}//TODO: validacion
		public void setLongitud(double v){longitud=v;}
	//public void setLatLon(double lat, double lon){_lugar = new GeoLocation(lat,lon);}

	//______________________________________________________________________________________________
	public Lugar(){}
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', latitud='%f', longitud='%f'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), latitud, longitud);
	}

	//// FIREBASE
	//______________________________________________________________________________________________
	public void eliminar(Firebase.CompletionListener listener)
	{
		if(_datos != null)
		{
			_datos.setValue(null, listener);
		}
		else if(getId() != null)
		{
			_datos = newFirebase().child(getId());
			_datos.setValue(null, listener);
		}
		delGeo();
	}
	public void guardar(Firebase.CompletionListener listener)//TODO: todos igual, llevar a objeto?
	{
		if(_datos != null)
		{
			_datos.setValue(this, listener);
		}
		else
		{
			if(getId() != null)
			{
				_datos = newFirebase().child(getId());
			}
			else
			{
				_datos = newFirebase().push();
				setId(_datos.getKey());
			}
			_datos.setValue(this, listener);
		}
		saveGeo(getLatitud(), getLongitud());
	}
	/*public static void getById(String sId, ValueEventListener listener)
	{
		Firebase ref1 = newFirebase().child(sId);
		ref1.addListenerForSingleValueEvent(listener);
	}*/
	public static void getLista(ValueEventListener listener)
	{
		Firebase ref = newFirebase();
		//ref.addValueEventListener(listener);//TODO: cual es mejor?
		ref.addListenerForSingleValueEvent(listener);
	}


	public static void getLista(ValueEventListener listener, Filtro filtro)
	{
//TODO-----------------------------------------------------------------------------------------------------
//getLista(listener);if(1==1)return;
		if(filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)//filtro.getRadio() > Util.NADA &&
		{
			buscarPorGeoFiltro(listener, filtro);
		}
		else
		{
			buscarPorFiltro(listener, filtro);
		}
	}
	public static void buscarPorFiltro(ValueEventListener listener, Filtro filtro)
	{
		Firebase ref = newFirebase();
		Query queryRef = ref.orderByChild("height").equalTo(25);
	}
	//https://github.com/firebase/geofire-java
	public static void buscarPorGeoFiltro(ValueEventListener listener, Filtro filtro)
	{
		if(filtro.getRadio() < 1)filtro.setRadio(100);
		//final Firebase ref = newFirebase();
		GeoFire geoFire = newGeoFire();
		final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		GeoQueryEventListener lisGeo = new GeoQueryEventListener()
		{
			private ArrayList<String> asID = new ArrayList<>();
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				System.err.println("Lugar:getLista:--------------------------------------------onKeyEntered:"+key+", "+location);
				asID.add(key);
			}
			@Override
			public void onKeyExited(String key)
			{
				System.err.println("Lugar:getLista:--------------------------------------------onKeyExited");
				asID.remove(key);
			}
			@Override
			public void onKeyMoved(String key, GeoLocation location)
			{
				System.err.println("Lugar:getLista:--------------------------------------------onKeyMoved"+key+", "+location);
			}
			@Override
			public void onGeoQueryReady()
			{
				System.err.println("Lugar:getLista:--------------------------------------------onGeoQueryReady");

				//Firebase ref = newFirebase().child(sId);
				Firebase ref = newFirebase().
				ref.addListenerForSingleValueEvent(listener);
				/*
				IndexedNode in = new IndexedNode(
				DataSnapshot ds = new DataSnapshot(ref, 1);
				listener.onDataChange(*/

				//geoQuery.removeAllListeners();
				geoQuery.removeGeoQueryEventListener(this);
			}
			@Override
			public void onGeoQueryError(FirebaseError error)
			{
				System.err.println("Lugar:getLista:--------------------------------------------onGeoQueryError:"+error);
			}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);

/*
System.err.println("Lugar:getLista:filtro: "+filtro);

		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		Query queryRef;// = ref.orderByChild("height").equalTo(25);

		//LIKE '%ABC%' https://www.firebase.com/blog/2014-01-02-queries-part-two.html



		//--FILTRO
		StringBuilder sb = new StringBuilder();//" created = created "
		if( ! filtro.getNombre().isEmpty())
		{
			//ref.orderByChild("nombre").equalTo(filtro.getNombre());
			ref.equalTo(filtro.getNombre(), "nombre");
			//sb.append(" nombre LIKE '%");			sb.append(filtro.getNombre());			sb.append("%' ");
		}
		if(filtro.getRadio() > Util.NADA && filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(String.format(java.util.Locale.ENGLISH, " distance(%f, %f, lugar.latitude, lugar.longitude ) < km(%f) ",
					filtro.getPunto().latitude, filtro.getPunto().longitude, filtro.getRadio()/1000.0));
		}
		if(filtro.getFechaIni() != null)//DateFormat df = java.text.DateFormat.getDateTimeInstance();
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(" created >= ");
			sb.append(filtro.getFechaIni().getTime());
		}
		if(filtro.getFechaFin() != null)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(" created <= ");
			sb.append(filtro.getFechaFin().getTime());
		}
System.err.println("Lugar:getLista:SQL: "+sb.toString());
		//if(sb.length() > 0)
		//--FILTRO*/

	}


	// PARCELABLE
	//______________________________________________________________________________________________
	protected Lugar(Parcel in)
	{
		super(in);
		//
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
		//setLatLon(, in.readDouble());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
	}

	@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Creator<Lugar> CREATOR = new Creator<Lugar>()
	{
		@Override
		public Lugar createFromParcel(Parcel in)
		{
			return new Lugar(in);
		}
		@Override
		public Lugar[] newArray(int size)
		{
			return new Lugar[size];
		}
	};


	//----------------------------------------------------------------------------------------------
	// GEOFIRE
	private GeoFire _datGeo;
	private static GeoFire newGeoFire(){return new GeoFire(new Firebase(GEOFIRE).child(NOMBRE));}
	private void saveGeo(final double lat, final double lon)
	{
		if(_datos.getKey() == null)
		{
			System.err.println("Lugar:saveGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(lat, lon), new GeoFire.CompletionListener()
		{
    		@Override
    		public void onComplete(String key, FirebaseError error)
			{
        		if(error != null)
            		System.err.println("There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+lat+"/"+lon);
        		else
            		System.out.println("Location saved on server successfully!");
			}
        });
	}
	private void delGeo()
	{
		if(_datos.getKey() == null)
		{
			System.err.println("Lugar:delGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.removeLocation(_datos.getKey());
	}
	// GEOFIRE
	//----------------------------------------------------------------------------------------------
}

