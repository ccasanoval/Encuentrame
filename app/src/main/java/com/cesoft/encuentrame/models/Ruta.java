package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.cesoft.encuentrame.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

//@formatter:off
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Config : max number of points per rute => cuando alcanza el limite corta...
//TODO: config : Radious of geofence (si se utilizase ruta por geofence)....
public class Ruta extends Objeto implements Parcelable
{
	public transient static final String NOMBRE = "ruta";//TRANSIENT so not to be included in backendless
	public transient static final String FECHA = "fecha";//TRANSIENT so not to be included in backendless

	public Ruta(){}

	/*private boolean activo = false;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean b){activo = b;}*/

	private List<GeoPoint> puntos = new ArrayList<>();
		public List<GeoPoint> getPuntos()
		{
			Collections.sort(puntos, new Comparator<GeoPoint>()
			{
        		@Override
        		public int compare(GeoPoint gp1, GeoPoint gp2)
				{
					Object o1 = gp1.getMetadata(FECHA);
					Object o2 = gp2.getMetadata(FECHA);
					if(o1 == null || o2 == null)return 1;//Dont compare...Supose are different...
					return String.valueOf(o1).compareTo(String.valueOf(o2));
     		   	}
    		});
			return puntos;
		}
		public void addPunto(GeoPoint gp){addPunto(gp, new java.util.Date());}
		public void addPunto(GeoPoint gp, Date d)
		{
			gp.addMetadata(FECHA, String.valueOf(d.getTime()));//Se guarda como string...
			puntos.add(gp);
		}
		public Date getFechaPunto(GeoPoint gp)
		{
			try
			{
				Object o = gp.getMetadata(FECHA);
				if(o == null)return null;
				long time = Long.parseLong(String.valueOf(o));//if(o instanceof String)time = Long.parseLong((String)o);
				return new Date(time);
			}
			catch(Exception e){System.err.println("Ruta:getFechaPunto:e:"+e+":::"+gp.getMetadata(FECHA));return null;}
		}

	//Quitar si se utiliza geofence tracking y cambiar por radio...
	private int periodo=2*60*1000;
		public int getPeriodo(){return periodo;}
		public void setPeriodo(int v){periodo=v;}

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
			String id = in.readString();
			double lat = in.readDouble();
			double lon = in.readDouble();
			GeoPoint gp = new GeoPoint(lat, lon);
			gp.addMetadata(FECHA, in.readLong());
			gp.setObjectId(id);
			puntos.add(gp);
		}
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeInt(puntos.size());
		for(GeoPoint p : puntos)
		{
			dest.writeString(p.getObjectId());
			dest.writeDouble(p.getLatitude());
			dest.writeDouble(p.getLongitude());
			try{dest.writeLong(Long.parseLong((String)p.getMetadata(FECHA)));}catch(Exception e){dest.writeLong(0);System.err.println("-----"+p.getMetadata(FECHA)+":e:"+e);}
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


	//// BACKENDLESS
	public void eliminar(AsyncCallback<Long> ac)
	{
System.err.println("Ruta:eliminar:r:" + this);
		for(GeoPoint gp : puntos)//Si borro directamente la ruta me dice: ClassCastException: com.backendless.geo.GeoPoint cannot be cast to java.util.Map
		{
			Backendless.Geo.removePoint(gp, new AsyncCallback<Void>()
			{
				@Override public void handleResponse(Void response){}
				@Override public void handleFault(BackendlessFault fault){}
			});
		}
		puntos.clear();
		/*Backendless.Persistence.save(this, new AsyncCallback<Ruta>()
		{
			@Override public void handleResponse(Ruta ruta){}
			@Override public void handleFault(BackendlessFault backendlessFault){}
		});*/
		Backendless.Persistence.of(Ruta.class).remove(this, ac);
	}
	public void guardar(AsyncCallback<Ruta> ac)
	{
		//Backendless.Persistence.of(Lugar.class).save(this, ac);
		Backendless.Persistence.save(this, ac);
	}
	public static void getById(String sId, AsyncCallback<BackendlessCollection<Ruta>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("puntos");//los puntos no los devuelve por orden!!!!!
		query.setWhereClause("objectId = '" + sId + "'");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Ruta.class).find(query, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Ruta>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("puntos");//los puntos no los devuelve por orden!!!!!
		queryOptions.addSortByOption("created ASC");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Ruta.class).find(query, res);
	}
	//public static void sortPuntos(GeoPoint[] gp)
	public static void getLista(AsyncCallback<BackendlessCollection<Ruta>> res, Filtro filtro)
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
	}

}
//@formatter:on