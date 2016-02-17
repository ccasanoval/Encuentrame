package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
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

	private String nombre;
	private String descripcion;
	public String getNombre(){return nombre;}
	public void setNombre(String v){nombre=v;}
	public String getAviso(){return descripcion;}
	public void setAviso(String v){descripcion=v;}



	//// PARCEL
	protected Aviso(Parcel in)
	{
		setObjectId(in.readString());
		nombre = in.readString();
		descripcion = in.readString();
		//
		//TODO:GeoFence
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getObjectId());
		dest.writeString(nombre);
		dest.writeString(descripcion);
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
	public static void getLista(AsyncCallback<BackendlessCollection<Aviso>> res)
	{
		Backendless.Persistence.of(Aviso.class).find(res);
	}

}
