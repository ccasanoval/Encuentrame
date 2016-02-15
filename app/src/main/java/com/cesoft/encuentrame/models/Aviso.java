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
public class Aviso implements Parcelable
{
	public Aviso(){}

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
	//TODO: GeoFence
	//TODO: si no GeoFence => GeoPoint + radius

	private String _sNombre;
	private String _sAviso;
	public String getNombre(){return _sNombre;}
	public void setNombre(String v){_sNombre=v;}
	public String getAviso(){return _sAviso;}
	public void setAviso(String v){_sAviso=v;}



	//// PARCEL
	protected Aviso(Parcel in)
	{
		setObjectId(in.readString());
		_sNombre = in.readString();
		_sAviso = in.readString();
		//
		//TODO:GeoFence
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getObjectId());
		dest.writeString(_sNombre);
		dest.writeString(_sAviso);
		//
		//TODO:GeoFence
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

}
