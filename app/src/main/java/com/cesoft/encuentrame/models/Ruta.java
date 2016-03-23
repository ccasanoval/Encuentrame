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

import java.util.ArrayList;

import java.util.List;

//@formatter:off
////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
//TODO: Añadir fecha a cada punto del tracking... +metadata?
//TODO: Config : max number of points per rute => cuando alcanza el limite corta...
//TODO: config : lenght of geofence....
public class Ruta extends Objeto implements Parcelable
{

	public transient static final String NOMBRE = "ruta";//TRANSIENT so not to be included in backendless

	public Ruta(){}

	/*private boolean activo = false;
		public boolean isActivo(){return activo;}
		public void setActivo(boolean b){activo = b;}*/

	private List<GeoPoint> puntos = new ArrayList<>();
		public List<GeoPoint> getPuntos(){return puntos;}
		public void addPunto(GeoPoint v){puntos.add(v);}

	//TODO: Quitar si se utiliza geofence tracking y cambiar por radio...
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
		queryOptions.addRelated("puntos");
		query.setWhereClause("objectId = '" + sId + "'");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Ruta.class).find(query, res);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Ruta>> res)
	{
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("puntos");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Ruta.class).find(query, res);

		//Backendless.Persistence.of(Ruta.class).find(res);
		/*
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Lugar.class).find(query, res);
		* */
	}

}
//@formatter:on