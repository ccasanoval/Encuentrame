package com.cesoft.encuentrame.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.cesoft.encuentrame.Util;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoLocation;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Config : max number of points per rute => cuando alcanza el limite corta...
//TODO: config : Radious of geofence (si se utilizase ruta por geofence)....
public class Ruta extends Objeto implements Parcelable
{
	public static final String NOMBRE = "ruta";

	private Firebase _datos;

	private List<GeoLocation> puntos = new ArrayList<>();
		public List<GeoLocation> getPuntos()
		{
			return puntos;
		}
		public void addPunto(GeoLocation gp){addPunto(gp, new java.util.Date());}
		public void addPunto(GeoLocation gp, Date d)
		{
			//TODO
			//gp.addMetadata(FECHA, String.valueOf(d.getTime()));//Se guarda como string...
			puntos.add(gp);
		}
		public Date getFechaPunto(GeoLocation gp)
		{
			/*try
			{
				Object o = gp.getMetadata(FECHA);
				if(o == null)return null;
				long time = Long.parseLong(String.valueOf(o));//if(o instanceof String)time = Long.parseLong((String)o);
				return new Date(time);
			}
			catch(Exception e){System.err.println("Ruta:getFechaPunto:e:"+e+":::"+gp.getMetadata(FECHA));return null;}
			*/
			return new Date();//TODO
		}

	//Quitar si se utiliza geofence tracking y cambiar por radio...
	/*private int periodo=2*60*1000;
		public int getPeriodo(){return periodo;}
		public void setPeriodo(int v){periodo=v;}*/

	//______________________________________________________________________________________________
	public Ruta(){}
	@Override
	public String toString()
	{
		return super.toString() + ", RUT:"+(puntos==null?"null":puntos.size());
	}

	//// PARCEL
	protected Ruta(Parcel in)
	{
		super(in);
		//
		puntos.clear();
		int n = in.readInt();
		for(int i=0; i < n; i++)
		{
			//String id = in.readString();
			double lat = in.readDouble();
			double lon = in.readDouble();
			GeoLocation gp = new GeoLocation(lat, lon);
			//gp.addMetadata(FECHA, in.readLong());
			//gp.setObjectId(id);
			//TODO
			puntos.add(gp);
		}
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeInt(puntos.size());
		for(GeoLocation p : puntos)
		{
			//TODO
			//dest.writeString(p.getObjectId());
			dest.writeDouble(p.latitude);
			dest.writeDouble(p.longitude);
			//try{dest.writeLong(Long.parseLong((String)p.getMetadata(FECHA)));}catch(Exception e){dest.writeLong(0);System.err.println("-----"+p.getMetadata(FECHA)+":e:"+e);}
		}
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
			Firebase ref = new Firebase(FIREBASE);
			_datos = ref.child(NOMBRE).child(getId());
			_datos.setValue(null, listener);
		}
		puntos.clear();
	}
	public void guardar(Firebase.CompletionListener listener)
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
	public static void getById(String sId, ValueEventListener listener)
	{
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		Query queryRef = ref.orderByKey().equalTo(sId);//.limitToFirst(1);
		queryRef.addListenerForSingleValueEvent(listener);
    	//queryRef.addChildEventListener(listener);
		//ref.addListenerForSingleValueEvent(listener);
	}
	public static void getLista(ValueEventListener listener)
	{
		Firebase ref = new Firebase(FIREBASE).child(NOMBRE);
		ref.addValueEventListener(listener);
		//ref.addListenerForSingleValueEvent(listener);
	}

	public static void getLista(ValueEventListener listener, Filtro filtro)
	{
//TODO-----------------------------------------------------------------------------------------------------
getLista(listener);
if(1==1)return;
	}

	//public static void sortPuntos(GeoPoint[] gp)
	/*public static void getLista(AsyncCallback<BackendlessCollection<Ruta>> res, Filtro filtro)
	{
System.err.println("Ruta:getLista:filtro: "+filtro);
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addSortByOption("created ASC");
		queryOptions.addRelated("puntos");
		query.setQueryOptions(queryOptions);
		//--FILTRO
		StringBuilder sb = new StringBuilder();//" created = created "
		if( ! filtro.getNombre().isEmpty())
		{
			sb.append(" nombre LIKE '%");
			sb.append(filtro.getNombre());
			sb.append("%' ");
		}
		if(filtro.getRadio() > 0 && filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(String.format(java.util.Locale.ENGLISH, " distance(%f, %f, puntos.latitude, puntos.longitude ) < km(%f) ",
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
		if(filtro.getActivo() != Util.NADA)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(" activo = ");
			sb.append(filtro.getActivo()==Filtro.ACTIVO?"true":"false");
		}
System.err.println("Ruta:getLista:SQL: "+sb.toString());
		if(sb.length() > 0)
			query.setWhereClause(sb.toString());
		//--FILTRO
		Backendless.Persistence.of(Ruta.class).find(query, res);
	}*/

	////////////////////////////////////////////////////////////////////////////////////////////////////
	public static class RutaPunto implements Parcelable
	{
		protected String id = null;
			public String getId(){return id;}
			public void setId(String v){id = v;}

		protected String idRuta = null;
			public String getIdRuta(){return idRuta;}
			public void setIdRuta(String v){idRuta = v;}

		private double lat, lon;
			public double getLat(){return lat;}
			public double getLon(){return lon;}
			public void setLat(double v){lat=v;}
			public void setLon(double v){lon=v;}

		private Date fecha;
			public Date getFecha(){return fecha;}
			public void setFecha(Date v){fecha=v;}

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
			dest.writeDouble(getLat());
			dest.writeDouble(getLon());
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
	}
}
