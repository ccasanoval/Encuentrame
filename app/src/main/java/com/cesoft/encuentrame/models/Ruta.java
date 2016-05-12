package com.cesoft.encuentrame.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
// ONLINE C COMPILER http://cpp.sh

//TODO: por que veo  ---------Ruta:getPuntosCount: 53 : DataSnapshot { key = puntosCount, value = 53 }   mil veces???
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Config : max number of points per rute => cuando alcanza el limite corta...
//TODO: config : Radious of geofence (si se utilizase ruta por geofence)....
@JsonIgnoreProperties({"_datos"})
public class Ruta extends Objeto implements Parcelable
{
	public static final String NOMBRE = "ruta";
	private Firebase _datos;

	//TODO: fecha fin...
	//Quitar si se utiliza geofence tracking y cambiar por radio...
	/*private int periodo=2*60*1000;
		public int getPeriodo(){return periodo;}
		public void setPeriodo(int v){periodo=v;}*/

//TODO:
protected static Firebase newFirebase(){return new Firebase(FIREBASE).child(NOMBRE);}
protected static GeoFire newGeoFire(){return new GeoFire(new Firebase(GEOFIRE).child(NOMBRE));}
	//______________________________________________________________________________________________
	public Ruta(){}
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
		puntosCount = in.readLong();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
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
	public void eliminar(Firebase.CompletionListener listener)
	{
System.err.println("Ruta:eliminar:r:" + this);
		if(_datos != null)
		{
			_datos.setValue(null, listener);
		}
		else if(getId() != null)
		{
			_datos = newFirebase().child(NOMBRE).child(getId());
			_datos.setValue(null, listener);
		}
		RutaPunto.eliminar(getId());
	}
	public void guardar(Firebase.CompletionListener listener)
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
	/*public static void getLista(ValueEventListener listener)
	{
		//ref.addValueEventListener(listener);//TODO: cual me viene mejor?
		newFirebase().addListenerForSingleValueEvent(listener);
	}*/
	public static void getLista(final ObjetoListener<Ruta> listener)
	{
		newFirebase().addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot data)
			{
				int n = (int)data.getChildrenCount();
System.err.println("Ruta:getLista:a----------"+n);
				ArrayList<Ruta> aRutas = new ArrayList<>(n);
				for(DataSnapshot o : data.getChildren())
					aRutas.add(o.getValue(Ruta.class));
				listener.onData(aRutas.toArray(new Ruta[n]));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("Ruta:getLista:onCancelled:"+err);
				listener.onError("Ruta:getLista:onCancelled:"+err);
			}
		});
	}

	//----------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------
	/*@Override
	public boolean pasaFiltro(Filtro filtro)//TODO:
	{
	final String sId = Util.getTrackingRoute();//Ruta activa
		if(sId.isEmpty())return;
		Ruta.getById(sId, new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot rutas)
			{

		if( ! super.pasaFiltro(filtro))return false;
		if(filtro.getActivo()==Filtro.ACTIVO && !isActivo()  ||  filtro.getActivo()==Filtro.INACTIVO && isActivo())return false;
		return true;
	}*/
	public static void getLista(ObjetoListener<Ruta> listener, Filtro filtro)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro);
		else
			buscarPorGeoFiltro(listener, filtro);
	}
	//----
	public static void buscarPorFiltro(final ObjetoListener<Ruta> listener, final Filtro filtro)
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
					if( ! r.pasaFiltro(filtro))continue;
					aRutas.add(r);
				}
				listener.onData(aRutas.toArray(new Ruta[aRutas.size()]));
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("Ruta:buscarPorFiltro:onCancelled:"+err);
				listener.onError(err.toString());
			}
		});
	}
	//----
	public static void buscarPorGeoFiltro(final ObjetoListener<Ruta> listener, final Filtro filtro)
	{//TODO: guardar puntos de ruta en GeoFire y buscar por esoso puntos y luego por la ruta asociada. Hacer algo para no almacenar puntos redundantes, distancia > 10 metros (no solo con punto anterior?)
System.err.println("Ruta:buscarPorGeoFiltro:--------------------------:"+filtro);
		if(filtro.getRadio() < 1)filtro.setRadio(100);

		//TODO:
		//TODO:
		//TODO:
		//TODO:
		//TODO:-------------------------------------------------------------------------------------
		//TODO:
		//TODO:
		//TODO:
		//TODO:

		final ArrayList<Ruta> aRutas = new ArrayList<>();//TODO buscar RutaPunto  : como hacer join???????
		final Firebase ref = newFirebase();
		final GeoQuery geoQuery = newGeoFire().queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);//TODO: dejar el radio del filtro en metros antes
		GeoQueryEventListener lisGeo = new GeoQueryEventListener()
		{
			private int nCount = 0;
			@Override
			public void onKeyEntered(String key, GeoLocation location)
			{
				System.err.println("Ruta:buscarPorGeoFiltro:onKeyEntered:"+key+", "+location);
				nCount++;
				ValueEventListener vel = new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot data)
					{
						nCount--;
						Ruta r = data.getValue(Ruta.class);
						if( ! r.pasaFiltro(filtro))return;
						aRutas.add(r);
						if(nCount < 1)listener.onData(aRutas.toArray(new Ruta[aRutas.size()]));
					}
					@Override
					public void onCancelled(FirebaseError err)
					{
						nCount--;
						System.err.println("Ruta:buscarPorGeoFiltro:onKeyEntered:onCancelled:"+err);
						if(nCount < 1)listener.onData(aRutas.toArray(new Ruta[aRutas.size()]));
					}
				};
				//newFirebase().child(key).addListenerForSingleValueEvent(vel);
				ref.child(key).addListenerForSingleValueEvent(vel);
			}
			@Override
			public void onGeoQueryReady()
			{
System.err.println("Ruta:buscarPorGeoFiltro:--------------------------------------------onGeoQueryReady:A:"+nCount);
				geoQuery.removeGeoQueryEventListener(this);//geoQuery.removeAllListeners();
			}
			@Override public void onKeyExited(String key){}
			@Override public void onKeyMoved(String key, GeoLocation location){}
			@Override public void onGeoQueryError(FirebaseError error)
			{
				System.err.println("Ruta:buscarPorGeoFiltro:onGeoQueryError:"+error);
			}
		};
		geoQuery.addGeoQueryEventListener(lisGeo);
	}


	//______________________________________________________________________________________________
	private long puntosCount = 0;
	public long getPuntosCount()//TODO: Se llama demasiado??
	{
		Ruta.getPuntosCount(getId(), new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot count)
			{
				if(count != null && count.getValue() != null)
					puntosCount = (Long)count.getValue();
System.err.println("---------Ruta:getPuntosCount: "+puntosCount+" : "+count);
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				System.err.println("Ruta:getPuntosCount:e:"+err);
			}
		});
		return puntosCount;
	}
	public static void getPuntosCount(String idRuta, ValueEventListener listener)
	{
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE).child(idRuta).child("puntosCount");
		ref.addListenerForSingleValueEvent(listener);//ref.addValueEventListener(listener);
	}
	public void getPuntos(final ValueEventListener listener)
	{
System.err.println("---------Ruta:getPuntos:0:"+getId());
		RutaPunto.getLista(getId(), new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot ds)
			{
				puntosCount = ds.getChildrenCount();
				listener.onDataChange(ds);
			}
			@Override
			public void onCancelled(FirebaseError err)
			{
				puntosCount = 0;
				listener.onCancelled(err);
			}
		});
		/*
		RutaPunto.getLista(getId(), new ObjetoListener<RutaPunto>()
		{
			@Override
			public void onData(RutaPunto[] aData)
			{
				puntosCount = aData.length;
				listener.onData(aData);
			}
			@Override
			public void onError(String err)
			{
				puntosCount = 0;
				listener.onError(err.toString());
			}
		});
		* */
	}
	//______________________________________________________________________________________________
	public static void addPunto(final String idRuta, double lat, double lon, final Firebase.CompletionListener listener)
	{
		RutaPunto pto = new RutaPunto(idRuta, lat, lon);
		pto.guardar(new Firebase.CompletionListener()
		{
			@Override
			public void onComplete(FirebaseError err, Firebase firebase)
			{
				if(err == null)
				{
					Firebase ref = new Firebase(FIREBASE).child(NOMBRE).child(idRuta).child("puntosCount");
					ref.runTransaction(new Transaction.Handler()
					{
						@Override
						public Transaction.Result doTransaction(MutableData currentData)
						{
							if(currentData.getValue() == null)
								currentData.setValue(1);
        					else
            					currentData.setValue((Long)currentData.getValue() + 1);
							return Transaction.success(currentData);//we can also abort by calling Transaction.abort()
						}
						@Override
						public void onComplete(FirebaseError err, boolean b, DataSnapshot dataSnapshot)
						{
							System.err.println("Ruta:addPunto:inc count:e:"+err+" "+b+" "+dataSnapshot);
						}
					});
				}
				listener.onComplete(err, firebase);
			}
		});
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////
	// RUTA PUNTO
	////////////////////////////////////////////////////////////////////////////////////////////////////
	@JsonIgnoreProperties({"_datos"})
	public static class RutaPunto implements Parcelable
	{
		public static final String NOMBRE = "ruta_punto";
		private Firebase _datos;

		protected String id = null;
			public String getId(){return id;}
			public void setId(String v){id = v;}

		protected String idRuta = null;
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

//TODO:
protected static Firebase newFirebase(){return new Firebase(FIREBASE).child(NOMBRE);}
protected static GeoFire newGeoFire(){return new GeoFire(new Firebase(GEOFIRE).child(NOMBRE));}

		//__________________________________________________________________________________________
		@Override
		public String toString()
		{
			return String.format(Locale.ENGLISH, "RutaPunto{id='%s', fecha='%s', latitud='%f', longitud='%f'}",
					getId(), DATE_FORMAT.format(fecha), latitud, longitud);
		}
		//__________________________________________________________________________________________
		//public RutaPunto(){}
		public RutaPunto(String idRuta, double lat, double lon)
		{
			this.idRuta = idRuta;
			this.latitud = lat;
			this.longitud = lon;
			this.fecha = new Date();
		}

		//// PARCEL
		protected RutaPunto(Parcel in)
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

		//// FIREBASE
		public static void eliminar(String idRuta)
		{
			Query queryRef = newFirebase().orderByChild("idRuta").equalTo(idRuta);
			queryRef.addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot puntos)
				{
					puntos.getRef().setValue(null);
					System.err.println("-----------Ruta:RutaPunto:eliminar:");
				}
				@Override
				public void onCancelled(FirebaseError err)
				{
					System.err.println("-----------Ruta:RutaPunto:eliminar:e:"+err);
				}
			});
		}
		public void guardar(Firebase.CompletionListener listener)
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
		public static void getLista(String sIdRuta, ValueEventListener listener)
		{
System.err.println("RutaPunto:getLista---------"+NOMBRE);
			Query queryRef = newFirebase().child("idRuta").equalTo(sIdRuta);//TODO: cambiar los orderby por child...
			//Query queryRef = newFirebase().equalTo("idRuta", sIdRuta);//No funciona
			queryRef.addListenerForSingleValueEvent(listener);
		}



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
		public double distanciaReal(RutaPunto v)
		{
			return distanciaReal(getLatitud(), getLongitud(), v.getLatitud(), v.getLongitud());
		}
		/*
		 * Calculate distance between two points in latitude and longitude taking into account height difference.
		 * Uses Haversine method as its base.
		 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters el2 End altitude in meters
		 * @returns Distance in Meters
		 */
		public static double distanciaReal(double lat1, double lat2, double lon1, double lon2)//, double el1, double el2)
		{
			final int R = 6371; // Radius of the earth
			Double latDistance = Math.toRadians(lat2 - lat1);
			Double lonDistance = Math.toRadians(lon2 - lon1);
			Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
					+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
			Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			return R * c * 1000; // convert to meters
			// -- sin altura --
			//double distancia = R * c * 1000; // convert to meters
			//double height = el1 - el2;
			//distancia = Math.pow(distancia, 2) + Math.pow(height, 2);
			//return Math.sqrt(distancia);
		}
	}
}
