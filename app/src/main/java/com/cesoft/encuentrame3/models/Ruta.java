package com.cesoft.encuentrame3.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cesoft.encuentrame3.Login;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Config : max number of points per rute => cuando alcanza el limite corta...
//TODO: config : Radious of geofence (si se utilizase ruta por geofence)....
@IgnoreExtraProperties
public class Ruta extends Objeto implements Parcelable
{
	private static final String TAG = "CESoft:Ruta:";
	public static final String NOMBRE = "ruta";
	private static final String IDRUTA = "idRuta";
	private static DatabaseReference newFirebase(){return Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);}
	//protected static GeoFire newGeoFire(){return new GeoFire(FirebaseDatabase.getInstance().getReference().child(Login.getCurrentUserID()).child(GEO).child(NOMBRE));}
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
	protected Date fecha;
		public Date getFecha(){return fecha;}
		public void setFecha(Date v){fecha=v;}
	///______________________________________________________________

	//TODO: fecha fin...
	//Quitar si se utiliza geofence tracking y cambiar por radio...
	/*private int periodo=2*60*1000;
		public int getPeriodo(){return periodo;}
		public void setPeriodo(int v){periodo=v;}*/

	//______________________________________________________________________________________________
	public Ruta(){fecha = new Date();}
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "Ruta{id='%s', nombre='%s', descripcion='%s', fecha='%s', puntosCount='%d'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), DATE_FORMAT.format(fecha), puntosCount);
	}

	//// PARCEL
	protected Ruta(Parcel in)
	{
		super(in);
		setId(in.readString());
		puntosCount = in.readLong();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		dest.writeString(getId());
		dest.writeLong(puntosCount);
	}
	@Override
	public int describeContents(){return 0;}
	public static final Creator<Ruta> CREATOR = new Creator<Ruta>()
	{
		@Override
		public Ruta createFromParcel(Parcel in){return new Ruta(in);}
		@Override
		public Ruta[] newArray(int size){return new Ruta[size];}
	};


	//// FIREBASE
	//
	public void eliminar(DatabaseReference.CompletionListener listener)
	{
		RutaPunto.eliminar(getId());
		if(_datos != null)
		{
			_datos.setValue(null, listener);
		}
		else if(getId() != null)
		{
			_datos = newFirebase().child(getId());
			_datos.setValue(null, listener);
		}
		_datos.removeValue();
		puntosCount = 0;
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
	}
	public static void getById(String sId, ValueEventListener listener)
	{
		Query queryRef = newFirebase().orderByKey().equalTo(sId);//.limitToFirst(1);
		queryRef.addListenerForSingleValueEvent(listener);//queryRef.addChildEventListener(listener);
	}

	public static void getLista(final ObjetoListener<Ruta> listener)
	{
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				int n = (int)data.getChildrenCount();
				ArrayList<Ruta> aRutas = new ArrayList<>(n);
				for(DataSnapshot o : data.getChildren())
					aRutas.add(o.getValue(Ruta.class));
				listener.onData(aRutas.toArray(new Ruta[n]));
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, "getLista:onCancelled:"+err);
				listener.onError("Ruta:getLista:onCancelled:"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------
	//@Override
	private boolean pasaFiltro(Filtro filtro, String idRuta, String idRutaAct)
	{
		if( ! super.pasaFiltro(filtro))return false;
		boolean bActivo = idRuta!=null && idRuta.equals(idRutaAct);//Ruta activa
		if(filtro.getActivo()==Filtro.ACTIVO && !bActivo  ||  filtro.getActivo()==Filtro.INACTIVO && bActivo)return false;
		return true;
	}
	public static void getLista(ObjetoListener<Ruta> listener, Filtro filtro, String idRutaAct)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro, idRutaAct);
		else
			buscarPorGeoFiltro(listener, filtro, idRutaAct);
	}
	//----
	private static void buscarPorFiltro(final ObjetoListener<Ruta> listener, final Filtro filtro, final String idRutaAct)
	{
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Ruta> aRutas = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Ruta r = o.getValue(Ruta.class);
					if( ! r.pasaFiltro(filtro, r.getId(), idRutaAct))continue;
					aRutas.add(r);
				}
				listener.onData(aRutas.toArray(new Ruta[aRutas.size()]));
			}

			@Override
			public void onCancelled(DatabaseError err)
			{
				Log.e(TAG, String.format("buscarPorFiltro:onCancelled:%s",err));
				listener.onError(err.toString());
			}
		});
	}
	//----
	private static void buscarPorGeoFiltro(final ObjetoListener<Ruta> listener, final Filtro filtro, final String idRutaAct)
	{
		if(filtro.getRadio() < 1)filtro.setRadio(100);

		final ObjetoListener<String> lis = new ObjetoListener<String>()
		{
			@Override
			public void onData(final String[] aData)
			{
				final ArrayList<Ruta> aRutas = new ArrayList<>();
				final ArrayList<Ruta> aIgnorados = new ArrayList<>();
				if(aData.length < 1)listener.onData(new Ruta[0]);
				else
				for(String idRuta : aData)
				{
					Ruta.newFirebase().child(idRuta).addListenerForSingleValueEvent(new ValueEventListener()
					{
						@Override
						public void onDataChange(DataSnapshot data)
						{
							Ruta r = data.getValue(Ruta.class);
							if(r.pasaFiltro(filtro, r.getId(), idRutaAct))aRutas.add(r);
							else aIgnorados.add(r);
							if(aRutas.size()+aIgnorados.size() == aData.length)listener.onData(aRutas.toArray(new Ruta[aRutas.size()]));
						}
						@Override
						public void onCancelled(DatabaseError err)
						{
							Log.e(TAG, String.format("buscarPorGeoFiltro:onKeyEntered:onCancelled:2:e:%s",err));
							if(aRutas.size() == aData.length)listener.onData(aRutas.toArray(new Ruta[aRutas.size()]));
						}
					});
				}
			}
			@Override
			public void onError(String err)
			{
				listener.onError(err);	//System.err.println("Ruta:buscarPorGeoFiltro:onError:e:"+err);
			}
		};

		//------
		final GeoQuery geoQuery = RutaPunto.newGeoFire().queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		geoQuery.addGeoQueryEventListener(new GeoQueryEventListener()
		{
			private Set<String> asRutas = new TreeSet<>();
			private int nCount = 0;
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				nCount++;
				RutaPunto.newFirebase().child(key).addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot data)
					{
						nCount--;
						RutaPunto rp = data.getValue(RutaPunto.class);
						asRutas.add(rp.getIdRuta());
						if(nCount < 1)lis.onData(asRutas.toArray(new String[asRutas.size()]));
					}
					@Override
					public void onCancelled(DatabaseError err)
					{
						nCount--;
						Log.e(TAG, String.format("buscarPorGeoFiltro:onKeyEntered:onCancelled:e:%s",err));
						if(nCount < 1)lis.onData(asRutas.toArray(new String[asRutas.size()]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
				if(nCount == 0)lis.onData(new String[0]);
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}

			@Override public void onKeyExited(String key){}
			@Override public void onKeyMoved(String key, GeoLocation location){}
			@Override public void onGeoQueryError(DatabaseError err)
			{
				Log.e(TAG, String.format("buscarPorGeoFiltro:onGeoQueryError:%s",err));
			}
		});
	}


	//______________________________________________________________________________________________
	private long puntosCount = 0;
	public long getPuntosCount(){return puntosCount;}
	//______________________________________________________________________________________________
	public void getPuntos(final ValueEventListener listener)
	{
		RutaPunto.getLista(getId(), new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot ds)
			{
				puntosCount = ds.getChildrenCount();
				listener.onDataChange(ds);
			}
			@Override
			public void onCancelled(DatabaseError err)
			{
				puntosCount = 0;
				listener.onCancelled(err);
			}
		});
	}
	//______________________________________________________________________________________________
	public static void addPunto(final String idRuta, double lat, double lon,
	                            float precision, double altura, float velocidad, float direccion,
	                            final Transaction.Handler listener)
	{
		RutaPunto pto = new RutaPunto(idRuta, lat, lon, precision, altura, velocidad, direccion);
		pto.guardar(new DatabaseReference.CompletionListener()
		{
			@Override
			public void onComplete(DatabaseError err, DatabaseReference databaseReference)
			{
				if(err == null)
				{
					DatabaseReference ref = newFirebase().child(idRuta).child("puntosCount");
					ref.runTransaction(new Transaction.Handler()
					{
						@Override
						public Transaction.Result doTransaction(MutableData mutableData)
						{
							if(mutableData.getValue() == null)
								mutableData.setValue(1);
        					else
            					mutableData.setValue((Long)mutableData.getValue() + 1);
							return Transaction.success(mutableData);//we can also abort by calling Transaction.abort()
						}
						@Override
						public void onComplete(DatabaseError err, boolean b, DataSnapshot data)
						{
							//Log.w(TAG, "addPunto:inc count:"+err+" "+b+" "+data);
							listener.onComplete(err, b, data);
						}
					});
				}
			}
		});
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////
	// RUTA PUNTO
	////////////////////////////////////////////////////////////////////////////////////////////////////
	@IgnoreExtraProperties
	public static class RutaPunto implements Parcelable
	{
		public static final String NOMBRE = "ruta_punto";//TODO:? or parent?
		private static DatabaseReference newFirebase(){return Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(NOMBRE);}
		private static GeoFire newGeoFire(){return new GeoFire(Login.getDBInstance().getReference().child(Login.getCurrentUserID()).child(GEO).child(NOMBRE));}
		@Exclude
		private DatabaseReference _datos;

		protected String id = null;
			public String getId(){return id;}
			public void setId(String v){id = v;}

		String idRuta = null;
			public String getIdRuta(){return idRuta;}
			public void setIdRuta(String v){idRuta = v;}

		private double latitud, longitud;
			public double getLatitud(){return latitud;}
			public double getLongitud(){return longitud;}
			public void setLat(double v){latitud=v;}
			public void setLon(double v){longitud=v;}

		private Date fecha;
			public Date getFecha(){return fecha;}
			public void setFecha(Date v){fecha=v;}

		private float precision;
			public float getPrecision(){return precision;}
			//public void setPrecision(float v){precision = v;}
		private double altura;
			public double getAltura(){return altura;}
			//public void setAltura(double v){altura = v;}
		private float velocidad;
			public float getVelocidad(){return velocidad;}
			//public void setVelocidad(float v){velocidad = v;}
		private float direccion;
			public float getDireccion(){return direccion;}
			//public void setDireccion(float v){direccion = v;}

		//__________________________________________________________________________________________
		@Override
		public String toString()
		{
			return String.format(Locale.ENGLISH, "RutaPunto{id='%s', fecha='%s', latitud='%f', longitud='%f', idRuta='%s'}",
					getId(), DATE_FORMAT.format(fecha), latitud, longitud, idRuta);
		}
		//__________________________________________________________________________________________
		public RutaPunto(){}//Required for Firebase
		RutaPunto(String idRuta, double lat, double lon, float precision, double altura, float velocidad, float direccion)
		{
			this.idRuta = idRuta;
			this.latitud = lat;
			this.longitud = lon;
			this.fecha = new Date();
			this.precision = precision;
			this.altura = altura;
			this.velocidad = velocidad;
			this.direccion = direccion;
		}

		//// PARCEL
		RutaPunto(Parcel in)
		{
			setId(in.readString());
			setIdRuta(in.readString());
			setLat(in.readDouble());
			setLon(in.readDouble());
			setFecha(new Date(in.readLong()));
		}
		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			dest.writeString(getId());
			dest.writeString(getIdRuta());
			dest.writeDouble(getLatitud());
			dest.writeDouble(getLongitud());
			dest.writeLong(getFecha().getTime());
		}
		@Override
		public int describeContents(){return 0;}
		public static final Creator<RutaPunto> CREATOR = new Creator<RutaPunto>()
		{
			@Override
			public RutaPunto createFromParcel(Parcel in){return new RutaPunto(in);}
			@Override
			public RutaPunto[] newArray(int size){return new RutaPunto[size];}
		};

		//----------------------------------------------------------------------------------------------
		// FIREBASE
		public static void eliminar(final String idRuta)
		{
			//delGeo(idRuta);

			RutaPunto.getLista(idRuta, new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot ds)
				{
					for(DataSnapshot o : ds.getChildren())
					{
						RutaPunto rp = o.getValue(RutaPunto.class);
						newFirebase().child(rp.getId()).setValue(null, null);
						delGeo(rp.getId());
					}
				}
				@Override
				public void onCancelled(DatabaseError err)
				{
					Log.e(TAG, "RutaPunto:delGeo:e:"+err+", idRuta:"+idRuta);
				}
			});
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
		static void getLista(String sIdRuta, final ValueEventListener listener)
		{
			Query queryRef = RutaPunto.newFirebase().orderByChild(IDRUTA).equalTo(sIdRuta);
			//Query queryRef = newFirebase().equalTo("idRuta", sIdRuta);//No funciona
			//queryRef.addListenerForSingleValueEvent(listener);
			queryRef.addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot ds)
				{
					/*for(DataSnapshot o : ds.getChildren())
					{
						System.err.println("RutaPunto:getLista:onDataChange:o------------------------"+o);
						System.err.println("RutaPunto:getLista:onDataChange:rutPto---------------------"+o.getValue(Ruta.RutaPunto.class));
					}*/
					listener.onDataChange(ds);
				}
				@Override
				public void onCancelled(DatabaseError err)
				{
					listener.onCancelled(err);
				}
			});
		}
		// FIREBASE
		//----------------------------------------------------------------------------------------------

		//----------------------------------------------------------------------------------------------
		// GEOFIRE
		private void saveGeo()
		{
			final double DISTANCIA_MIN = 10/1000.0;//Km
			if(_datos.getKey() == null)
			{
				Log.e(TAG, "RutaPunto:saveGeo:id==null");
				return;
			}

			Query queryRef = RutaPunto.newFirebase().orderByChild(IDRUTA).equalTo(getIdRuta());
			queryRef.addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot ds)
				{
					//Los puntos geofire los utilizo solo para buscar.. de modo que no necesito todos...
					for(DataSnapshot o : ds.getChildren())
					{
						RutaPunto rp = o.getValue(RutaPunto.class);
						if(rp.distanciaReal(RutaPunto.this) < DISTANCIA_MIN)return;//No se guarda
					}
					saveGeo2();
				}
				@Override
				public void onCancelled(DatabaseError err)
				{
					Log.e(TAG, "RutaPunto:saveGeo:onCancelled:e:"+err);
				}
			});
		}
		private void saveGeo2()
		{
			newGeoFire().setLocation(_datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), new GeoFire.CompletionListener()
			{
				@Override
				public void onComplete(String key, DatabaseError error)
				{
					if(error != null)
						Log.e(TAG, "RutaPunto:saveGeo:e: There was an error saving the location to GeoFire: "+error+" : "+key+" : "+_datos.getKey()+" : "+getLatitud()+"/"+getLongitud()+" : "+idRuta);
					else
						Log.w(TAG, "RutaPunto:saveGeo: Location saved on server successfully! "+idRuta);
				}
			});
		}
		private static void delGeo(final String idRutaPunto)//TODO: Aqui estas borrando tambien -> mueve a RutaPunto delete
		{
			final GeoFire datGeo = newGeoFire();
			datGeo.removeLocation(idRutaPunto);
			/*RutaPunto.getLista(idRuta, new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot ds)
				{
					for(DataSnapshot o : ds.getChildren())
					{
						RutaPunto rp = o.getValue(RutaPunto.class);
						datGeo.removeLocation(rp.getId());
						newFirebase().child(rp.getId()).setValue(null, null);
System.err.println("----------------Ruta:delGeo:deleting:"+rp.getId());
					}
System.err.println("----------------Ruta:delGeo:"+ds.getRef());
				}
				@Override
				public void onCancelled(DatabaseError err)
				{
					System.out.println("RutaPunto:delGeo:e:"+err+" : "+idRuta);
				}
			});*/
		}
		// GEOFIRE
		//----------------------------------------------------------------------------------------------


		//----------------------------------------------------------------------------------------------
		//----------- HELPING FUNC
		/*public boolean equalTo(RutaPunto v)
		{
			return (getLatitud() == v.getLatitud() && getLongitud() == v.getLongitud());
		}
		public double distanciaSimple(RutaPunto v)
		{
			double dLat = getLatitud() - v.getLatitud();
			double dLon = getLongitud() - v.getLongitud();
			return dLat*dLat + dLon*dLon;
		}*/
		public float distanciaReal(RutaPunto v)
		{
			return distanciaReal(getLatitud(), getLongitud(), v.getLatitud(), v.getLongitud());
		}
		// http://www.movable-type.co.uk/scripts/latlong.html
		static float distanciaReal(double lat1, double lon1, double lat2, double lon2)//, double el1, double el2)
		{
			/*
			//Haversine
			final int R = 6371; // Radius of the earth (Km)
			Double latDistance = Math.toRadians(lat2 - lat1);
			Double lonDistance = Math.toRadians(lon2 - lon1);
			Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
					+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
					* Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
			Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			Log.e(TAG, "-----------0-----"+( R * c * 1000)); // convert to meters

			//Equirectangular approximation
			double latD2 = Math.toRadians(lat1+lat2);
			double la2 = Math.toRadians(lat2);
			double x = lonDistance * Math.cos(latD2/2);
			double y = latDistance;
			Log.e(TAG, "-----------1-----"+( Math.sqrt(x*x + y*y) * R * 1000));*/

			float[] dist = new float[1];
			android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, dist);
			//Log.e(TAG, "-----------2-----"+dist[0]);
			return dist[0];
		}
	}
}
