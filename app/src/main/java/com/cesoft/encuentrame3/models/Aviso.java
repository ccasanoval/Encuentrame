package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
@IgnoreExtraProperties
public class Aviso extends Objeto
{
	public static final String NOMBRE = "aviso";
	protected static DatabaseReference newFirebase(){return FirebaseDatabase.getInstance().getReference().child(NOMBRE);}
	@Exclude
	protected DatabaseReference _datos;

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
	public Aviso(){}
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
System.err.println("------------------AVISO-EQUALS-"+o+" : "+this);
		return getId().equals(a.getId())
			&& getLatitud() == a.getLatitud() && getLongitud() == a.getLongitud() && getRadio() == a.getRadio()
			&& getNombre().equals(a.getNombre()) && getDescripcion().equals(a.getDescripcion());
	}

	//// PARCELABLE
	//
	protected Aviso(Parcel in)
	{
		//setObjectId(in.readString());nombre = in.readString();descripcion = in.readString();
		super(in);
		//
		setActivo(in.readByte() > 0);
		setId(in.readString());
		setLatitud(in.readDouble());
		setLongitud(in.readDouble());
		setRadio(in.readDouble());
System.err.println("----------------Aviso:from parcel 2:" + this);
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
System.err.println("----------------Aviso:writeToParcel:"+this);
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
		Query queryRef = newFirebase().orderByChild(ACTIVO).equalTo(true);
			//Query queryRef = ref.equalTo(true, ACTIVO);//NO PIRULA
		queryRef.addListenerForSingleValueEvent(listener);
    	//queryRef.addChildEventListener(listener);//TODO:Cual mejor?
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
				System.err.println("Aviso:getLista:onCancelled:"+err);
				listener.onError("Aviso:getLista:onCancelled:"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	@Override
	public boolean pasaFiltro(Filtro filtro)
	{
		if( ! super.pasaFiltro(filtro))return false;
System.err.println("----------"+filtro.getActivo()+" : "+isActivo());
		if(filtro.getActivo()==Filtro.ACTIVO && !isActivo()  ||  filtro.getActivo()==Filtro.INACTIVO && isActivo())return false;
System.err.println("----------pasaFiltro FINAL");
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
	public static void buscarPorFiltro(final ObjetoListener<Aviso> listener, final Filtro filtro)
	{
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
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
				System.err.println("Aviso:buscarPorFiltro:onCancelled:"+err);
				listener.onError(err.toString());
			}
		});
	}
	//----
	public static void buscarPorGeoFiltro(final ObjetoListener<Aviso> listener, final Filtro filtro)
	{
System.err.println("Aviso:buscarPorGeoFiltro:--------------------------:"+filtro);
		if(filtro.getRadio() < 1)filtro.setRadio(100);

		final ArrayList<Aviso> aAvisos = new ArrayList<>();

		final GeoQuery geoQuery = newGeoFire().queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		GeoQueryEventListener lisGeo = new GeoQueryEventListener()
		{
			private int nCount = 0;
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				System.err.println("Aviso:buscarPorGeoFiltro:onKeyEntered:"+key+", "+location);
				nCount++;
				newFirebase().child(key).addListenerForSingleValueEvent(new ValueEventListener()
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
						System.err.println("Aviso:buscarPorGeoFiltro:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onData(aAvisos.toArray(new Aviso[aAvisos.size()]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
System.err.println("Aviso:buscarPorGeoFiltro:onGeoQueryReady:"+nCount);
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}

			@Override public void onGeoQueryError(DatabaseError err){System.err.println("Aviso:buscarPorGeoFiltro:onGeoQueryError:"+err);}
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
			System.err.println("Aviso:saveGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), new GeoFire.CompletionListener()
		{
			@Override
    		public void onComplete(String key, DatabaseError error)
			{
        		if(error != null)
            		System.err.println("Aviso:saveGeo:There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud());
        		else
            		System.out.println("Aviso:saveGeo:Location saved on server successfully!");
			}
        });
	}
	private void delGeo()
	{
		if(_datos.getKey() == null)
		{
			System.err.println("Aviso:delGeo:id==null");
			return;
		}
		if(_datGeo == null)_datGeo = newGeoFire();
		_datGeo.removeLocation(_datos.getKey());
	}
	// GEOFIRE
	//----------------------------------------------------------------------------------------------
}
	/*
	public transient static final String NOMBRE = "aviso";
	public transient static final String RADIO = "radio";//TRANSIENT so not to include in backendless
	public transient static final String LUGAR = "lugar";

	public Aviso(){}

	//______________________________________________________________________________________________
	protected boolean activo = true;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean b){activo=b;}

	protected Date fechaActivo;
		public void desactivarPorHoy(AsyncCallback<Aviso> ac)//TODO: Desactivar por hoy, tambien desactivar todos los avisos... incluso: modo avion para app completa
		{
			fechaActivo = Calendar.getInstance().getTime();
			Backendless.Persistence.save(this, ac);
		}
		public void reactivarPorHoy(AsyncCallback<Aviso> ac)
		{

			fechaActivo = new Date(0);
			Backendless.Persistence.save(this, ac);
		}

	private GeoPoint lugar = new GeoPoint(0,0);
		public GeoPoint getLugar(){return lugar;}
		public void setLugar(GeoPoint v){lugar=v;}
		public double getLatitud(){if(lugar==null || lugar.getLatitude() == null)return 0.0;return lugar.getLatitude();}
		public double getLongitud(){if(lugar==null || lugar.getLatitude() == null)return 0.0;return lugar.getLongitude();}
		public void setLatLon(double lat, double lon){lugar.setLatitude(lat);lugar.setLongitude(lon);}

		public int getRadio()//TODO: quiza aumentar radio (transparente para user) para que google pille antes la geofence ¿COMO MEJORAR GOOGLE GEOFENCE? Probar backendless geofences?????
		{
			if(lugar == null)return 0;
			Object o = lugar.getMetadata(RADIO);
			if(o == null)return 0;
			if(String.class == o.getClass())
				return Integer.parseInt((String)o);
			else if(Integer.class == o.getClass()) return (Integer)o;
			else return 0;
		}
		public void setRadio(int v){lugar.addMetadata(RADIO, v);}

	//______________________________________________________________________________________________
	public String toString()
	{
		//return super.toString() +", ACT:"+activo+", POS:"+(lugar==null?"null":lugar.getLatitude()+"/"+lugar.getLongitude()+":"+getRadio()+" "+lugar.getObjectId());
		return String.format(java.util.Locale.ENGLISH, "Aviso{id='%s', nombre='%s', descripcion='%s', created='%s == %d', radio='%d' }",
				getObjectId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), created!=null?DATE_FORMAT.format(created):"", created!=null?created.getTime():0, getRadio());
	}
	//______________________________________________________________________________________________
	@Override public boolean equals(Object o)
	{
		if(o == null)return false;
		if(this == o)return true;
		if(!(o instanceof Aviso))return false;
		Aviso a = (Aviso)o;
//System.err.println("------------------AVISO-EQUALS-"+o+" : "+this);
		return getObjectId().equals(a.getObjectId())
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
		lugar.setObjectId(in.readString());
		lugar.setLatitude(in.readDouble());
		lugar.setLongitude(in.readDouble());
		setRadio(in.readInt());
//System.err.println("----------------Aviso:from parcel 2:" + this);
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeByte(isActivo()?(byte)1:0);
		if(lugar == null)lugar = new GeoPoint(0,0);
		dest.writeString(lugar.getObjectId());
		dest.writeDouble(lugar.getLatitude());
		dest.writeDouble(lugar.getLongitude());
		dest.writeInt(getRadio());
System.err.println("----------------Aviso:writeToParcel:"+lugar);
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


	//// BACKENDLESS
	//
	public void eliminar(AsyncCallback<Long> ac)
	{
		Backendless.Geo.removePoint(lugar, new AsyncCallback<Void>()
		{
			@Override public void handleResponse(Void response){System.err.println("Aviso:eliminar:geoPoint:ok:"+response);}
			@Override public void handleFault(BackendlessFault fault){System.err.println("Aviso:eliminar:geoPoint:e:"+fault);}
		});
		//removePoint( GeoPoint geoPoint, AsyncCallback<Void> responder )
		Backendless.Persistence.of(Aviso.class).remove(this, ac);
	}
	public void guardar(AsyncCallback<Aviso> ac)
	{
		//Backendless.Persistence.of(Lugar.class).save(this, ac);
		Backendless.Persistence.save(this, ac);
	}

	public static void getById(String sId, AsyncCallback<Aviso> res)
	{
		ArrayList<String> relationProps = new ArrayList<>();
		relationProps.add(LUGAR);
		Backendless.Persistence.of(Aviso.class).findById(sId, relationProps, res);
	}
	public static void getActivos(AsyncCallback<BackendlessCollection<Aviso>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		query.setWhereClause("activo > 0");
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated(LUGAR);
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Aviso.class).find(query, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Aviso>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated(LUGAR);
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Aviso.class).find(query, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Aviso>> res, Filtro filtro)
	{
System.err.println("Aviso:getLista:filtro: "+filtro);
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addSortByOption("created ASC");
		queryOptions.addRelated(LUGAR);
		query.setQueryOptions(queryOptions);
		//--FILTRO
		StringBuilder sb = new StringBuilder();
		if( ! filtro.getNombre().isEmpty())
		{
			sb.append(" nombre LIKE '%");
			sb.append(filtro.getNombre());
			sb.append("%' ");
		}
		if(filtro.getRadio() > 0 && filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(String.format(java.util.Locale.ENGLISH, " distance(%f, %f, lugar.latitude, lugar.longitude ) < km(%f) ",
					filtro.getPunto().latitude, filtro.getPunto().longitude, filtro.getRadio()/1000.0));
		}
		if(filtro.getFechaIni() != null)
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
		if(filtro.getActivo() != Util.NADA)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(" activo = ");
			sb.append(filtro.getActivo()==Filtro.ACTIVO?"true":"false");
		}
System.err.println("Aviso:getLista:SQL: "+sb.toString());
		if(sb.length() > 0)
			query.setWhereClause(sb.toString());
		//--FILTRO
		Backendless.Persistence.of(Aviso.class).find(query, res);
	}

}
*/