package com.cesoft.encuentrame.models;

import android.os.Parcel;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.cesoft.encuentrame.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import weborb.service.ExcludeProperties;//import weborb.service.ExcludeProperty;

//https://develop.backendless.com/#Encuentrame/v1/main/data/Aviso
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//ExcludeProperty(propertyName = "latitud")
@ExcludeProperties(propertyNames = { "latitud, longitud" })
public class Aviso extends Objeto
{
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
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -2);
			fechaActivo = cal.getTime();
			Backendless.Persistence.save(this, ac);
		}

	private GeoPoint lugar = new GeoPoint(0,0);
		public GeoPoint getLugar(){return lugar;}
		public void setLugar(GeoPoint v){lugar=v;}
		//public void setLugar(GeoPoint v, int radio){lugar=v; setRadio(radio);}
		public double getLatitud(){if(lugar==null || lugar.getLatitude() == null)return 0.0;return lugar.getLatitude();}
		public double getLongitud(){if(lugar==null || lugar.getLatitude() == null)return 0.0;return lugar.getLongitude();}
		public void setLatLon(double lat, double lon){lugar.setLatitude(lat);lugar.setLongitude(lon);}
		//public void setLatitud(Double lat){lugar.setLatitude(lat);}
		//public void setLongitud(Double lon){lugar.setLongitude(lon);}

		public int getRadio()//TODO: quiza aumentar radio (transparente para user) para que google pille antes la geofence ¿COMO MEJORAR GOOGLE GEOFENCE? Probar backendless geofences?????
		{
			if(lugar == null)return 0;
			Object o = lugar.getMetadata(RADIO);
			if(o == null)return 0;
			if(String.class == o.getClass())
				return Integer.parseInt((String)o);
			else if(Integer.class == o.getClass()) return (Integer)o;
			else return 0;
			//return (Integer)lugar.getMetadata(RADIO);
		}
		public void setRadio(int v){lugar.addMetadata(RADIO, v);}

	//______________________________________________________________________________________________
	public String toString()
	{
		return super.toString() +", ACT:"+activo+", POS:"+(lugar==null?"null":lugar.getLatitude()+"/"+lugar.getLongitude()+":"+getRadio()+" "+lugar.getObjectId());
	}
	//______________________________________________________________________________________________
	@Override public boolean equals(Object o)
	{
		if(this == o)return true;
		if(!(o instanceof Aviso))return false;
		Aviso a = (Aviso)o;

		return getObjectId().equals(a.getObjectId())
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
		//lugar = new GeoPoint(0,0);
		lugar.setObjectId(in.readString());
		lugar.setLatitude(in.readDouble());
		lugar.setLongitude(in.readDouble());
System.err.println("----------------Aviso:from parcel 1:" + this);
		setRadio(in.readInt());
System.err.println("----------------Aviso:from parcel 2:" + this);
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
		relationProps.add("lugar");
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
		queryOptions.addRelated("lugar");
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
