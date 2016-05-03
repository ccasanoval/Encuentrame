package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.cesoft.encuentrame.Util;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.Locale;
//TODO:
//Robert Kiyosaki
//Raimon Samsó / El código del dinero

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 10/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Lugar extends Objeto implements Parcelable
{
	public static final String NOMBRE = "lugar";

	private Firebase _datos;

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
			Firebase ref = new Firebase(FIREBASE);
			_datos = ref.child(NOMBRE).child(getId());
			_datos.setValue(null, listener);
		}
	}
	public void guardar(Firebase.CompletionListener listener)//TODO: todos igual, llevar a objeto?
	{
		if(_datos != null)
		{
			_datos.setValue(this, listener);
		}
		else
		{
			Firebase ref = new Firebase(FIREBASE);
			if(getId() != null)
			{
				_datos = ref.child(NOMBRE).child(getId());
			}
			else
			{
				_datos = ref.child(NOMBRE).push();
				setId(_datos.getKey());
			}
			_datos.setValue(this, listener);
		}
	}
	public static void getById(String sId, ChildEventListener listener)
	{
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		Query queryRef = ref.orderByKey().equalTo(sId);//.limitToFirst(1);
    	queryRef.addChildEventListener(listener);
		//ref.addListenerForSingleValueEvent(listener);
			/*new ChildEventListener() {
		ArrayList<String> relationProps = new ArrayList<>();
		relationProps.add("lugar");
		Backendless.Persistence.of(Aviso.class).findById(sId, relationProps, res);*/
	}
	public static void getLista(ValueEventListener listener)
	{
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		ref.addValueEventListener(listener);
	}


	public static void getListaByPos(GeoQueryEventListener listener, Filtro filtro)
	{
System.err.println("-----------------------------------------Lugar:getListaByPos:");
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		GeoFire geoFire = new GeoFire(ref);
		GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio());
		geoQuery.addGeoQueryEventListener(listener);
/*    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
    }
    @Override
    public void onKeyExited(String key) {
        System.out.println(String.format("Key %s is no longer in the search area", key));
    }
    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
    }
    @Override
    public void onGeoQueryReady() {
        System.out.println("All initial data has been loaded and events have been fired!");
    }
    @Override
    public void onGeoQueryError(FirebaseError error) {
        System.err.println("There was an error with this query: " + error);
    }
});*/
	}

	public static void getLista(ValueEventListener listener, Filtro filtro)
	{
//TODO-----------------------------------------------------------------------------------------------------
getLista(listener);
if(1==1)return;


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
		//--FILTRO

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

}

/*TODO: como lo hace firebase?
https://github.com/firebase/geofire-java
https://geofire-java.firebaseapp.com/docs/

GeoFire geoFire = new GeoFire(new Firebase("https://<your-firebase>.firebaseio.com/"));
geoFire.setLocation("firebase-hq", new GeoLocation(37.7853889, -122.4056973), new GeoFire.CompletionListener() {
    @Override
    public void onComplete(String key, FirebaseError error) {
        if (error != null)
            System.err.println("There was an error saving the location to GeoFire: " + error);
         else
            System.out.println("Location saved on server successfully!");
    }});

geoFire.removeLocation("firebase-hq");

geoFire.getLocation("firebase-hq", new LocationCallback() {
    @Override
    public void onLocationResult(String key, GeoLocation location) {
        if (location != null)             System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
         else            System.out.println(String.format("There is no location for key %s in GeoFire", key));
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {        System.err.println("There was an error getting the GeoFire location: " + firebaseError);    }
});

GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(37.7832, -122.4056), 0.6);

geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
    }

    @Override
    public void onKeyExited(String key) {
        System.out.println(String.format("Key %s is no longer in the search area", key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
    }

    @Override
    public void onGeoQueryReady() {
        System.out.println("All initial data has been loaded and events have been fired!");
    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        System.err.println("There was an error with this query: " + error);
    }
});

removeGeoQueryEventListener to remove a single event listener or removeAllListeners

GeoQuery search area can be changed with setCenter and setRadius



*/