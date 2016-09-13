package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.cesoft.encuentrame3.Login;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Exclude;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.ArrayList;
import java.util.Date;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//https://firebase.google.com/docs/database/android/save-data
@IgnoreExtraProperties
public class Lugar extends Objeto
{
	public static final String NOMBRE = "lugar";//TODO: transaccion, si no guarda en firebase, no guardar en geofire
	protected static DatabaseReference newFirebase(){return FirebaseDatabase.getInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);}
	protected static GeoFire newGeoFire(){return new GeoFire(FirebaseDatabase.getInstance().getReference().child(Login.getCurrentUserID()).child(GEO).child(NOMBRE));}
	@Exclude
	protected DatabaseReference _datos;

	///______________________________________________________________
	//Yet Another Firebase Bug:
	//Serialization of inherited properties from the base class, is missing in the current release of the
	// Firebase Database SDK for Android. It will be added back in an upcoming version.
	protected String id = null;
		public String getId(){return id;}
		public void setId(String v){id = v;}
	/*protected String uid = null;
		public String getUid(){return uid;}
		public void setUid(String v){uid = v;}*/
	protected String nombre;
	protected String descripcion;
		public String getNombre(){return nombre;}
		public void setNombre(String v){nombre=v;}
		public String getDescripcion(){return descripcion;}
		public void setDescripcion(String v){descripcion=v;}
	protected Date fecha;
		public Date getFecha(){return fecha;}
		public void setFecha(Date v){fecha=v;}
	///______________________________________________________________

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
		return String.format(java.util.Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', latitud='%f', longitud='%f', fecha='%s'}",
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
		saveGeo();//TODO: transaccion fire->geo mediante listener... o _datos.runTransaction???
	}

	//______________________________________________________________________________________________
	public static void getLista(final Objeto.ObjetoListener<Lugar> listener)
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
	public static void getLista(Objeto.ObjetoListener<Lugar> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	public static void buscarPorFiltro(final Objeto.ObjetoListener<Lugar> listener, final Filtro filtro)//ValueEventListener listener
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
				System.err.println("--------------------Lugar:N:"+aLugares.size());
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				System.err.println("Lugar:buscarPorFiltro:onCancelled:"+err);
				listener.onError("Lugar:buscarPorFiltro:onCancelled:"+err);
			}
		});
	}
	public static void buscarPorGeoFiltro(final Objeto.ObjetoListener<Lugar> listener, final Filtro filtro)
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
System.err.println("____________0Lugar:onDataChange:"+nCount);
				nCount++;
				newFirebase().child(key).addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot data)
					{
System.err.println("____________Lugar:onDataChange:"+nCount);
						nCount--;
						Lugar l = data.getValue(Lugar.class);
System.err.println("____________Lugar:onDataChange:"+nCount+"___"+l+"____"+l.pasaFiltro(filtro));
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
				System.err.println("Lugar:getLista:onGeoQueryReady:Count:::::::::::::::"+nCount);
				if(nCount==0)listener.onData(aLugares.toArray(new Lugar[aLugares.size()]));
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}
			@Override public void onKeyExited(String key){System.err.println("Lugar:getLista:onKeyExited");}
			@Override public void onKeyMoved(String key, GeoLocation location){System.err.println("Lugar:getLista:onKeyMoved"+key+", "+location);}
			@Override public void onGeoQueryError(DatabaseError error){System.err.println("Lugar:getLista:onGeoQueryError:"+error);}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	protected Lugar(Parcel in)
	{
		//super(in);
		setId(in.readString());
		nombre = (in.readString());
		setDescripcion(in.readString());
		setFecha(new Date(in.readLong()));
		//
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		//super.writeToParcel(dest, flags);
		dest.writeString(getId());
		dest.writeString(nombre);
		dest.writeString(getDescripcion());
		dest.writeLong(getFecha().getTime());
		//
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
	}

	//@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Parcelable.Creator<Lugar> CREATOR = new Parcelable.Creator<Lugar>()
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
	private void saveGeo()
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
			public void onComplete(String key, DatabaseError error)
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
