package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.Date;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Aviso extends Objeto implements Parcelable
{
	public transient static final String NOMBRE = "aviso";
	public transient static final String RADIO = "radio";//TRANSIENT so not to include in backendless

	public Aviso(){}

	//Payload
	//TODO: GeoFence
	//TODO: si no GeoFence => GeoPoint + radius
	private GeoPoint lugar;
		public GeoPoint getLugar(){return lugar;}
		public void setLugar(GeoPoint v){lugar=v;}
	public void setLugar(GeoPoint v, int radio){lugar=v; setRadio(radio);}

		public int getRadio()
		{
			Object o = lugar.getMetadata(RADIO);
			if(String.class == o.getClass())
			{
				System.err.println("Aviso:getRadio:String:------------------------"+o);
				return Integer.parseInt((String)o);
			}
			else if(Integer.class == o.getClass()) return (Integer)o;
			else return 0;
			//return (Integer)lugar.getMetadata(RADIO);
		}
		public void setRadio(int v){lugar.addMetadata(RADIO, v);}

	public String toString()
	{
		return super.toString() + ", POS:"+(lugar==null?"":lugar.getLatitude()+"/"+lugar.getLongitude()+":"+getRadio());
	}

	//// PARCELABLE
	//
	protected Aviso(Parcel in)
	{
		super(in);
		//
		//TODO:GeoFence:  geopoint + radius
		lugar = new GeoPoint();
		lugar.setObjectId(in.readString());
		lugar.setLatitude(in.readDouble());
		lugar.setLongitude(in.readDouble());
		//lugar.setDistance(in.readDouble());
		setRadio(in.readInt());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		super.writeToParcel(dest, flags);
		//
		//TODO:GeoFence:  geopoint + radius
		dest.writeString(lugar.getObjectId());
		dest.writeDouble(lugar.getLatitude());
		dest.writeDouble(lugar.getLongitude());
		//dest.writeDouble(lugar.getDistance());
		/*Object o = lugar.getMetadata(RADIO);
		if(String.class == o.getClass())
		{
			dest.writeInt(Integer.parseInt((String)o));
			System.err.println("Aviso:writeToParcel:String:"+o);
		}
		else if(Integer.class == o.getClass())
		{
			dest.writeInt((Integer)o);
			System.err.println("Aviso:writeToParcel:Integer:" + o);
		}*/
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
	public static void getLista(AsyncCallback<BackendlessCollection<Aviso>> res)
	{
		//Backendless.Persistence.of(Aviso.class).find(res);
		BackendlessDataQuery query = new BackendlessDataQuery();
		QueryOptions queryOptions = new QueryOptions();
		queryOptions.addRelated("lugar");//TODO:GeoFence:  geopoint + radius
		query.setQueryOptions(queryOptions);
		Backendless.Persistence.of(Aviso.class).find(query, res);
	}

}
