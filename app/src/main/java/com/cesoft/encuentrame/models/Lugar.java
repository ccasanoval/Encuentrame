package com.cesoft.encuentrame.models;

import android.os.Parcel;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;

import weborb.service.ExcludeProperties;


////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//ExcludeProperty(propertyName = "latitud")
@ExcludeProperties(propertyNames = { "latitud, longitud, Latitud, Longitud" })
public class Lugar extends Objeto
{
	public transient static final String NOMBRE = "lugar";//TRANSIENT so not to include in backendless

	public Lugar(){}

	//______________________________________________________________________________________________
	private GeoPoint lugar = new GeoPoint(0,0);
		public GeoPoint getLugar(){return lugar;}
		public void setLugar(GeoPoint v){lugar=v;}

		public double getLatitud(){if(lugar==null || lugar.getLatitude() == null)return 0.0;return lugar.getLatitude();}
		public double getLongitud(){if(lugar==null || lugar.getLatitude() == null)return 0.0;return lugar.getLongitude();}
		public void setLatLon(double lat, double lon){lugar.setLatitude(lat);lugar.setLongitude(lon);}

	//______________________________________________________________________________________________
	public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', fecha='%s == %d'}",
				getObjectId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), created!=null?DATE_FORMAT.format(created):"", created!=null?created.getTime():0);
	}
	//______________________________________________________________________________________________
	@Override public boolean equals(Object o)
	{
		if(o == null)return false;
		if(this == o)return true;
		if(!(o instanceof Lugar))return false;
		Lugar a = (Lugar)o;
		return ( (getObjectId() == null && a.getObjectId() == null) || (a.getObjectId() != null && getObjectId().equals(a.getObjectId())) )
			&& getLatitud() == a.getLatitud() && getLongitud() == a.getLongitud()
			&& getNombre().equals(a.getNombre()) && getDescripcion().equals(a.getDescripcion());
	}

	//// PARCELABLE
	//
	protected Lugar(Parcel in)
	{
		super(in);
		//
		lugar.setObjectId(in.readString());
		lugar.setLatitude(in.readDouble());
		lugar.setLongitude(in.readDouble());
//System.err.println("----------------Lugar:from parcel 2:" + this);
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		if(lugar == null)lugar = new GeoPoint(0,0);
		dest.writeString(lugar.getObjectId());
		dest.writeDouble(lugar.getLatitude());
		dest.writeDouble(lugar.getLongitude());
System.err.println("----------------Lugar:writeToParcel:"+lugar);
	}
	@Override
	public int describeContents(){return 0;}
	public static final Creator<Lugar> CREATOR = new Creator<Lugar>()
	{
		@Override
		public Lugar createFromParcel(Parcel in){return new Lugar(in);}
		@Override
		public Lugar[] newArray(int size){return new Lugar[size];}
	};


	//// BACKENDLESS
	//
	public void eliminar(AsyncCallback<Long> ac)
	{
		//removePoint( GeoPoint geoPoint, AsyncCallback<Void> responder )
		Backendless.Persistence.of(Lugar.class).remove(this, ac);
	}
	public void guardar(AsyncCallback<Lugar> ac)
	{
		//Backendless.Persistence.of(Lugar.class).save(this, ac);
		Backendless.Persistence.save(this, ac);
	}

	public static void getById(String sId, AsyncCallback<Lugar> res)
	{
		ArrayList<String> relationProps = new ArrayList<>();
		relationProps.add("lugar");
		Backendless.Persistence.of(Lugar.class).findById(sId, relationProps, res);
	}
	public static void getActivos(AsyncCallback<BackendlessCollection<Lugar>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		query.setWhereClause("activo > 0");
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated(NOMBRE);
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Lugar.class).find(query, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Lugar>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated(NOMBRE);
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Lugar.class).find(query, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Lugar>> res, Filtro filtro)
	{
System.err.println("Lugar:getLista:filtro: "+filtro);
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addSortByOption("created ASC");
		queryOptions.addRelated(NOMBRE);
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
System.err.println("Lugar:getLista:SQL: "+sb.toString());
		if(sb.length() > 0)
			query.setWhereClause(sb.toString());
		//--FILTRO
		Backendless.Persistence.of(Lugar.class).find(query, res);
	}
/*
	public static void addNuevo(String sNombre, double lat, double lon, final AsyncCallback<Lugar> listener)
	{
System.err.println("------------------------------------------------Lugar:addNuevo:"+sNombre+" : "+lat+"/"+lon);
		Lugar l = new Lugar();
		l.setLugar(new GeoPoint(lat, lon));	//l.setLatLon(lat, lon);
		l.setNombre("A");//sNombre);
		l.setDescripcion("Widget");//System.err.println("------------------------------------------------Lugar:addNuevo:l:"+l);
		//l.guardar(listener);
		Backendless.Persistence.save(l, new AsyncCallback<Lugar>(){
			@Override
			public void handleResponse(Lugar lugar)
			{
				System.err.println("--------------++++++++++++++----------------------------------Lugar:addNuevo:lugar:"+lugar);
				listener.handleResponse(lugar);
			}
			@Override
			public void handleFault(BackendlessFault backendlessFault)
			{
				System.err.println("--------------++++++++++++++----------------------------------Lugar:addNuevo:lugar:backendlessFault:"+backendlessFault);
				listener.handleFault(backendlessFault);
			}
		});
	}*/

}
