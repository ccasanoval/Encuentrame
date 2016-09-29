package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import android.util.Log;

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
import java.util.Date;
import com.cesoft.encuentrame3.Login;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
@IgnoreExtraProperties
public class Aviso extends Objeto
{
	private static final String TAG = "CESoft:Aviso:";
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
	protected String id = null;
		public String getId(){return id;}
		public void setId(String v){id = v;}
	protected String nombre;
	protected String descripcion;
		public String getNombre(){return nombre;}
		public void setNombre(String v){nombre=v;}
		public String getDescripcion(){return descripcion;}
		public void setDescripcion(String v){descripcion=v;}
	//protected long fecha;//TODO:? para que firebase no se queje de 'No setter/field for day found on class java.util.Date'...
	protected Date fecha;
		public Date getFecha(){return fecha;}
		public void setFecha(Date v){fecha=v;}
	///______________________________________________________________

	//______________________________________________________________________________________________
	private final static String ACTIVO = "activo";
	protected boolean activo = true;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean v){activo=v;}

	private double latitud, longitud;
		public double getLatitud(){return latitud;}//TODO: con GeoFire, quiza esto podrias sobra...pero complicaria igual que quitar id de objeto
		public double getLongitud(){return longitud;}
		public void setLatitud(double v){latitud=v;}//TODO: validacion
		public void setLongitud(double v){longitud=v;}

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
	public Aviso(){fecha = new Date();}
	@Override
	public String toString()
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
		setId(in.readString());
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
		setRadio(in.readDouble());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeByte(isActivo()?(byte)1:0);
		dest.writeString(getId());
		dest.writeDouble(getLatitud());
		dest.writeDouble(getLongitud());
		dest.writeDouble(getRadio());
	}
	@Override
	public int describeContents(){return 0;}
	public static final Creator<Aviso> CREATOR = new Creator<Aviso>()
	{
		@Override
		public Aviso createFromParcel(Parcel in){return new Aviso(in);}
		@Override
		public Aviso[] newArray(int size){return new Aviso[size];}
	};


	//// FIREBASE
	//
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
	public void guardar(DatabaseReference.CompletionListener listener)
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
	public static void getById(String sId, ValueEventListener listener)
	{
		newFirebase().child(sId).addListenerForSingleValueEvent(listener);
	}
	public static void getActivos(ValueEventListener listener)
	{
		Query queryRef = newFirebase().orderByChild(ACTIVO).equalTo(true);	//Query queryRef = ref.equalTo(true, ACTIVO);//NO PIRULA
		//queryRef.addListenerForSingleValueEvent(listener);
    	queryRef.addValueEventListener(listener);//AJAX
	}
	public static void getLista(final ObjetoListener<Aviso> listener)
	{
		//newFirebase().addValueEventListener(listener);//TODO:Cual mejor?
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Aviso> aAvisos = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
					aAvisos.add(o.getValue(Aviso.class));
				listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, "getLista:onCancelled:"+err);
				listener.onError("Aviso:getLista:onCancelled:"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public boolean pasaFiltro(Filtro filtro)
	{
		if( ! super.pasaFiltro(filtro))return false;
		if(filtro.getActivo()==Filtro.ACTIVO && !isActivo()  ||  filtro.getActivo()==Filtro.INACTIVO && isActivo())return false;
		return true;
	}
	public static void getLista(ObjetoListener<Aviso> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	//----
	private static void buscarPorFiltro(final ObjetoListener<Aviso> listener, final Filtro filtro)
	{
		//newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		newFirebase().addValueEventListener(new ValueEventListener()//AJAX
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Aviso> aAvisos = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Aviso a = o.getValue(Aviso.class);
					if( ! a.pasaFiltro(filtro))continue;
					aAvisos.add(o.getValue(Aviso.class));
				}
				listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, "buscarPorFiltro:onCancelled:"+err);
				listener.onError(err.toString());
			}
		});
	}
	//----
	private static void buscarPorGeoFiltro(final ObjetoListener<Aviso> listener, final Filtro filtro)
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
					public void onDataChange(DataSnapshot data)
					{
						nCount--;
						Aviso a = data.getValue(Aviso.class);
						if(a.pasaFiltro(filtro))aAvisos.add(a);
						if(nCount < 1)listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
					}
					@Override
					public void onCancelled(DatabaseError err)
					{
						nCount--;
						Log.e(TAG, "buscarPorGeoFiltro:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
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
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), new GeoFire.CompletionListener()
		{
			@Override
    		public void onComplete(String key, DatabaseError error)
			{
        		if(error != null)
            		Log.e(TAG, "saveGeo:There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
        		else
            		Log.w(TAG, "saveGeo:Location saved on server successfully!");
			}
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
