package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Ruta extends Objeto implements Parcelable
{
	public transient static final String NOMBRE = "ruta";//TRANSIENT so not to include in backendless

	public Ruta(){}

	//Payload
	private List<LatLng> puntos;//TODO: latLon ?
		public List<LatLng> getPuntos(){return puntos;}
		public void addPunto(LatLng v){puntos.add(v);}
	private int periodo=2*60*1000;
		public int getPeriodo(){return periodo;}
		public void setPeriodo(int v){periodo=v;}

	public String toString()
	{
		return super.toString() + ", RUT:"+(puntos==null?"":puntos.size());
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
			double lat = in.readDouble();
			double lon = in.readDouble();
			puntos.add(new LatLng(lat, lon));
		}
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		dest.writeInt(puntos.size());
		for(LatLng p : puntos)
		{
			dest.writeDouble(p.latitude);
			dest.writeDouble(p.longitude);
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
		//removePoint( GeoPoint geoPoint, AsyncCallback<Void> responder )
		Backendless.Persistence.of(Ruta.class).remove(this, ac);
	}
	public void guardar(AsyncCallback<Ruta> ac)
	{
		//Backendless.Persistence.of(Lugar.class).save(this, ac);
		Backendless.Persistence.save(this, ac);
	}
	public static void getLista(AsyncCallback<BackendlessCollection<Ruta>> res)
	{
		Backendless.Persistence.of(Ruta.class).find(res);
		/*
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Lugar.class).find(query, res);
		* */
	}

}
