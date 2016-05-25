package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;

import weborb.service.ExcludeProperty;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
//import weborb.service.ExcludeProperty;
//@ ExcludeProperty(propertyName = "fecha")
public class Objeto implements Parcelable
{
	public transient static final String NOMBRE = "objeto";//TRANSIENT so not to include in backendless

	public static final java.text.SimpleDateFormat DATE_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
	public static final java.text.DateFormat DATE_FORMAT2 = java.text.DateFormat.getDateTimeInstance();

	public Objeto(){}//created = new Date();

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

	protected String nombre="";
	protected String descripcion="";
	public String getNombre(){return nombre;}
	public void setNombre(String v){nombre=v;}
	public String getDescripcion(){return descripcion;}
	public void setDescripcion(String v){descripcion=v;}

	//______________________________________________________________________________________________
	public String toString()
	{
		return String.format(java.util.Locale.ENGLISH, "Objeto{id='%s', nombre='%s', descripcion='%s', created='%s'}",
				getObjectId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion), DATE_FORMAT.format(created));
	}

	//______________________________________________________________________________________________
	/*@Override public boolean equals(Object o)
	{
		// Return true if the objects are identical. (This is just an optimization, not required for correctness.)
		if(this == o)return true;

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's specification.
		if(!(o instanceof Objeto))return false;

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access private fields.
		Objeto lhs = (Objeto)o;

		// Check each field. Primitive fields, reference fields, and nullable reference
		// fields are all treated differently.
		if(getObjectId() == null && lhs.getObjectId() == null)
			return getNombre().equals(lhs.getNombre()) && getDescripcion().equals(lhs.getDescripcion());
		return getObjectId().equals(lhs.getObjectId());
	}*/



	// PARCELABLE
	//______________________________________________________________________________________________
	protected Objeto(Parcel in)
	{
		setObjectId(in.readString());
		nombre = in.readString();
		descripcion = in.readString();
		created = new Date(in.readLong());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getObjectId());
		dest.writeString(nombre);
		dest.writeString(descripcion);
		dest.writeLong(created.getTime());
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
