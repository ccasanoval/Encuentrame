package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;

import java.util.Date;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 15/02/2016
////////////////////////////////////////////////////////////////////////////////////////////////////
public class Ruta implements Parcelable
{
	public Ruta(){}

	//Backendless
	private String objectId;
	private Date created;
	private Date updated;
	public String getObjectId(){return objectId;}
	public void setObjectId(String objectId){this.objectId = objectId;}
	public Date getCreated(){return created;}
	public void setCreated(Date created){this.created = created;}
	public Date getUpdated(){return updated;}
	public void setUpdated(Date updated){this.updated = updated;}

	//Payload
	private List<GeoPoint> _aPuntos;
	public List<GeoPoint> getPuntos(){return _aPuntos;}

	private String _sNombre;
	private String _sDescripcion;
	public String getNombre(){return _sNombre;}
	public void setNombre(String v){_sNombre=v;}
	public String getDescripcion(){return _sDescripcion;}
	public void setDescripcion(String v){_sDescripcion=v;}



	//// PARCEL
	protected Ruta(Parcel in)
	{
		setObjectId(in.readString());
		_sNombre = in.readString();
		_sDescripcion = in.readString();
		//
		_aPuntos.clear();
		int n = in.readInt();
		for(int i=0; i < n; i++)
		{
			double lat = in.readDouble();
			double lon = in.readDouble();
			_aPuntos.add(new GeoPoint(lat, lon));
		}
		/*
		_lugar.setObjectId(in.readString());
		_lugar.setLatitude(in.readDouble());
		_lugar.setLongitude(in.readDouble());*/
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getObjectId());
		dest.writeString(_sNombre);
		dest.writeString(_sDescripcion);
		//
		dest.writeInt(_aPuntos.size());
		for(GeoPoint p : _aPuntos)
		{
			dest.writeDouble(p.getLatitude());
			dest.writeDouble(p.getLongitude());
		}
		//
		/*
		dest.writeString(_lugar.getObjectId());
		dest.writeDouble(_lugar.getLatitude());
		dest.writeDouble(_lugar.getLongitude());

		puntos = new ArrayList<GeoPoint>(); => pero GeoPoint no es parcelable
		in.readList(products, null);
		*/
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

}
