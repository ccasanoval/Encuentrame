package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
//import weborb.service.ExcludeProperty;
//@ExcludeProperty( propertyName = "foo" )
public class Objeto implements Parcelable
{
	public transient static final String NOMBRE = "objeto";//TRANSIENT so not to include in backendless

	public Objeto(){}

	//Backendless
	protected String objectId;
	protected Date created;
	protected Date updated;
	public String getObjectId(){return objectId;}
	public void setObjectId(String objectId){this.objectId = objectId;}
	public Date getCreated(){return created;}
	public void setCreated(Date created){this.created = created;}
	public Date getUpdated(){return updated;}
	public void setUpdated(Date updated){this.updated = updated;}

	protected String nombre;
	protected String descripcion;
	public String getNombre(){return nombre;}
	public void setNombre(String v){nombre=v;}
	public String getDescripcion(){return descripcion;}
	public void setDescripcion(String v){descripcion=v;}

	//______________________________________________________________________________________________
	public String toString()
	{
		return "ID:"+getObjectId()+", NOM:"+(nombre==null?"":nombre) + ", DESC:"+(descripcion==null?"":descripcion);
	}


	// PARCELABLE
	//______________________________________________________________________________________________
	protected Objeto(Parcel in)
	{
		setObjectId(in.readString());
		nombre = in.readString();
		descripcion = in.readString();
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getObjectId());
		dest.writeString(nombre);
		dest.writeString(descripcion);
	}
	@Override
	public int describeContents()
	{
		return 0;
	}
	public static final Parcelable.Creator<Objeto> CREATOR = new Parcelable.Creator<Objeto>()
	{
		@Override
		public Objeto createFromParcel(Parcel in)
		{
			return new Objeto(in);
		}
		@Override
		public Objeto[] newArray(int size)
		{
			return new Objeto[size];
		}
	};

}
