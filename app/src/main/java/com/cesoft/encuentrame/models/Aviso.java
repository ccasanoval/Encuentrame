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

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//ExcludeProperty(propertyName = "latitud")
@ExcludeProperties(propertyNames = { "latitud, longitud" })
public class Aviso extends Objeto
{
	public transient static final String NOMBRE = "aviso";
	public transient static final String RADIO = "radio";//TRANSIENT so not to include in backendless

	public Aviso(){}

	//______________________________________________________________________________________________
	protected boolean activo = true;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean b){activo=b;}

	protected Date fechaActivo;
		public void desactivarPorHoy(AsyncCallback<Aviso> ac)
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

		public int getRadio()//TODO: quiza aumentar radio (transparente para user) para que google pille antes la geofence
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
		setRadio(in.readInt());
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
		/*BackendlessDataQuery query = new BackendlessDataQuery();
		query.setWhereClause("objectId = "+sId);
		//query.setWhereClause("activo > 0");
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Aviso.class).find(query, res);*/
		ArrayList<String> relationProps = new ArrayList<>();
		relationProps.add("lugar");
		Backendless.Persistence.of(Aviso.class).findById(sId, relationProps, res);
System.err.println("-------------------1------------------Aviso:getById:" + sId);
	}
	public static void getActivos(AsyncCallback<BackendlessCollection<Aviso>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		//query.setWhereClause("activo = True");
		query.setWhereClause("activo > 0");
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Aviso.class).find(query, res);
		//BackendlessCollection<Aviso> result = Backendless.Persistence.of( Contact.class ).find( dataQuery );
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Aviso>> res)
	{
		//Backendless.Persistence.of(Aviso.class).find(res);
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Aviso.class).find(query, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Aviso>> res, Filtro filtro)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addSortByOption("created ASC");//TODO: igual en los otros getXXX ?
		query.setQueryOptions(queryOptions);
		StringBuilder sb = new StringBuilder(" 1 = 1 ");
		if( ! filtro.getNombre().isEmpty())
			sb.append(" AND nombre like = '%" + filtro.getNombre() + "%' ");
		if(filtro.getRadio() > Util.NADA && filtro.getPunto().latitude != 0 && filtro.getPunto().longitude != 0)
		{
			//TODO: Find by the points inside ???
			//filtro.getPunto();
			//filtro.getRadio()
		}
		if(filtro.getFechaIni() != null)//DateFormat df = java.text.DateFormat.getDateTimeInstance();
			sb.append(" AND created >= " + filtro.getFechaIni().getTime() + " ");
		if(filtro.getFechaFin() != null)
			sb.append(" AND created <= " + filtro.getFechaFin().getTime() + " ");
		if(sb.length() > 0)
			query.setWhereClause(sb.toString());
		Backendless.Persistence.of(Aviso.class).find(query, res);
	}

}
