package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.cesoft.encuentrame3.util.Log;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

import com.cesoft.encuentrame3.Login;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
@IgnoreExtraProperties
public class Aviso extends Objeto
{
	private static final String TAG =  Aviso.class.getSimpleName();
	public static final String NOMBRE = "aviso";
	private static DatabaseReference newFirebase()
	{
		return Login.getDBInstance()
				.getReference()
				.child(Login.getCurrentUserID())
				.child(NOMBRE);
	}
	private static GeoFire newGeoFire(){return new GeoFire(Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(GEO).child(NOMBRE));}
	@Exclude
	private DatabaseReference _datos;

	///______________________________________________________________
	//Yet Another Firebase Bug:
	//Serialization of inherited properties from the base class, is missing in the current release of the
	// Firebase Database SDK for Android. It will be added back in an upcoming version.
	/*protected String id = null;
		public String getId(){return id;}
		public void setId(String v){id = v;}
	protected String nombre;
	protected String descripcion;
		public String getNombre(){return nombre;}
		public void setNombre(String v){nombre=v;}
		public String getDescripcion(){return descripcion;}
		public void setDescripcion(String v){descripcion=v;}

		//TODO:? para que firebase no se queje de 'No setter/field for day found on class java.util.Date'...
		//^(?!.*(No setter|NativeCrypto|IOnlyOwnerSimSupport|Asset path|IInputConnectionWrapper)).*$
	//protected long fecha;
	private Date fecha;
		public Date getFecha(){return fecha;}
		public void setFecha(Date v){fecha=v;}*/
	///______________________________________________________________

	//______________________________________________________________________________________________
	private final static String ACTIVO = "activo";
	protected boolean activo = true;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean v){activo=v;}

	/*private double latitud, longitud;
		public double getLatitud(){return latitud;}//TODO: con GeoFire, quiza esto podrias sobrar...pero complicaria igual que quitar id de objeto
		public double getLongitud(){return longitud;}
		public void setLatitud(double v){latitud=v;}//TODO: validacion
		public void setLongitud(double v){longitud=v;}*/

	private double radio;//TODO: quiza aumentar radio (transparente para user) para que google pille antes la geofence ¿COMO MEJORAR GOOGLE GEOFENCE? Probar backendless geofences?????
		public double getRadio(){return radio;}
		public void setRadio(double v){if(v >= 0 && v < 10000)radio=v;}

	//TODO: Desactivar por hoy, tambien desactivar todos los avisos... incluso: modo avion para app completa
	/*protected Date fechaActivo;
		public void desactivarPorHoy()
		{
			fechaActivo = Calendar.getInstance().getTime();
			//Backendless.Persistence.save(this, ac);
		}
		public void reactivarPorHoy()
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -2);
			fechaActivo = cal.getTime();
			//Backendless.Persistence.save(this, ac);
		}*/

	//______________________________________________________________________________________________
	public Aviso() { super(); }	//NOTE: Firebase necesita un constructor sin argumentos
	@Override public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Aviso{id='%s', nombre='%s', descripcion='%s', fecha='%s', latitud='%f', longitud='%f', radio='%f', activo='%b'}",
				getId(), nombre, descripcion, DATE_FORMAT.format(fecha), latitud, longitud, radio, activo);
	}

	//______________________________________________________________________________________________
	@Override public boolean equals(Object o)
	{
		if(this == o)return true;
		if(!(o instanceof Aviso))return false;
		Aviso a = (Aviso)o;
		return getId().equals(a.getId())
			&& getLatitud() == a.getLatitud() && getLongitud() == a.getLongitud() && getRadio() == a.getRadio()
			&& getNombre().equals(a.getNombre()) && getDescripcion().equals(a.getDescripcion());
	}

	//// PARCELABLE
	//
	protected Aviso(Parcel in)
	{
		super(in);
		//
		setActivo(in.readByte() > 0);
		//setId(in.readString());
		//setLatLon(in.readDouble(), in.readDouble());
		setRadio(in.readDouble());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeByte(isActivo()?(byte)1:0);
		//dest.writeString(getId());
		//dest.writeDouble(getLatitud());
		//dest.writeDouble(getLongitud());
		dest.writeDouble(getRadio());
	}
	//@Override public int describeContents(){return 0;}
	public static final Creator<Aviso> CREATOR = new Creator<Aviso>()
	{
		@Override public Aviso createFromParcel(Parcel in){return new Aviso(in);}
		@Override public Aviso[] newArray(int size){return new Aviso[size];}
	};


	//// FIREBASE
	//
	public void eliminar(Fire.CompletadoListener listener)//DatabaseReference.CompletionListener listener)
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
	public void guardar(Fire.CompletadoListener listener)//DatabaseReference.CompletionListener listener)
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

	//______________________________________________________________________________________________
	public static void getById(String sId, final Fire.SimpleListener<Aviso> listener)
	{
		newFirebase().child(sId).addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot ds)
			{
				Aviso a = ds.getValue(Aviso.class);
				listener.onDatos(new Aviso[]{a});
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
			{
				listener.onError(err.getMessage());
			}
		});
	}
	public static void getActivos(final Fire.DatosListener<Aviso> listener)
	{
		if(listener == null)
		{
			Log.e(TAG, "getActivos:e:-------------------------------------------------------------- LISTENER == NULL");
			return;
		}
		Query queryRef = newFirebase().orderByChild(ACTIVO).equalTo(true);	//Query queryRef = ref.equalTo(true, ACTIVO);//NO PIRULA
		//queryRef.addListenerForSingleValueEvent(listener);
		ValueEventListener vel = new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot ds)
			{
				int i = 0;
				Aviso[] aAvisos = new Aviso[(int)ds.getChildrenCount()];
				for(DataSnapshot l : ds.getChildren())
				{
					aAvisos[i++] = l.getValue(Aviso.class);
				}
				listener.onDatos(aAvisos);
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				listener.onError(err.toString());
			}
		};
		listener.setRef(queryRef.getRef());
		//listener.delListener();
		listener.setListener(vel);
    	queryRef.addValueEventListener(vel);
	}
	public static void getLista(final Fire.DatosListener<Aviso> listener)
	{
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel;//= listener.getListener();
		//if(vel != null)ddbb.removeEventListener(vel);
		vel = new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Aviso> aAvisos = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
					aAvisos.add(o.getValue(Aviso.class));
				listener.onDatos(aAvisos.toArray(new Aviso[aAvisos.size()]));
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
			{
				Log.e(TAG, "getLista:onCancelled:"+err);
				listener.onError("Aviso:getLista:onCancelled:"+err);
			}
		};
		listener.setRef(ddbb);
		//listener.delListener();
		listener.setListener(vel);
		ddbb.addValueEventListener(vel);//.addListenerForSingleValueEvent
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public boolean pasaFiltro(Filtro filtro)
	{
		return super.pasaFiltro(filtro) && !(filtro.getActivo() == Filtro.ACTIVO && !isActivo() || filtro.getActivo() == Filtro.INACTIVO && isActivo());
	}
	public static void getLista(Fire.DatosListener<Aviso> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	//----
	private static void buscarPorFiltro(final Fire.DatosListener<Aviso> listener, final Filtro filtro)
	{
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel;// = listener.getListener();
		//if(vel != null)ddbb.removeEventListener(vel);
		vel = new ValueEventListener()//AJAX
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Aviso> aAvisos = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Aviso a = o.getValue(Aviso.class);
					if(a != null && ! a.pasaFiltro(filtro))continue;
					aAvisos.add(o.getValue(Aviso.class));
				}
				listener.onDatos(aAvisos.toArray(new Aviso[aAvisos.size()]));
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
			{
				Log.e(TAG, "buscarPorFiltro:onCancelled:"+err);
				listener.onError(err.toString());
			}
		};
		listener.setRef(ddbb);
		//listener.delListener();
		listener.setListener(vel);
		ddbb.addValueEventListener(vel);//.addListenerForSingleValueEvent
	}
	//----
	private static void buscarPorGeoFiltro(final Fire.SimpleListener<Aviso> listener, final Filtro filtro)
	{
		if(filtro.getRadio() < 1)filtro.setRadio(100);

		final ArrayList<Aviso> aAvisos = new ArrayList<>();

		final GeoQuery geoQuery = newGeoFire().queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		GeoQueryEventListener lisGeo = new GeoQueryEventListener()
		{
			private int nCount = 0;
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				nCount++;
				//newFirebase().child(key).addListenerForSingleValueEvent(new ValueEventListener()
				newFirebase().child(key).addValueEventListener(new ValueEventListener()//AJAX
				{
					@Override
					public void onDataChange(@NonNull DataSnapshot data)
					{
						nCount--;
						Aviso a = data.getValue(Aviso.class);
						if(a != null && a.pasaFiltro(filtro))aAvisos.add(a);
						if(nCount < 1)listener.onDatos(aAvisos.toArray(new Aviso[aAvisos.size()]));
					}
					@Override
					public void onCancelled(@NonNull DatabaseError err)
					{
						nCount--;
						Log.e(TAG, "buscarPorGeoFiltro:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onDatos(aAvisos.toArray(new Aviso[aAvisos.size()]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}

			@Override public void onGeoQueryError(DatabaseError err){Log.e(TAG, "buscarPorGeoFiltro:onGeoQueryError:"+err);}
			@Override public void onKeyExited(String key){}
			@Override public void onKeyMoved(String key, GeoLocation location){}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}



	//----------------------------------------------------------------------------------------------
	// GEOFIRE
	private GeoFire _datGeo;
	private void saveGeo()
	{
		if(_datos.getKey() == null)
		{
			Log.e(TAG, "saveGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), (key, error) ->
		{
			if(error != null)
				Log.e(TAG, "saveGeo:There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
			else
				Log.w(TAG, "saveGeo:Location saved on server successfully!");
		});
	}
	private void delGeo()
	{
		if(_datos.getKey() == null)
		{
			Log.e(TAG, "delGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.removeLocation(_datos.getKey());
	}
	// GEOFIRE
	//----------------------------------------------------------------------------------------------
}
