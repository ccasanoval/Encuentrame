package com.cesoft.encuentrame.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 17/02/2016.
////////////////////////////////////////////////////////////////////////////////////////////////////
//http://stackoverflow.com/questions/32108969/why-do-i-get-failed-to-bounce-to-type-when-i-turn-json-from-firebase-into-java
public class Objeto implements Parcelable
{
	public static final String FIREBASE = "https://blazing-heat-3755.firebaseio.com/";
	public static final String NOMBRE = "objeto";
	public static final String GEOFIRE = "https://blazing-heat-3755.firebaseio.com/GEO/";

	public Objeto(){}

	//General
	protected String id = null;
		public String getId(){return id;}
		public void setId(String v){id = v;}

	protected String nombre;
	protected String descripcion;
		public String getNombre(){return nombre;}
		public void setNombre(String v){nombre=v;}
		public String getDescripcion(){return descripcion;}
		public void setDescripcion(String v){descripcion=v;}

	//______________________________________________________________________________________________
	@Override
	public String toString()
	{
		//return "Objeto{id='"+getId()+"', nombre='"+(nombre==null?"":nombre) + "', descripcion='"+(descripcion==null?"":descripcion)+"'}";
		return String.format(Locale.ENGLISH, "Objeto{id='%s', nombre='%s', descripcion='%s'}", getId(), (nombre==null?"":nombre), (descripcion==null?"":descripcion));
	}

	// PARCELABLE
	//______________________________________________________________________________________________
	protected Objeto(Parcel in)
	{
		setId(in.readString());
		setNombre(in.readString());
		setDescripcion(in.readString());
	}
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(getId());
		dest.writeString(getNombre());
		dest.writeString(getDescripcion());
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
