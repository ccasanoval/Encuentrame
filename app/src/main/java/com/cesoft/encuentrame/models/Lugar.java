package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
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
	protected static DatabaseReference newFirebase(){return FirebaseDatabase.getInstance().getReference();}
	protected static GeoFire newGeoFire(){return new GeoFire(new Firebase(GEOFIRE).child(NOMBRE));}

	@Exclude
	protected DatabaseReference _datos;

	//______________________________________________________________________________________________
	private double latitud, longitud;
		public double getLatitud(){return latitud;}
		public double getLongitud(){return longitud;}
		public void setLatitud(double v){latitud=v;}//TODO: validacion
		public void setLongitud(double v){longitud=v;}

	//______________________________________________________________________________________________
	public Lugar()
	{
		this.fecha = new Date();
	}
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', latitud='%f', longitud='%f', fecha='%s'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), latitud, longitud, DATE_FORMAT.format(fecha));
	}

	//// FIREBASE
	//______________________________________________________________________________________________
	public void eliminar(DatabaseReference.CompletionListener listener)
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
	public void guardar(DatabaseReference.CompletionListener listener)//TODO: todos igual, llevar a objeto?
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
		saveGeo();
	}
	/*public static void getById(String sId, ValueEventListener listener)
	{
		Firebase ref1 = newFirebase().child(sId);
		ref1.addListenerForSingleValueEvent(listener);
	}*/

	//______________________________________________________________________________________________
	public static void getLista(final ObjetoListener<Lugar> listener)
	{
		//newFirebase().addValueEventListener(listener);//TODO: cual es mejor?
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Lugar> aLugares = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
					aLugares.add(o.getValue(Lugar.class));
				listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				System.err.println("Lugar:getLista:onCancelled:"+err);
				listener.onError("Lugar:getLista:onCancelled:"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	public static void getLista(ObjetoListener<Lugar> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	public static void buscarPorFiltro(final ObjetoListener<Lugar> listener, final Filtro filtro)//ValueEventListener listener
	{
System.err.println("Lugar:buscarPorFiltro:--------------------------0:"+filtro);
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Lugar> aLugares = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Lugar l = o.getValue(Lugar.class);
System.err.println("--------------------Lugar:"+l);
					if( ! l.pasaFiltro(filtro))continue;
					aLugares.add(l);
				}
				listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				System.err.println("Lugar:buscarPorFiltro:onCancelled:"+err);
				listener.onError("Lugar:buscarPorFiltro:onCancelled:"+err);
			}
		});
	}
	//https://github.com/firebase/geofire-java
	public static void buscarPorGeoFiltro(final ObjetoListener<Lugar> listener, final Filtro filtro)
	{
		if(filtro.getRadio() < 1)filtro.setRadio(100);

		final ArrayList<Lugar> aLugares = new ArrayList<>();

		GeoFire geoFire = newGeoFire();
		final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		GeoQueryEventListener lisGeo = new GeoQueryEventListener()
		{
			private int nCount = 0;
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				nCount++;
				newFirebase().child(key).addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot data)
					{
						nCount--;
						Lugar l = data.getValue(Lugar.class);
						if(l.pasaFiltro(filtro))aLugares.add(l);
						if(nCount < 1)listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
					}
					@Override
					public void onCancelled(DatabaseError err)
					{
						nCount--;
						System.err.println("Lugar:getLista:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
				System.err.println("Lugar:getLista:onGeoQueryReady:A:"+nCount);
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}
			@Override public void onKeyExited(String key){System.err.println("Lugar:getLista:onKeyExited");}
			@Override public void onKeyMoved(String key, GeoLocation location){System.err.println("Lugar:getLista:onKeyMoved"+key+", "+location);}
			@Override public void onGeoQueryError(FirebaseError error){System.err.println("Lugar:getLista:onGeoQueryError:"+error);}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	protected Lugar(Parcel in)
	{
		super(in);
		//
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
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
	@Exclude
	private GeoFire _datGeo;
	private void saveGeo()//final double lat, final double lon)
	{
		if(_datos.getKey() == null)
		{
			System.err.println("Lugar:saveGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), new GeoFire.CompletionListener()
		{
    		@Override
    		public void onComplete(String key, FirebaseError error)
			{
        		if(error != null)
            		System.err.println("There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
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
