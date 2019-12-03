package com.cesoft.encuentrame3.models;

import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
@IgnoreExtraProperties
public class Ruta extends Objeto implements Parcelable
{
	private static final String TAG = Ruta.class.getSimpleName();
	public static final String NOMBRE = "ruta";
	private static final String IDRUTA = "idRuta";

	private static DatabaseReference newFirebase() {
		return Fire.newFirebase().child(NOMBRE);
	}
	@Exclude private DatabaseReference datos;

	private static final int DELAY = 5000;
	private Handler handler = new Handler();

	//______________________________________________________________________________________________
	public Ruta() { super(); }	//NOTE: Firebase necesita un constructor sin argumentos
	@NonNull
	@Override
	public String toString()
	{
		return String.format(Locale.ENGLISH, "Ruta{id='%s', nombre='%s', descripcion='%s', fecha='%s', puntosCount='%d'}",
				getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), DATE_FORMAT.format(fechaLong), puntosCount);
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
	public static final Creator<Ruta> CREATOR = new Creator<Ruta>()
	{
		@Override public Ruta createFromParcel(Parcel in){return new Ruta(in);}
		@Override public Ruta[] newArray(int size){return new Ruta[size];}
	};


	//// FIREBASE
	//
	public void eliminar(Fire.CompletadoListener listener)
	{
		RutaPunto.eliminar(getId());
		if(datos != null)
		{
			datos.setValue(null, listener);
		}
		else if(getId() != null)
		{
			datos = newFirebase().child(getId());
			datos.setValue(null, listener);
		}
		if(datos != null)
			datos.removeValue();
		puntosCount = 0;

		/// Just in case there's no Internet connection...
		handler.postDelayed(() -> listener.onComplete(null, newFirebase()), DELAY);
	}

	public void guardar(final Fire.CompletadoListener listener)
	{
		if(datos != null)
		{
			datos.setValue(this, listener);
		}
		else
		{
			if(getId() != null)
			{
				datos = newFirebase().child(getId());
			}
			else
			{
				datos = newFirebase().push();
				setId(datos.getKey());
			}
			datos.setValue(this, listener);
		}
		/// Just in case there's no Internet connection...
		handler.postDelayed(listener::onTimeout, DELAY);
	}
	public static void getById(String sId, final Fire.SimpleListener<Ruta> listener)
	{
		Query queryRef = newFirebase().orderByKey().equalTo(sId);
		queryRef.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot ds)
			{
				Ruta ruta = ds.getChildren().iterator().next().getValue(Ruta.class);
				listener.onDatos(new Ruta[]{ruta});
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
			{
				listener.onError(err.toString());
			}
		});
	}

	public static void getLista(final Fire.DatosListener<Ruta> listener)
	{
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel = new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot data)
			{
				int n = (int)data.getChildrenCount();
				if(n > 0)
				{
					ArrayList<Ruta> aRutas = new ArrayList<>(n);
					for(DataSnapshot o : data.getChildren()) {
						Ruta ruta = o.getValue(Ruta.class);
						if(ruta != null) {
							ruta.checkDateAndCorrect();
							aRutas.add(ruta);
						}
					}
					listener.onDatos(reverse(aRutas));
				}
				else
				{
					listener.onDatos(new Ruta[0]);
				}
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
			{
				Log.e(TAG, "getLista:onCancelled:"+err);
				listener.onError("Ruta:getLista:onCancelled:"+err);
			}
		};
		listener.setRef(ddbb);
		listener.setListener(vel);
		ddbb.addValueEventListener(vel);
	}

	//----------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------
	//@Override
	private boolean pasaFiltro(Filtro filtro, String idRuta, String idRutaAct)
	{
		if( ! super.pasaFiltro(filtro))return false;
		boolean bActivo = idRuta!=null && idRuta.equals(idRutaAct);//Ruta activa
		return !(filtro.getActivo() == Filtro.ACTIVO && !bActivo || filtro.getActivo() == Filtro.INACTIVO && bActivo);
	}
	public static void getLista(Fire.DatosListener<Ruta> listener, Filtro filtro, String idRutaAct)
	{
		if(filtro.getPunto().latitude == 0 && filtro.getPunto().longitude == 0)
			buscarPorFiltro(listener, filtro, idRutaAct);
		else
			buscarPorGeoFiltro(listener, filtro, idRutaAct);
	}
	//----
	private static void buscarPorFiltro(final Fire.DatosListener<Ruta> listener, final Filtro filtro, final String idRutaAct)
	{
		DatabaseReference ddbb = newFirebase();
		ValueEventListener vel = new ValueEventListener()
		{
			@Override
			public void onDataChange(@NonNull DataSnapshot data)
			{
				long n = data.getChildrenCount();
				ArrayList<Ruta> aRutas = new ArrayList<>((int)n);
				for(DataSnapshot o : data.getChildren())
				{
					Ruta ruta = o.getValue(Ruta.class);
					if(ruta == null || ! ruta.pasaFiltro(filtro, ruta.getId(), idRutaAct))continue;
					ruta.checkDateAndCorrect();
					aRutas.add(ruta);
				}
				listener.onDatos(reverse(aRutas));
			}
			@Override
			public void onCancelled(@NonNull DatabaseError err)
			{
				Log.e(TAG, String.format("buscarPorFiltro:onCancelled:%s",err));
				listener.onError(err.toString());
			}
		};
		listener.setRef(ddbb);
		listener.setListener(vel);
		ddbb.addValueEventListener(vel);
	}
	//----
	private static Fire.SimpleListener<String> getBuscarPorGeoFiltroSimpleListener(final Fire.SimpleListener<Ruta> listener, final Filtro filtro, final String idRutaAct) {
		return new Fire.SimpleListener<String>()
		{
			@Override
			public void onDatos(final String[] aData)
			{
				final ArrayList<Ruta> aRutas = new ArrayList<>();
				final ArrayList<Ruta> aIgnorados = new ArrayList<>();
				if(aData.length < 1)
					listener.onDatos(new Ruta[0]);
				else
				{
					for (String idRuta : aData) {
						Ruta.newFirebase().child(idRuta).addListenerForSingleValueEvent(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot data) {
								Ruta r = data.getValue(Ruta.class);
								if (r != null && r.pasaFiltro(filtro, r.getId(), idRutaAct))
									aRutas.add(r);
								else
									aIgnorados.add(r);
								if (aRutas.size() + aIgnorados.size() == aData.length) {
									listener.onDatos(reverse(aRutas));
								}
							}

							@Override
							public void onCancelled(@NonNull DatabaseError err) {
								Log.e(TAG, String.format("buscarPorGeoFiltro:onKeyEntered:onCancelled:2:e:%s", err));
								if (aRutas.size() == aData.length) {
									listener.onDatos(reverse(aRutas));
								}
							}
						});
					}
				}
			}
			@Override
			public void onError(String err)
			{
				listener.onError(err);
			}
		};
	}
	private static GeoQueryEventListener getBuscarPorGeoFiltroGeoQueryEventListener(Fire.SimpleListener<String> lis, GeoQuery geoQuery) {
		return new GeoQueryEventListener()
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
					public void onDataChange(@NonNull DataSnapshot data)
					{
						nCount--;
						RutaPunto rp = data.getValue(RutaPunto.class);
						if(rp != null)
							asRutas.add(rp.getIdRuta());
						if(nCount < 1)
							lis.onDatos(asRutas.toArray(new String[0]));
					}
					@Override
					public void onCancelled(@NonNull DatabaseError err)
					{
						nCount--;
						Log.e(TAG, String.format("buscarPorGeoFiltro:onKeyEntered:onCancelled:e:%s",err));
						if(nCount < 1)
							lis.onDatos(asRutas.toArray(new String[0]));
					}
				});
			}
			@Override
			public void onGeoQueryReady()
			{
				if(nCount == 0)
					lis.onDatos(new String[0]);
				geoQuery.removeGeoQueryEventListener(this);
			}

			@Override public void onKeyExited(String key){}
			@Override public void onKeyMoved(String key, GeoLocation location){}
			@Override public void onGeoQueryError(DatabaseError err)
			{
				Log.e(TAG, String.format("buscarPorGeoFiltro:onGeoQueryError:%s",err));
			}
		};
	}
	private static void buscarPorGeoFiltro(final Fire.SimpleListener<Ruta> listener, final Filtro filtro, final String idRutaAct)
	{
		if(filtro.getRadio() < 1)filtro.setRadio(100);

		final Fire.SimpleListener<String> lis = getBuscarPorGeoFiltroSimpleListener(listener, filtro, idRutaAct);
		final GeoQuery geoQuery = RutaPunto.newGeoFire().queryAtLocation(new GeoLocation(filtro.getPunto().latitude, filtro.getPunto().longitude), filtro.getRadio()/1000.0);
		geoQuery.addGeoQueryEventListener(getBuscarPorGeoFiltroGeoQueryEventListener(lis, geoQuery));
	}


	//______________________________________________________________________________________________
	private long puntosCount = 0;
	public long getPuntosCount(){return puntosCount;}
	//______________________________________________________________________________________________
	public void getPuntos(final Fire.SimpleListener<Ruta.RutaPunto> listener)
	{
		RutaPunto.getLista(getId(), new Fire.SimpleListener<Ruta.RutaPunto>()
		{
			@Override
			public void onDatos(RutaPunto[] aData)
			{
				puntosCount = aData.length;
				listener.onDatos(aData);
				//
				Ruta.getById(getId(), new Fire.SimpleListener<Ruta>() {
					@Override
					public void onDatos(Ruta[] aData) {
						if(aData.length > 0) {
							aData[0].puntosCount = puntosCount;
							//...
							//TODO: salvar ruta...
						}
					}

					@Override
					public void onError(String err) {

					}
				});
			}
			@Override
			public void onError(String err)
			{
				puntosCount = 0;
				listener.onError(err);
			}
		});
	}
	//______________________________________________________________________________________________
	public static void addPunto(final String idRuta, double lat, double lon,
	                            float precision, double altura, float velocidad, float direccion,
								int actividad,
								final Fire.SimpleListener<Long> listener)
	{
		RutaPunto pto = new RutaPunto(idRuta, lat, lon, precision, altura, velocidad, direccion, actividad);
		pto.guardar((err, databaseReference) ->
		{
			if(err == null)
			{
				DatabaseReference ref = newFirebase().child(idRuta).child("puntosCount");
				ref.runTransaction(new Transaction.Handler()
				{
					@NonNull
					@Override
					public Transaction.Result doTransaction(@NonNull MutableData mutableData)
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
						if(err == null)
							listener.onDatos(new Long[]{(Long)data.getValue()});
						else
							listener.onError(err.getMessage()+", "+err.getCode());
					}
				});
			}
			else
				listener.onError(err.getMessage()+", "+err.getCode());
		});
	}



	////////////////////////////////////////////////////////////////////////////////////////////////
	// RUTA PUNTO
	////////////////////////////////////////////////////////////////////////////////////////////////
	@IgnoreExtraProperties
	public static class RutaPuntoOld extends RutaPunto {
		@Exclude public long fechaLong;
	}

	@IgnoreExtraProperties
	public static class RutaPunto implements Parcelable
	{
		public static final String NOMBRE = "ruta_punto";
		private static DatabaseReference newFirebase() {
			return Fire.newFirebase().child(NOMBRE);
		}
		private static GeoFire newGeoFire() {
			return new GeoFire(Fire.newFirebase().child(GEO).child(NOMBRE));
		}
		@Exclude
		private DatabaseReference datos;

		protected String id = null;
			public String getId(){return id;}
			public void setId(String v){id = v;}

		@SuppressWarnings("WeakerAccess")
		public String idRuta = null;			// Very fkng important, it must be public!
			String getIdRuta(){return idRuta;}
			void setIdRuta(String v){idRuta = v;}

		private double latitud;
		private double longitud;
			public double getLatitud(){return latitud;}
			public double getLongitud(){return longitud;}
			void setLat(double v){latitud=v;}
			void setLon(double v){longitud=v;}

		public long fechaLong;
		@Exclude public long getFecha() {
				return fechaLong;
			}
		@Exclude void setFecha(Long v) {
				fechaLong = v;
			}

		private float precision;
			public float getPrecision(){return precision;}
		private double altura;
			public double getAltura(){return altura;}
		private float velocidad;
			public float getVelocidad(){return velocidad;}
		private float direccion;
			public float getDireccion(){return direccion;}

//		DetectedActivity.STILL,
//		DetectedActivity.ON_FOOT,
//		DetectedActivity.WALKING,
//		DetectedActivity.RUNNING,
//		DetectedActivity.ON_BICYCLE,
//		DetectedActivity.IN_VEHICLE,
//		DetectedActivity.TILTING,
//		DetectedActivity.UNKNOWN
		private int actividad;//
			public int getActividad(){return actividad;}

		//__________________________________________________________________________________________
		@NonNull
		@Override public String toString()
		{
			return String.format(Locale.ENGLISH, "RutaPunto{id='%s', fecha='%s', latitud='%f', longitud='%f', idRuta='%s'}",
					getId(), DATE_FORMAT.format(fechaLong), latitud, longitud, idRuta);
		}
		//__________________________________________________________________________________________
		@SuppressWarnings("unused") public RutaPunto(){}//NOTE: Constructor with no arguments required by Firebase !!!!!!!!!!!!!!!!!!!!
		RutaPunto(String idRuta, double lat, double lon, float precision,
						  double altura, float velocidad, float direccion, int actividad)
		{
			this.idRuta = idRuta;
			this.latitud = lat;
			this.longitud = lon;
			this.fechaLong = new Date().getTime();
			this.precision = precision;
			this.altura = altura;
			this.velocidad = velocidad;
			this.direccion = direccion;
			this.actividad = actividad;
		}

		//// PARCEL
		@SuppressWarnings("unused") RutaPunto(Parcel in)
		{
			setId(in.readString());
			setIdRuta(in.readString());
			setLat(in.readDouble());
			setLon(in.readDouble());
			setFecha(in.readLong());
		}
		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			dest.writeString(getId());
			dest.writeString(getIdRuta());
			dest.writeDouble(getLatitud());
			dest.writeDouble(getLongitud());
			dest.writeLong(getFecha());
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
		public static void eliminarPto(final String idRutaPunto) {
			Query queryRef = RutaPunto.newFirebase().child(idRutaPunto);
			queryRef.addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(@NonNull DataSnapshot ds) {
					RutaPunto rp = ds.getValue(RutaPunto.class);
					if(rp != null) {
						newFirebase().child(rp.getId()).setValue(null, null);
						delGeo(rp.getId());
					}
				}
				@Override
				public void onCancelled(@NonNull DatabaseError err)
				{
					Log.e(TAG, "RutaPunto:eliminar:e:"+err+", idRuta:");
				}
			});
		}

		public static void eliminar(final String idRuta)
		{
			Query queryRef = RutaPunto.newFirebase().orderByChild(IDRUTA).equalTo(idRuta);
			queryRef.addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(@NonNull DataSnapshot ds)
				{
					for(DataSnapshot o : ds.getChildren())
					{
						RutaPunto rp = o.getValue(RutaPunto.class);
						if(rp != null) {
							newFirebase().child(rp.getId()).setValue(null, null);
							delGeo(rp.getId());
						}
					}
				}
				@Override
				public void onCancelled(@NonNull DatabaseError err)
				{
					Log.e(TAG, "RutaPunto:eliminar:e:"+err+", idRuta:"+idRuta);
				}
			});
		}
		public void guardar(DatabaseReference.CompletionListener listener)
		{
			if(datos != null)
			{
				datos.setValue(this, listener);
			}
			else
			{
				if(getId() != null)
				{
					datos = newFirebase().child(getId());
				}
				else
				{
					datos = newFirebase().push();
					setId(datos.getKey());
				}
				datos.setValue(this, listener);
			}
			saveGeo();
		}
		public static void getLista(String sIdRuta, final Fire.SimpleListener<Ruta.RutaPunto> listener)
		{
			Query queryRef = RutaPunto.newFirebase().orderByChild(IDRUTA).equalTo(sIdRuta);
			queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot ds)
				{
					int i = 0;
					Ruta.RutaPunto[] aPts = new Ruta.RutaPunto[(int)ds.getChildrenCount()];
					for(DataSnapshot o : ds.getChildren())
					{
						Ruta.RutaPunto pto = o.getValue(Ruta.RutaPunto.class);

						aPts[i++] = pto;
					}
					listener.onDatos(aPts);
				}
				@Override
				public void onCancelled(@NonNull DatabaseError err)
				{
					listener.onError("Ruta:getLista:onCancelled:e:"+err);
				}
			});
		}
		public static void getListaRep(String sIdRuta, final Fire.DatosListener<Ruta.RutaPunto> listener)
		{
			Query queryRef = RutaPunto.newFirebase().orderByChild(IDRUTA).equalTo(sIdRuta);
			ValueEventListener vel = new ValueEventListener()
			{
				@Override
				public void onDataChange(@NonNull DataSnapshot data)
				{
					long n = data.getChildrenCount();
					ArrayList<Ruta.RutaPunto> a = new ArrayList<>((int)n);
						for (DataSnapshot o : data.getChildren())
							try {
								a.add(o.getValue(Ruta.RutaPunto.class));
							}
							catch(Exception e) {
								Log.e(TAG, "getListaRep:onDataChange:----------------------------------------e", e);
								a.add(o.getValue(Ruta.RutaPuntoOld.class));
							}

					listener.onDatos(a.toArray(new RutaPunto[0]));
				}
				@Override
				public void onCancelled(@NonNull DatabaseError err)
				{
					Log.e(TAG, "getListaAsync:onCancelled:"+err);
					listener.onError("Ruta:getListaAsync:onCancelled:"+err);
				}
			};
			listener.setRef(queryRef.getRef());
			listener.setListener(vel);
			queryRef.addValueEventListener(vel);
		}
		// FIREBASE
		//----------------------------------------------------------------------------------------------

		//----------------------------------------------------------------------------------------------
		// GEOFIRE
		private void saveGeo()
		{
			final double DISTANCIA_MIN = 10/1000.0;//Km
			if(datos.getKey() == null)
			{
				Log.e(TAG, "RutaPunto:saveGeo:id==null");
				return;
			}

			Query queryRef = RutaPunto.newFirebase().orderByChild(IDRUTA).equalTo(getIdRuta());
			queryRef.addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(@NonNull DataSnapshot ds)
				{
					//Los puntos geofire los utilizo solo para buscar.. de modo que no necesito todos...
					for(DataSnapshot o : ds.getChildren())
					{
						RutaPunto rp = o.getValue(RutaPunto.class);
						if(rp != null && rp.distanciaReal(RutaPunto.this) < DISTANCIA_MIN)return;//No se guarda
					}
					saveGeo2();
				}
				@Override
				public void onCancelled(@NonNull DatabaseError err)
				{
					Log.e(TAG, "RutaPunto:saveGeo:onCancelled:e:"+err);
				}
			});
		}
		private void saveGeo2()
		{
			if(datos.getKey() != null)
				newGeoFire().setLocation(datos.getKey(), new GeoLocation(getLatitud(), getLongitud()), (key, error) ->
				{
					if(error != null)
						Log.e(TAG, "RutaPunto:saveGeo2:e: There was an error saving the location to GeoFire: "+error+" : "+key+" : "+ datos.getKey()+" : "+getLatitud()+"/"+getLongitud()+" : "+idRuta);
				});
		}
		private static void delGeo(final String idRutaPunto)
		{
			final GeoFire datGeo = newGeoFire();
			datGeo.removeLocation(idRutaPunto, (key, error) -> {
				if(error != null)	Log.e(TAG, "delGeo:e:"+key+" - "+error);
				//else				Log.e(TAG, "delGeo:OK:"+key);
			});
		}
		// GEOFIRE
		//----------------------------------------------------------------------------------------------


		//----------------------------------------------------------------------------------------------
		//----------- HELPING FUNC
		public float distanciaReal(RutaPunto v)
		{
			if(v == null)return 0;
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
			return dist[0];
		}
	}


	private static Ruta[] reverse(ArrayList<Ruta> aRutas) {
		Collections.reverse(aRutas);
		return aRutas.toArray(new Ruta[0]);
	}

}
