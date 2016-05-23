package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.cesoft.encuentrame.Util;

import weborb.service.ExcludeProperties;

//https://develop.backendless.com/#Encuentrame/v1/main/data/Aviso
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 10/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
@ExcludeProperties(propertyNames = { "latitud, longitud" })
public class Lugar extends Objeto implements Parcelable
{
	public transient static final String NOMBRE = "lugar";//TRANSIENT so not to include in backendless

	public Lugar(){}

	private GeoPoint lugar = new GeoPoint(0,0);
		public GeoPoint getLugar(){return lugar;}
		public void setLugar(GeoPoint v){lugar=v;lugar.addCategory(NOMBRE);}
		public Double getLatitud(){return lugar.getLatitude();}
		public Double getLongitud(){return lugar.getLongitude();}
		public void setLatLon(Double lat, Double lon){lugar.setLatitude(lat);lugar.setLongitude(lon);}
		//public void setLatitud(Double lat){lugar.setLatitude(lat);}
		//public void setLongitud(Double lon){lugar.setLongitude(lon);}

	public String toString()
	{
		//return super.toString() + ", POS:"+(lugar==null?"null":lugar.getLatitude()+"/"+lugar.getLongitude()+" "+lugar.getObjectId());
		return String.format(java.util.Locale.ENGLISH, "Lugar{id='%s', nombre='%s', descripcion='%s', fecha='%s' / %d }",
				getObjectId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), DATE_FORMAT.format(created), created.getTime());
	}

	//// Backendless
	//______________________________________________________________________________________________
	public void eliminar(AsyncCallback<Long> res)
	{
		Backendless.Persistence.of(Lugar.class).remove(this, res);
	}
	public void guardar(AsyncCallback<Lugar> res)
	{
		Backendless.Persistence.save(this, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Lugar>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Lugar.class).find(query, res);
		//Backendless.Persistence.of(Lugar.class).find(res);
	}
	//https://backendless.com/documentation/data/android/data_search_and_query.htm
	public static void getLista(AsyncCallback<BackendlessCollection<Lugar>> res, Filtro filtro)
	{
System.err.println("Lugar:getLista:filtro: "+filtro);
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addSortByOption("created ASC");
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		//--FILTRO
		StringBuilder sb = new StringBuilder();//" created = created "
		if( ! filtro.getNombre().isEmpty())
		{
			sb.append(" nombre LIKE '%");
			sb.append(filtro.getNombre());
			sb.append("%' ");
		}
		if(filtro.getRadio() > Util.NADA && filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)
		{
			if(sb.length() > 0)sb.append(" AND ");
			sb.append(String.format(java.util.Locale.ENGLISH, " distance(%f, %f, lugar.latitude, lugar.longitude ) < km(%f) ",
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
System.err.println("Lugar:getLista:SQL: "+sb.toString());
		if(sb.length() > 0)
			query.setWhereClause(sb.toString());
		//--FILTRO
		Backendless.Persistence.of(Lugar.class).find(query, res);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	protected Lugar(Parcel in)
	{
		super(in);
		//
		lugar = new GeoPoint();
		lugar.setObjectId(in.readString());
		lugar.setLatitude(in.readDouble());
		lugar.setLongitude(in.readDouble());
System.err.println("Lugar:read Parcel:++++++++++++++++B: "+this);
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeString(lugar.getObjectId());
		dest.writeDouble(lugar.getLatitude());
		dest.writeDouble(lugar.getLongitude());
System.err.println("Lugar:write Parcel:++++++++++++++++A: "+this);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Creator<Lugar> CREATOR = new Creator<Lugar>()
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

}
